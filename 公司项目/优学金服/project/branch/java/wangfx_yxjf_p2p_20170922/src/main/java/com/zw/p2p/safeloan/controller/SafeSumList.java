package com.zw.p2p.safeloan.controller;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.safeloan.common.SafeLoanConstants.MoneyDetailRecordType;
import com.zw.p2p.safeloan.view.SafeSum;

@Component
@Scope(ScopeType.VIEW)
public class SafeSumList extends EntitySQLQuery<SafeSum> {
	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;
	
    private Date salMaxTime,salMinTime;
    private Date endMaxTime,endMinTime;
    private Date loanMaxrepayday,loanMinrepayday;

	private static final String lazyModelCountHql = "SELECT COUNT(aaa.loanid) FROM ((SELECT DISTINCT (sul.investId), r.returnIncome, r.expectincome, '' AS wtfhmoney, mrfu.outmoney AS mrfumoney, mrbq.inMoney AS mrbqmoney, sul.safeLoanId AS safeLoanId, sul.userid AS safeuserid, r.money AS money, sul.status AS loanstatus, sul.commitTime AS loancommitTime, l.id AS loanid, l.user_id AS loanuser, sul.loanMoney AS investmoney, l.rate AS loanrate, s.name AS NAME, s.deadline AS deadline, u.username AS username, s.rate AS rate, s.unit, s.status AS safestatus, r.salTime AS salTime, r.endTime AS endTime, '' AS tuihuanri, ui.username AS loanusername FROM safeloan_user_loan sul LEFT JOIN SafeLoanRecord r ON r.salrid = sul.safeLoanRecordId LEFT JOIN safeloan s ON s.id = sul.safeLoanId LEFT JOIN loan l ON l.id = sul.loanid LEFT JOIN USER u ON u.id = sul.userid LEFT JOIN USER ui ON ui.id = l.user_id LEFT JOIN moneydetailrecord mrbq ON mrbq.type = 8 AND mrbq.safeLoanRecordId = r.salrid LEFT JOIN moneydetailrecord mrfu ON mrfu.type = 4 AND mrfu.safeLoanRecordId = r.salrid LEFT JOIN moneydetailrecord mrwtfh ON mrwtfh.type = 9 AND mrwtfh.safeLoanRecordId = r.salrid) UNION ALL (SELECT sul.investId, r.returnIncome, r.expectincome, mrbas.inMoney AS wtfhmoney, '' AS mrfumoney, '' AS mrbqmoney, r.safeLoanId AS safeLoanId, r.userid AS safeuserid, r.money AS money, sul.status AS loanstatus, '' AS loancommitTime, '' AS loanid, '' AS loanuser, '' AS investmoney, 0 AS loanrate, s.name AS NAME, s.deadline AS deadline, u.username AS username, s.rate AS rate, s.unit, s.status AS safestatus, r.salTime AS salTime, r.endTime AS endTime, mrbas.commitTime AS tuihuanri, '' AS loanusername FROM moneydetailrecord mrbas LEFT JOIN safeloan_user_loan sul ON sul.safeLoanRecordId = mrbas.safeLoanRecordId LEFT JOIN SafeLoanRecord r ON r.salrid = mrbas.safeLoanRecordId LEFT JOIN safeloan s ON s.id = mrbas.safeLoanId LEFT JOIN loan l ON l.id = sul.loanid LEFT JOIN USER u ON u.id = sul.userid WHERE mrbas.type = 9)) aaa";
	
	private static final String lazyModelHql = "(SELECT DISTINCT (sul.investId), r.returnIncome, r.expectincome, '' AS wtfhmoney, mrfu.outmoney AS mrfumoney, mrbq.inMoney AS mrbqmoney, sul.safeLoanId AS safeLoanId, sul.userid AS safeuserid, r.money AS money, sul.status AS loanstatus, sul.commitTime AS loancommitTime, l.id AS loanid, l.user_id AS loanuser, sul.loanMoney AS investmoney, l.rate AS loanrate, s.name AS NAME, s.deadline AS deadline, u.username AS username, s.rate AS rate, s.unit, s.status AS safestatus, r.salTime AS salTime, r.endTime AS endTime, '' AS tuihuanri, ui.username AS loanusername FROM safeloan_user_loan sul LEFT JOIN SafeLoanRecord r ON r.salrid = sul.safeLoanRecordId LEFT JOIN safeloan s ON s.id = sul.safeLoanId LEFT JOIN loan l ON l.id = sul.loanid LEFT JOIN USER u ON u.id = sul.userid LEFT JOIN USER ui ON ui.id = l.user_id LEFT JOIN moneydetailrecord mrbq ON mrbq.type = 8 AND mrbq.safeLoanRecordId = r.salrid LEFT JOIN moneydetailrecord mrfu ON mrfu.type = 4 AND mrfu.safeLoanRecordId = r.salrid LEFT JOIN moneydetailrecord mrwtfh ON mrwtfh.type = 9 AND mrwtfh.safeLoanRecordId = r.salrid) UNION ALL (SELECT sul.investId, r.returnIncome, r.expectincome, mrbas.inMoney AS wtfhmoney, '' AS mrfumoney, '' AS mrbqmoney, r.safeLoanId AS safeLoanId, r.userid AS safeuserid, r.money AS money, sul.status AS loanstatus, '' AS loancommitTime, '' AS loanid, '' AS loanuser, '' AS investmoney, 0 AS loanrate, s.name AS NAME, s.deadline AS deadline, u.username AS username, s.rate AS rate, s.unit, s.status AS safestatus, r.salTime AS salTime, r.endTime AS endTime, mrbas.commitTime AS tuihuanri, '' AS loanusername FROM moneydetailrecord mrbas LEFT JOIN safeloan_user_loan sul ON sul.safeLoanRecordId = mrbas.safeLoanRecordId LEFT JOIN SafeLoanRecord r ON r.salrid = mrbas.safeLoanRecordId LEFT JOIN safeloan s ON s.id = mrbas.safeLoanId LEFT JOIN loan l ON l.id = sul.loanid LEFT JOIN USER u ON u.id = sul.userid WHERE mrbas.type = 9)";
		
		public SafeSumList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
			
				" name like #{safeSumList.example.name}",
				" safeLoanId like #{safeSumList.example.safeLoanId}",
				" deadline = #{safeSumList.example.deadline}",
				" rate = #{safeSumList.example.rate}",
				" username like #{safeSumList.example.investusername}",
				" money = #{safeSumList.example.money}",
				" salTime <= #{safeSumList.salMaxTime}",
				"salTime >= #{safeSumList.salMinTime}",
				" status = #{safeSumList.example.safestatus}",
				" endTime <= #{safeSumList.endMaxTime}",
				" endTime >= #{safeSumList.endMinTime}",
				" id like #{safeSumList.example.loanid}",
				" username like #{safeSumList.example.loanusername}",
				" loan_Money = #{safeSumList.example.investmoney}",
				" rate = #{safeSumList.example.loanrate}",
				" status in #{safeSumList.example.loanstatus}"};
		
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		addOrder("salTime", "desc");
	}

	public Date getSalMaxTime() {
		return salMaxTime;
	}

	public void setSalMaxTime(Date salMaxTime) {
		this.salMaxTime = salMaxTime;
	}

	public Date getSalMinTime() {
		return salMinTime;
	}

	public void setSalMinTime(Date salMinTime) {
		this.salMinTime = salMinTime;
	}

	public Date getEndMaxTime() {
		return endMaxTime;
	}

	public void setEndMaxTime(Date endMaxTime) {
		this.endMaxTime = endMaxTime;
	}

	public Date getEndMinTime() {
		return endMinTime;
	}

	public void setEndMinTime(Date endMinTime) {
		this.endMinTime = endMinTime;
	}

	public Date getLoanMaxrepayday() {
		return loanMaxrepayday;
	}

	public void setLoanMaxrepayday(Date loanMaxrepayday) {
		this.loanMaxrepayday = loanMaxrepayday;
	}

	public Date getLoanMinrepayday() {
		return loanMinrepayday;
	}

	public void setLoanMinrepayday(Date loanMinrepayday) {
		this.loanMinrepayday = loanMinrepayday;
	}
    
	
	public String statusestr(int index){
		return MoneyDetailRecordType.getName(index);
	}
	
}
