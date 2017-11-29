package com.zw.p2p.safeloan.model;

// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.zw.p2p.loan.model.Loan;

/**
 * Loan entity. // TODO:无忧宝产品投标记录
 */
@Entity
@Table(name = "safeloan_user_loan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class SafeLoan_User_Loan implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields
	private String id;
	// 无忧宝id
	private String safeLoanId;
	//无忧宝投资记录id 一人可能有多条记录
	private String safeLoanRecordId;
	// 用户id
	private String userid;
	/** 债权id */
	private Loan loanid;
	/** 债权投资记录id */
	private String investId;
	/** 投资金额 */
	private Double loanMoney;
	/**
	 * 投资时间
	 */
	private Date commitTime;
	/**
	 * 0投资中 1流标 2已收益3已转让
	 */
	private Integer status;
	//债权转让记录id
	private String turnId;
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "safeLoanId")
	public String getSafeLoanId() {
		return safeLoanId;
	}

	public void setSafeLoanId(String safeLoanId) {
		this.safeLoanId = safeLoanId;
	}

	@Column(name = "userid")
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "loanid", nullable = false)
	public Loan getLoanid() {
		return loanid;
	}

	public void setLoanid(Loan loanid) {
		this.loanid = loanid;
	}

	@Column(name = "investId")
	public String getInvestId() {
		return investId;
	}

	public void setInvestId(String investId) {
		this.investId = investId;
	}

	@Column(name = "loanMoney", precision = 22, scale = 0)
	public Double getLoanMoney() {
		return loanMoney;
	}

	public void setLoanMoney(Double loanMoney) {
		this.loanMoney = loanMoney;
	}

	@Column(name = "commitTime", length = 19)
	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	@Column(name = "safeLoanRecordId")
	public String getSafeLoanRecordId() {
		return safeLoanRecordId;
	}

	public void setSafeLoanRecordId(String safeLoanRecordId) {
		this.safeLoanRecordId = safeLoanRecordId;
	}
	@Column(name = "turnId")
	public String getTurnId() {
		return turnId;
	}

	public void setTurnId(String turnId) {
		this.turnId = turnId;
	}

}
