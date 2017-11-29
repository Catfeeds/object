package com.zw.p2p.repay.controller;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.model.LoanRepay;

@Component
@Scope(ScopeType.VIEW)
public class LoanRepayList extends EntityQuery<LoanRepay> {
	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;
	private Date searchMinTime;
	private Date searchMaxTime;
	private String initSearchLine;
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdftime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public LoanRepayList() {
		final String[] RESTRICTIONS = { "id like #{loanRepayList.example.id}",
				"loan.id like #{loanRepayList.example.loan.id}",
				"loan.user.id = #{loanRepayList.example.loan.user.id}",
				"repayDay >= #{loanRepayList.searchMinTime}",
				"repayDay <= #{loanRepayList.searchMaxTime}",
				"status like #{loanRepayList.example.status}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	@Override
	protected void initExample() {
		LoanRepay example = new LoanRepay();
		Loan loan = new Loan();
		loan.setUser(new User());
		example.setLoan(loan);
		setExample(example);
	}
	
	public Object getSumMoney(){
		final String hql = parseHql("Select sum(corpus),sum(interest),sum(fee) from LoanRepay");
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

	public Date getSearchMinTime() {
		return searchMinTime;
	}

	public void setSearchMinTime(Date searchMinTime) {
		this.searchMinTime = searchMinTime;
	}

	public Date getSearchMaxTime() {
		if(null!=searchMaxTime){
			try {
				return sdftime.parse(sdf.format(searchMaxTime)+" 23:59:59");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			return searchMaxTime;
		
		
	}

	public void setSearchMaxTime(Date searchMaxTime) {
		this.searchMaxTime = searchMaxTime;
	}
	


	public String getInitSearchLine() {
		return initSearchLine;
	}

	//初始化搜索时间
	public void setInitSearchLine(String initSearchLine){
		if(null!=initSearchLine&&initSearchLine.length()>0){
			int days=Integer.parseInt(initSearchLine)-1;
			Date date=new Date();
			setSearchMaxTime(new Date(date.getTime()+days*24*60*60*1000l));
			setSearchMinTime(date);
		}
	}
	public Long getNumSearchLine(String initSearchLine) {
		if(null!=initSearchLine&&initSearchLine.length()>0){
			int days=Integer.parseInt(initSearchLine)-1;
			Date searchMinTimed=new Date();
			Date searchMaxTimed=new Date(searchMinTimed.getTime()+days*24*60*60*1000l);
			
			String searchMinTimedstr=sdf.format(searchMinTimed)+" 00:00:00";
			String searchMaxTimedstr=sdf.format(searchMaxTimed)+" 23:59:59";
		String hql = "select count(loanRepay.id) from LoanRepay loanRepay where status='"+RepayStatus.REPAYING+"' and repayDay >= '"+searchMinTimedstr+"' and repayDay <= '"+searchMaxTimedstr+"'";
		List<Object> oos = ht.find(hql);
		Object o = oos.get(0);
		if (o == null) {
			return 0l;
		}
		return (Long) o;
		}
		
		return 0l;
	}
}
