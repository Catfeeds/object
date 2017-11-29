package com.zw.p2p.invest.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.loan.model.Loan;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Filename: InvestList.java Description: Copyright: Copyright (c)2013 Company:
 * p2p
 * 
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:27:32
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class InvestList extends EntityQuery<Invest> implements Serializable{

	private Date searchcommitMinTime;
	private Date searchcommitMaxTime;
	private String saflInvest="";
	private static final String lazyModelCountHql = "select count(distinct invest) from Invest invest";
	private static final String lazyModelHql = "select distinct invest from Invest invest";
	

	public InvestList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				"invest.id like #{investList.example.id}",
				"invest.money = #{investList.example.money}",
				"invest.status like #{investList.example.status}",
				"invest.loan.user.id like #{investList.example.loan.user.id}",
				"invest.user.realname like #{investList.example.user.realname}",
				"invest.loan.id like #{investList.example.loan.id}",
				"invest.loan.name like #{investList.example.loan.name}",
				"invest.loan.type like #{investList.example.loan.type}",
				"invest.user.id = #{investList.example.user.id}",
				"invest.user.referrerId = #{investList.example.user.referrerId}",
				"invest.user.username = #{investList.example.user.username}",
				"invest.time >= #{investList.searchcommitMinTime}",
				"invest.status like #{investList.example.status}",
				"invest.time <= #{investList.searchcommitMaxTime}",
				"invest.transferApply.id = #{investList.example.transferApply.id}"};
		this.addOrder("time","desc");
		this.setPageSize(7);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	@Override
	protected void initExample() {
		Invest example = new Invest();
		Loan loan = new Loan();
		loan.setUser(new User());
		example.setUser(new User());
		example.setLoan(loan);
		example.setTransferApply(new TransferApply());
		setExample(example);
	}

	public Object getSumMoney(){
		
		final String hql = parseHql("Select sum(invest.money) from Invest invest");
		@SuppressWarnings("unchecked")
		List<Object> resultList = getHt().execute(new HibernateCallback<List<Object>>() {

			public List<Object> doInHibernate(Session session)
			
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
				// 从第0行开始
				query.setFirstResult(0);
				query.setMaxResults(5);
				for (int i = 0; i < getParameterValues().length; i++) {
					query.setParameter(i, getParameterValues()[i]);
				}
				return query.list();
			}

		});
		
		if(resultList != null && resultList.get(0) != null){
			return resultList.get(0);
		}
		return 0D;
	}
	
	public Date getSearchcommitMinTime() {
		return searchcommitMinTime;
	}

	public void setSearchcommitMinTime(Date searchcommitMinTime) {
		this.searchcommitMinTime = searchcommitMinTime;
	}

	public Date getSearchcommitMaxTime() {
		return searchcommitMaxTime;
	}

	public void setSearchcommitMaxTime(Date searchcommitMaxTime) {
		this.searchcommitMaxTime = searchcommitMaxTime;
	}





	public String getSaflInvest() {
		return saflInvest;
	}

	public void setSaflInvest(String sInvest) {
		String saloanstr="invest.isSafeLoanInvest = true";//无忧宝投资
		String nosaloanstr="invest.isSafeLoanInvest = false";//散标投资
		if(sInvest.equals("2")){
			this.removeRestriction(saloanstr);
			this.removeRestriction(nosaloanstr);
		}else if(sInvest.equals("0")){
			this.removeRestriction(saloanstr);
			this.removeRestriction(nosaloanstr);
			this.addRestriction(nosaloanstr);
		}else{
			this.removeRestriction(saloanstr);
			this.removeRestriction(nosaloanstr);
			this.addRestriction(saloanstr);
		}
	}

	/**
	 * 设置查询的起始和结束时间
	 */
	public void setSearchStartEndTime(Date startTime, Date endTime) {
		this.searchcommitMinTime = startTime;
		this.searchcommitMaxTime = endTime;
	}

}
