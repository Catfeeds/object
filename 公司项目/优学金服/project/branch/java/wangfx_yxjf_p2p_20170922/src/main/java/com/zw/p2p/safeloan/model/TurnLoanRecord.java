package com.zw.p2p.safeloan.model;

// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Loan entity. // TODO:债权转让记录记录
 */
@Entity
@Table(name = "turnloanrecord")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class TurnLoanRecord implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields
	private String id;
	// 特殊用户id
	private String tuserid;
	// 债权id
	private String loanid;
	// 转让人id
	private String userid;
	/** 转让金额 */
	private Double turnmoney;
	/**
	 * 投资实体id
	 */
	private String investid;
	/**
	 * 0初始  1已转出
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date commitTime;

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "tuserid")
	public String getTuserid() {
		return tuserid;
	}

	public void setTuserid(String tuserid) {
		this.tuserid = tuserid;
	}

	@Column(name = "loanid")
	public String getLoanid() {
		return loanid;
	}

	public void setLoanid(String loanid) {
		this.loanid = loanid;
	}

	@Column(name = "userid")
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	@Column(name = "loanMoney", precision = 22, scale = 0)
	public Double getTurnmoney() {
		return turnmoney;
	}

	public void setTurnmoney(Double turnmoney) {
		this.turnmoney = turnmoney;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	@Column(name = "investid")
	public String getInvestid() {
		return investid;
	}

	public void setInvestid(String investid) {
		this.investid = investid;
	}
	@Column(name = "commitTime", nullable = false, length = 19)
	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}

}
