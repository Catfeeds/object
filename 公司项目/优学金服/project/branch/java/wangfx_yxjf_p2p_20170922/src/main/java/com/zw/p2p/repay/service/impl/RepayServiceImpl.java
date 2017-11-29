package com.zw.p2p.repay.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.config.ConfigConstants.RepayAlert;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.bean.ZwJson;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.NumberUtil;
import com.zw.core.util.SpringBeanUtil;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.huifu.service.HuiFuTransferService;
import com.zw.p2p.invest.InvestConstants.InvestStatus;
import com.zw.p2p.invest.InvestConstants.TransferStatus;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.LoanConstants;
import com.zw.p2p.loan.LoanConstants.LoanStatus;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.LoanType;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.impl.MessageBO;
import com.zw.p2p.repay.RepayConstants.RepayType;
import com.zw.p2p.repay.RepayConstants.RepayUnit;
import com.zw.p2p.repay.exception.AdvancedRepayException;
import com.zw.p2p.repay.exception.IllegalLoanTypeException;
import com.zw.p2p.repay.exception.NormalRepayException;
import com.zw.p2p.repay.exception.OverdueRepayException;
import com.zw.p2p.repay.exception.RepayException;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.repay.model.Repay;
import com.zw.p2p.repay.service.NormalRepayCalculator;
import com.zw.p2p.repay.service.RepayCalculator;
import com.zw.p2p.repay.service.RepayService;
import com.zw.p2p.risk.FeeConfigConstants.FeePoint;
import com.zw.p2p.risk.FeeConfigConstants.FeeType;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.risk.service.impl.FeeConfigBO;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-22 上午10:35:51
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-22 wangzhi 1.0
 */
@Service("repayService")
public class RepayServiceImpl implements RepayService {

	@Logger
	static Log log;

	@Resource
	HibernateTemplate ht;

	@Resource
	UserBillBO userBillBO;

	@Resource
	SystemBillService systemBillService;

	@Resource
	FeeConfigBO feeConfigBO;

	private LoanService loanService;

	@Resource
	TransferService transferService;

	@Resource
	NormalRepayCalculator normalRepayRFCLCalculator;

	@Resource
	NormalRepayCalculator normalRepayCPMCalculator;

	@Resource
	NormalRepayRLIOCalculator normalRepayRLIOCalculator;

	@Resource
	MessageBO messageBO;

	@Resource
	ConfigService configService;

	@Resource
	AutoMsgService autoMsgService;

	@Resource
	RepayCalculator repayCalculator;
	@Resource
	SafeLoanTaskService safeLoanTaskService;
	@Resource
	private HuiFuTradeService huiFuTradeService;
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	
	@Resource
	private InvestService investService;
	
	@Resource
	private HuiFuTransferService huiFuTransferService;

	/**
	 * 正常还款
	 * 
	 * @param repayId
	 *            还款编号
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void normalRepay(String repayId) throws InsufficientBalance, NormalRepayException {
		// 正常还款
		LoanRepay repay = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
		// 第二天7点的时候发短信
		// boolean flag = normalRepay(repay);
		// if (flag){
		// sendRepayMsg(repay,"normal");
		// }
		userBillBO.checkBalance(repay);
		if(null!=FacesUtil.getSessionAttribute(repayId+"normal")){
			throw new NormalRepayException("稍后操作");
		}
		FacesUtil.setSessionAttribute(repayId+"normal", repayId);
		normalRepay(repay);
		FacesUtil.getHttpSession().removeAttribute(repayId+"normal");
		List<TransferApply> tas = ht.find(
				"from TransferApply ta where ta.invest.loan.id = ? and ta.status = ? and ta.invest.status = ?",
				repay.getLoan().getId(), "transfering", "complete");
		for (TransferApply ta : tas) {
			ta.setStatus("cancel");
		}
	}

	private LoanService getLoanService() {
		if(loanService == null){
			loanService = (LoanService) SpringBeanUtil.getBeanByName("loanService");
		}
		return loanService;
	}

	@Override
	public void investSendSms() throws InsufficientBalance, NormalRepayException {
		List<InvestRepay> irs = ht
				.find("from InvestRepay ir where ir.smsSended=0 and ir.invest.isSafeLoanInvest=false");

		Set<String> loanRepayIds = new HashSet<String>();
		for (InvestRepay investRepay : irs) {
			loanRepayIds.add(investRepay.getLoanRepayId());
		}

		for (String repayId : loanRepayIds) {
			LoanRepay loanRepay = ht.get(LoanRepay.class, repayId);
			sendRepayMsg(loanRepay, "normal");
		}

		ht.bulkUpdate("update InvestRepay ir set ir.smsSended=1 where ir.smsSended=0");
	}

	/**
	 * 发送还款信息
	 * 
	 * @param loanRepay
	 *            corpus 本金 interest 利息 fee 手续费 defaultInterest 罚息
	 */
	public void sendRepayMsg(LoanRepay loanRepay, String type) {
		ht.evict(loanRepay);
		loanRepay = ht.get(LoanRepay.class, loanRepay.getId(), LockMode.UPGRADE);
		Map<String, String> params = new HashMap<String, String>();
		List<Invest> invests = ht.find("from Invest i where i.loan.id= ? and i.isSafeLoanInvest=false and i.money != 0",
				loanRepay.getLoan().getId());
		double totalCorpus = 0D;
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		String username = null;
		for (Invest invest : invests) {
			if (safeLoanTaskService.investInclude(invest)) {
				continue;
			}
			totalCorpus = 0D;
			// 本金
			double corpus = 0D;
			// 手续费
			double fee = 0D;
			// 利息
			double interest = 0D;
			// 罚息
			double punishInterest = 0D;
			String hql = "from InvestRepay ir where ir.invest.loan.id=? and  ir.invest.isSafeLoanInvest=false and ir.repayWay=? and ir.invest.user.username=?";
			params.put("dealName", "'" + invest.getLoan().getName() + "'");
			params.put("username", invest.getUser().getUsername());
			List<InvestRepay> irs = ht.find(hql,
					new Object[] { loanRepay.getLoan().getId(), "advance", invest.getUser().getUsername() });
			for (InvestRepay ir : irs) {
				totalCorpus += ir.getCorpus();
			}

			List<InvestRepay> investRepays = ht.find(
					"from InvestRepay ir where ir.invest.loan.id = ? and ir.invest.isSafeLoanInvest=false and ir.period = ? and ir.invest.user.username = ?",
					new Object[] { loanRepay.getLoan().getId(), loanRepay.getPeriod(),
							invest.getUser().getUsername() });
			for (InvestRepay investRepay : investRepays) {
				corpus += investRepay.getCorpus();
				fee += investRepay.getFee();
				interest += investRepay.getInterest();
				punishInterest += investRepay.getDefaultInterest();
			}

			if ("normal".equals(type)) {
				double all = ArithUtil.add(corpus, 0 - fee, interest);
				params.put("money", NumberUtil.insertComma(ArithUtil.round(all, 2) + "", 2));
				if (all != 0 && !invest.getUser().getUsername().equals(username)) {
					username = invest.getUser().getUsername();
					try {
						autoMsgService.sendMsg(invest.getUser(), "loan_repay", params);
					} catch (Exception e) {
						log.error("正常还款信息发送错误");
					}
				}
			} else if ("overdue".equals(type)) {
				double all = ArithUtil.add(corpus, 0 - fee, punishInterest, interest);
				params.put("money", NumberUtil.insertComma(ArithUtil.round(all, 2) + "", 2));
				if (all != 0 && !invest.getUser().getUsername().equals(username)) {
					username = invest.getUser().getUsername();
					try {
						autoMsgService.sendMsg(invest.getUser(), "loan_repay", params);
					} catch (Exception e) {
						log.error("逾期还款信息发送错误");
					}
				}
			} else if ("advance".equals(type)) {
				double feeToInvestor = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null, null,
						totalCorpus);
				double all = ArithUtil.add(totalCorpus, feeToInvestor);
				params.put("money", NumberUtil.insertComma(ArithUtil.round(all, 2) + "", 2));
				if (all != 0 && !invest.getUser().getUsername().equals(username)) {
					username = invest.getUser().getUsername();
					try {
						autoMsgService.sendMsg(invest.getUser(), "loan_repay", params);
					} catch (Exception e) {
						log.error("提前还款信息发送错误");
					}
				}
			}
		}
	}

	/**
	 * 处理还款延迟，把所有结息中的状态变为完成，并给投资者账户打款
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void investDelay() throws InsufficientBalance, NormalRepayException {
		// 第二天0点的时候，如果所有的投资还款都已经结束了，则要把Invest的状态标记为 COMPLETE
		List<Invest> is = ht.find("from Invest invest where invest.status in (?,?,?)",
				new String[] { InvestStatus.REPAYING, InvestStatus.OVERDUE, InvestStatus.BAD_DEBT });

		for (Invest invest : is) {
			List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.status=? and ir.invest.id=?",
					new Object[] { RepayStatus.INTERESTING, invest.getId() });
			if (irs.size() < 1)
				continue;

			// 本金
			double corpus = 0D;
			// 利息
			double interest = 0D;
			// 手续费
			double fee = 0D;
			LoanRepay repay = null;
			for (InvestRepay ir : irs) {
				ir.setStatus(RepayStatus.COMPLETE);
				ir.setSmsSended(0);
				ht.update(ir);
				repay = ht.get(LoanRepay.class, ir.getLoanRepayId());

				corpus += ir.getCorpus();
				interest += ir.getInterest();
				fee += ir.getFee();
			}
			//
			if (safeLoanTaskService.updateWuyoubaoLoanRecord(invest, interest,
					SafeLoanConstants.SafeLoanUserLoanStatus.YSY.getIndex())) {

			} else {
				// 第二天0点的时候得要把InvestRepay变更为 COMPLETE
				userBillBO.transferIntoBalance(invest.getUser().getId(), ArithUtil.add(corpus, interest),
						OperatorInfo.NORMAL_REPAY,
						"  项目:" + invest.getLoan().getName() + ",本金：" + corpus + "  利息：" + interest,
						invest.getLoan().getId());
				// 投资者手续费
				userBillBO.transferOutFromBalance(invest.getUser().getId(), fee, OperatorInfo.NORMAL_REPAY,
						"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费", invest.getLoan().getId());
				if (repay != null)
					systemBillService.transferInto(fee, OperatorInfo.NORMAL_REPAY,
							"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费" + ",本金：" + corpus + "  利息：" + interest,
							null, null, invest.getInvestRepays().get(repay.getPeriod() - 1), null, null, null, null,
							null);
				else
					systemBillService.transferInto(fee, OperatorInfo.NORMAL_REPAY,
							"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费" + ",本金：" + corpus + "  利息：" + interest,
							null, null, irs.get(0), null, null, null, null, null);
			}

			if (invest.getLoan().getStatus().equals(RepayStatus.COMPLETE)) {
				invest.setStatus(InvestStatus.COMPLETE);
				ht.update(invest);
			}
		}
	}
	
	/**
	 * 根据投资记录还款
	 * @author majie
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 * @date 2016年8月23日 下午12:21:11
	 */
	public void investRepay(String loanId) throws InsufficientBalance,NormalRepayException{
		// 第二天0点的时候，如果所有的投资还款都已经结束了，则要把Invest的状态标记为 COMPLETE
				List<Invest> is = ht.find("from Invest invest where invest.status in (?,?,?) and invest.loan.id=? ",
						new Object[] {InvestStatus.REPAYING, InvestStatus.OVERDUE, InvestStatus.BAD_DEBT, loanId});
				for (Invest invest : is) {
					List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.status=? and ir.invest.id=?",
							new Object[] { RepayStatus.INTERESTING, invest.getId() });
					System.out.println("invest集合的大小："+irs.size());
					if (irs.size() < 1)
						continue;
					// 本金
					double corpus = 0D;
					// 利息
					double interest = 0D;
					// 手续费
					double fee = 0D;
					LoanRepay repay = null;
					for (InvestRepay ir : irs) {
						ir.setStatus(RepayStatus.COMPLETE);
						ir.setSmsSended(0);
						ht.update(ir);
						repay = ht.get(LoanRepay.class, ir.getLoanRepayId());

						corpus += ir.getCorpus();
						interest += ir.getInterest();
						fee += ir.getFee();
					}
					//
					if (safeLoanTaskService.updateWuyoubaoLoanRecord(invest, interest,
							SafeLoanConstants.SafeLoanUserLoanStatus.YSY.getIndex())) {

					} else {
						// 第二天0点的时候得要把InvestRepay变更为 COMPLETE
						userBillBO.transferIntoBalance(invest.getUser().getId(), ArithUtil.add(corpus, interest),
								OperatorInfo.NORMAL_REPAY,
								"  项目:" + invest.getLoan().getName() + ",本金：" + corpus + "  利息：" + interest,
								invest.getLoan().getId());
						// 投资者手续费
						userBillBO.transferOutFromBalance(invest.getUser().getId(), fee, OperatorInfo.NORMAL_REPAY,
								"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费", invest.getLoan().getId());
						if (repay != null)
							systemBillService.transferInto(fee, OperatorInfo.NORMAL_REPAY,
									"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费" + ",本金：" + corpus + "  利息：" + interest,
									null, null, invest.getInvestRepays().get(repay.getPeriod() - 1), null, null, null, null,
									null);
						else
							systemBillService.transferInto(fee, OperatorInfo.NORMAL_REPAY,
									"项目：" + invest.getLoan().getName() + ",收到还款，扣除手续费" + ",本金：" + corpus + "  利息：" + interest,
									null, null, irs.get(0), null, null, null, null, null);
					}

					if (investRepaysComplete(invest)) {
						try {
							giveUserRateMoney(invest);
						} catch (Exception e) {
							e.printStackTrace();
						}
						invest.setStatus(InvestStatus.COMPLETE);
						ht.update(invest);
						System.out.println("标完成");
					}
				}
	}
	
	/**
	 * 还款完成给用户发放加息券金额(如果定时任务执行  则为第一天没有转完的金额)
	 * @throws Exception 
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveUserRateMoney(Invest invest) throws Exception{
		if(invest.getRateTicket() != null && invest.getRateMoney()>0){
			if(invest.getRateMoney()<200){
				ZwJson json = huiFuTransferService.transferMoney(invest.getUser().getUsrCustId(), invest.getRateMoney());
				if(json.isSuccess()){
					userBillBO.transferIntoBalance(invest.getUser().getId(), invest.getRateMoney(),
							"rate_money","投资:"+invest.getLoan().getName()+"发放加息金额"+invest.getRateMoney()+"元",
							invest.getLoan().getId());
					systemBillService.transferOut(invest.getRateMoney(), "rate_money","投资:"+invest.getLoan().getName()+"发放加息金额"+invest.getRateMoney()+"元", null, null, null, null, invest, null, null, null, invest.getUser());
					invest.setRateMoney(0);
				}else{
					System.out.println(invest.getLoan().getName()+"加息券转账失败:"+json.getMsg());
				}
			}else if(invest.getRateMoney()>200){
				double rm = invest.getRateMoney();
				int count = 0;
				do {//此循环每次转账200   转够3次或者不足200跳出循环
					if(count==3){
						break;//每日只能转账三次 每笔200
					}
					ZwJson json = huiFuTransferService.transferMoney(invest.getUser().getUsrCustId(), 200.0);
					if(json.isSuccess()){
						rm = ArithUtil.sub(rm, 200);
						count = count +1;
						userBillBO.transferIntoBalance(invest.getUser().getId(), 200.0,
								"rate_money","投资:"+invest.getLoan().getName()+"发放加息金额,第"+count+"笔：200 元",
								invest.getLoan().getId());
						systemBillService.transferOut(200.0, "rate_money","投资:"+invest.getLoan().getName()+"发放加息金额共"+invest.getRateMoney()+"元,第"+count+"笔：200 元", null, null, null, null, invest, null, null, null, invest.getUser());
						invest.setRateMoney(rm);
					}else{
						System.out.println(invest.getLoan().getName()+"加息券转账失败:"+json.getMsg());
					}
					
				} while (rm>200);
				if(count<3 && rm>0){//循环完以后 如果没有转账三次  就将剩下的钱转给用户
					ZwJson json = huiFuTransferService.transferMoney(invest.getUser().getUsrCustId(), rm);
					if(json.isSuccess()){
						count = count+1;
						userBillBO.transferIntoBalance(invest.getUser().getId(), invest.getRateMoney(),
								"rate_money","投资:"+invest.getLoan().getName()+"发放加息金额,第"+count+"笔："+rm+"元",
								invest.getLoan().getId());
						systemBillService.transferOut(rm, "rate_money","投资:"+invest.getLoan().getName()+"发放加息金额共"+invest.getRateMoney()+"元,第"+count+"笔："+rm+"元", null, null, null, null, invest, null, null, null, invest.getUser());
						invest.setRateMoney(0);
					}else{
						System.out.println(invest.getLoan().getName()+"加息券转账失败:"+json.getMsg());
					}
				}
			}
			ht.update(invest);
		}
	}
	
	/**
	 * 投资记录中所有还款计划是否完成
	 * @param investRepays
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月26日 下午2:12:59
	 */
	private boolean investRepaysComplete(Invest invest){
		if(invest == null)
			return false;
		for(InvestRepay repay : invest.getInvestRepays()){
			if(!RepayStatus.COMPLETE.equals(repay.getStatus())){
				return false;
			}
		}
		
		return true;
	}
	

	/**
	 * 正常还款
	 * 
	 * @param repay
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean normalRepay(LoanRepay repay) throws InsufficientBalance, NormalRepayException {
//		investRepay(repay.getLoan().getId());
		boolean flag = true;
		ht.evict(repay);
//		repay = ht.get(LoanRepay.class, repay.getId(), LockMode.UPGRADE);
		// 正常还款
		if (!(repay.getStatus().equals(LoanConstants.RepayStatus.REPAYING)
				|| repay.getStatus().equals(LoanConstants.RepayStatus.WAIT_REPAY_VERIFY))) {
			// 该还款不处于正常还款状态。
			flag = false;
			throw new NormalRepayException("还款：" + repay.getId() + "不处于正常还款状态。");
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=?  and ir.status=? ",
				new Object[] { repay.getLoan().getId(), repay.getPeriod() , RepayStatus.REPAYING});
		InvestRepay lastRepay = irs.get(irs.size()-1);
		for (InvestRepay investRepay : irs) {
			double fee = 0d;
			if(investRepay.getId().equals(lastRepay.getId())){
				fee = repay.getFee();
			}
			Invest invest=investRepay.getInvest();
			String orderId=investRepay.getId()+investRepay.getPeriod();
			String MerPriv = "normal"+","+repay.getId()+","+irs.size()+","+invest.getId();
			huiFuTradeService.tradeRepayment(orderId,invest.getLoan().getId(),repay.getLoan().getUser().getUsrCustId(), invest.getOrdId(),
				invest.getOrdDate(), df.format(investRepay.getCorpus()), df.format(investRepay.getInterest()),
				df.format(fee), investRepay.getInvest().getUser().getUsrCustId(), MerPriv);
		}

		return flag;
	}
	
	
	@Override
	public boolean normalInvestRepay(LoanRepay repay,String investId) throws InsufficientBalance {
		boolean flag=true;
		ht.evict(repay);
		// 正常还款
		if (!(repay.getStatus().equals(LoanConstants.RepayStatus.REPAYING)
				|| repay.getStatus().equals(LoanConstants.RepayStatus.WAIT_REPAY_VERIFY))) {
			// 该还款不处于正常还款状态。
			flag = false;
			try {
				throw new NormalRepayException("还款：" + repay.getId() + "不处于正常还款状态。");
			} catch (NormalRepayException e) {
				e.printStackTrace();
			}
		}
		List<Invest> invests =ht.find("from Invest i where i.id = ? and i.money != 0",investId);
		for (Invest invest : invests) {
			List<InvestRepay> irs = ht
					.find("from InvestRepay ir where ir.invest.id=? and ir.period=? ",
							new Object[] { invest.getId(),repay.getPeriod()});
			for (InvestRepay ir : irs) {
				// FIXME: 记录repayWay信息
				ir.setStatus(RepayStatus.INTERESTING);
				ir.setRepayWay("normal");
				ir.setTime(new Date());
				ir.setLoanRepayId(repay.getId());
				ht.update(ir);
			}
		}
		return flag;
	}
	
	/**
	 * 正常还款更新投资人账户
	 * 
	 * @param repay
	 * @throws InsufficientBalance
	 * @Date 2016年8月19日 上午11:12:23
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean normalRepayUpdateAcct(LoanRepay repay) throws InsufficientBalance {
		boolean flag = true;
		ht.evict(repay);

		//取消债权
		/*try {
			cancelTransfering(repay.getLoan().getId());
		} catch (RepayException e) {
			flag = false;
			throw new NormalRepayException(e.getMessage(), e.getCause());
		}*/

		// 更改借款的还款信息
		double payMoney = ArithUtil.add(
				ArithUtil.add(repay.getCorpus(), repay.getInterest()),
				repay.getFee());
		repay.setTime(new Date());
		repay.setStatus(LoanConstants.RepayStatus.COMPLETE);
		repay.setRepayWay("normal");

		// 借款者的账户，扣除还款。
		userBillBO.transferOutFromBalance(
				repay.getLoan().getUser().getId(),
				payMoney,
				OperatorInfo.NORMAL_REPAY,
				"项目：" + repay.getLoan().getName() + " ,本金：" + repay.getCorpus() + "  利息："
						+ repay.getInterest() + "  手续费：" + repay.getFee(),repay.getLoan().getId());
		// 借款者手续费
		systemBillService.transferInto(repay.getFee(),
				OperatorInfo.NORMAL_REPAY, "项目：" + repay.getLoan().getName()
						+ ",正常还款，扣除手续费",repay,null,null,null,null,null,null,null);

		ht.merge(repay);
		// 判断是否所有还款结束，更改等待还款的投资状态和还款状态，还有项目状态。
		getLoanService().dealComplete(repay.getLoan().getId(),false);
		return flag;
	}

	/**
	 * 产生还款信息
	 * 
	 * @param loan
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void generateRepay(Loan loan) {

		LoanType loanType = ht.get(LoanType.class, loan.getType().getId());
		if (loan.getInterestBeginTime() == null) {
			// 发起借款的时候，如果不制定计息开始时间，则默认为放款日
			loan.setInterestBeginTime(new Date());
		}
		// 先付利息后还本金
		if (loanType.getRepayType().equals(RepayType.RFCL)) {
			gRepays(loan, normalRepayRFCLCalculator);
			// 等额本息
		} else if (loanType.getRepayType().equals(RepayType.CPM)) {
			gRepays(loan, normalRepayCPMCalculator);
			// 到期还本付息
		} else if (loanType.getRepayType().equals(RepayType.RLIO)) {
			gRepays(loan, normalRepayRLIOCalculator);
		} else {
			throw new IllegalLoanTypeException("RepayType: " + loan.getType().getRepayType() + ". 不支持该还款类型。");
		}
	}

	/**
	 * 生成还款数据
	 * 
	 * @param loan
	 * @param
	 */
	private void gRepays(Loan loan, NormalRepayCalculator normalRepayCalculator) {
		LoanType loanType = loan.getType();
		List<List<InvestRepay>> allInvestRepays = new ArrayList<List<InvestRepay>>();
		List<Invest> invests = getLoanService().getSuccessfulInvests(loan.getId());
		for (Invest im : invests) {
			List<Repay> repays = normalRepayCalculator.generateRepays(im.getMoney(), im.getTime(), loan.getRate(),
					loan.getDeadline(), loanType.getRepayTimeUnit(), loanType.getRepayTimePeriod(),
					loan.getInterestBeginTime(), loanType.getInterestType(), loanType.getInterestPoint(), null);
			// 保存投资的还款信息
			Double investInterest = 0D;
			List<InvestRepay> irs = new ArrayList<InvestRepay>();
			double interestTotal = 0D;
			for (Repay r : repays) {
				interestTotal += r.getInterest();
			}
			if(im.getRateTicket() != null){
				if(interestTotal>0){
					double rateMoney = ArithUtil.mul(ArithUtil.div(interestTotal, loan.getRate()), ArithUtil.div(im.getRateTicket().getRate(), 100),2);//加息金额=总利息÷借款利率×加息券利率
					im.setRateMoney(rateMoney);
				}
			}
			for (Repay repay : repays) {
				InvestRepay investRepay = new InvestRepay();
				investRepay.setCorpus(repay.getCorpus());
				investRepay.setDefaultInterest(repay.getDefaultInterest());
				// 投资编号+还款第几期（四位，左侧补0）
				investRepay.setId(im.getId() + StringUtils.leftPad(repay.getPeriod().toString(), 4, "0"));
				investRepay.setInterest(repay.getInterest());
				investRepay.setInvest(im);
				investRepay.setLength(repay.getLength());
				investRepay.setPeriod(repay.getPeriod());
				investRepay.setRepayDay(repay.getRepayDay());
				investRepay.setStatus(LoanConstants.RepayStatus.REPAYING);
				// 投资者手续费=所得利息*借款中存储的投资者手续费比例
				investRepay.setFee(ArithUtil.round(ArithUtil.mul(repay.getInterest(), loan.getInvestorFeeRate()), 2));
				investInterest = ArithUtil.add(investInterest, investRepay.getInterest());
				ht.save(investRepay);
				irs.add(investRepay);
			}
			ht.update(im);
			allInvestRepays.add(irs);
		}

		generateLoanRepays(loan, loanType, allInvestRepays);
	}

	/**
	 * 生成借款的还款数据
	 * 
	 * @param loan
	 * @param loanType
	 * @param allInvestRepays
	 */
	private void generateLoanRepays(Loan loan, LoanType loanType, List<List<InvestRepay>> allInvestRepays) {
		// 创建loanRepays以便保存
		List<LoanRepay> loanRepays = new ArrayList<LoanRepay>();
		// 借款手续费，平均到每笔还款中收取
		Double fee = null;
		if (loanType.getRepayType().equals(RepayType.RLIO)) {
			fee = loan.getFeeOnRepay();
		} else {
			fee = ArithUtil.div(loan.getFeeOnRepay(), loan.getDeadline(), 2);
		}
		for (int i = 1; i <= loan.getDeadline(); i++) {
			// 初始化loanRepay信息
			LoanRepay loanRepay = new LoanRepay();
			loanRepay.setCorpus(0D);
			loanRepay.setDefaultInterest(0D);
			loanRepay.setId(loan.getId() + StringUtils.leftPad(String.valueOf(i), 4, "0"));
			loanRepay.setInterest(0D);
			loanRepay.setLength(loanType.getRepayTimePeriod());
			loanRepay.setLoan(loan);
			// 借款者手续费
			loanRepay.setFee(fee);
			loanRepay.setPeriod(i);
			if (loanType.getRepayTimeUnit().equals(RepayUnit.DAY)) {
				// 按天s还款
				loanRepay.setRepayDay(DateUtil.addDay(loan.getInterestBeginTime(), i * loanType.getRepayTimePeriod()));
			} else if (loanType.getRepayTimeUnit().equals(RepayUnit.MONTH)) {
				// 按月s还款
				loanRepay
						.setRepayDay(DateUtil.addMonth(loan.getInterestBeginTime(), i * loanType.getRepayTimePeriod()));
			}
			loanRepay.setStatus(LoanConstants.RepayStatus.REPAYING);

			loanRepays.add(loanRepay);
			// 到期还本付息，这里有bug
			if (loanType.getRepayType().equals(RepayType.RLIO)) {
				if (loanType.getRepayTimeUnit().equals(RepayUnit.DAY)) {
					// 按天s还款
					loanRepay.setLength(loan.getDeadline());
					// 计息日+第几期*还款周期单位=还款日
					loanRepay.setRepayDay(DateUtil.addDay(loan.getInterestBeginTime(), loan.getDeadline()));
				} else if (loanType.getRepayTimeUnit().equals(RepayUnit.MONTH)) {
					// 按月s还款
					loanRepay.setLength(loan.getDeadline());
					// 计息日+第几期*还款周期=还款日
					loanRepay.setRepayDay(DateUtil.addMonth(loan.getInterestBeginTime(), loan.getDeadline()));
				}
				break;
			}
		}

		// 根据每笔投资的还款信息，更新借款的还款信息。
		for (List<InvestRepay> irs : allInvestRepays) {
			for (InvestRepay ir : irs) {
				loanRepays.get(ir.getPeriod() - 1)
						.setCorpus(ArithUtil.add(loanRepays.get(ir.getPeriod() - 1).getCorpus(), ir.getCorpus()));
				loanRepays.get(ir.getPeriod() - 1)
						.setInterest(ArithUtil.add(loanRepays.get(ir.getPeriod() - 1).getInterest(), ir.getInterest()));
			}
		}
		// 保存借款的还款信息
		for (LoanRepay loanRepay : loanRepays) {
			ht.save(loanRepay);
		}
	}

	/**
	 * 提前还款
	 * 
	 * @param loanId
	 *            借款id
	 * @throws InsufficientBalance
	 * @throws AdvancedRepayException
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void advanceRepay(String loanId) throws InsufficientBalance, AdvancedRepayException {
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		if (loan.getStatus().equals(LoanStatus.REPAYING) || loan.getStatus().equals(LoanStatus.WAIT_REPAY_VERIFY)) {
			// 手续费+罚息
			double feeAll = 0D;
			// 查询当期还款是否已还清
			String repaysHql = "select lr from LoanRepay lr where lr.loan.id = ?";
			List<LoanRepay> repays = ht.find(repaysHql, loanId);
			for (LoanRepay repay : repays) {
				userBillBO.checkBalance(repay);
				if (repay.getStatus().equals(LoanConstants.RepayStatus.REPAYING)
						|| repay.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)) {
					// 在还款期，而且没还款
					if (isInRepayPeriod(repay.getRepayDay())) {
						// 有未完成的当期还款。
						throw new AdvancedRepayException("当期还款未完成");
					}else{
						feeAll = ArithUtil.add(feeAll, repay.getFee());
						// 给系统的罚金
						double feeToSystem = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_SYSTEM, FeeType.PENALTY, null, null,repay.getCorpus());
						feeAll = ArithUtil.add(feeAll, feeToSystem);
					}
				} else if (repay.getStatus().equals(LoanConstants.RepayStatus.BAD_DEBT)
						|| repay.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)) {
					// 还款中存在逾期或者坏账
					throw new AdvancedRepayException("还款中存在逾期或者坏账!");
				}
			}

			DecimalFormat df = new DecimalFormat("0.00");
			List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.loan.id=? and ir.status=? ",
						new Object[] { loan.getId(), RepayStatus.REPAYING });
			InvestRepay lastRepay = irs.get(irs.size()-1);
			for (InvestRepay investRepay : irs) {
				double fee = 0d;//手续费+罚金
				if(investRepay.getId().equals(lastRepay.getId())){
					fee = feeAll;
				}
				// 给投资人的罚金
				double feeToInvestor = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null, null,investRepay.getCorpus());
				Invest invest=investRepay.getInvest();
				String MerPriv = "advance"+","+loanId+","+irs.size()+","+invest.getId();
				String orderId=investRepay.getId()+investRepay.getPeriod();
				huiFuTradeService.tradeRepayment(orderId,invest.getLoan().getId(),loan.getUser().getUsrCustId(), invest.getOrdId(),
						invest.getOrdDate(), df.format(investRepay.getCorpus()), df.format(investRepay.getInterest()+feeToInvestor),
						df.format(fee), investRepay.getInvest().getUser().getUsrCustId(), MerPriv);
			}
		}
	}
	
	/**
	 * 提前还款更新信息
	 * @throws InsufficientBalance 
	 * @Date 2016年8月19日 下午2:30:22
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void advanceRepayUpdateAcct(String loanId) throws InsufficientBalance{
		Loan loan = ht.get(Loan.class, loanId);
		LoanRepay lr = null;
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		if (loan.getStatus().equals(LoanStatus.REPAYING) || loan.getStatus().equals(LoanStatus.WAIT_REPAY_VERIFY)) {
			// 查询当期还款是否已还清
			String repaysHql = "select lr from LoanRepay lr where lr.loan.id = ?";
			List<LoanRepay> repays = ht.find(repaysHql, loanId);
			//剩余本金
			double corpusAll = 0D;
			// 给系统的罚金
			double penaltyAll = 0D;
			//给投资人的罚金
			double penaltyInvestAll = 0D;
			// 手续费总额
			double feeAll = 0D;
			
			for (LoanRepay repay : repays) {
				if (repay.getStatus().equals(LoanConstants.RepayStatus.REPAYING)
						|| repay.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)) {
					corpusAll = ArithUtil.add(corpusAll, repay.getCorpus());
					feeAll = ArithUtil.add(feeAll, repay.getFee());
					// 给系统的罚金
					double feeToSystem = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_SYSTEM, FeeType.PENALTY, null, null,repay.getCorpus());
					penaltyAll = ArithUtil.add(penaltyAll, feeToSystem);
					double feeToInvestor = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null, null,repay.getCorpus());
					penaltyInvestAll = ArithUtil.add(penaltyInvestAll, feeToInvestor);
					
					repay.setTime(new Date());
					repay.setStatus(LoanConstants.RepayStatus.COMPLETE);
					repay.setRepayWay("advance");
					repay.setDefaultInterest(ArithUtil.add(feeToSystem, feeToInvestor));
					ht.update(repay);
				}
			}

			double sumPay = ArithUtil.add(corpusAll, penaltyAll,penaltyInvestAll,feeAll);
			// 扣除还款金额+罚金
			userBillBO.transferOutFromBalance(loan.getUser().getId(), sumPay, OperatorInfo.ADVANCE_REPAY,
					"项目：" + loan.getName() + "提前还款，本金：" + corpusAll + "，罚金：" +  ArithUtil.add(penaltyAll,penaltyInvestAll) + "，手续费：" + feeAll,loanId);
			
			List<Invest> invests = ht.find("from Invest i where i.loan.id = ? and i.money != 0", loan.getId());
			for (Invest invest : invests) {
				List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.id=? and ir.status=? and ir.invest.user.username=?",
						new Object[] { invest.getId(), RepayStatus.REPAYING, invest.getUser().getUsername() });
				// 罚金
				double cashFine = 0D;
				// 剩余本金
				double corpus = 0D;
				// fax
				double defaultAllInterest = 0D;
				for (int i = 0; i < irs.size(); i++) {
					InvestRepay ir = irs.get(i);
					// FIXME: 记录repayWay信息
					ir.setStatus(LoanConstants.RepayStatus.COMPLETE);
					ir.setRepayWay("advance");
					ir.setTime(new Date());
					ir.setInterest(0D);
					ir.setFee(0D);

					corpus += ir.getCorpus();
					//给投资人的罚金
					double defaultInterest = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null,
							null, ir.getCorpus());

					if (i != irs.size() - 1) {
						defaultAllInterest += defaultInterest;
						ir.setDefaultInterest(ArithUtil.round(defaultInterest, 2));
					} else {
						cashFine = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null, null,
								corpus);
						if (cashFine == defaultAllInterest + defaultInterest) {
							ir.setDefaultInterest(ArithUtil.round(defaultInterest, 2));
						} else {
							ir.setDefaultInterest(ArithUtil.round(cashFine - defaultAllInterest, 2));
						}
					}
					ht.update(ir);
				}
				if (safeLoanTaskService.updateWuyoubaoLoanRecord(invest, cashFine,
						SafeLoanConstants.SafeLoanUserLoanStatus.YSY.getIndex())) {

				} else {
					//给投资人的罚金
					double defaultInterest = feeConfigBO.getFee(FeePoint.ADVANCE_REPAY_INVESTOR, FeeType.PENALTY, null,null, corpus);
						userBillBO.transferIntoBalance(invest.getUser().getId(), ArithUtil.add(corpus, defaultInterest),
								OperatorInfo.ADVANCE_REPAY, "项目：" + invest.getLoan().getName() + ",收到还款" + "  本金："
										+ ArithUtil.round(corpus, 2) + "  罚息：" + ArithUtil.round(defaultInterest, 2),
								invest.getLoan().getId());
				}

				corpus = 0D;
				defaultAllInterest = 0D;
				invest.setStatus(RepayStatus.COMPLETE);
				ht.update(invest);
			}

			systemBillService.transferInto(ArithUtil.add(penaltyAll, feeAll), OperatorInfo.ADVANCE_REPAY,
					"提前还款罚金及借款手续费到账，项目:" + loan.getName(), lr, null, null, null, null, null, null, null);

			// 改项目状态。
			loan.setStatus(LoanConstants.LoanStatus.COMPLETE);
			ht.merge(loan);
		}
		
		List<TransferApply> tas = ht.find("from TransferApply ta where ta.invest.loan.id = ? and ta.status = ? and ta.invest.status = ?", loanId,
				"transfering", "complete");
		for (TransferApply ta : tas) {
			ta.setStatus("cancel");
			ht.update(ta);
		}
	}

	/**
	 * 逾期还款
	 * 
	 * @param repayId
	 *            还款id
	 * @throws InsufficientBalance
	 * @throws OverdueRepayException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void overdueRepay(String repayId) throws InsufficientBalance, OverdueRepayException {
		LoanRepay lr = ht.get(LoanRepay.class, repayId);
		userBillBO.checkBalance(lr);
		ht.evict(lr);
		lr = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
		if (lr.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)
				|| lr.getStatus().equals(LoanConstants.RepayStatus.BAD_DEBT)
				|| lr.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)) {
			List<Invest> invests = ht.find("from Invest i where i.loan.id = ? and i.money != 0", lr.getLoan().getId());
			DecimalFormat df = new DecimalFormat("0.00");
			//最后一条投资记录
			Invest lastInvest = invests.get(invests.size()-1);
			//支付给平台的罚息
			double defaultInterest = lr.getDefaultInterest();
			for (Invest invest : invests) {
				List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.period=? and ir.invest.id = ? and ir.status=?",
						new Object[] {lr.getPeriod(), invest.getId(),RepayStatus.OVERDUE});
				//是否是最后一条投资记录，最后一条时处理借款手续费和罚息
				boolean isLast = false;
				if(invest.getId().equals(lastInvest.getId())){
					isLast = true;
				}
				double fee = 0D;
				for (InvestRepay investRepay : irs) {
					defaultInterest = ArithUtil.sub(defaultInterest, investRepay.getDefaultInterest());
					if(isLast){
						isLast = false;
						fee = ArithUtil.add(lr.getFee(),defaultInterest);
					}
					//调用HuiFu接口
					String MerPriv = "overdue"+","+repayId+","+irs.size()+","+invest.getId();
					String orderId=investRepay.getId()+investRepay.getPeriod();
					huiFuTradeService.tradeRepayment(orderId,invest.getLoan().getId(),lr.getLoan().getUser().getUsrCustId(), invest.getOrdId(),
							invest.getOrdDate(), df.format(investRepay.getCorpus()), df.format(investRepay.getInterest()+investRepay.getDefaultInterest()),
							df.format(fee), investRepay.getInvest().getUser().getUsrCustId(), MerPriv);
				}
			}
		} else {
			throw new OverdueRepayException("还款不处于逾期还款状态");
		}

	}
	
	/**
	 * 逾期还款只更新状态
	 * */
	public void overdueInvestRepay(String investId,String repayId) throws InsufficientBalance, OverdueRepayException {
		LoanRepay lr = ht.get(LoanRepay.class, repayId);
		ht.evict(lr);
		lr = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
		if (lr.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)
				|| lr.getStatus().equals(LoanConstants.RepayStatus.BAD_DEBT)
				|| lr.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)) {
			List<Invest> invests = ht.find("from Invest i where i.id = ? and i.money != 0", investId);
			for (Invest invest : invests) {

				List<InvestRepay> irs = ht.find(
						"from InvestRepay ir where ir.invest.id=? and ir.period=? and ir.invest.user.username = ?",
						new Object[] {invest.getId(), lr.getPeriod(), invest.getUser().getUsername() });
				// 更改投资的还款信息
				for (InvestRepay ir : irs) {
					ir.setStatus(LoanConstants.RepayStatus.INTERESTING);
					ir.setRepayWay("overdue");
					ir.setTime(new Date());
					ht.update(ir);
				}
			}	
		}
	}
	
	/**
	 * 逾期还款回掉
	 * 
	 * @param repayId
	 *            还款id
	 * @throws InsufficientBalance
	 * @throws OverdueRepayException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void overdueRepayUpdateAcct(String repayId) throws InsufficientBalance, OverdueRepayException {
		LoanRepay lr = ht.get(LoanRepay.class, repayId);
		ht.evict(lr);
		lr = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
		if (lr.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)
				|| lr.getStatus().equals(LoanConstants.RepayStatus.BAD_DEBT)
				|| lr.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)) {
			List<Invest> invests = ht.find("from Invest i where i.loan.id = ? and i.money != 0", lr.getLoan().getId());
			double defaultInterest = lr.getDefaultInterest();
			for (Invest invest : invests) {

				List<InvestRepay> irs = ht.find(
						"from InvestRepay ir where ir.invest.id=? and ir.period=? and ir.invest.user.username = ?",
						new Object[] {invest.getId(), lr.getPeriod(), invest.getUser().getUsername() });
				// 本金
				double corpus = 0D;
				// 利息
				double interest = 0D;
				// 手续费
				double fee = 0D;
				// 罚息
				double punishInterest = 0D;
				// 更改投资的还款信息
				for (InvestRepay ir : irs) {
					ir.setStatus(LoanConstants.RepayStatus.COMPLETE);
					ir.setRepayWay("overdue");
					ir.setTime(new Date());
					ht.update(ir);
					corpus += ir.getCorpus();
					interest += ir.getInterest();
					fee += ir.getFee();
					punishInterest += ir.getDefaultInterest();
					defaultInterest = ArithUtil.sub(defaultInterest, ir.getDefaultInterest());
				}
				if (safeLoanTaskService.updateWuyoubaoLoanRecord(invest, interest,
						SafeLoanConstants.SafeLoanUserLoanStatus.YSY.getIndex())) {
				} else {
						userBillBO.transferIntoBalance(invest.getUser().getId(),
								ArithUtil.add(corpus, interest, punishInterest), OperatorInfo.OVERDUE_REPAY,
								" 项目:" + lr.getLoan().getName() + " ,本金：" + corpus + "  利息：" + interest + "  罚息："
										+ punishInterest,
								lr.getLoan().getId());
						// 投资者手续费
						userBillBO.transferOutFromBalance(invest.getUser().getId(), fee, OperatorInfo.OVERDUE_REPAY,
								"收到还款，扣除手续费, 项目:" + lr.getLoan().getName(), lr.getLoan().getId());
						systemBillService.transferInto(fee, OperatorInfo.OVERDUE_REPAY,
								"收到还款，扣除手续费, 项目:" + lr.getLoan().getName() + ",本金：" + corpus + "  利息：" + interest, null,
								null, invest.getInvestRepays().get(lr.getPeriod() - 1), null, null, null, null, null);
				}
				
			}

			// 更改借款的还款信息
			double payMoney = ArithUtil.add(lr.getCorpus(), lr.getInterest(), lr.getFee(),lr.getDefaultInterest());
			lr.setTime(new Date());
			lr.setStatus(LoanConstants.RepayStatus.COMPLETE);
			lr.setRepayWay("overdue");

			// 投资者的借款账户，扣除还款。
			userBillBO.transferOutFromBalance(lr.getLoan().getUser().getId(), payMoney,
					OperatorInfo.OVERDUE_REPAY, "项目：" + lr.getLoan().getName() + " 本金：" + lr.getCorpus() + "  利息："
							+ lr.getInterest() + "  手续费：" + lr.getFee() + "  罚息：" + lr.getDefaultInterest(),
					lr.getLoan().getId());
			// 借款者手续费
			systemBillService.transferInto(lr.getFee(), OperatorInfo.OVERDUE_REPAY,
					"项目：" + lr.getLoan().getId() + "逾期还款，扣除手续费", lr, null, null, null, null, null, null, null);
			// 罚息转入网站账户
			systemBillService.transferInto(defaultInterest, OperatorInfo.OVERDUE_REPAY,
					"项目：" + lr.getLoan().getId() + "逾期还款，扣除罚金", lr, null, null, null, null, null, null, null);
			ht.merge(lr);
			Long count = (Long) ht
					.find("select count(repay) from LoanRepay repay where repay.loan.id=? and (repay.status=? or repay.status=?)",
							lr.getLoan().getId(), RepayStatus.OVERDUE, RepayStatus.BAD_DEBT)
					.get(0);
			if (count == 0) {
				// 如果没有逾期或者坏账的还款，则更改借款状态。
				lr.getLoan().setStatus(LoanStatus.REPAYING);
				ht.update(lr.getLoan());
				for (Invest invest : invests) {
					// 如果没有逾期或者坏账的还款，则更改投资记录状态。
					invest.setStatus(LoanStatus.REPAYING);
					ht.update(invest);
				}
			}
			// 取消债权
			/*
			 * try { cancelTransfering(lr.getLoan().getId()); } catch
			 * (RepayException e) { flag = false; throw new
			 * OverdueRepayException(e.getMessage(), e.getCause()); }
			 */
			// 判断是否所有还款结束，更改等待还款的投资状态和还款状态，还有项目状态。
			getLoanService().dealComplete(lr.getLoan().getId(),true);
		} else {
			throw new OverdueRepayException("还款不处于逾期还款状态");
		}
		List<TransferApply> tas = ht.find(
				"from TransferApply ta where ta.invest.loan.id = ? and ta.status = ? and ta.invest.status = ?",
				lr.getLoan().getId(), "transfering", "complete");
		for (TransferApply ta : tas) {
			ta.setStatus("cancel");
		}
	}

	/**
	 * 还款时候，取消正在转让的债权
	 * 
	 * @param loanId
	 * @throws RepayException
	 */
	private void cancelTransfering(String loanId) throws RepayException {
		// 取消投资下面的所有正在转让的债权
		String hql = "from TransferApply ta where ta.invest.loan.id=? and ta.status=?";
		List<TransferApply> tas = ht.find(hql, new String[] { loanId, TransferStatus.WAITCONFIRM });
		if (tas.size() > 0) {
			// 有购买了等待第三方确认的债权，所以不能还款。
			throw new RepayException("有等待第三方确认的债权转让，不能还款！");
		}
		tas = ht.find(hql, new String[] { loanId, TransferStatus.TRANSFERING });
		for (TransferApply ta : tas) {
			transferService.cancel(ta.getId());
		}
	}

	@Override
	public void overdueRepayByAdmin(String repayId, String adminUserId) {
		// TODO Auto-generated method stub

	}

	/**
	 * 检查是否是否发生逾期
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void checkLoanOverdue() {
		if (log.isDebugEnabled()) {
			log.debug("checkLoanOverdue start");
		}
		List<LoanRepay> lrs = ht
				.find("from LoanRepay repay where repay.status !='" + LoanConstants.RepayStatus.COMPLETE + "'");
		for (LoanRepay lr : lrs) {
			ht.lock(lr, LockMode.UPGRADE);
			Loan loan = lr.getLoan();
			ht.lock(loan, LockMode.UPGRADE);
			List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=?",
					new Object[] { lr.getLoan().getId(), lr.getPeriod() });

			if (lr.getStatus().equals(LoanConstants.RepayStatus.REPAYING)) {
				Date repayDay = DateUtil.addDay(
						DateUtil.StringToDate(DateUtil.DateToString(lr.getRepayDay(), DateStyle.YYYY_MM_DD_CN)), 1);
				if (repayDay.before(new Date())) {
					lr.setStatus(LoanConstants.RepayStatus.OVERDUE);
					// FIXME:冻结用户，只允许还钱，其他都不能干。
					loan.setStatus(LoanConstants.LoanStatus.OVERDUE);
					/*
					 * OverdueInfo oi = new OverdueInfo();
					 * oi.setId(IdGenerator.randomUUID());
					 * oi.setUserId(loan.getUser().getId());
					 * oi.setLoanRepayId(lr.getId());
					 */
					for (InvestRepay ir : irs) {
						ir.setStatus(RepayStatus.OVERDUE);
						ir.getInvest().setStatus(InvestStatus.OVERDUE);
						ht.update(ir.getInvest());
						ht.update(ir);
					}
				}
			}

			if (lr.getStatus().equals(LoanConstants.RepayStatus.OVERDUE)) {
				if (log.isDebugEnabled()) {
					log.debug("checkLoanOverdue overdue repayId:" + lr.getId());
				}
				// 计算逾期罚息, 用户罚息+网站罚息
				double defalutInterestAll = 0D;
				for (InvestRepay ir : irs) {
					// 单笔投资罚息
					double overdueAllMoney = ArithUtil.mul(ArithUtil.add(ir.getCorpus(), ir.getInterest()),
							DateUtil.getIntervalDays(new Date(), ir.getRepayDay()));
					ir.setDefaultInterest(feeConfigBO.getFee(FeePoint.OVERDUE_REPAY_INVESTOR, FeeType.PENALTY, null,
							null, overdueAllMoney));
					defalutInterestAll = ArithUtil.add(defalutInterestAll, ir.getDefaultInterest());
				}
				ht.update(lr);
				// 网站罚息
				double overdueLRAllMoney = ArithUtil.mul(ArithUtil.add(lr.getCorpus(), lr.getInterest()),
						DateUtil.getIntervalDays(new Date(), lr.getRepayDay()));
				// 用户罚息+网站罚息
				lr.setDefaultInterest(ArithUtil.add(defalutInterestAll, feeConfigBO
						.getFee(FeePoint.OVERDUE_REPAY_SYSTEM, FeeType.PENALTY, null, null, overdueLRAllMoney)));
				if (DateUtil.addYear(lr.getRepayDay(), 1).before(new Date())) {
					// 逾期一年以后，项目改为坏账状态
					if (log.isDebugEnabled()) {
						log.debug("checkLoanOverdue badDebt repayId:" + lr.getId());
					}
					lr.setStatus(LoanConstants.RepayStatus.BAD_DEBT);
					loan.setStatus(LoanConstants.LoanStatus.BAD_DEBT);
					for (InvestRepay ir : irs) {
						ir.setStatus(RepayStatus.BAD_DEBT);
						ir.getInvest().setStatus(InvestStatus.BAD_DEBT);
						ht.update(ir.getInvest());
						ht.update(ir);
					}
				}
			}
			ht.update(lr);
			ht.update(loan);
			if (lr.getStatus().equals("overdue")) {
				sendOverdueMsg(lr);
			}
		}
	}

	/**
	 * 逾期信息通知
	 */
	public void sendOverdueMsg(LoanRepay loanRepay) {
		Map<String, String> params = new HashMap<String, String>();
		Loan loan = ht.get(Loan.class, loanRepay.getLoan().getId());
		params.put("username", loan.getUser().getUsername());
		params.put("dealName", "'" + loan.getName() + "'");
		params.put("money", NumberUtil.insertComma(loanRepay.getDefaultInterest() + "", 2));
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		try {
			autoMsgService.sendMsg(loan.getUser(), "borrowing_overdue", params);
		} catch (Exception e) {
			log.error("逾期通知信息发送错误");
		}
	}

	/**
	 * 自动还款
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void autoRepay() {
		if (log.isDebugEnabled()) {
			log.debug("autoRepay start");
		}
		List<LoanRepay> lrs = ht.find(
				"from LoanRepay repay where repay.status =?",
				LoanConstants.RepayStatus.REPAYING);
		boolean  flag = true;
		for (LoanRepay lr : lrs) {
			ht.lock(lr, LockMode.UPGRADE);
			Loan loan = lr.getLoan();
			ht.lock(loan, LockMode.UPGRADE);
			List<InvestRepay> irs = ht
					.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=?",
							new Object[] { lr.getLoan().getId(), lr.getPeriod() });
			Date repayDay = DateUtil.addDay(
					DateUtil.StringToDate(DateUtil.DateToString(
							lr.getRepayDay(), DateStyle.YYYY_MM_DD_CN)), 1);
			if (repayDay.before(new Date())) {
				// 到还款日了，自动扣款
				try {
					flag = normalRepay(lr);
				} catch (InsufficientBalance e) {
					// 账户余额不足，则逾期
					if (log.isDebugEnabled()) {
						log.debug("autoRepay InsufficientBalance overdue repayId:"
								+ lr.getId());
					}
					lr.setStatus(LoanConstants.RepayStatus.OVERDUE);
					// FIXME:冻结用户，只允许还钱，其他都不能干。
					loan.setStatus(LoanConstants.LoanStatus.OVERDUE);
					for (InvestRepay ir : irs) {
						ir.setStatus(RepayStatus.OVERDUE);
						ir.getInvest().setStatus(InvestStatus.OVERDUE);
						ht.update(ir.getInvest());
						ht.update(ir);
					}
					ht.update(lr);
					ht.update(loan);
				} catch (NormalRepayException e) {
					throw new RuntimeException(e);
				}
			}
			
		}
	}

	/**
	 * 判断还款期
	 * 
	 * @param repayDate
	 * @return
	 */
	@Override
	public boolean isInRepayPeriod(Date repayDate) {
		repayDate = DateUtil.StringToDate(DateUtil.DateToString(repayDate, DateStyle.YYYY_MM_DD_CN));
		Date now = new Date();
		Date upperLimit = DateUtil.addMonth(repayDate, -1);
		repayDate = DateUtil.addMinute(repayDate, 1439);
		// 还款日上推一个月，算是还款期。     (去掉时间限制 2016年8月23日11:22:37)
		if (repayDate != null && (now.before(repayDate)) && (!now.before(upperLimit))) {
			return true;
		}
		return false;
	}

	/**
	 * 还款提醒
	 */
	@Override
	@Transactional
	public void repayAlert() {
		int daysBefore = Integer.parseInt(configService.getConfigValue(RepayAlert.DAYS_BEFORE));

		List<LoanRepay> lrs = ht.find("from LoanRepay lr where lr.status =? and lr.repayDay<=?",
				LoanConstants.RepayStatus.REPAYING, DateUtil.addDay(new Date(), daysBefore));
		if (log.isInfoEnabled()) {
			log.info("repay alert start, size:" + lrs.size());
		}
		int days = 0;
		// 还款提醒。。。
		for (LoanRepay lr : lrs) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", lr.getLoan().getUser().getUsername());
			params.put("money", NumberUtil
					.insertComma(ArithUtil.round(lr.getInterest() + lr.getCorpus() + lr.getFee(), 2) + "", 2));
			days = DateUtil.getIntervalDays(new Date(), lr.getRepayDay());
			params.put("days", String.valueOf(days));
			params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
			try {
				if (days <= daysBefore && days > 0) {
					autoMsgService.sendMsg(lr.getLoan().getUser(), "repay_alert", params);
				}
			} catch (Exception e) {
				log.error("还款提醒信息发送错误");
			}
		}
	}

	@Override
	public List<Invest> getUnRepayInvest() {
		return ht.find("from Invest i where i.rateMoney>0 and i.status='complete'");
	}

}
