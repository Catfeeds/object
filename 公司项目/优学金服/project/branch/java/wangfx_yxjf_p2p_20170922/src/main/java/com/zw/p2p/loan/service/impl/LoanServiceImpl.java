package com.zw.p2p.loan.service.impl;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.hibernate.collection.AbstractPersistentCollection;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.config.model.Config;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.impl.UserBO;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.bean.ZwJson;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.core.util.NumberUtil;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.InvestConstants.InvestStatus;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.LoanConstants;
import com.zw.p2p.loan.LoanConstants.LoanStatus;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.exception.BorrowedMoneyTooLittle;
import com.zw.p2p.loan.exception.ExistWaitAffirmInvests;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.exception.InvalidExpectTimeException;
import com.zw.p2p.loan.exception.LoanException;
import com.zw.p2p.loan.model.ApplyEnterpriseLoan;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.repay.service.RepayService;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;
import com.zw.p2p.schedule.ScheduleConstants;
import com.zw.p2p.schedule.job.CheckLoanOverExpectTime;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-14 下午7:45:48
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-14 wangzhi 1.0
 */
@Service("loanService")
public class LoanServiceImpl implements LoanService {

	@Logger
	private static Log log;

	@Resource
	LoanBO loanBO;

	@Resource
	RepayService repayService;
	@Resource
	private SmsService smsService;
	@Resource
	private UserBO userBO;
	@Resource
	UserBillBO ubs;

	@Resource
	SystemBillService sbs;

	@Resource
	HibernateTemplate ht;

	@Resource
	StdScheduler scheduler;

	@Resource
	ConfigService configService;

	@Resource
	LoanCalculator loanCalculator;

	@Resource
	UserBillBO userBillBO;
	@Resource
	SafeLoanTaskService safeLoanTaskService;
	
	@Resource
	HuiFuLoanService huiFuLoanService;
	@Resource
	HuiFuTradeService huiFuTradeService;
	
	private DictUtil dictUtil;
	@Resource
	private AutoMsgService autoMsgService;
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	
	/**
	 * 申请借款
	 * 
	 * @param loan
	 * @throws InsufficientBalance
	 *             余额不足以支付保证金
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void applyLoan(Loan loan) throws InsufficientBalance {
		// 借款保证金费率
		double cashDepositMoney = loanCalculator.calculateCashDeposit(loan
				.getLoanMoney());
		// 如果保证金不够，需要先进行充值
		if (cashDepositMoney > ubs.getBalance(loan.getUser().getId())) {
			throw new InsufficientBalance("用户余额不足以支付保证金。");
		}
		loan.setDeposit(cashDepositMoney);

		loan.setCommitTime(new Date());
		// 设置借款状态
		loan.setStatus(LoanConstants.LoanStatus.WAITING_VERIFY);
		loan.setId(loanBO.generateId());
		ht.save(loan);

		// 冻结保证金
		/*ubs.freezeMoney(loan.getUser().getId(), cashDepositMoney,
				OperatorInfo.APPLY_LOAN, "借款ID:" + loan.getId() + "申请，冻结保证金");*/
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean fail(String loanId, String operatorId)
			throws ExistWaitAffirmInvests {
	

		// 流标
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		loan.setCancelTime(new Date());
	
		List<Invest> invests = loan.getInvests();

		try {
			boolean loanFailSucess=true;
			for (Invest investment : invests) {
				if (investment.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					throw new ExistWaitAffirmInvests("investID:"
							+ investment.getId());
				}
				ZwJson zj=new ZwJson();
				if (investment.getStatus().equals(
						InvestConstants.InvestStatus.BID_SUCCESS)) {
					// FIXME：investMoney-代金券金额，优惠券变为可用
					/**
					 * 判断是否使用代金券
					 */
				
					 Double investRealMoney=investment.getMoney();
					if(null!=investment.getCoupons()){
						//使用代金券的情况  1.代金券释放  2.真实投资金额为投资金额减去代金券的金额
						Coupons coupons=investment.getCoupons();
						coupons.setStatus("un_used");
						coupons.setUsedTime(null);
						ht.saveOrUpdate(coupons);
						investRealMoney=investRealMoney-coupons.getMoney();
					}
					//判断是否使用加息券
					if(null!=investment.getRateTicket()){
						RateTicket rateTicket = investment.getRateTicket();
						rateTicket.setStatus("un_used");
						rateTicket.setUsedTime(null);
						ht.saveOrUpdate(rateTicket);
						investment.setRatePercent(null);
						investment.setRateMoney(0d);
					}
					
					//流标
					if(safeLoanTaskService.updateWuyoubaoLoanRecord(investment, 0,SafeLoanConstants.SafeLoanUserLoanStatus.LB.getIndex())){
						
					}else{
						 zj=	huiFuMoneyService.unfreezeMoney(investment.getUser().getUsrCustId(), investment.getFreezeTrxId());
					}
					// 更改投资状态
					if(zj.isSuccess()){
						ubs.unfreezeMoney(investment.getUser().getId(),
								investRealMoney, OperatorInfo.CANCEL_LOAN,
								"项目:" + loan.getName() + "流标，解冻投资金额",loanId);
						investment.setStatus(InvestConstants.InvestStatus.CANCEL);
						ht.update(investment);
					}else{
						loanFailSucess=false;
					}
				}
			}
			if (loan.getStatus().equals(LoanStatus.RAISING)
					|| loan.getStatus().equals(LoanStatus.RECHECK)) {
				ubs.unfreezeMoney(loan.getUser().getId(), loan.getDeposit(),
						OperatorInfo.CANCEL_LOAN, "项目:" + loan.getName()
								+ "流标，解冻保证金",loanId);
			}
			if(loanFailSucess){
				loan.setStatus(LoanConstants.LoanStatus.CANCEL);
			}
			ht.merge(loan);
			return loanFailSucess;
		} catch (InsufficientBalance ib) {
			throw new RuntimeException(ib);
		}
	
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void failReturn(String loanId, String operatorId)
			throws ExistWaitAffirmInvests {
		// 流标
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		loan.setCancelTime(new Date());
		loan.setStatus(LoanConstants.LoanStatus.CANCEL);
		List<Invest> invests = loan.getInvests();

		try {
			for (Invest investment : invests) {
				if (investment.getStatus().equals(
						InvestConstants.InvestStatus.WAIT_AFFIRM)) {
					throw new ExistWaitAffirmInvests("investID:"
							+ investment.getId());
				}
				if (investment.getStatus().equals(
						InvestConstants.InvestStatus.BID_SUCCESS)) {
					// FIXME：investMoney-代金券金额，优惠券变为可用
					/**
					 * 判断是否使用代金券
					 */
					 Double investRealMoney=investment.getMoney();
					if(null!=investment.getCoupons()){
						//使用代金券的情况  1.代金券释放  2.真实投资金额为投资金额减去代金券的金额
						Coupons coupons=investment.getCoupons();
						coupons.setStatus("un_used");
						coupons.setUsedTime(null);
						ht.saveOrUpdate(coupons);
						investRealMoney=investRealMoney-coupons.getMoney();
					}
					//流标
					if(safeLoanTaskService.updateWuyoubaoLoanRecord(investment, 0,SafeLoanConstants.SafeLoanUserLoanStatus.LB.getIndex())){
						
					}else{
						ubs.unfreezeMoney(investment.getUser().getId(),
								investRealMoney, OperatorInfo.CANCEL_LOAN,
								"项目:" + loan.getName() + "流标，解冻投资金额",loanId);
					}
					
				}
				// 更改投资状态
				investment.setStatus(InvestConstants.InvestStatus.CANCEL);
				ht.update(investment);
			}

			if (loan.getStatus().equals(LoanStatus.RAISING)
					|| loan.getStatus().equals(LoanStatus.RECHECK)) {
				ubs.unfreezeMoney(loan.getUser().getId(), loan.getDeposit(),
						OperatorInfo.CANCEL_LOAN, "项目:" + loan.getName()
								+ "流标，解冻保证金",loanId);
			}

			ht.merge(loan);
		} catch (InsufficientBalance ib) {
			throw new RuntimeException(ib);
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void delayExpectTime(String loanId, Date newExpectTime)
			throws InvalidExpectTimeException {
		// 预计执行时间是否在当前时间之前，如果是，抛异常
		if (!newExpectTime.after(new Date())) {
			throw new InvalidExpectTimeException();
		} else {
			Loan loan = ht.get(Loan.class, loanId);
			ht.evict(loan);
			loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
			// FIXME:loan不存在
			// 添加项目到期调度任务
			loan.setExpectTime(newExpectTime);
			loan.setStatus(LoanConstants.LoanStatus.RAISING);
			ht.merge(loan);
			try {
				SimpleTrigger trigger = (SimpleTrigger) scheduler
						.getTrigger(TriggerKey
								.triggerKey(
										loanId,
										ScheduleConstants.TriggerGroup.CHECK_LOAN_OVER_EXPECT_TIME));
				if (trigger != null) {
					// 修改时间
					Trigger newTrigger = trigger
							.getTriggerBuilder()
							.withSchedule(
									SimpleScheduleBuilder.simpleSchedule())
							.startAt(newExpectTime).build();
					// 重启触发器
					scheduler.rescheduleJob(trigger.getKey(), newTrigger);
				} else {
					JobDetail jobDetail = JobBuilder
							.newJob(CheckLoanOverExpectTime.class)
							.withIdentity(
									loanId,
									ScheduleConstants.JobGroup.CHECK_LOAN_OVER_EXPECT_TIME)
							.build();// 任务名，任务组，任务执行类
					jobDetail.getJobDataMap().put(
							CheckLoanOverExpectTime.LOAN_ID, loanId);
					trigger = TriggerBuilder
							.newTrigger()
							.forJob(jobDetail)
							.startAt(newExpectTime)
							.withSchedule(
									SimpleScheduleBuilder.simpleSchedule())
							.withIdentity(
									loanId,
									ScheduleConstants.TriggerGroup.CHECK_LOAN_OVER_EXPECT_TIME)
							.build();
					scheduler.scheduleJob(jobDetail, trigger);
				}
			} catch (SchedulerException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void recheckLoan(String loanId)throws Exception{
		if (log.isInfoEnabled()) {
			log.info("放款" + loanId);
		}
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		if(LoanConstants.LoanStatus.RECHECK.equals(loan.getStatus())){
			// 更改项目状态，放款。
			loan.setStatus(LoanConstants.LoanStatus.FINANCIALLOAN);
		}else {
			throw new Exception("标的状态已更新，刷新后再试");
		}
		
		ht.merge(loan);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveMoneyToBorrower(String loanId)
			throws ExistWaitAffirmInvests, BorrowedMoneyTooLittle {
		if (log.isInfoEnabled()) {
			log.info("放款" + loanId);
		}
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		// FIXME:loan不存在
		// 有两种放款，一种是项目募集完成了，放款；一种是项目未募集满额，得根据项目的实际募集金额，修改项目借款金额，然后进行放款。

		// 更改项目状态，放款。
		loan.setStatus(LoanConstants.LoanStatus.REPAYING);
		// 获取当前日期
		Date dateNow = new Date();
		// 设置放款日期
		loan.setGiveMoneyTime(dateNow);
		loan.setInterestBeginTime(dateNow);
		if (loan.getInterestBeginTime() == null) {
		}

		// 实际到借款账户的金额
		double actualMoney = 0D;

		List<Invest> invests = loan.getInvests();

		for (Invest invest : invests) {
			if (invest.getStatus().equals(
					InvestConstants.InvestStatus.WAIT_AFFIRM)) {
				// 放款时候，需要检查是否要等待确认的投资，如果有，则不让放款。
				throw new ExistWaitAffirmInvests("investID:" + invest.getId());
			}
			if (invest.getStatus().equals(
					InvestConstants.InvestStatus.BID_SUCCESS)) {
				// 放款时候，需要只更改BID_SUCCESS 的借款。
				try {
					
					//无忧宝自动投资单独处理
					if(!safeLoanTaskService.investInclude(invest)){
						// investMoney-代金券金额
						if (invest.getCoupons() != null) {
							double realMoney = ArithUtil.sub(invest.getMoney(),
									invest.getCoupons().getMoney());
							if (realMoney < 0) {
								realMoney = 0;
							}
							
							ubs.transferOutFromFrozen(invest.getUser().getId(),
									realMoney, OperatorInfo.GIVE_MONEY_TO_BORROWER,
									"放款成功，取出投资金额, 项目：" + loan.getName(), loanId);
							userBillBO.transferOutFromCoupon(invest.getUser().getId(), invest.getCoupons().getMoney(),
									UserBillConstants.OperatorInfo.COUPON, "使用红包投资",loanId);
							sbs.transferOut(invest.getCoupons().getMoney(), OperatorInfo.COUPON,
									"使用红包投资", null, null, null, null, invest,null,invest.getCoupons(),null,null);
						} else {
							ubs.transferOutFromFrozen(invest.getUser().getId(),
									invest.getMoney(),
									OperatorInfo.GIVE_MONEY_TO_BORROWER,
									"放款成功，取出投资金额, 项目：" + loan.getName(),loanId);
						}
					}else{
						ubs.transferOutFromFrozen(invest.getUser().getId(),
								invest.getMoney(),
								OperatorInfo.GIVE_MONEY_TO_BORROWER,
								"放款成功，取出投资金额, 项目：" + loan.getName(),loanId);
					}
					actualMoney = ArithUtil.add(actualMoney,
							invest.getInvestMoney());
				} catch (InsufficientBalance e) {
					log.error(e);
					throw new RuntimeException(e);
				}
//				// 更改投资状态  
				invest.setStatus(InvestConstants.InvestStatus.REPAYING);
				ht.update(invest);
			}
		}
		// 设置借款实际借到的金额
		loan.setMoney(actualMoney);
		// 根据借款期限产生还款信息
		repayService.generateRepay(loan);

		// 借款手续费-借款保证金
		double subR = ArithUtil.sub(loan.getLoanGuranteeFee(),
				loan.getDeposit());

		double tooLittle = ArithUtil.sub(actualMoney, subR);
		// 借到的钱，可能不足以支付借款手续费
		if (tooLittle <= 0) {
			throw new BorrowedMoneyTooLittle("actualMoney：" + tooLittle);
		}
		// 把借款转给借款人账户
		ubs.transferIntoBalance(loan.getUser().getId(), actualMoney,
				OperatorInfo.GIVE_MONEY_TO_BORROWER,
				"借款到账, 项目：" + loan.getName(),loanId);
		try {
			if (loan.getDeposit() != 0){
				ubs.unfreezeMoney(loan.getUser().getId(), loan.getDeposit(),
						OperatorInfo.GIVE_MONEY_TO_BORROWER, "借款成功，解冻借款保证金, 项目："
						+ loan.getName(),loanId);
			}
			if (loan.getLoanGuranteeFee() != 0){
				ubs.transferOutFromBalance(loan.getUser().getId(),
					loan.getLoanGuranteeFee(),OperatorInfo.GIVE_MONEY_TO_BORROWER, "取出借款管理费, 项目："+ loan.getName(),loanId);
			}
			sbs.transferInto(loan.getLoanGuranteeFee(),
					OperatorInfo.GIVE_MONEY_TO_BORROWER,
					"借款管理费, 项目：" + loan.getName(),null,null,null,null,null,null,null,null);
		} catch (InsufficientBalance e) {
			log.error(e);
			throw new RuntimeException(e);
		}
		ht.merge(loan);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void passApply(Loan loan) throws InvalidExpectTimeException, InsufficientBalance {
		// FIXME:验证
		// 预计执行时间是否在当前时间之前，如果是，抛异常
		if (!loan.getExpectTime().after(new Date())) {
			throw new InvalidExpectTimeException();
		}
		setPics(loan);
		//addDealLoanStatusJob(loan);
		// 等待汇付审核
		loan.setVerified(LoanConstants.LoanVerifyStatus.HUIFUCHECK);
		//loan.setStatus(LoanConstants.LoanStatus.RAISING);需等待汇付回调修改
		loan.setVerifyTime(new Date());
		loan.setTransferType("transfer_type");
		ht.merge(loan);
		
		/*// 冻结保证金
		if (loan.getDeposit() != null && loan.getDeposit() > 0) {
			ubs.freezeMoney(loan.getUser().getId(), loan.getDeposit(),
					OperatorInfo.APPLY_LOAN, "发起借款，冻结保证金，项目:" + loan.getName(),loan.getId());
		}*/

		if (log.isDebugEnabled())
			log.debug(MessageFormat.format("借款[编号：{0},名称：{1}]审核通过!审核人：{2}",
					loan.getId(), loan.getName(), "//FIXME:"));
		
		JSONObject jso=huiFuLoanService.AddBidInfo(loan);
		System.out.println(jso);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void huiFuPassApply(String  ProId)  {
		Loan loan=findLoanById(ProId);
		if(LoanConstants.LoanVerifyStatus.HUIFUCHECK.equals(loan.getVerified())){
			setPics(loan);
			loan.setVerified(LoanConstants.LoanVerifyStatus.PASSED);
			loan.setStatus(LoanConstants.LoanStatus.RAISING);
			ht.merge(loan);
			// 冻结保证金
			if (loan.getDeposit() != null && loan.getDeposit() > 0) {
				try {
					ubs.freezeMoney(loan.getUser().getId(), loan.getDeposit(),
							OperatorInfo.APPLY_LOAN, "发起借款，冻结保证金，项目:" + loan.getName(),loan.getId(),null);
				} catch (InsufficientBalance e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			addDealLoanStatusJob(loan);
			autoSendLoanMessage(loan);
			System.out.println("AddBidInfo:汇付已审核成功"+ProId);
		}else{
			System.out.println("AddBidInfo:汇付重复审核"+ProId);
		}
		
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void autoSendLoanMessage(Loan loan){
		boolean bAutoSend=false;
		Config config = ht.get(Config.class, "auto_send_loan_message");
		if (config != null && (config.getValue().equals("1"))){
				bAutoSend=true;
		}
		if (!bAutoSend)
			return;


		String unit=dictUtil.getValue("repay_unit", loan.getType().getRepayTimeUnit());
		if (unit.equals("月"))
			unit="个月";

		DecimalFormat df=new DecimalFormat("0");
		DecimalFormat df2=new DecimalFormat("0.##");
		String msg=String.format("亲，优学金服今天发布了一个借款金额%s万、期限%d%s、年化利率%s%%、筹集期 %s-%s 的标的，等您来投资噢。",
				df.format(loan.getLoanMoney()/10000),loan.getDeadline(),unit, df2.format(loan.getRate()*100),
				DateUtil.DateToString(loan.getVerifyTime(),"yyyy年MM月dd日"),
				DateUtil.DateToString(loan.getExpectTime(),"yyyy年MM月dd日"));
		Set<String> userMobiles= userBO.getUserWithMobileNumber();
		for (String mobile:userMobiles){
			try {
				smsService.sendMsg(msg, mobile);

				Thread.sleep(50);
			}catch (Exception e){
				log.info("投资通知短信发送失败,,手机号:"+ mobile);
			}
		}
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void refuseApply(String loanId, String refuseInfo,
			String verifyUserId) {
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		User verifyUser = ht.get(User.class, verifyUserId);
		// FIXME:loan不存在，用户不存在的验证
		// 审核未通过
		loan.setVerified(LoanConstants.LoanVerifyStatus.FAILED);
		loan.setStatus(LoanConstants.LoanStatus.VERIFY_FAIL);
		loan.setExpectTime(null);
		// 审核人
		loan.setVerifyUser(verifyUser);
		loan.setVerifyMessage(refuseInfo);
		loan.setCancelTime(new Date());
		loan.setVerifyTime(new Date());
		// 借款保证金费率
		ht.merge(loan);
		/*Double cashDepositMoney = loan.getDeposit();
		if (cashDepositMoney != null) {
			try {
				ubs.unfreezeMoney(loan.getUser().getId(), cashDepositMoney,
						OperatorInfo.REFUSE_APPLY_LOAN, "借款审核未通过，解冻保证金。借款ID:"
								+ loan.getId());
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			}
		}*/
	}

	@Override
	public boolean isCompleted(String loanId) {
		// 如果有一笔还款状态不是“完成”，则返回false
		Long count = (Long) ht
				.find("select count(repay) from LoanRepay repay where repay.loan.id=? and repay.status!=?",
						loanId, RepayStatus.COMPLETE).get(0);
		return (count == 0);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean dealComplete(String loanId,boolean isInvest) {
		// 所有还款都完成了，则借款状态为“完成”
		if (isCompleted(loanId)) {
			Loan loan = ht.get(Loan.class, loanId);
			ht.evict(loan);
			loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
			loan.setCompleteTime(new Date());
			loan.setStatus(LoanConstants.LoanStatus.COMPLETE);
			ht.merge(loan);
			if(isInvest){
				List<Invest> is = ht.find("from Invest invest where invest.loan.id=? and invest.status in (?,?,?)",
						new String[] { loanId, InvestStatus.REPAYING,InvestStatus.OVERDUE, InvestStatus.BAD_DEBT });
				for (Invest invest : is) {
					invest.setStatus(InvestStatus.COMPLETE);
					ht.update(invest);
				}
			}
			return  true;
		}else {
			return  false;
		}
	}

	@Override
	public boolean isRaiseCompleted(String loanId)
			throws NoMatchingObjectsException {
		if (loanCalculator.calculateMoneyNeedRaised(loanId) == 0) {
			return true;
		}
		return false;
	}

	@Override
	public void dealRaiseComplete(String loanId)
			throws NoMatchingObjectsException {
		if (loanCalculator.calculateMoneyNeedRaised(loanId) == 0) {
			// 项目募集完成
			Loan loan = ht.get(Loan.class, loanId);
			loan.setStatus(LoanConstants.LoanStatus.RECHECK);
			ht.update(loan);
		}
	}

	@Override
	public boolean isOverExpectTime(String loanId) {
		Loan loan = ht.get(Loan.class, loanId);
		// FIXME:loan为空验证
		if (new Date().before(loan.getExpectTime())) {
			return false;
		}
		return true;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void dealOverExpectTime(String loanId) {
		// FIXME loan需要验证
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		// 只有筹款中的借款，才能通过调度改成等待复核
		if (isOverExpectTime(loanId)
				&& LoanConstants.LoanStatus.RAISING.equals(loan.getStatus())) {
			loan.setStatus(LoanConstants.LoanStatus.RECHECK);
			try {
				ht.merge(loan);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Long getActiveInvestsCount(String user_id) {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.user.id=? and invest.status in (?,?)";
		List<Object> oos = ht.find(hql, new String[] {
				user_id,
				InvestConstants.InvestStatus.BID_SUCCESS,
				InvestConstants.InvestStatus.REPAYING });
		Object o = oos.get(0);
		if (o == null) {
			return 0l;
		}

		return (Long)o;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void createLoanByAdmin(Loan loan) throws InsufficientBalance,
			InvalidExpectTimeException,LoanException {
		/*String uid= loan.getUser().getId();
		if (uid!=null) {
			Long activeInvestsCount = getActiveInvestsCount(uid);
			if (activeInvestsCount > 0) {
				throw new LoanException("用户" + uid + "现在正在投资，不能借款！");
			}
		}*/

		loan.setExpectTime(DateUtil.StringToDate(DateUtil.DateToString(loan.getExpectTime(),"yyyy-MM-dd")+" 23:59:59"));
		// FIXME:开始计息时间，必须在（当前时间往前一个还款阶段）之后。
		// 预计执行时间是否在当前时间之前，如果是，抛异常
		if (null==loan.getExpectTime()||!loan.getExpectTime().after(new Date())) {
			throw new InvalidExpectTimeException();
		}

		//借款保证金大于余额
		if (loan.getDeposit() != null && loan.getDeposit() > ubs.getBalance(loan.getUser().getId()) ) {
			throw new InsufficientBalance();
		}
		loan.setCommitTime(new Date());
		loan.setMoney(0D);
		// 设置借款状态
		loan.setStatus(LoanConstants.LoanStatus.WAITING_VERIFY);
		loan.setId(loanBO.generateId());
		//初始化部分借款交易数据
		//loan.setFeeOnRepay(0.0);
		loan.setInterestBeginTime( new Date());
		loan.setLoanGuranteeFee(0.0);
		loan.setDeposit(0.0);
		loan.setContractName(loan.getUser().getRealname());
		loan.setContractIdCard(loan.getUser().getIdCard());
		setPics(loan);
		ht.save(loan);
		
		if (log.isDebugEnabled())
			log.debug("添加项目成功，编号[" + loan.getId() + "],名称：[" + loan.getName()
					+ "]");

	}

	/**
	 * 添加项目到预计执行时间自动改状态调度
	 * 
	 * @param loan
	 */
	private void addDealLoanStatusJob(Loan loan) {
		// 调度，到期自动改项目状态
		JobDetail jobDetail = JobBuilder
				.newJob(CheckLoanOverExpectTime.class)
				.withIdentity(loan.getId(),
						ScheduleConstants.JobGroup.CHECK_LOAN_OVER_EXPECT_TIME)
				.build();// 任务名，任务组，任务执行类
		jobDetail.getJobDataMap().put(CheckLoanOverExpectTime.LOAN_ID,
				loan.getId());
		SimpleTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(
						loan.getId(),
						ScheduleConstants.TriggerGroup.CHECK_LOAN_OVER_EXPECT_TIME)
				.forJob(jobDetail)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(loan.getExpectTime()).build();
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

		if (log.isDebugEnabled())
			log.debug("添加[到期自动修改项目状态]调度成功，项目编号[" + loan.getId() + "]");
	}

	/**
	 * 赋值项目资料和抵押相关物资的图片
	 * 
	 * @param loan
	 * @return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private void setPics(Loan loan) {
		List<BannerPicture> lips = loan.getLoanInfoPics();
		List<BannerPicture> gips = loan.getGuaranteeInfoPics();
		List<BannerPicture> feps = loan.getFrontEndInfoPics();
		if (lips != null && !(lips instanceof AbstractPersistentCollection)) {
			for (BannerPicture lip : lips) {
				ht.saveOrUpdate(lip);
			}
		}
		if (gips != null && !(gips instanceof AbstractPersistentCollection)) {
			for (BannerPicture gip : gips) {
				ht.saveOrUpdate(gip);
			}
		}
        if (feps != null && !(feps instanceof AbstractPersistentCollection)) {
            for (BannerPicture fep : feps) {
                ht.saveOrUpdate(fep);
            }
        }
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void applyEnterpriseLoan(ApplyEnterpriseLoan ael) {
		ael.setId(IdGenerator.randomUUID());
		ael.setStatus(LoanConstants.ApplyEnterpriseLoanStatus.WAITING_VERIFY);
		ael.setApplyTime(new Date());

		ht.save(ael);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void verifyEnterpriseLoan(ApplyEnterpriseLoan ael) {
		ael.setStatus(LoanConstants.ApplyEnterpriseLoanStatus.VERIFIED);
		ht.update(ael);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void update(Loan loan) {
		// 只能更改不影响流程的字段
		setPics(loan);
		ht.update(loan);

		if (log.isDebugEnabled())
			log.debug("修改项目成功，编号[" + loan.getId() + "]");
	}

	@Override
	public List<Invest> getSuccessfulInvests(String loanId) {
		return ht
				.find("select im from Invest im where im.loan.id=? and im.status in (?,?,?,?,?)",
						new String[] { loanId,
								InvestConstants.InvestStatus.BID_SUCCESS,
								InvestConstants.InvestStatus.OVERDUE,
								InvestConstants.InvestStatus.COMPLETE,
								InvestConstants.InvestStatus.BAD_DEBT,
								InvestConstants.InvestStatus.REPAYING });
	}

	@Override
	public Date loanFinishTime(String loanId) {
		// TODO Auto-generated method stub
		List<LoanRepay> listloan=  ht
				.find("from LoanRepay repay where repay.loan.id=? order by repayDay desc",
						loanId);
		if(listloan!=null&&listloan.size()>0){
			return listloan.get(0).getRepayDay();
		}
		return null;
	}

	@Override
	public Loan getFirstTuiJian() {
		// TODO Auto-generated method stub
		List<Loan> list =ht.find("from Loan loan where loan.status='raising' and loan.loanAttrs.size>0 order by commitTime desc ");
		if(null!=list&&list.size()>0){
			return list.get(0);
		}else{
			List<Loan> listnor =ht.find("from Loan loan where loan.status='raising' order by commitTime desc ");
			if(null!=listnor&&listnor.size()>0){
				return listnor.get(0);
			}else{
				List<Loan> listnorr =ht.find("from Loan loan where loan.status in ('raising','repaying','complete','recheck') order by commitTime desc ");
				if(null!=listnorr&&listnorr.size()>0){
					return listnorr.get(0);
				}
			}
		}
		return null;
	}

	@Override
	public Loan findLoanById(String id) {
		// TODO Auto-generated method stub
		return ht.get(Loan.class, id);
	}
	
	
	/**
	 * 放款后发送信息
	 */
	public void sendInvestMsg(String loanId){
		List<Invest> invests = ht.find("from Invest i where i.loan.id=? ",loanId);
		String username = null;
		for (Invest invest : invests){
			if(safeLoanTaskService.investInclude(invest)){
				continue;
			}
			double money = 0D;
			double autoMoney = 0D;
			User user = invest.getUser();
			Map<String,String>  params  = new HashMap<String, String>();
			params.put("username",user.getUsername());
			params.put("dealNumber","'" + invest.getLoan().getName() +"'");
			params.put("status","成交");
			params.put("person","借款人");
			params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
			List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.user.username = ? and ir.invest.loan.id = ?",user.getUsername(),loanId);
			for (InvestRepay ir : irs){
				if (!ir.getInvest().getIsAutoInvest()){
					money += ir.getCorpus();
				}else {
					autoMoney += ir.getCorpus();
				}
			}
			if (!user.getUsername().equals(username)){
				username = user.getUsername();
				if (invest.getIsAutoInvest()){
					params.put("money", NumberUtil.insertComma(ArithUtil.round(autoMoney,2) + "",2));
					try {

						autoMsgService.sendMsg(user,"auto_invest_success",params);
					}catch (Exception e){
						log.error("自动投标信息发送失败");
					}
				}else {
					params.put("money", NumberUtil.insertComma(ArithUtil.round(money,2) + "",2));
					try {

						autoMsgService.sendMsg(user,"invest_success",params);
					}catch (Exception e){
						log.error("投标信息发送失败");
					}
				}
			}
		}
	}

	@Override
	public List<Loan> getRecheckLoanList() {
		// TODO Auto-generated method stub
		return ht.find("from Loan loan where loan.status='recheck' ");
	}

	@Override
	public void sendVerifyMsg(User user) {
		Map<String,String> params = new HashMap<String, String>();
		params.put("username",user.getUsername());
		try {

			autoMsgService.sendMsg(user,"verify",params);
		}catch (Exception e){
			log.error("流标通知发送失败");
		}
	}

	@Override
	public void sendFailMsg(Loan loan) {
		Map<String,String> params = new HashMap<String, String>();
		params.put("dealNumber","'" + loan.getName() + "'");
		params.put("status", "失败");
		params.put("person", "您");
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		List<Invest> invests = ht.find("from Invest  where  loan_id = ?",loan.getId());
		for (Invest invest : invests){
			params.put("username",invest.getUser().getUsername());
			params.put("money",NumberUtil.insertComma(invest.getMoney() + "",2));
			try {
				if (safeLoanTaskService.investInclude(invest)){
					continue;
				}else{
					autoMsgService.sendMsg(invest.getUser(),"invest_success",params);
				}
			}catch (Exception e){
				log.error("流标信息发送错误");
			}
		}
	}
	@Override
	public List<Loan> getUserVerify(User user) {
		// TODO Auto-generated method stub
		return ht.find("from Loan loan where loan.user='"+user.getId()+"' and loan.status='"+LoanStatus.WAITING_VERIFY+"'");
	}
}
