package com.zw.p2p.invest.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.VirtualLoanRecord;

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
public class VirtualLoanList extends EntityQuery<VirtualLoanRecord> implements Serializable{

	private static final String lazyModelCountHql = "select count(distinct vir) from VirtualLoanRecord vir";
	private static final String lazyModelHql = "select distinct vir from VirtualLoanRecord vir";
	

	public VirtualLoanList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				"vir.userid like #{virtualLoanList.example.userid}"
				};
		this.setPageSize(7);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	@Override
	protected void initExample() {
		VirtualLoanRecord example = new VirtualLoanRecord();
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

}
