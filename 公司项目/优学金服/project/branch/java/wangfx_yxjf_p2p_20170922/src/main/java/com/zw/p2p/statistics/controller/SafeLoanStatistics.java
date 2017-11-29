package com.zw.p2p.statistics.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.core.annotations.ScopeType;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanRecordStatus;

/**
 * 无忧宝统计
 * 
 * @author zhenghaifeng
 * @date 2016-1-22 下午3:24:01
 */
@Component
@Scope(ScopeType.REQUEST)
public class SafeLoanStatistics {

	@Resource
	HibernateTemplate ht;

	/**
	 * 获取理财产品总数
	 * 
	 * @return
	 */
	public Long getLoanRSCount() {
		String hql = "select count(loan) from SafeLoan loan where status in (1,2,3)";
		List<Object> oos = ht.find(hql, new String[] { });
		Object o = oos.get(0);
		if (o == null) {
			return 0L;
		}
		return (Long) o;
	}
	
	/**
	 * 获取投标中笔数
	 *
	 * @return
	 */
	public Long getRaisingLoanCount() {
		String hql = "select count(loan) from SafeLoan loan where loan.status=?";
		List<Object> oos = ht.find(hql, new Object[]{SafeLoanConstants.SafeLoanStatus.TZZ.getIndex()});
		Object o = oos.get(0);
		if (o == null) {
			return 0L;
		}
		return (Long) o;
	}
	
	/**
	 * 无忧宝投资成功的总的金额
	 * 
	 * @return
	 */
	public double getAllInvestsMoney() {
		String hql = "select sum(invest.money) from SafeLoanRecord invest ";
		List<Object> oos = ht.find(hql, new String[] { });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	
	/**
	 * 无忧宝累计投资金额
	 * 
	 * @return
	 */
	public double getInvestsMoneyByUserid(String userId) {
		String hql = "select sum(money) from SafeLoanRecord where userid.username=? ";
		List<Object> oos = ht.find(hql, userId);
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	
	
	/**
	 * 投资已赚金额
	 * 
	 * @return
	 */
	public double getReceivedInterest(String userId) {
		// InvestRepay
		String hql = "select sum(returnIncome) from SafeLoanRecord where  userid.username=?";

		List<Object> irs = ht.find(hql, userId);
		double money = 0;
		if(irs.size() >0 && null != irs.get(0)){
			money = Double.parseDouble(irs.get(0).toString());
		}
		return money;
	}
	
	/**
	 * 还款中的投资金额
	 * 
	 * @return
	 */
	public double getReceivableCorpus(String userId) {
		String hql = "select sum(money) from SafeLoanRecord where userid.username=? and (safeloanid.status =? or safeloanid.status =?)";
		List<Object> result = ht.find(hql, userId, SafeLoanConstants.SafeLoanStatus.TZZ.getIndex(), SafeLoanConstants.SafeLoanStatus.YMB.getIndex());
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}
	
	/**
	 * 无忧宝待收本金
	 * 
	 * @return
	 */
	public double getToReceivableCorpus(String userId) {
		String hql = "select sum(money) from SafeLoanRecord where userid.username=? and status in ("+SafeLoanRecordStatus.FBQ.getIndex()+","+SafeLoanRecordStatus.JSQ.getIndex()+")";
		List<Object> result = ht.find(hql, userId);
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		return money;
	}
	
	/**
	 * 本月待收金额
	 * 
	 * @return
	 */
	public double getReceivableInterest(String userId) throws ParseException{
		String hql = "select sum(expectincome) + sum(money) from SafeLoanRecord where userid.username=? and (safeloanid.endTime >= ? and safeloanid.endTime < ?)";

		List<Object> result = ht.find(hql, userId, nowMonthFirstDate(), nextMonthFirstDate());
		double money = 0;
		if (result != null && result.get(0) != null) {
			money = (Double) result.get(0);
		}
		
		return money;
	}
	
	
	private static Date nextMonthFirstDate() throws ParseException{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String nextMonthFirstDate = sdf.format(calendar.getTime());
		 
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(nextMonthFirstDate + " 00:00:00");
	}
	
	private static Date nowMonthFirstDate() throws ParseException {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String nextMonthFirstDate = sdf.format(calendar.getTime());
		 
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse(nextMonthFirstDate + " 00:00:00");
	}
}
