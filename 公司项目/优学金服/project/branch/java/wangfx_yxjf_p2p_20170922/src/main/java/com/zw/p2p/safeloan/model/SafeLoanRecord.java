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

import com.zw.archer.user.model.User;

/**
 * Loan entity. // TODO:无忧宝投标记录
 */
@Entity
@Table(name = "safeloanrecord")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class SafeLoanRecord implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields

	private String salrid;
	/** 投资金额 */
	private Double money;
	/** 剩余可用投资金额 */
	private Double enableMoney;
	//已返还的本金
	private Double returnMoney;
	//已返还的利息
	private Double returnIncome;
	//真实 利息总收益(每次利息和不包含本金)
	private Double income;
	//近期收益 上次返还之后累加
	private Double recentlyIcome;
	//预期收益
	private Double expectincome;
	// 发起时间
	private Date salTime;
	// 到期时间
	private Date endTime;
	//上次投资时间
	private Date beforeInvestTime;
	// 启用停用 1启用2停用
	private Integer enableStatus;
	// 投标人ID
	private User userid;
	// 无忧宝标id
	private SafeLoan safeloanid;
	/**
	 * 封闭期，结算期，已结清
	 */
	private Integer status;
	private Integer firstSms;//0初始 1待发送 2已发送
	private Integer lastSms;//0初始 1待发送 2已发送

	@Id
	@Column(name = "salrid", unique = true, nullable = false, length = 32)
	public String getSalrid() {
		return salrid;
	}

	public void setSalrid(String salrid) {
		this.salrid = salrid;
	}

	@Column(name = "money", precision = 22, scale = 0)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}
	@Column(name = "returnMoney", precision = 22, scale = 0)
	public Double getReturnMoney() {
		return returnMoney;
	}

	public void setReturnMoney(Double returnMoney) {
		this.returnMoney = returnMoney;
	}
	@Column(name = "returnIncome", precision = 22, scale = 0)
	public Double getReturnIncome() {
		return returnIncome;
	}

	public void setReturnIncome(Double returnIncome) {
		this.returnIncome = returnIncome;
	}

	@Column(name = "salTime", nullable = false, length = 19)
	public Date getSalTime() {
		return salTime;
	}

	public void setSalTime(Date salTime) {
		this.salTime = salTime;
	}

	@Column(name = "enableStatus")
	public Integer getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(Integer enableStatus) {
		this.enableStatus = enableStatus;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userid", nullable = false)
	public User getUserid() {
		return userid;
	}

	public void setUserid(User userid) {
		this.userid = userid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "safeloanid", nullable = false)
	public SafeLoan getSafeloanid() {
		return safeloanid;
	}

	public void setSafeloanid(SafeLoan safeloanid) {
		this.safeloanid = safeloanid;
	}

	@Column(name = "income", precision = 22, scale = 0)
	public Double getIncome() {
		return income;
	}

	public void setIncome(Double income) {
		this.income = income;
	}
	@Column(name = "recentlyIcome", precision = 22, scale = 0)
	public Double getRecentlyIcome() {
		return recentlyIcome;
	}

	public void setRecentlyIcome(Double recentlyIcome) {
		this.recentlyIcome = recentlyIcome;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "enableMoney", precision = 22, scale = 0)
	public Double getEnableMoney() {
		return enableMoney;
	}

	public void setEnableMoney(Double enableMoney) {
		this.enableMoney = enableMoney;
	}
	@Column(name = "expectincome", precision = 22, scale = 0)
	public Double getExpectincome() {
		return expectincome;
	}

	public void setExpectincome(Double expectincome) {
		this.expectincome = expectincome;
	}
	@Column(name = "endTime", nullable = false, length = 19)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	@Column(name = "beforeInvestTime", nullable = false, length = 19)
	public Date getBeforeInvestTime() {
		return beforeInvestTime;
	}

	public void setBeforeInvestTime(Date beforeInvestTime) {
		this.beforeInvestTime = beforeInvestTime;
	}
	@Column(name = "firstSms")
	public Integer getFirstSms() {
		return firstSms;
	}

	public void setFirstSms(Integer firstSms) {
		this.firstSms = firstSms;
	}
	@Column(name = "lastSms")
	public Integer getLastSms() {
		return lastSms;
	}

	public void setLastSms(Integer lastSms) {
		this.lastSms = lastSms;
	}

}
