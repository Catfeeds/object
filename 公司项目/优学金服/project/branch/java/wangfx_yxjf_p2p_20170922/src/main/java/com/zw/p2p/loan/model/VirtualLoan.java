package com.zw.p2p.loan.model;

// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Loan entity. // TODO:虚拟标
 */
@Entity
@Table(name = "virtualloan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class VirtualLoan implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields

	private String id;
	// 标名称
	private String name;
	// 说明
	private String description;
	/** 实际借到的金额（放款金额），放款后才有值 */
	private Double money;
	/** 预计的借款金额 */
	private Double loanMoney;

	/**
	 * 最小投资金额
	 */
	private Double minInvestMoney;

	/**
	 * 最大投资金额
	 */
	private Double maxInvestMoney;

	// 利率0.12
	private Double rate;

	// 项目发起时间
	private Date commitTime;

	// 借款期限
	private Integer deadline;

	private Integer actType; // 活动类型 1体验标
	private Integer enableStatus; //启用停用 1启用2停用
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name = "money" , precision = 22, scale = 0)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}
	@Column(name = "loanMoney", precision = 22, scale = 0)
	public Double getLoanMoney() {
		return loanMoney;
	}

	public void setLoanMoney(Double loanMoney) {
		this.loanMoney = loanMoney;
	}
	@Column(name = "minInvestMoney", precision = 22, scale = 0)
	public Double getMinInvestMoney() {
		return minInvestMoney;
	}

	public void setMinInvestMoney(Double minInvestMoney) {
		this.minInvestMoney = minInvestMoney;
	}
	@Column(name = "maxInvestMoney", precision = 22, scale = 0)
	public Double getMaxInvestMoney() {
		return maxInvestMoney;
	}

	public void setMaxInvestMoney(Double maxInvestMoney) {
		this.maxInvestMoney = maxInvestMoney;
	}
	@Column(name = "rate", precision = 22, scale = 0)
	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}
	@Column(name = "commitTime",nullable = false, length = 19)
	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}
	@Column(name = "deadline")
	public Integer getDeadline() {
		return deadline;
	}

	public void setDeadline(Integer deadline) {
		this.deadline = deadline;
	}
	@Column(name = "actType")
	public Integer getActType() {
		return actType;
	}

	public void setActType(Integer actType) {
		this.actType = actType;
	}
	@Column(name = "enableStatus")
	public Integer getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(Integer enableStatus) {
		this.enableStatus = enableStatus;
	}

	}
