package com.zw.p2p.safeloan.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.core.util.SpringBeanUtil;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.LoanConstants.LoanStatus;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.LoanType;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanInteerType;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanRecordStatus;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanUserLoanStatus;
import com.zw.p2p.safeloan.model.SafeLoan;
import com.zw.p2p.safeloan.model.SafeLoanRecord;
import com.zw.p2p.safeloan.model.SafeLoan_User_Loan;
import com.zw.p2p.safeloan.model.TurnLoanRecord;
import com.zw.p2p.safeloan.service.MoneyDetailRecordService;
import com.zw.p2p.safeloan.service.SafeLoanRecordService;
import com.zw.p2p.safeloan.service.SafeLoanService;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;
@Service("safeLoanTaskService")
public class SafeLoanTaskServiceImpl implements SafeLoanTaskService {
	@Resource
	HibernateTemplate ht;
	@Resource
	LoanCalculator loanCalculator;
	@Resource
	InvestService investService;
	@Resource
	UserBillService ubs;
	@Resource
	UserBillService userBillService;
	@Resource
	SystemBillService systemBillService;
	@Resource
	SafeLoanService safeLoanService;
	@Resource
	SafeLoanRecordService safeLoanRecordService;
	@Resource
	MoneyDetailRecordService moneyDetailRecordService;
	@Resource
	ConfigService configService;
	@Resource
	TransferService transferService;
	@Resource
	private SmsService smsService;
	@Resource
	private AutoMsgService autoMsgService;
	@Logger
	Log log;
	public void updateSafeLoanStatus() {
		String nowTimeStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		//1更新产品审核期内未投满为复核状态
		String sql = "update SafeLoan set  status=" + SafeLoanConstants.SafeLoanStatus.FHZ.getIndex() + " where status="+SafeLoanConstants.SafeLoanStatus.TZZ.getIndex()+" and approveEndTime<'"+nowTimeStr+"'";
		ht.bulkUpdate(sql);
		
		String sqlDFX="From SafeLoanRecord where  status ="+SafeLoanConstants.SafeLoanRecordStatus.JSQ.getIndex();
		//将投资记录结算期进行付息并更新为已结清状态
		List<SafeLoanRecord> slr = ht.find(sqlDFX);
		for (SafeLoanRecord safeLoanRecord : slr) {
			SafeLoan safeloan=ht.get(SafeLoan.class,safeLoanRecord.getSafeloanid().getId());
			try {
				paybenxi(safeLoanRecord,safeloan);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	
	/**
	 * @Description: TODO(根据投资记录为待付息的进行本息返还,只返还总额-已返的)
	 * @author cuihang
	 * @date 2016-1-13 下午1:49:12
	 * @param safeLoanRecord
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private void paybenxi(SafeLoanRecord safeLoanRecord,SafeLoan safeLoan)throws Exception{
		//Double expectincome=safeLoanRecord.getExpectincome();//计划收益
		Double income=safeLoanRecord.getExpectincome();//实际收益
		safeLoanRecord.setStatus(SafeLoanConstants.SafeLoanRecordStatus.YJQ.getIndex());
		ht.update(safeLoanRecord);
		if(safeLoan.getInteertype()==SafeLoanInteerType.ONE.getIndex()){
			//第一种无忧宝需要去结算，第二种每笔返还已经结算，只需更新状态为已结清
			double benjin=getReturnBenJin(safeLoanRecord, safeLoanRecord.getMoney());
			income=getReturnInCome(safeLoanRecord, income);
			double expectincome=safeLoanRecordService.getrealIncome(benjin, safeLoanRecord.getSalTime(),new Date(), safeLoan.getRate());//真实获得利息，计划收益
			returnbenxi(benjin,income, expectincome, safeLoanRecord, safeLoan,true,null);
		}
		
		
	}
	@Override
	public synchronized void autoSafeLoan() throws Exception {
		// 所有可投的债权
		String nowTimeStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd hh:mm:ss");
		String getloanlist = "FROM Loan WHERE STATUS IN ('raising') AND expect_time >'"+nowTimeStr+"'";
		List<Loan> loanList = ht.find(getloanlist);
		
		double minSumLoan=0d;//一遍最小投资总和
		for (Loan loan : loanList) {
			minSumLoan=ArithUtil.add(minSumLoan,loan.getMinInvestMoney());
		}
		Map<String ,Integer> timeMap=new HashMap<String ,Integer>();
		String safeLoanReSql = "from SafeLoanRecord sl where sl.safeloanid.inteertype="+SafeLoanInteerType.ONE.getIndex()+" and sl.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and sl.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + "   and sl.enableMoney>0 order by sl.salTime asc";
		List<SafeLoanRecord> safeLoanReList = ht.find(safeLoanReSql);
		for (SafeLoanRecord safeLoanRecord : safeLoanReList) {
			timeMap.put(safeLoanRecord.getSalrid(), (int)(safeLoanRecord.getEnableMoney()/minSumLoan));
		}
		if(safeLoanReList.size()>0){
			for (Loan loan : loanList) {
				investLoan(loan,null,timeMap,SafeLoanInteerType.ONE.getIndex(),0,null);
			}
		}
		
	}
	@Override
	public synchronized void autoSafeLoanTwo() throws Exception {
		// 所有可投的债权
		
		String sql="from SafeLoanRecord sl where sl.safeloanid.inteertype="+SafeLoanInteerType.TWO.getIndex()+" and sl.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and sl.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + "   and sl.enableMoney>0 order by sl.salTime asc";
		List<SafeLoanRecord> listtype=ht.find(sql);
		Map<String,  SafeLoan> deadunit=new HashMap<String,SafeLoan>();
		for (SafeLoanRecord slr : listtype) {
			SafeLoan safeLoan=ht.get(SafeLoan.class, slr.getSafeloanid().getId());
			int time=safeLoan.getDeadline();
			String unit=safeLoan.getUnit();
			deadunit.put(time+unit, safeLoan);
		}
		String nowTimeStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd hh:mm:ss");
		for (String key : deadunit.keySet()) {
			SafeLoan safeLoan=deadunit.get(key);
			int deadline=safeLoan.getDeadline();
			String unit=safeLoan.getUnit();
			String getloanlist = "FROM Loan loan WHERE loan.status IN ('raising') and loan.deadline="+deadline+" and loan.type.interestType='"+unit+"' and loan.expectTime >'"+nowTimeStr+"'";
			List<Loan> loanList = ht.find(getloanlist);
			Map<Integer,Double> minSumLoan=new HashMap<Integer,Double>();
			
			for (Loan loan : loanList) {
				int deadlinetemp=loan.getDeadline();
				Double doubletemp=minSumLoan.get(deadlinetemp);
				if(null!=doubletemp&&doubletemp>0){
					doubletemp=ArithUtil.add(doubletemp,loan.getMinInvestMoney());
				}else{
					doubletemp=loan.getMinInvestMoney();
				}
				minSumLoan.put(deadlinetemp, doubletemp);
			}
			Map<String ,Integer> timeMap=new HashMap<String ,Integer>();
			String safeLoanReSql = "from SafeLoanRecord sl where sl.safeloanid.inteertype="+SafeLoanInteerType.TWO.getIndex()+" and sl.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and sl.safeloanid.deadline="+deadline+" and sl.safeloanid.unit='"+unit+"' and sl.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + "   and sl.enableMoney>0 order by sl.salTime asc";
			List<SafeLoanRecord> safeLoanReList = ht.find(safeLoanReSql);
			for (SafeLoanRecord safeLoanRecord : safeLoanReList) {
				SafeLoan sl=ht.get(SafeLoan.class, safeLoanRecord.getSafeloanid().getId());
				double minNum=minSumLoan.get(sl.getDeadline())==null?0:minSumLoan.get(sl.getDeadline());
				int times=0;
				if(minNum>0){
					times=(int)(safeLoanRecord.getEnableMoney()/minNum);
				}
				timeMap.put(safeLoanRecord.getSalrid(), times);
			}
			for (Loan loan : loanList) {
				investLoan(loan,null,timeMap,SafeLoanInteerType.TWO.getIndex(),deadline,unit);
			}
			
		}
		
		
		
		
		//double minSumLoan=0d;//一遍最小投资总和
		/**
		 * Map<几个月的债权,一遍最小投资总和> 
		 */
		
	}
	@Override
	public void autoTurnSafeLoan() throws Exception {
		// 所有待转让的债权
		String hql="from TurnLoanRecord where status="+SafeLoanConstants.TurnLoanRecordStatus.CS.getIndex()+" order by commitTime desc"; 
		List<TurnLoanRecord> list=ht.find(hql);
		for (TurnLoanRecord tlr : list) {
			turnLoan(tlr);
		}
	}
	/**
	 * 无忧宝投待转让的债权
	 * 2016年1月15日 14:48:16
	 * @param tlr
	 * @throws Exception 
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private void  turnLoan(TurnLoanRecord tlr) throws Exception{
			String safeLoanReSql = "from SafeLoanRecord slr where  slr.safeloanid.inteertype ="+SafeLoanInteerType.ONE.getIndex()+" and slr.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + " and slr.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and slr.enableMoney>="+tlr.getTurnmoney() ;
			List<SafeLoanRecord> safeLoanReList = ht.find(safeLoanReSql);
			if(safeLoanReList.size()>0){
				SafeLoanRecord slr=safeLoanReList.get(0);
				tlr.setStatus(SafeLoanConstants.TurnLoanRecordStatus.YZC.getIndex());
				ht.update(tlr);
				String invertid=tlr.getInvestid();
				Invest invest=ht.get(Invest.class,invertid);
				invest.setUser(slr.getUserid());
				ht.update(invest);
				recordAndUpdateMoney(tlr.getLoanid(),slr.getUserid().getId(),invertid, tlr.getTurnmoney(), slr.getSalrid(),slr.getSafeloanid(),SafeLoanConstants.MoneyDetailRecordType.JSZQ.getIndex());
				userBillService.transferIntoBalance(tlr.getTuserid(),  tlr.getTurnmoney(), OperatorInfo.SAFE_LOAN_TURN, "转让债权,项目："+invest.getLoan().getName());//(tlr.getTuserid(), tlr.getTurnmoney(), OperatorInfo.SAFE_LOAN_TURN,"转让债权,项目："+invest.getLoan().getName(),invest.getLoan().getId());
				//userBillService.addSafeLoanRecord(tlr.getTuserid(), tlr.getTurnmoney(), OperatorInfo.SAFE_LOAN_TURN,"转让债权,项目："+invest.getLoan().getName(),invest.getLoan().getId());
			}
	}
	
	
	/**
	 * @Description: TODO(对债权进行投标)
	 * @author cuihang
	 * @date 2016-1-13 下午4:15:55
	 * @param loanid
	 * @throws Exception 
	 */
	private void investLoan(Loan loan,List<SafeLoanRecord> safeLoanReList,	Map<String ,Integer> timeMap,int inteertypepara,int deadline,String unit) throws Exception {
		String loanid=loan.getId();
		double minMoney=loan.getMinInvestMoney();//债权起投金额
		// 债权可投金额
		double remainMoney;
		try {
			remainMoney = loanCalculator.calculateMoneyNeedRaised(loanid);
		} catch (NoMatchingObjectsException e) {
			throw new RuntimeException(e);
		}
		if(!(remainMoney>0)){
			return;
		}
		String sqlwhrstr="";
		if(inteertypepara==SafeLoanInteerType.TWO.getIndex()){
			sqlwhrstr="and sl.safeloanid.deadline="+deadline+" and sl.safeloanid.unit='"+unit+"' ";
		}
		
			// 每个可投项目
			String safeLoanReSql = "from SafeLoanRecord sl where sl.safeloanid.inteertype="+inteertypepara+" and sl.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+sqlwhrstr+"  and sl.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + "   and sl.enableMoney>0 order by sl.salTime asc";
				safeLoanReList = ht.find(safeLoanReSql);
			// 无忧宝可投金额
				LoanType loantype=ht.get(LoanType.class, loan.getType().getId());
			for (SafeLoanRecord safeLoanRecord : safeLoanReList) { 
				SafeLoan safeLoan=ht.get(SafeLoan.class, safeLoanRecord.getSafeloanid().getId());
				int inteertype=safeLoan.getInteertype();
				if(inteertype==SafeLoanInteerType.TWO.getIndex()){
					if(loan.getDeadline().intValue()==safeLoan.getDeadline().intValue()&&loantype.getInterestType().equals(safeLoan.getUnit())){
						//无忧宝和债权匹配
						//继续
					}else{
						//不匹配，跳过;
						continue;
					}
				}
				String salrid = safeLoanRecord.getSalrid();
				int time=1;
				if(null!=timeMap&&null!=timeMap.get(salrid)){
					time=timeMap.get(salrid);
					if(time==0){
						time=1;
					}
				}
				double enableMoney=	safeLoanRecord.getEnableMoney();
				double invMoney=minMoney*time;//投资金额
				if(enableMoney>=minMoney){
					
				}else{
					invMoney=enableMoney;
				}
				if(remainMoney<invMoney){
					invMoney=remainMoney;
				}
				if(!creat(loanid, invMoney, safeLoanRecord.getUserid().getId(), salrid, safeLoanRecord.getSafeloanid())){
					break;
				}
				remainMoney=ArithUtil.sub(remainMoney, invMoney);
			}
	}

	/**
	 * @Description: TODO(投资债权)
	 * @author cuihang
	 * @date 2016-1-13 下午4:08:40
	 * @param loanid
	 * @param money
	 * @param userid
	 * @return
	 * @throws Exception 
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private boolean creat(String loanid, Double money, String userid,String safeLoanReId,SafeLoan safeLoan) throws Exception {
		Invest invest = new Invest();
		invest.setUser(ht.get(User.class, userid));
		invest.setMoney(money);
		invest.setIsAutoInvest(false);
		invest.setIsSafeLoanInvest(true);
		invest.setDitch("PC");
	//	String loanId = invest.getLoan().getId();
		invest.setInvestMoney(invest.getMoney());
		// 防止并发出现
		Loan loan = ht.get(Loan.class, loanid);
		invest.setLoan(loan);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanid, LockMode.UPGRADE);
		// 如果借款不是在筹款中，抛出异常
		if (!loan.getStatus().equals(LoanStatus.RAISING)) {
			return false;
		}
		// 判断项目尚未认购的金额，如果用户想认购的金额大于此金额，则。。。
		double remainMoney;
		try {
			remainMoney = loanCalculator.calculateMoneyNeedRaised(loan.getId());
		} catch (NoMatchingObjectsException e) {
			return false;
		}
		// 输入的投资金额 大于 可投资的金额（尚未认购的金额） 抛出异常
		if (invest.getMoney() > remainMoney||remainMoney==0) {
			return false;
		}

		invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
		invest.setRate(loan.getRate());
		invest.setTime(new Date());

		// 投资成功以后，判断项目是否有尚未认购的金额，如果没有，则更改项目状态。
		invest.setId(investService.generateId(invest.getLoan().getId()));
		if (invest.getTransferApply() == null || StringUtils.isEmpty(invest.getTransferApply().getId())) {
			invest.setTransferApply(null);
		}
		ht.save(invest);
		try {
			LoanService loanService = (LoanService) SpringBeanUtil.getBeanByName("loanService");
			// 处理借款募集完成
			loanService.dealRaiseComplete(loan.getId());
		} catch (NoMatchingObjectsException e) {
			throw new RuntimeException(e);
		}
			recordAndUpdateMoney(loanid, userid, invest.getId(), money, safeLoanReId,safeLoan,SafeLoanConstants.MoneyDetailRecordType.ZDTB.getIndex());
		return true;

	}

	/**
	 * @Description: TODO(记录无忧宝及用户投标记录，更新无忧宝投资记录可投金额)
	 * @author cuihang
	 * @date 2016-1-13 下午4:52:38
	 * @throws Exception
	 */
	private void recordAndUpdateMoney(String loanid,String userid,String investId,double money,String safeLoanReId,SafeLoan safeLoan,int type) throws Exception {
		SafeLoan_User_Loan sul=new SafeLoan_User_Loan();
		sul.setId(IdGenerator.randomUUID());
		sul.setInvestId(investId);
		Loan loan=ht.get(Loan.class, loanid);
		sul.setLoanid(loan);
		sul.setUserid(userid);
		sul.setLoanMoney(money);
		sul.setStatus(SafeLoanConstants.SafeLoanUserLoanStatus.TZZ.getIndex());
		sul.setCommitTime(new Date());
		sul.setSafeLoanRecordId(safeLoanReId);
		sul.setSafeLoanId(safeLoan.getId());
		ht.save(sul);
		SafeLoanRecord slr=	ht.get(SafeLoanRecord.class, safeLoanReId);
		slr.setEnableMoney(new BigDecimal(slr.getEnableMoney()).subtract(new BigDecimal(money)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		slr.setBeforeInvestTime(new Date());
		ht.update(slr);
		if(type==SafeLoanConstants.MoneyDetailRecordType.ZDTB.getIndex()){
			userBillService.transferOutFromsafeloanFrozenToFrozen(userid, money, OperatorInfo.SAFE_LOAN_INVEST,"计划投标,项目："+loan.getName(),slr.getSafeloanid().getId());
		}else if(type==SafeLoanConstants.MoneyDetailRecordType.JSZQ.getIndex()){
			if(loan.getStatus().equals("raising")||loan.getStatus().equals("recheck")||loan.getStatus().equals("dqgs")){
				//未放款
				userBillService.transferOutFromsafeloanFrozenToFrozen(userid, money, OperatorInfo.SAFE_LOAN_INVEST_TURNL,"购买债权,项目："+loan.getName(),slr.getSafeloanid().getId());
			}else{
				//已放款
				userBillService.transferOutFromsafeloanFrozen(userid, money, OperatorInfo.SAFE_LOAN_INVEST_TURNL,"购买债权,项目："+loan.getName(),slr.getSafeloanid().getId());
			}
		}
		moneyDetailRecordService.addRecord(loanid, safeLoan.getId(), safeLoanReId, type, 0, money);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void updateExpireSafeLoanIncome() throws Exception {
		String nowTimeStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		String safeLoanReSql = "from SafeLoanRecord slr where  slr.safeloanid.inteertype ="+SafeLoanInteerType.ONE.getIndex()+" and slr.status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and slr.enableStatus=" + SafeLoanConstants.EnableStatus.Y.getIndex() + "  and slr.endTime<'"+nowTimeStr+"'";
		List<SafeLoanRecord> safeLoanReList = ht.find(safeLoanReSql);
		for (SafeLoanRecord safeLoanRecord : safeLoanReList) {
			SafeLoan safeLoan=safeLoanRecord.getSafeloanid();
			if(new BigDecimal(userBillService.getBalance(safeLoanService.getTUSER())).compareTo(new BigDecimal(safeLoan.getMoney()).subtract(new BigDecimal(2)))>0){
				turnLoanToTUser(safeLoanRecord.getSalrid());
				
			}else{
				 log.error("账户"+safeLoanService.getTUSER()+"余额不足，无法进行债权转让，请充值");
				long beftime= SafeLoanConstants.remindTime.getTime();
				long nowtime=new Date().getTime();
				if((nowtime-beftime)/(1000*60*60)>=1){
					 String userid=configService.getConfigValue("safeloan_balnosuf");
					User user= ht.get(User.class, userid);
					 smsService.sendMsg("无忧宝中间账户"+userid+"账户余额不足，无法进行债权转让，请及时充值", user.getMobileNumber());
					 SafeLoanConstants.remindTime=new Date();
				}
				
			}
		}
	}
	/**
	 * @Description: TODO(产品到期将结算期的进行期债权转让给预制用户)
	 * @author cuihang
	 * @date 2016年1月14日 16:24:47
	 * @throws Exception
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private void turnLoanToTUser(String slrid)throws Exception{
		String sql="from SafeLoan_User_Loan where safeLoanRecordId='"+slrid+"' and status="+SafeLoanConstants.SafeLoanUserLoanStatus.TZZ.getIndex()+" order by userid";
		List<SafeLoan_User_Loan> safeLoanList = ht.find(sql);
		String oldUserid="";
		for (SafeLoan_User_Loan safeLoan_User_Loan : safeLoanList) {
			String newUSerid=safeLoan_User_Loan.getUserid();
			if(!newUSerid.equals(oldUserid)&&oldUserid.length()>0){
				//将olduserid safeloanrecord更新为待付息
				String upsql="update SafeLoanRecord set status="+SafeLoanConstants.SafeLoanRecordStatus.JSQ.getIndex()+" where salrid='"+slrid+"' and userid='"+oldUserid+"' and status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex();
				ht.bulkUpdate(upsql);
			}
				//转让给tuser 并将当前SafeLoan_User_Loan更新为已转让
				String userid=safeLoan_User_Loan.getUserid();
				String loanid=safeLoan_User_Loan.getLoanid().getId();
				Double		 money=transferService.calculateWorth(safeLoan_User_Loan.getInvestId(), safeLoan_User_Loan.getLoanMoney());
				if(money==0){
					money=safeLoan_User_Loan.getLoanMoney();
				}
				//解决有的时候三位小数点
				money=new BigDecimal(money).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				Loan loan=ht.get(Loan.class, loanid);
				TurnLoanRecord tlr=new TurnLoanRecord();
				String tlrid=IdGenerator.randomUUID();
				tlr.setId(tlrid);
				tlr.setLoanid(loanid);
				tlr.setStatus(SafeLoanConstants.TurnLoanRecordStatus.CS.getIndex());
				tlr.setTurnmoney(money);
				tlr.setTuserid(safeLoanService.getTUSER());
				tlr.setUserid(userid);
				tlr.setInvestid(safeLoan_User_Loan.getInvestId());
				tlr.setCommitTime(new Date());
				ht.save(tlr);
				safeLoan_User_Loan.setStatus(SafeLoanConstants.SafeLoanUserLoanStatus.YZR.getIndex());
				safeLoan_User_Loan.setTurnId(tlrid);
				ht.update(safeLoan_User_Loan);
				SafeLoanRecord slre=ht.get(SafeLoanRecord.class, safeLoan_User_Loan.getSafeLoanRecordId());
				slre.setIncome(ArithUtil.add(slre.getIncome(),ArithUtil.sub(money, safeLoan_User_Loan.getLoanMoney())));
				slre.setRecentlyIcome(ArithUtil.add(slre.getRecentlyIcome(),ArithUtil.sub(money, safeLoan_User_Loan.getLoanMoney())));
				slre.setEnableMoney(new BigDecimal(slre.getEnableMoney()).add(new BigDecimal(money)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				ht.update(slre);
				
				userBillService.transferOutFromBalance(safeLoanService.getTUSER(), money, OperatorInfo.SAFE_LOAN_INVEST_TURNL, safeLoanService.getTUSER()+"接收"+userid+"债权"+loanid+"共"+money);
				moneyDetailRecordService.addRecord(loanid,  safeLoan_User_Loan.getSafeLoanId(),  safeLoan_User_Loan.getSafeLoanRecordId(), SafeLoanConstants.MoneyDetailRecordType.ZQZC.getIndex(), money, 0);
				if(loan.getStatus().equals("raising")||loan.getStatus().equals("recheck")||loan.getStatus().equals("dqgs")){
					//债权未放款
					userBillService.transferOutFromFrozenTosafeloanFrozen(userid, money, OperatorInfo.SAFE_LOAN_TURN,"转让债权"+loanid+loan.getName(),slre.getSafeloanid().getId());
				}else{
					//债权已放款
					userBillService.transferInsafeloanFrozen(userid, money, OperatorInfo.SAFE_LOAN_TURN,"转让债权"+loanid+loan.getName(),slre.getSafeloanid().getId());
				}
				oldUserid=newUSerid;
		}
		if(oldUserid.length()>0){
			//将olduserid safeloanrecord更新为待付息
			String upsql="update SafeLoanRecord set status="+SafeLoanConstants.SafeLoanRecordStatus.JSQ.getIndex()+" where salrid='"+slrid+"' and userid='"+oldUserid+"' and status="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex();
			ht.bulkUpdate(upsql);
		}
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean updateWuyoubaoLoanRecord(Invest invest,double interest,int status) {
		// TODO Auto-generated method stub
		boolean result=false;
		String investid=invest.getId();
		String hql="from SafeLoan_User_Loan where investId='"+investid +"'";
		List<SafeLoan_User_Loan> sullist=ht.find(hql);
		//只要有记录说明是无忧宝投的，就单独处理
		if(sullist.size()>0){
			result=true;
		}
		for (SafeLoan_User_Loan safeLoan_User_Loan : sullist) {
			if(safeLoan_User_Loan.getStatus()==SafeLoanConstants.SafeLoanUserLoanStatus.TZZ.getIndex()){
			
				//算出本息
				Double benxi=new BigDecimal(ArithUtil.add(invest.getMoney(),interest)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				//真实利息
				Double reallixi=new BigDecimal(ArithUtil.sub(benxi, safeLoan_User_Loan.getLoanMoney())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();//因为可能是债权转让的，并不是债权初始值
				//真实本金
				Double benjin=safeLoan_User_Loan.getLoanMoney();
				String safeLoanReId=safeLoan_User_Loan.getSafeLoanRecordId();
				
				SafeLoan safeLoan=ht.get(SafeLoan.class, safeLoan_User_Loan.getSafeLoanId());
				SafeLoanRecord slr=	ht.get(SafeLoanRecord.class, safeLoanReId);
				int inteertype=safeLoan.getInteertype();
				if(inteertype==SafeLoanInteerType.ONE.getIndex()){
					//自动匹配散标，自动复利投资 第一版无忧宝
					
					slr.setEnableMoney(new BigDecimal(slr.getEnableMoney()).add(new BigDecimal(benxi)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
					slr.setBeforeInvestTime(new Date());
					slr.setIncome(ArithUtil.add(slr.getIncome(),reallixi));
					slr.setRecentlyIcome(ArithUtil.add(slr.getRecentlyIcome(),reallixi));
					ht.update(slr);
					safeLoan_User_Loan.setStatus(status);
					ht.update(safeLoan_User_Loan);
					if(status==SafeLoanConstants.SafeLoanUserLoanStatus.YSY.getIndex()){
						moneyDetailRecordService.addRecord(safeLoan_User_Loan.getLoanid().getId(), safeLoan_User_Loan.getSafeLoanId(), safeLoan_User_Loan.getSafeLoanRecordId(), SafeLoanConstants.MoneyDetailRecordType.ZQDQ.getIndex(),benjin, 0);
						moneyDetailRecordService.addRecord(safeLoan_User_Loan.getLoanid().getId(),  safeLoan_User_Loan.getSafeLoanId(), safeLoan_User_Loan.getSafeLoanRecordId(), SafeLoanConstants.MoneyDetailRecordType.JX.getIndex(), reallixi, 0);
						userBillService.transferInsafeloanFrozen(slr.getUserid().getId(), benxi, OperatorInfo.SAFE_LOAN_INVSET_INCOM,"无忧宝投资债权到期收益",safeLoan_User_Loan.getSafeLoanId());
					}else if(status==SafeLoanConstants.SafeLoanUserLoanStatus.LB.getIndex()){
						//流标从冻结金额到理财冻结金额
						userBillService.transferOutFromFrozenTosafeloanFrozen(slr.getUserid().getId(),benjin, OperatorInfo.SAFE_LOAN_INVSET_INCOM,"无忧宝投资债权到期收益",safeLoan_User_Loan.getSafeLoanId());
						moneyDetailRecordService.addRecord(safeLoan_User_Loan.getLoanid().getId(), safeLoan_User_Loan.getSafeLoanId(), safeLoan_User_Loan.getSafeLoanRecordId(), SafeLoanConstants.MoneyDetailRecordType.LB.getIndex(), benjin, 0);
					}
				}else if(inteertype==SafeLoanInteerType.TWO.getIndex()){
					//自动匹配期限一致的散标，不复利投资 直接返回用户余额
					try {
						//此债权对应预期收益。
						double expectincome=safeLoanRecordService.getrealIncome(invest.getMoney(), safeLoan_User_Loan.getLoanid().getGiveMoneyTime(), new Date(), safeLoan.getRate());
						
						returnbenxi(benjin, interest, expectincome, slr, safeLoan, false,invest);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					safeLoan_User_Loan.setStatus(status);
					ht.update(safeLoan_User_Loan);
				}
				
				
				break;
			}else if(safeLoan_User_Loan.getStatus()==SafeLoanConstants.SafeLoanUserLoanStatus.YZR.getIndex()){
				//债权到期 如果债权还没有转让给其他产品将转让状态更新为债权到期
				TurnLoanRecord tlr=	ht.get(TurnLoanRecord.class,safeLoan_User_Loan.getTurnId());
				if(tlr.getStatus()==SafeLoanConstants.TurnLoanRecordStatus.CS.getIndex()){
					tlr.setStatus(SafeLoanConstants.TurnLoanRecordStatus.ZQDQ.getIndex());
					Double benxi=new BigDecimal(ArithUtil.add(invest.getMoney(),interest)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					userBillService.transferIntoBalance(tlr.getTuserid(), benxi, OperatorInfo.SAFE_LOAN_INVSET_INCOM, "债权到期收益："+invest.getLoan().getName());
					ht.update(tlr);
				}
			}
		}
		return result;
	}



	@Override
	public boolean investInclude(Invest invest) {
		// TODO Auto-generated method stub
		boolean result=false;
		String investid=invest.getId();
		String hql="from SafeLoan_User_Loan where investId='"+investid +"'";
		List<SafeLoan_User_Loan> sullist=ht.find(hql);
		//只要有记录说明是无忧宝投的，就单独处理
		if(sullist.size()>0){
			result=true;
		}
		return result;
	}



	@Override
	public boolean retunAutoTimeout() {
		// TODO Auto-generated method stub
		String hours=configService.getConfigValue(ConfigConstants.Schedule.AUTO_LOAN_OUTTIME);
		if(null!=hours&&hours.length()>0){
			Date befTime=new Date(new Date().getTime()-Long.parseLong(hours)*60*60*1000);
			String befTimeStr=DateUtil.DateToString(befTime, "yyyy-MM-dd HH:mm:ss");
			//所有超时的投资记录，并且是FBQ封闭期
			String sqlout="From SafeLoanRecord where  status ="+SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex()+" and enableMoney>0 and beforeInvestTime<'"+befTimeStr+"'";
			List<SafeLoanRecord> sllist=ht.find(sqlout);
			for (SafeLoanRecord safeLoanRecord : sllist) {
				double enablemoney=safeLoanRecord.getEnableMoney();//可投金额
				double income=safeLoanRecord.getRecentlyIcome();//近期收益实际收益
				double benjin=ArithUtil.sub(enablemoney, income);
				SafeLoan safeloan=ht.get(SafeLoan.class,safeLoanRecord.getSafeloanid().getId());
				double expectincome=safeLoanRecordService.getrealIncome(benjin, safeLoanRecord.getSalTime(),new Date(), safeloan.getRate());//真实获得利息，计划收益
				benjin=getReturnBenJin(safeLoanRecord, benjin);
				income=getReturnInCome(safeLoanRecord, income);
				
				try {
					returnbenxi(benjin,income, expectincome, safeLoanRecord, safeloan,false,null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error(e.getMessage());
				}
				
			}
		}else{
			log.error("未设置无忧宝自动投标超时小时数");
		}
		return false;
	}
	/**
	 * 
	*@Description: TODO(根据投资记录及预计要返回的本金返回真实的本金) 
	* @author cuihang   
	*@date 2016-2-18 下午3:21:31 
	*@param safeLoanRecord
	*@param benjin
	*@return
	 */
	public double getReturnBenJin(SafeLoanRecord safeLoanRecord,double benjin){
		double enablereturn=ArithUtil.sub(safeLoanRecord.getMoney(), safeLoanRecord.getReturnMoney());
		if(benjin>enablereturn){
			benjin=enablereturn;
		}
		if(enablereturn<0){
			benjin=0;
		}
		return benjin;
		
	}
	/**
	 * 
	*@Description: TODO(根据投资记录及预计要返回的利息返回真实的利息) 
	* @author cuihang   
	*@date 2016-2-18 下午3:22:34 
	*@param safeLoanRecord
	*@param income
	*@return
	 */
	
	public double getReturnInCome(SafeLoanRecord safeLoanRecord,double income){

		double enableincome=ArithUtil.sub(safeLoanRecord.getExpectincome(), safeLoanRecord.getReturnIncome());
		if(income>enableincome){
			income=enableincome;
		}
		if(enableincome<0){
			income=0;
		}
		return income;
	}
	/**
	 * 
	*@Description: TODO(返还本息操作) 
	* @author cuihang   
	*@date 2016-2-17 下午5:38:26 
	*@param income准备反
	*@param expectincome预期
	*@param safeLoanRecord
	*@param safeLoan
	*@param islast 是否最后一次反还，是的话才进行扣除管理费或者补贴收益
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void returnbenxi(double returnbenjin,double income,double expectincome,SafeLoanRecord safeLoanRecord,SafeLoan safeLoan,boolean islast,Invest invest) throws Exception{
		int inteertype=safeLoan.getInteertype();
		String detial="产品"+safeLoan.getName()+"编号:"+safeLoan.getId()+",投资人编号:"+safeLoanRecord.getUserid().getId();
		String userid=safeLoanRecord.getUserid().getId();
		if(inteertype==SafeLoanInteerType.ONE.getIndex()){
			//自动匹配散标，自动复利投资 第一版无忧宝
			String islaststr="";
			int status=SafeLoanConstants.MoneyDetailRecordType.BT.getIndex();
			if(!islast){
				islaststr="未投返还";
				status=SafeLoanConstants.MoneyDetailRecordType.WTFH.getIndex();
			}
				double realincome=safeLoanRecord.getRecentlyIcome();
				if(realincome>=expectincome){
					//扣除管理费
						//实际收益大于计划收益，收入
						Double money=new BigDecimal(realincome).subtract(new BigDecimal(expectincome)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
						systemBillService.transferInto(money, "无忧宝"+islaststr+"收益", detial+",收益"+money, null, null, null, null, null,safeLoanRecord,null,null);
						moneyDetailRecordService.addRecord(null, safeLoan.getId(), safeLoanRecord.getSalrid(), SafeLoanConstants.MoneyDetailRecordType.KCGLF.getIndex(), 0, money);
						userBillService.transferOutFromsafeloanFrozen(userid, money, OperatorInfo.INVEST_SAFE_LOAN_FEE,"产品"+safeLoan.getName()+"扣除管理费",safeLoan.getId());
					
				}else{
						Double money=new BigDecimal(expectincome).subtract(new BigDecimal(realincome)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
						systemBillService.transferOut(money, "无忧宝"+islaststr+"收益", detial+",补贴"+money, null, null, null, null, null,safeLoanRecord,null,null,null);
						moneyDetailRecordService.addRecord(null,  safeLoan.getId(), safeLoanRecord.getSalrid(),status, money,0 );
						userBillService.transferInsafeloanFrozen(userid, money, OperatorInfo.INVEST_SAFE_LOAN_ADDFEE,"产品"+safeLoan.getName()+"补齐收益",safeLoan.getId());
				}
			Double comAddMoney=new BigDecimal(returnbenjin).add(new BigDecimal(expectincome)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			moneyDetailRecordService.addRecord(null,safeLoan.getId(), safeLoanRecord.getSalrid(),SafeLoanConstants.MoneyDetailRecordType.FBFX.getIndex(), comAddMoney, 0);
			userBillService.unfreezeSafeLoanMoney(userid,comAddMoney,  OperatorInfo.INVEST_SAFE_LOAN_INCOM, "无忧宝"+safeLoan.getName()+"到期本息收益",safeLoan.getId());
			
			User user = ht.get(User.class,safeLoanRecord.getUserid().getId());
			Map<String,String> params = new HashMap<String, String>();
			if(islast){
				//无忧宝到期
				params.put("safeLoanName", safeLoan.getName());
				params.put("money", comAddMoney+"");
				params.put("username",user.getUsername());
				try {
					autoMsgService.sendMsg(user, "safeLoan_DQ", params);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}else{
				//未投返还
				
				params.put("safeLoanName", safeLoan.getName());
				params.put("enableMoney", comAddMoney+"");
				params.put("username",user.getUsername());
				params.put("deadline", safeLoan.getDeadline()+"");
				try {
					autoMsgService.sendMsg(user, "safeLoan_WT", params);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			safeLoanRecord.setRecentlyIcome(0d);
			safeLoanRecord.setEnableMoney(0d);//此种方式可投金额会被清空
		}else if(inteertype==SafeLoanInteerType.TWO.getIndex()){
			//自动匹配期限一致的散标，不复利投资 直接返回用户余额
			//如果实际收益大于预期收益则收取管理费
			User user=ht.get(User.class, userid);
			if(income>expectincome){
				//管理费
				double glf=new BigDecimal(income).subtract(new BigDecimal(expectincome)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
				moneyDetailRecordService.addRecord(null,  safeLoan.getId(), safeLoanRecord.getSalrid(),SafeLoanConstants.MoneyDetailRecordType.KCGLF.getIndex(), 0,glf );
				systemBillService.transferInto(glf, "无忧宝收益", detial+",收益"+glf, null, null, null, null, null,safeLoanRecord,null,null);
				income=expectincome;
				
			}
				int status=SafeLoanConstants.MoneyDetailRecordType.WTFH.getIndex();
				String string="债权到期本息收益";
				Double comAddMoney=new BigDecimal(returnbenjin).add(new BigDecimal(income)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			String opterStr= OperatorInfo.INVEST_SAFE_LOAN_INCOM;
				boolean sendsmsjs=false;
				if(null!=invest) {
				if(income==0){
					//流标或者啥的
					string="流标返还本金";
					status=SafeLoanConstants.MoneyDetailRecordType.LB.getIndex();
					userBillService.transferOutFromFrozenTosafeloanFrozen(userid,returnbenjin, OperatorInfo.SAFE_LOAN_INVSET_FAILRETURN,string,safeLoan.getId());
					//moneyDetailRecordService.addRecord(invest.getLoan().getId(), safeLoan.getId(),safeLoanRecord.getSalrid(), SafeLoanConstants.MoneyDetailRecordType.LB.getIndex(),returnbenjin, 0);
					 Map<String,String> params = new HashMap<String, String>();
					 params.put("username", user.getUsername());
					 params.put("safeLoanName", safeLoan.getName());
					 String loanid= invest.getLoan().getId();
					 Loan loan=ht.get(Loan.class, loanid);
				     params.put("loanName",loan.getName());
					 autoMsgService.sendMsg(user, "safeLoantwo_lb", params);
					 opterStr=OperatorInfo.SAFE_LOAN_INVSET_FAILRETURN;
				}else{
					//返本付息
					moneyDetailRecordService.addRecord(invest.getLoan().getId(), safeLoan.getId(),safeLoanRecord.getSalrid(), SafeLoanConstants.MoneyDetailRecordType.ZQDQ.getIndex(),returnbenjin, 0);
					moneyDetailRecordService.addRecord(invest.getLoan().getId(),  safeLoan.getId(), safeLoanRecord.getSalrid(), SafeLoanConstants.MoneyDetailRecordType.JX.getIndex(), income, 0);
					status=SafeLoanConstants.MoneyDetailRecordType.FBFX.getIndex();
					userBillService.transferInsafeloanFrozen(userid, comAddMoney, OperatorInfo.SAFE_LOAN_INVSET_INCOM,"无忧宝"+safeLoan.getName()+"到期本息收益",safeLoan.getId());
					sendsmsjs=true;
				}
				
			}else{
				//未匹配到散标
				//短信提醒
				string="未投资散标，返还本金";
				 Map<String,String> params = new HashMap<String, String>();
				 params.put("username", user.getUsername());
			     params.put("safeLoanName", safeLoan.getName());
				 autoMsgService.sendMsg(user, "safeLoantwo_wtfh", params);
				 opterStr=OperatorInfo.SAFE_LOAN_NOINVSET_RETURN;
			}
			moneyDetailRecordService.addRecord(null,  safeLoan.getId(), safeLoanRecord.getSalrid(),status, comAddMoney,0 );
			userBillService.transferSafeLoanIntoBalance(userid, comAddMoney,opterStr,  "无忧宝"+safeLoan.getName()+string,safeLoan.getId());
			
			if(sendsmsjs){
				String sluHql="from SafeLoan_User_Loan slul where slul.safeLoanRecordId ='"+safeLoanRecord.getSalrid()+"' and slul.status="+SafeLoanUserLoanStatus.YSY.getIndex();
				List list=ht.find(sluHql);
			
				if(list.size()==0){
					//第一次返 无忧宝投资散标第一笔结算短信
						   
							 safeLoanRecord.setFirstSms(1);
				}
				
				String sluHql1="from SafeLoan_User_Loan slul where slul.safeLoanRecordId ='"+safeLoanRecord.getSalrid()+"' and slul.status="+SafeLoanUserLoanStatus.TZZ.getIndex();
				List list1=ht.find(sluHql1);
				if(list1.size()==1){
					 safeLoanRecord.setLastSms(1);
					//最后一次返 无忧宝投资散标最后一笔结算短信
					//最后一次结算之后更新为结算期
					 safeLoanRecord.setStatus(SafeLoanRecordStatus.JSQ.getIndex());
				}
			}
			
		}
			
		double returnMoney=ArithUtil.add(safeLoanRecord.getReturnMoney(),returnbenjin);//更新累计返还本金
		double returnIncome=ArithUtil.add(safeLoanRecord.getReturnIncome(),income);//更新累计返还利息
		if(safeLoanRecord.getReturnMoney()==0&&returnMoney==safeLoanRecord.getMoney()){
			//以前没有反过并且一次性全反回则更新状态为已结清
			safeLoanRecord.setStatus(SafeLoanConstants.SafeLoanRecordStatus.YJQ.getIndex());
		}
		safeLoanRecord.setReturnMoney(returnMoney);
		safeLoanRecord.setReturnIncome(returnIncome);
		safeLoanRecord.setBeforeInvestTime(new Date());
		ht.update(safeLoanRecord);
	}



	@Override
	public void sendSafeLoanSms() throws Exception{
		// TODO Auto-generated method stub
		//0为初始 1为待发送 2为已发送
		
		String hql="from SafeLoanRecord slr where slr.firstSms=1 or slr.lastSms=1";
		List<SafeLoanRecord> list=ht.find(hql);
		for (SafeLoanRecord safeLoanRecord : list) {
			SafeLoan safeloan=ht.get(SafeLoan.class, safeLoanRecord.getSafeloanid().getId());
			User user=ht.get(User.class, safeLoanRecord.getUserid().getId());
			if(safeloan.getInteertype()==SafeLoanInteerType.TWO.getIndex()){
				if(safeLoanRecord.getLastSms()==1){
					  Map<String,String> params = new HashMap<String, String>();
						 params.put("username", user.getUsername());
					     params.put("safeLoanName", safeloan.getName());
						 autoMsgService.sendMsg(user, "safeLoantwo_last", params);
					
					safeLoanRecord.setLastSms(2);
					safeLoanRecord.setFirstSms(2);
					ht.update(safeLoanRecord);
					continue;
				}
				if(safeLoanRecord.getFirstSms()==1){
					  Map<String,String> params = new HashMap<String, String>();
						 params.put("username", user.getUsername());
					     params.put("safeLoanName", safeloan.getName());
						 autoMsgService.sendMsg(user, "safeLoantwo_first", params);
					safeLoanRecord.setFirstSms(2);
					ht.update(safeLoanRecord);
				}
			}
		}
	}
	
}
