package com.zw.archer.user.controller;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.RechargeBankCard;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.user.service.RechargeService;

/**
 * 充值查询
 * 
 */
@Component
@Scope(ScopeType.VIEW)
public class RechargeList extends EntityQuery<Recharge> implements
		java.io.Serializable {

	private static final long serialVersionUID = 9057256750216810237L;

	@Logger
	static Log log;

	@Resource
	private RechargeService rechargeService;

	private List<RechargeBankCard> rechargeBankCards;
	
	private Date startTime ;
	private Date endTime ;

	private String userName;
	private String realName;

	public RechargeList() {
		final String[] RESTRICTIONS = { "id like #{rechargeList.example.id}",
				"time >= #{rechargeList.startTime}",
				"time <= #{rechargeList.endTime}",
				"status = #{rechargeList.example.status}",
				"rechargeWay like #{rechargeList.example.rechargeWay}",
//				"user.username like #{rechargeList.example.user.username}"
				"user.username like #{rechargeList.userName}",
				"user.realname like #{rechargeList.realName}"
		};

		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
//		addOrder("time", super.DIR_DESC);
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = "%"+realName+"%";
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = "%"+userName+"%";
	}

	@Override
	protected void initExample() {
		super.initExample();
		getExample().setUser(new User());
	}

	public List<RechargeBankCard> getRechargeBankCards() {
		if (this.rechargeBankCards == null) {
			this.rechargeBankCards = rechargeService.getBankCardsList();
		}
		return this.rechargeBankCards;
	}

	public Double getSumActualMoney(){
		final String hql = parseHql("Select sum(actualMoney) from Recharge");
		@SuppressWarnings("unchecked")
		List<Double> resultList = getHt().execute(new HibernateCallback<List<Double>>() {

			public List<Double> doInHibernate(Session session)
			
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
	
	//~~~~~~~~~~~~~~~~
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
