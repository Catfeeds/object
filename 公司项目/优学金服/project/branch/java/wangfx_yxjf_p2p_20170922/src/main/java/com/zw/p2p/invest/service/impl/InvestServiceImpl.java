package com.zw.p2p.invest.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cfca.util.pki.cipher.Session;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.invest.exception.ExceedMaxAcceptableRate;
import com.zw.p2p.invest.exception.ExceedMoneyNeedRaised;
import com.zw.p2p.invest.exception.IllegalLoanStatusException;
import com.zw.p2p.invest.exception.InvestException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.loan.LoanConstants.LoanStatus;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.service.RateTicketService;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.service.RepayService;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-22 上午10:48:02
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-22 wangzhi 1.0
 */
@Service("investService")
public class InvestServiceImpl implements InvestService {

	@Resource
	HibernateTemplate ht;

	@Resource
	UserBillService ubs;

	@Resource
	ConfigService cs;

	@Resource
	LoanCalculator loanCalculator;

	@Resource
	RepayService repayService;

	@Resource
	UserBillBO userBillBO;

	@Resource
	CouponSService couponSService;
	@Resource
	private HuiFuLoanService huiFuLoanService;
	@Resource
	private ConfigService configService;
	@Resource
	private RateTicketService rateTicketService;
	@Logger
	private static Log log;
	
	// @Resource
	// AutoInvestService autoInvestService;

	@Override
	public String generateId(String loanId) {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);

		String hql = "select im from Invest im where im.id = (select max(imM.id) from Invest imM where imM.id like ?)";
		List<Invest> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			Invest im = contractList.get(0);
			ht.lock(im, LockMode.UPGRADE);
			String temp = im.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%06d", itemp);
		return gid;
	}

	/**
	 * 获取指定用户融资笔数，除了不通过/流标/完成的
	 *
	 * @return
	 */
	public Long getActiveLoanCount(String user_id) {
		String hql = "select count(loan) from Loan loan where loan.user.id=? and loan.status not in(?,?,?)";
		List<Object> oos = ht.find(hql, new String[] {user_id,
				LoanStatus.COMPLETE, LoanStatus.CANCEL, LoanStatus.VERIFY_FAIL});
		Object o = oos.get(0);

		if (o == null) {
			log.info(">>getActiveLoanCount is null ,value = 0");
			return 0L;
		}

		log.info(">>getActiveLoanCount value = "+ o+"\t user_id="+ user_id);

		return (Long) o;
	}
	/**
	 * 投资
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String create(Invest invest) throws InsufficientBalance,
			ExceedMoneyNeedRaised, ExceedMaxAcceptableRate,
			IllegalLoanStatusException,InvestException,Exception {

		String loanId = invest.getLoan().getId();
		invest.setInvestMoney(invest.getMoney());
		//防止并发出现
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);

		//如果正在借款，则不能投资,by lijin,2015-2-1
//		Long cnt= loanStatistics.getActiveLoanCount(loginUser.getLoginUserId());
		String loanpeopleinvest=configService.getConfigValue("loanpeopleinvest");
		//正在借款的人是否可以投资 1可以，2不可以
		if("2".equals(loanpeopleinvest)){
			Long cnt= getActiveLoanCount(invest.getUser().getId());
			if (cnt>0)
				throw new InvestException("您现在正在借款，不能投资！");
		}
		

		//如果借款不是在筹款中，抛出异常
		if (!loan.getStatus().equals(LoanStatus.RAISING)) {
			throw new IllegalLoanStatusException(loan.getStatus());
		}

		// 判断项目尚未认购的金额，如果用户想认购的金额大于此金额，则。。。
		double remainMoney;
		try {
			remainMoney = loanCalculator.calculateMoneyNeedRaised(loan.getId());
		} catch (NoMatchingObjectsException e) {
			throw new RuntimeException(e);
		}
		//输入的投资金额 大于 可投资的金额（尚未认购的金额） 抛出异常
		if (invest.getMoney() > remainMoney) {
			throw new ExceedMoneyNeedRaised();
		}
		//判断加息券是否可用
		RateTicket rateTicket=null; 
		String rateTicketId=null;
		if(invest.getRateTicket()!=null){
			rateTicketId=invest.getRateTicket().getId();
		}
		if (rateTicketId!=null) {
			rateTicket=rateTicketService.getRateTicketById(rateTicketId);
			//使用红包
			invest.setRateTicket(rateTicket);
			/*rateTicket.setStatus("using");
			rateTicket.setUsedTime(new Date());
			ht.update(rateTicket);*/
		}
		if(rateTicketId==null){
			invest.setRateTicket(null);
		}
		
		//判断红包是否可用
		Coupons coupons=null; 
		String couponsid=null;
		if(invest.getCoupons()!=null){
			couponsid=invest.getCoupons().getId();
		}
		if (couponsid!=null) {
			coupons=couponSService.getCouponsById(couponsid);
			//使用红包
			invest.setCoupons(coupons);
			/*coupons.setStatus("using");
			coupons.setUsedTime(new Date());*/
			// investMoney>红包金额+余额
			if (invest.getMoney() >
					invest.getCoupons().getMoney()
					+ ubs.getBalance(invest.getUser().getId())) {
				throw new InsufficientBalance();
			}
			if(invest.getMoney() < coupons.getMoneyCanUse()){
				throw new InvestException("投资金额与红包不匹配，请返回操作！");
			}
			Double d=loanCalculator.calculateMoneyNeedRaised(invest.getLoan().getId());
			if(d < coupons.getMoney()){
				invest.getCoupons().setMoney(d);
			}
		}else if (invest.getMoney() > ubs.getBalance(invest.getUser().getId())) {
			throw new InsufficientBalance();
		}
		if(couponsid==null){
			invest.setCoupons(null);
		}
		// 判断是否有代金券；判断能否用代金券
//		if (invest.getUserCoupon() != null) {
//			//代金券不是未使用状态，抛出异常
//			if (!invest.getUserCoupon().getStatus()
//					.equals(CouponConstants.UserCouponStatus.UNUSED)) {
//				throw new ExceedDeadlineException();
//			}
//			// 判断代金券是否达到使用条件
//			if (invest.getMoney() < invest.getUserCoupon().getCoupon()
//					.getLowerLimitMoney()) {
//				throw new UnreachedMoneyLimitException();
//			}
//			// 用户填写认购的钱数以后，判断余额，如果余额不够，不能提交，弹出新页面让用户充值
//			// investMoney>代金券金额+余额
//			if (invest.getMoney() > invest.getUserCoupon().getCoupon()
//					.getMoney()
//					+ ubs.getBalance(invest.getUser().getId())) {
//				throw new InsufficientBalance();
//			}
//			//investMoney > 可用余额，抛异常
//		} else if (invest.getMoney() > ubs.getBalance(invest.getUser().getId())) {
//			throw new InsufficientBalance();
//		}
		invest.setRate(loan.getRate());
		invest.setTime(new Date());
		invest.setIsSafeLoanInvest(false);
		invest.setDitch("PC");
		invest.setId(generateId(invest.getLoan().getId()));
			if (invest.getTransferApply() == null || StringUtils.isEmpty(invest.getTransferApply().getId())) {
			invest.setTransferApply(null);
		}
			String result="";
			result=	 huiFuLoanService.InitiativeTender(invest,loan);
			
		return result;
		
		/*invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
		//invest.setRate(loan.getRate());
		invest.setTime(new Date());
		invest.setIsSafeLoanInvest(false);
		invest.setDitch("PC");
		// 投资成功以后，判断项目是否有尚未认购的金额，如果没有，则更改项目状态。
		invest.setId(generateId(invest.getLoan().getId()));
		if (invest.getTransferApply() == null || StringUtils.isEmpty(invest.getTransferApply().getId())) {
			invest.setTransferApply(null);
		}
		ht.save(invest);
		try {
			//处理借款募集完成
			loanService.dealRaiseComplete(loan.getId());
		} catch (NoMatchingObjectsException e) {
			throw new RuntimeException(e);
		}

		// 将金额冻结，借款项目执行时，把钱打给借款者
//		if (invest.getUserCoupon() != null) {
//			// 代金券已使用，冻结：investMoney-代金券金额
//			userCouponService.userUserCoupon(invest.getUserCoupon().getId());
//			//实际 冻结金额=investMoney-代金券
//			double realMoney = ArithUtil.sub(invest.getMoney(), invest
//					.getUserCoupon().getCoupon().getMoney());
//			if (realMoney < 0) {
//				realMoney = 0;
//			}
//			ubs.freezeMoney(invest.getUser().getId(), realMoney, OperatorInfo.INVEST_SUCCESS, "投资成功：冻结金额,项目：" + invest.getLoan().getName(),invest.getLoan().getId());
		if (coupons!=null) {
			// 代金券已使用，冻结：investMoney-代金券金额
			try {
				couponSService.userUserCoupon(couponsid);
			} catch (Exception e) {
				throw new InvestException(e.getMessage());
			}
			//实际 冻结金额=investMoney-代金券
			double realMoney = ArithUtil.sub(invest.getMoney(), invest.getCoupons().getMoney());
			if (realMoney < 0) {
				realMoney = 0;
			}
			ubs.freezeMoney(invest.getUser().getId(), realMoney, OperatorInfo.INVEST_SUCCESS, "投资成功：冻结金额,项目：" + invest.getLoan().getName(),invest.getLoan().getId());
		} else {
			ubs.freezeMoney(invest.getUser().getId(), invest.getMoney(), OperatorInfo.INVEST_SUCCESS,
					"投资成功：冻结金额,项目：" + invest.getLoan().getName(),invest.getLoan().getId());
		}
*/
		
	}

	@Override
	public Invest getInvstById(String id) {
		return ht.get(Invest.class, id);
		}
	@Override
	public Invest getInvstByOrdId(String ordId) {
		String hql="from Invest invest where invest.ordId=?";
		List<Invest> list=ht.find(hql, ordId);
		if(null!=list&&list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	public  Invest getLastInvestByUser(String userid) {
		String  hql = "from Invest invest where invest.user.id='"+userid+"' and invest.isSafeLoanInvest=false and invest.isAutoInvest=false order by invest.time desc";
		List<Invest> Investlist = ht.find(hql);
				if(null!=Investlist&&Investlist.size() > 0){
					Invest invest = new Invest();
					invest = Investlist.get(0);
					return invest;
					}
				return null;
	}
	
	@Transactional(readOnly = false,propagation = Propagation.REQUIRES_NEW)
	public void UpdateInvestRepay(InvestRepay investRepay) {
		ht.update(investRepay);
	}

	@Override
	public List<InvestRepay> findInvestRepay(String loanId, Integer period) {
		List<InvestRepay> irs = ht.find("from InvestRepay ir where ir.invest.loan.id=? and ir.period=? ",
				new Object[] { loanId, period });
		return irs;
	}

	public Long getInvestNumber(Date endDate,User user) {
		String hql="select count(id) from Invest invest where invest.time <="+endDate+" and invest.user.id='"+user.getId()+"'";
		return (Long) ht.find(hql).get(0);
	}
	
	@Override
	  public List<Invest> getInvestsByLoanType(User user, String loantypeb) {
	    // TODO Auto-generated method stub
	    String hql="from Invest invest where invest.user.id=? and invest.loan.loantypeb=?";
	    List<Invest> Investlist = ht.find(hql, user.getId(),loantypeb);
	    
	    return Investlist;
	  }
}

