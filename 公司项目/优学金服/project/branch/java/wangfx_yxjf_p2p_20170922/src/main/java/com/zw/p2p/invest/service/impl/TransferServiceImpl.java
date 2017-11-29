package com.zw.p2p.invest.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdScheduler;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.InvestConstants.InvestStatus;
import com.zw.p2p.invest.InvestConstants.TransferStatus;
import com.zw.p2p.invest.exception.ExceedInvestTransferMoney;
import com.zw.p2p.invest.exception.InvestTransferException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.LoanConstants.LoanStatus;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.RepayRoadmap;
import com.zw.p2p.risk.FeeConfigConstants.FeePoint;
import com.zw.p2p.risk.FeeConfigConstants.FeeType;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.risk.service.impl.FeeConfigBO;
import com.zw.p2p.schedule.ScheduleConstants;
import com.zw.p2p.schedule.job.CheckInvestTransferOverExpectTime;

@Service("transferService")
public class TransferServiceImpl implements TransferService {

	@Resource
	HibernateTemplate ht;

	@Resource
	FeeConfigBO feeConfigBO;

	@Resource
	ConfigService configService;

	@Resource
	StdScheduler scheduler;

	@Resource
	UserBillBO userBillBO;

	@Resource
	SystemBillService sbs;

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void applyInvestTransfer(TransferApply ta)
			throws ExceedInvestTransferMoney {
		RepayRoadmap investRRM = ta.getInvest().getRepayRoadmap();

		// (负的本债权未收利息-未还投资手续费) <= 折让金 <= (本债权未收本金-转让手续费)
		if (ArithUtil.add(-investRRM.getUnPaidInterest(),
				investRRM.getUnPaidFee()) > ta.getPremium()
				|| ta.getPremium() > ArithUtil.sub(investRRM.getUnPaidCorpus(),
						calculateFee(ta.getCorpus()))) {
			throw new ExceedInvestTransferMoney("折让金区间不正确");
		}

		if (ta.getPremium() > 0) {
			String canBigger = configService
					.getConfigValue(ConfigConstants.InvestTransfer.CAN_GREATER_THAN_SELF_WORTH);
			if (!"1".equals(canBigger)) {
				throw new ExceedInvestTransferMoney("转让金额不能大于可转让金额");
			}
		} else if (ta.getPremium() < 0) {
			String canLess = configService
					.getConfigValue(ConfigConstants.InvestTransfer.CAN_LESS_THAN_SELF_WORTH);
			if (!"1".equals(canLess)) {
				throw new ExceedInvestTransferMoney("转让金额不能小于可转让金额");
			}
		} else {// 如果相等
			String canEquel;
			try {
				canEquel = configService
						.getConfigValue(ConfigConstants.InvestTransfer.CAN_EQUAL_SELF_WORTH);
			} catch (ObjectNotFoundException onfe) {
				canEquel = "1";
			}
			if (!"1".equals(canEquel)) {
				throw new ExceedInvestTransferMoney("转让金额不能等于可转让金额");
			}
		}
		// 债权转让期限，到期自动取消转让
		String timelimit;
		try {
			timelimit = configService
					.getConfigValue(ConfigConstants.InvestTransfer.DEAD_LINE);
		} catch (ObjectNotFoundException onfe) {
			timelimit = "7";
		}
		// 到期时间
		Date deadline = DateUtil.addDay(new Date(), Integer.valueOf(timelimit));

		// 转让期限
		ta.setDeadline(deadline);
		ta.setApplyTime(new Date());

		ta.setId(generateId());
		ta.setStatus(TransferStatus.TRANSFERING);
		try {
			userBillBO.freezeMoney(ta.getInvest().getUser().getId(), ta.getFee(), OperatorInfo.TRANSFER, "冻结债权转让手续费", ta.getInvest().getLoan().getId(),null);
		} catch (Exception e) {
			// TODO: handle exception
			throw new ExceedInvestTransferMoney("余额不足支付转让手续费");
		}
	
		ht.save(ta);

		// 在转让有效期内未达成转让的，自动撤销其转让申请。
		JobDetail jobDetail = JobBuilder
				.newJob(CheckInvestTransferOverExpectTime.class)
				.withIdentity(
						ta.getId(),
						ScheduleConstants.JobGroup.CHECK_INVEST_TRANSFER_OVER_EXPECT_TIME)
				.build();// 任务名，任务组，任务执行类
		jobDetail.getJobDataMap().put(
				CheckInvestTransferOverExpectTime.INVEST_TRANSFER_ID,
				ta.getId());
		SimpleTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ta.getId(),
						ScheduleConstants.TriggerGroup.CHECK_INVEST_TRANSFER_OVER_EXPECT_TIME)
				.forJob(jobDetail)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(ta.getDeadline()).build();
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 生成充值id
	 *
	 * @return
	 */
	private String generateId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select ta from TransferApply ta where ta.id = (select max(taM.id) from TransferApply taM where taM.id like ?)";
		List<TransferApply> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			TransferApply ta = contractList.get(0);
			ht.lock(ta, LockMode.UPGRADE);
			String temp = ta.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%07d", itemp);
		return gid;
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void transfer(String transferApplyId, String userId,
			double transferCorpus) throws InsufficientBalance,
			ExceedInvestTransferMoney {
		// 购买债权，就是一笔投资啊，不过得考虑部分购买。
		double remainCorpus = calculateRemainCorpus(transferApplyId);
		// 出价必须大于0，小于可购买的金额
		if (transferCorpus <= 0 || transferCorpus > remainCorpus) {
			throw new ExceedInvestTransferMoney("购买本金必须小于等于" + remainCorpus
					+ "且大于0");
		}

		TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		ht.evict(ta);
		ta = ht.get(TransferApply.class, transferApplyId, LockMode.UPGRADE);

		// 购买的本金占所有转让本金的比例。
		double corpusRateInAll = ArithUtil.div(transferCorpus, ta.getCorpus());
		//转让本金占持有本金的本例
		double transferCorpusRate = ArithUtil.div(transferCorpus, ta.getInvest().getMoney());

		// 判断ta是否都被购买了
		if (remainCorpus == transferCorpus) {
			// 债权全部被购买，债权转让完成
			ta.setStatus(TransferStatus.TRANSFED);
		}

		Invest investNew = new Invest();
		investNew.setUser(new User(userId));
		investNew.setInvestMoney(transferCorpus);
		investNew.setIsAutoInvest(false);
		investNew.setMoney(transferCorpus);
		investNew.setStatus(InvestConstants.InvestStatus.REPAYING);
		investNew.setRate(ta.getInvest().getRate());
		investNew.setTime(new Date());
		investNew.setTransferApply(ta);
		investNew.setLoan(ta.getInvest().getLoan());
		investNew.setId(IdGenerator.randomUUID());
		investNew.setIsSafeLoanInvest(false);
		investNew.setDitch("PC");
		ht.save(investNew);

		// 减去invest中持有的本金
		ta.getInvest().setMoney(
				ArithUtil.sub(ta.getInvest().getMoney(), transferCorpus));
		if (ta.getInvest().getMoney() == 0) {
			// 投资全部被转让，则投资状态变为“完成”。
			ta.getInvest().setStatus(InvestStatus.COMPLETE);
		}


		// 债权的购买金额：债权的价格*corpusRate
		double buyPrice = ArithUtil.mul(ta.getPrice(), corpusRateInAll, 2);
		userBillBO.transferOutFromBalance(userId, buyPrice,
				OperatorInfo.TRANSFER, "购买债权，项目：" + ta.getInvest().getLoan().getName(),ta.getInvest().getLoan().getId());
		// 购买时候，扣除手续费，从转让人收到的金额中扣除。费用根据购买价格计算
		double fee = ta.getFee();
		// 购买人转出，原持有人转入，手续费转入系统
		sbs.transferInto(ArithUtil.round(fee * transferCorpus / ta.getCorpus(),2), "transfer_fee",
				"债权转让手续费，项目：" + ta.getInvest().getLoan().getName(),null,null,null,null,investNew,null,null,null);
		userBillBO
				.transferIntoBalance(ta.getInvest().getUser().getId(),
						buyPrice, "transfered", "债权转让成功，项目："
								+ ta.getInvest().getLoan().getName(),ta.getInvest().getLoan().getId());
		userBillBO.transferOutFromBalance(ta.getInvest().getUser().getId(),
				ArithUtil.round(fee * transferCorpus / ta.getCorpus(),2), "transfered", "债权转让成功手续费，项目：" + ta.getInvest().getLoan().getName(),ta.getInvest().getLoan().getId());
		// 生成购买债权后的还款数据，调整之前的还款数据
		for (Iterator iterator = ta.getInvest().getInvestRepays().iterator(); iterator.hasNext();) {
			InvestRepay ir =  (InvestRepay) iterator.next();
			if (ir.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)
					|| ir.getStatus().equals(RepayStatus.OVERDUE)
					|| ir.getStatus().equals(RepayStatus.BAD_DEBT)) {
				throw new RuntimeException("investRepay with status "
						+ RepayStatus.WAIT_REPAY_VERIFY + "exist!");
			} else if (ir.getStatus().equals(RepayStatus.REPAYING)) {
				// 根据购买本金比例，生成债权还款信息
				InvestRepay irNew = new InvestRepay();
				try {
					BeanUtils.copyProperties(irNew, ir);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			    irNew.setId(IdGenerator.randomUUID());
//			    还款本金*转让本金比例*购买本金比例
				irNew.setCorpus(ArithUtil.mul(ir.getCorpus(),transferCorpusRate, 2));
				irNew.setDefaultInterest(ArithUtil.mul(ir.getDefaultInterest(),transferCorpusRate,2));
				irNew.setFee(ArithUtil.mul(ir.getFee(),transferCorpusRate, 2));
				irNew.setInterest(ArithUtil.mul(ir.getInterest(),transferCorpusRate, 2));
				irNew.setInvest(investNew);
				// 修改原投资的还款信息
				ir.setCorpus(ArithUtil.sub(ir.getCorpus(),
						irNew.getCorpus()));
				ir.setDefaultInterest(ArithUtil.sub(
						ir.getDefaultInterest(), irNew.getDefaultInterest()));
				ir.setFee(ArithUtil.sub(ir.getFee(), irNew.getFee()));
				ir.setInterest(ArithUtil.sub(ir.getInterest(),
						irNew.getInterest()));
				ht.merge(irNew);
				if (ir.getCorpus()+ir.getInterest() == 0 && !ir.getStatus().equals("complete")) {
					ht.delete(ir);
					iterator.remove();
				}else{
				    ht.update(ir);
				}
			}
		}

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public double calculateInvestTransferCompletedRate(String transferApplyId)
			throws NoMatchingObjectsException {
		TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		if (ta == null){
			return 0;
		}else {
			return ArithUtil.round(ArithUtil.sub(1, ArithUtil.div(
					calculateRemainCorpus(ta.getId()), ta.getCorpus(), 4)) * 100,2);
		}
	}

	@Override
	public double calculateRemainCorpus(String transferApplyId) {
		// 查询该债权下所有的投资
		TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		double transferedCorpus = 0D;
		for (Invest i : ta.getInvests()) {
			if (!InvestStatus.CANCEL.equals(i.getStatus())) {
				transferedCorpus = ArithUtil.add(i.getInvestMoney(),
						transferedCorpus);
			}
		}
		return ArithUtil.sub(ta.getCorpus(), transferedCorpus);
	}

	@Override
	public boolean canTransfer(String investId) throws InvestTransferException {
		Invest invest = ht.get(Invest.class, investId);
		if (invest == null) {
			throw new ObjectNotFoundException(investId, Invest.class.getName());
		}

		//有正在等待第三方确认的还款，false
		if (invest.getLoan().getStatus().equals(LoanStatus.WAIT_REPAY_VERIFY)) {
			return false;
		}

		// 已经是债权转入的，不允许再次转让
		if (invest.getTransferApply() != null) {
			return false;
		}

		// 有正在进行部分债权转让的投资，不允许再次申请债权转让
		List<TransferApply> tas = Lists.newArrayList(Collections2.filter(
				invest.getTransferApplies(), new Predicate<TransferApply>() {
					public boolean apply(TransferApply transferApply) {
						return transferApply.getStatus().equals(
								TransferStatus.TRANSFERING)
								|| transferApply.getStatus().equals(
										TransferStatus.WAITCONFIRM);
					}
				}));

		if (tas.size() > 0) {
			return false;
		}

		// 1、投资人持有该债权满一个月。
		// 查询持有该债权多长时间
		String paidRepayMin;
		try {
			// 债权的最少持有时间
			paidRepayMin = configService
					.getConfigValue(ConfigConstants.InvestTransfer.PAID_REPAY_COUNT_MIN);
		} catch (ObjectNotFoundException onfe) {
			// 默认值为1
			paidRepayMin = "1";
		}
		Integer paidRepaySize = invest.getRepayRoadmap().getPaidPeriod();
		if (paidRepaySize < Integer.valueOf(paidRepayMin)) {
			// 不合格
			// FIXME:最好能提示不合格的原因，下同。
			return false;
		}
		// 要转让的债权剩余期数大于或等于?期，
		String remainRepayCountMin;
		try {
			remainRepayCountMin = configService
					.getConfigValue(ConfigConstants.InvestTransfer.REMAIN_REPAY_COUNT_MIN);
		} catch (ObjectNotFoundException onfe) {
			remainRepayCountMin = "3";
		}

		if (invest.getRepayRoadmap().getUnPaidPeriod() < Integer
				.valueOf(remainRepayCountMin)) {
			// 不合格
			return false;
		}
		// 剩余本金大于或等于?元。
		String remainCorpus;
		try {
			remainCorpus = configService
					.getConfigValue(ConfigConstants.InvestTransfer.REMAIN_CORPUS_MIN);
		} catch (ObjectNotFoundException onfe) {
			remainCorpus = "1000";
		}
		if (invest.getRepayRoadmap().getUnPaidCorpus() < Integer
				.valueOf(remainCorpus)) {
			// 不合格
			return false;
		}
		// 在申请日，该债权必须是正常还款中状态方可申请转让。当前为逾期状态不能转让，当期已收到全部或部分还款不能转让。
		if (!invest.getStatus().equals(InvestConstants.InvestStatus.REPAYING)) {
			// 不合格
			return false;
		}
		// 转让申请应为一个非还款日，至少在还款日的前?天
		// 下一个合约还款日
		Date today = new Date();// 当前日期
		String deadLine;
		try {
			deadLine = configService
					.getConfigValue(ConfigConstants.InvestTransfer.APPLY_BEFORE_REPAY_DAY);
		} catch (ObjectNotFoundException onfe) {
			deadLine = "7";
		}
		// 当前日期向后7天的日期
		Date newDate = DateUtil.addDay(new Date(), Integer.valueOf(deadLine));
		if (invest.getRepayRoadmap().getNextRepayDate() != null && invest.getRepayRoadmap().getUnPaidPeriod() > 0){
			if (newDate.after(invest.getRepayRoadmap().getNextRepayDate())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void dealOverExpectTime(String investTransferId) {
//		cancel(investTransferId);
		TransferApply ta = ht.get(TransferApply.class,investTransferId);
		if(TransferStatus.TRANSFERING.equals(ta.getStatus())){
			cancel(investTransferId);
		}
	}

	@Override
	public String calculateRemainTime(String transferApplyId)
			throws NoMatchingObjectsException {
		TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		if (ta == null) {
			throw new NoMatchingObjectsException(Loan.class, "transferApplyId:"
					+ transferApplyId);
		}
		if (ta.getDeadline() == null) {
			return "未开始";
		}
		Long time = (ta.getDeadline().getTime() - System.currentTimeMillis()) / 1000;

		if (time < 0) {
			return "已到期";
		}
		long days = time / 3600 / 24;
		long hours = (time / 3600) % 24;
		long minutes = (time / 60) % 60;
		if (minutes < 1) {
			minutes = 1L;
		}

		return days + "天" + hours + "时" + minutes + "分";
	}

	public double calculateWorth(String investId) {
		Invest invest = ht.get(Invest.class, investId);
		if (invest == null) {
			return 0D;
		}
		List<InvestRepay> irsOri = invest.getInvestRepays();

		// 根据还款日排序
		Collections.sort(irsOri, new Comparator<InvestRepay>() {
			@Override
			public int compare(InvestRepay ir1, InvestRepay ir2) {
				return ir1.getRepayDay().compareTo(ir2.getRepayDay());
			}
		});
		// 求债权的当前价格
		Double interestM = null;
		Integer daysM = null;
		Integer daysPassedM = null;
		Double unPaidCorpus = null;
		int period = 0;
		for (int i = 0; i < irsOri.size(); i++) {
			InvestRepay irTemp = irsOri.get(i);
			if (irTemp.getStatus().equals(RepayStatus.REPAYING)) {
				interestM = irTemp.getInterest();
				// 上一个还款日
				Date prevRepayDay;
				if (i == 0) {
//					prevRepayDay = irTemp.getInvest().getLoan()
//							.getInterestBeginTime();
					//放款后生息,从放款日开始计算
					prevRepayDay= irTemp.getInvest().getLoan()
							.getGiveMoneyTime();
				} else {
					prevRepayDay = irsOri.get(i - 1).getRepayDay();
				}
				daysM = DateUtil.getIntervalDays(prevRepayDay,
						irTemp.getRepayDay());
				if (prevRepayDay.before(new Date())) {
					daysPassedM = DateUtil
							.getIntervalDays(prevRepayDay, new Date());
				} else {
					daysPassedM = 0;
				}
				unPaidCorpus = irTemp.getInvest().getRepayRoadmap()
						.getUnPaidCorpus();
				period = irTemp.getPeriod();
				break;
			}
		}

		String hql= "from InvestRepay ir where ir.invest.transferApply.invest.id=? and ir.period=?";
		List<InvestRepay> irs = ht.find(hql, new Object[]{investId,period});
		for (InvestRepay irTemp : irs) {
			unPaidCorpus = ArithUtil.add(unPaidCorpus, irTemp.getInvest().getRepayRoadmap().getUnPaidCorpus());
			interestM = ArithUtil.add(interestM, irTemp.getInterest());
		}

		// (当期利息/当期天数)*已过天数+待收本金 = 债权的当前价格
		double worth = 0D;
		if (daysM != null){
			worth = (interestM / daysM) * daysPassedM + unPaidCorpus;
		}
		return  worth < invest.getRepayRoadmap().getUnPaidMoney() ? worth : 0;
	}
	@Override
	public double calculateWorth(String investId, double corpus) {
		Invest invest = ht.get(Invest.class, investId);
		if (invest == null) {
			return 0D;
		}
		List<InvestRepay> irsOri = invest.getInvestRepays();

		// 根据还款日排序
		Collections.sort(irsOri, new Comparator<InvestRepay>() {
			@Override
			public int compare(InvestRepay ir1, InvestRepay ir2) {
				return ir1.getRepayDay().compareTo(ir2.getRepayDay());
			}
		});
		// 求债权的当前价格
		Double interestM = null;
		Integer daysM = null;
		Integer daysPassedM = null;
		Double unPaidCorpus = null;
		int period = 0;
		for (int i = 0; i < irsOri.size(); i++) {
			InvestRepay irTemp = irsOri.get(i);
			if (irTemp.getStatus().equals(RepayStatus.REPAYING)) {
				interestM = irTemp.getInterest();
				// 上一个还款日
				Date prevRepayDay;
				if (i == 0) {
//					prevRepayDay = irTemp.getInvest().getLoan()
//							.getInterestBeginTime();
					//放款后生息,从放款日开始计算
					prevRepayDay= irTemp.getInvest().getLoan()
							.getGiveMoneyTime();
				} else {
					prevRepayDay = irsOri.get(i - 1).getRepayDay();
				}
				daysM = DateUtil.getIntervalDays(prevRepayDay,
						irTemp.getRepayDay());
				if (prevRepayDay.before(new Date())) {
					daysPassedM = DateUtil
						.getIntervalDays(prevRepayDay, new Date());
				} else {
					daysPassedM = 0;
				}
				unPaidCorpus = irTemp.getInvest().getRepayRoadmap()
						.getUnPaidCorpus();
				period = irTemp.getPeriod();
				break;
			}
		}

		String hql= "from InvestRepay ir where ir.invest.transferApply.invest.id=? and ir.period=?";
		List<InvestRepay> irs = ht.find(hql, new Object[]{investId,period});
		for (InvestRepay irTemp : irs) {
			unPaidCorpus = ArithUtil.add(unPaidCorpus, irTemp.getInvest().getRepayRoadmap().getUnPaidCorpus());
			interestM = ArithUtil.add(interestM, irTemp.getInterest());
		}

		// (当期利息/当期天数)*已过天数+待收本金 = 债权的当前价格
		double worth = 0D;
		if (daysM != null){
			worth = (interestM / daysM) * daysPassedM + unPaidCorpus;
		}
		return  corpus / invest.getInvestMoney() * worth;
	}

//	@Override
//	public double calculateFee(TransferApply ta) {
//		return feeConfigBO.getFee(FeePoint.TRANSFER, FeeType.FACTORAGE, null,
//				null, ta.getCorpus());
//	}
	@Override
	public double calculateFee(double corpus) {
		return feeConfigBO.getFee(FeePoint.TRANSFER, FeeType.FACTORAGE, null,
				null, corpus);
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancel(String investTransferId) {
		// 未转让的部分，取消。
		TransferApply ta = ht.get(TransferApply.class, investTransferId);
		ta.setStatus(TransferStatus.CANCEL);
		try {
			Invest invest=ht.get(Invest.class, ta.getInvest().getId());
			userBillBO.unfreezeMoney(invest.getUser().getId(), ta.getFee(), OperatorInfo.TRANSFER, "解冻债权转让手续费",invest.getLoan().getId());
			ht.merge(ta);
		} catch (InsufficientBalance e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		/*List<Invest> invests = ht.find("from Invest i where i.transferApply is not null  and i.transferApply.id = ?",ta.getId());
		for (Invest i : invests){
			double corpusRateInAll = ArithUtil.div(i.getInvestMoney(), ta.getCorpus());
			double buyPrice = ArithUtil.mul(ta.getTransferOutPrice(), corpusRateInAll, 2);
			ta.getInvest().setStatus("repaying");
			ta.getInvest().setMoney(ta.getInvest().getInvestMoney());
			List<InvestRepay> investRepays = ta.getInvest().getInvestRepays();
			for (InvestRepay ir : investRepays){
				ir.setStatus("repaying");
				double corpus = ArithUtil.round(ArithUtil.div(ir.getCorpus(), corpusRateInAll),2);
				ir.setCorpus(corpus);
				double defaultInterest = ArithUtil.round(ArithUtil.div(ir.getDefaultInterest(), corpusRateInAll),2);
				ir.setDefaultInterest(defaultInterest);
				double fee = ArithUtil.round(ArithUtil.div(ir.getFee(), corpusRateInAll),2);
				ir.setFee(fee);
				double interest = ArithUtil.round(ArithUtil.div(ir.getInterest(), corpusRateInAll),2);
				ir.setInterest(interest);
			}
			for (int j = 0; j < i.getRepayRoadmap().getPaidPeriod() - i.getRepayRoadmap().getUnPaidPeriod(); j ++ ){
				InvestRepay investRepay = investRepays.get(j);
				investRepay.setStatus("complete");
				double corpus = ArithUtil.round(ArithUtil.mul(investRepay.getCorpus(), corpusRateInAll),2);
				investRepay.setCorpus(corpus);
				double defaultInterest = ArithUtil.round(ArithUtil.mul(investRepay.getDefaultInterest(), corpusRateInAll),2);
				investRepay.setDefaultInterest(defaultInterest);
				double fee = ArithUtil.round(ArithUtil.mul(investRepay.getFee(), corpusRateInAll),2);
				investRepay.setFee(fee);
				double interest = ArithUtil.round(ArithUtil.mul(investRepay.getInterest(), corpusRateInAll),2);
				investRepay.setInterest(interest);
			}
			try {
				i.setStatus("complete");
				List<InvestRepay> irs = i.getInvestRepays();
				for (InvestRepay ir : irs){
					ir.setStatus("complete");
					ir.setCorpus(0D);
					ir.setInterest(0D);
					ir.setFee(0D);
					ir.getInvest().setMoney(0D);
					ir.getInvest().setInvestMoney(0D);
				}
				userBillBO.transferOutFromBalance(ta.getInvest().getUser().getId(), buyPrice, "transfer_fail", "债权转让失败，编号：" + ta.getId());
				userBillBO.transferIntoBalance(i.getUser().getId(), buyPrice, "transfer_fail", "债权转让失败，编号："+ ta.getId());
			} catch (InsufficientBalance insufficientBalance) {
				insufficientBalance.printStackTrace();
			}
		}*/
	}
	@Override
	public TransferApply getTransferApplyById(String transferApplyId) {
		// TODO Auto-generated method stub
		return  ht.get(TransferApply.class, transferApplyId);
	}

}
