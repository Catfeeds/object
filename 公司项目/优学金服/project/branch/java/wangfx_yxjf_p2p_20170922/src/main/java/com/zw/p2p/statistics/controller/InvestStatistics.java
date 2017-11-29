package com.zw.p2p.statistics.controller;

import com.zw.archer.config.model.Config;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.ArithUtil;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.InvestPulished;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.repay.model.InvestRepay;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 投资统计
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.REQUEST)
public class InvestStatistics {

	@Resource
	private HibernateTemplate ht;
	

	/**
	 * 已收本金
	 * 
	 * @return
	 */
	public double getReceivedCorpus(String userId) {
		// InvestRepay
		String hql = "Select sum(corpus) from InvestRepay where (time is not null and status!='interesting') and invest.user.id = ? and invest.isSafeLoanInvest!=? and time <= ?";
		List<Object> result = ht.find(hql, userId,true, new Date());

		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}

		return money;
	}

	/**
	 * 应收（待收）本金
	 * 
	 * @return
	 */
	public double getReceivableCorpus(String userId) {
		String hql = "Select sum(corpus) from InvestRepay where (time is null or status='interesting') and invest.user.id = ? and invest.isSafeLoanInvest!=?";
		List<Object> result = ht.find(hql, userId,true);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 累计投资额
	 * @param userId
	 * @return
	 */
	public double getAllCorpus(String userId){
		String hql = "from Invest i where i.user.id = ? and i.status != ? and i.isSafeLoanInvest!=?";
		double money = 0D;
		List<Invest> invests = ht.find(hql,userId,"cancel",true);
		for (Invest invest : invests){
			money += invest.getInvestMoney();
		}
		List<TransferApply> tas = ht.find("from TransferApply ta where ta.invest.user.id = ? and invest.isSafeLoanInvest!=? and ta.status = ?",userId,true,"transfered");
		for (TransferApply ta : tas){
			money -= ta.getCorpus();
		}
		return money;
	}

	/**
	 * 预计总收益
	 * @param userId
	 * @return
	 */
	public double getAllInterest(String userId){
		String hql = "from InvestRepay ir where  ir.invest.user.id = ?  and ir.invest.isSafeLoanInvest!=? and (ir.status = 'repaying' or ir.status = 'interesting')";
		double allInterest = 0D;
		List<InvestRepay> investRepays = ht.find(hql,userId,true);
		for (InvestRepay ir : investRepays){
			allInterest += ir.getInterest() - ir.getFee();
		}
		return  allInterest;
	}

	/**
	 * 应收（待收）本金，还款日在repayDay之前
	 * 
	 * @return
	 */
	public double getReceivableCorpus(String userId, Date repayDay) {
		String hql = "Select sum(corpus) from InvestRepay where time is null and invest.user.id = ? and invest.isSafeLoanInvest!=? and repayDay <? ";
		List<Object> result = ht.find(hql, new Object[] { userId,true, repayDay });
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 已收利息
	 * 
	 * @return
	 */
	public double getReceivedInterest(String userId) {
		// InvestRepay
		String hql = "from InvestRepay where (time is not null and status!='interesting') and invest.user.id = ? and invest.isSafeLoanInvest!=? and time <= ?";

		List<InvestRepay> irs = ht.find(hql, userId,true, new Date());
		double money = 0;
		for (InvestRepay  ir : irs){
			if (!"advance".equals(ir.getRepayWay())){
				money += ir.getInterest() + ir.getDefaultInterest() - ir.getFee();
			}else {
				money += ir.getDefaultInterest();
			}
		}
		String tahql = "from TransferApply ta where ta.invest.user.id = ? and ta.status = ?";
		double fee = 0D;
		List<TransferApply> tas = ht.find(tahql,userId,"transfered");
		for (TransferApply ta : tas){
			fee += ta.getPremium()+ta.getFee();
		}
		String ihql = "from Invest invest where invest.user.id = ? and invest.isSafeLoanInvest!=? and invest.transferApply is not null";
		double premium = 0D;
		List<Invest> invests = ht.find(ihql,userId,true);
		for (Invest invest : invests){
			premium += invest.getTransferApply().getPremium();
		}
		return money - fee + premium;
	}

	/**
	 * 应收（待收）利息
	 * 
	 * @return
	 */
	public double getReceivableInterest(String userId) {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where (time is null or status='interesting') and invest.user.id = ? and invest.isSafeLoanInvest!=?";

		List<Object> result = ht.find(hql, userId,true);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}

	/**
	 * 应收（待收）利息 ，还款日在repayDay之前
	 * 
	 * @return
	 */
	public double getReceivableInterest(String userId, Date repayDay) {
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is null and invest.user.id = ? and invest.isSafeLoanInvest!=? and repayDay <? ";

		List<Object> result = ht.find(hql, new Object[] { userId,true, repayDay });
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;

	}
	
	/**
	 * 计算平台上所有成功投资的总人数
	 */
	public long getAllInvestUserNumber(){
		String hql = "select count(distinct user) from Invest invest where invest.status <> ?";
		return (Long) ht.find(hql,InvestConstants.InvestStatus.CANCEL).get(0);
	}
	
	/**
	 * 计算平台上所有实现借款的总人数
	 */
	public long getAllLoanUserNumber(){
		String hql = "select count(distinct user) from Loan";
		return (Long) ht.find(hql).get(0);
	}


	/**
	 * 计算在平台上指定用户当前正在投资的数量(投标中,还款中)
	 *
	 * @return
	 */
	public Long getActiveInvestsCount(String user_id) {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.user.id=? and invest.isSafeLoanInvest!=? and invest.status in (?,?)";
		List<Object> oos = ht.find(hql, new Object[] {
				user_id,true,
				InvestConstants.InvestStatus.BID_SUCCESS,
				InvestConstants.InvestStatus.REPAYING });
		Object o = oos.get(0);
		if (o == null) {
			return 0l;
		}

		return (Long)o;
	}

	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney() {
		Double initMoney = 0d;
		Config config = ht.get(Config.class, "initmoney");
		if (config != null) {
			initMoney = config.getValue()==null?0d:Double.valueOf(config.getValue());
		}
		String hql = "select sum(invest.money) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.id IN (select id from Loan loan WHERE loan.loantypeb = 'ea38ad9c7e3140f5891720aad0efa30d')";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL });
		Object o = oos.get(0);
		if (o == null) {
			return initMoney;
		}
		return initMoney+(Double) o;
	}
	
	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney2() {
		Double initMoney = 0d;
		Config config = ht.get(Config.class, "initmoney");
		if (config != null) {
			initMoney = config.getValue()==null?0d:Double.valueOf(config.getValue());
		}
		String hql = "select sum(invest.money) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.id IN (select id from Loan loan WHERE loan.loantypeb = 'xsdb326c5b664870b698739e93f2d6xs')";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL });
		Object o = oos.get(0);
		if (o == null) {
			return initMoney;
		}
		return initMoney+(Double) o;
	}
	
	
	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney(String businessType) {
		String hql = "select sum(invest.investMoney) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.businessType=? and invest.transferApply is null";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL, businessType });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	
	/**
	 * 计算在平台上所有用户已经投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney2(String businessType) {
		String hql = "select sum(invest.investMoney) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.businessType=? and invest.transferApply is null"
				+ " and invest.loan.id IN (select id from Loan loan WHERE loan.loantypeb = 'xsdb326c5b664870b698739e93f2d6xs')";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL, businessType });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 计算在平台上所有用户已经投资成功的总的收益
	 * 
	 * @return
	 */
	public double getAllInvestsInterest() {
	Double initMoney = 0d;
		Config config = ht.get(Config.class, "initmoneyinterest");
		if (config != null) {
			initMoney = config.getValue()==null?0d:Double.valueOf(config.getValue());
		}
		String hql = "Select sum(interest+defaultInterest-fee) from InvestRepay where time is not null";
		List<Object> oos = ht.find(hql);
		Object o = oos.get(0);
		if (o == null) {
			return initMoney;
		}
		return  ArithUtil.add((Double)o, initMoney);
	}

	/**
	 * 获取所有成功的投资数量
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getAllSuccessInvestsNum() {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.status not in (?,?)";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL });
		if (oos.get(0) == null) {
			return 0;
		}
		return (Long) oos.get(0);
	}

	/**
	 * 获取所有成功的投资数量
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public long getAllSuccessInvestsNum(String businessType) {
		String hql = "select count(invest) from Invest invest "
				+ "where invest.status not in (?,?) and invest.loan.businessType=?";
		List<Object> oos = ht.find(hql, new String[] {
				InvestConstants.InvestStatus.WAIT_AFFIRM,
				InvestConstants.InvestStatus.CANCEL, businessType });
		if (oos.get(0) == null) {
			return 0;
		}
		return (Long) oos.get(0);
	}

	/**
	 * 投资排行榜
	 * 
	 * @return
	 */
	public List<InvestPulished> getIps() {
		Calendar c1 = Calendar.getInstance();
		c1.set(1000, 1, 1);
		
		Calendar c2 = Calendar.getInstance();
		c2.set(9000, 12, 31);
		
		return getIps(c1.getTime(), c2.getTime());
	}
	
	/**
	 * 投资排行榜
	 * 
	 * @return
	 */
	public List<InvestPulished> getIps(final Date startTime ,final Date endTime) {
		List<InvestPulished> ips = new ArrayList<InvestPulished>();
		final String hql = "SELECT invest.user.id, SUM(ir.corpus), SUM(ir.interest) FROM InvestRepay ir where ir.invest.time >= ? and ir.invest.time <= ? GROUP BY ir.invest.user ORDER BY SUM(ir.corpus) desc";
		@SuppressWarnings("unchecked")
		List<Object[]> objs = ht
				.execute(new HibernateCallback<List<Object[]>>() {
					public List<Object[]> doInHibernate(Session session)
							throws HibernateException, SQLException {
						Query query = session.createQuery(hql);
						
						query.setParameter(0, startTime);
						query.setParameter(1, endTime);
						// 从第0行开始
						query.setFirstResult(0);
						query.setMaxResults(5);
						return query.list();
					}
				});
		if (objs.size() > 0) {
			for (Object obj : objs) {
				Object[] objs2 = (Object[]) obj;
				InvestPulished ip = new InvestPulished((String) objs2[0],
						(Double) objs2[1], (Double) objs2[2]);
				ips.add(ip);
			}
		}
		return ips;
	}

}
