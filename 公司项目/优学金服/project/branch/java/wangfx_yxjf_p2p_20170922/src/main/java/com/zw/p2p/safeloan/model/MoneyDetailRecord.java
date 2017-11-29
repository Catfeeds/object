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
@Table(name = "moneydetailrecord")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class MoneyDetailRecord implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields
	private String id;
	// 债权id
	private String loanid;
	// 标name
	private String loanName;
	// 收入
	private double inMoney;
	// 支出
	private double outMoney;
	// 无忧宝投资记录id
	private String safeLoanRecordId;
	// 无忧宝id
	private String safeLoanId;
	/**
	 * 0初始 1已转出
	 */
	private Integer type;
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

	@Column(name = "loanid")
	public String getLoanid() {
		return loanid;
	}

	public void setLoanid(String loanid) {
		this.loanid = loanid;
	}

	@Column(name = "commitTime", nullable = false, length = 19)
	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}

	@Column(name = "loanName")
	public String getLoanName() {
		return loanName;
	}

	public void setLoanName(String loanName) {
		this.loanName = loanName;
	}

	@Column(name = "inMoney", precision = 22, scale = 0)
	public double getInMoney() {
		return inMoney;
	}

	public void setInMoney(double inMoney) {
		this.inMoney = inMoney;
	}

	@Column(name = "outMoney", precision = 22, scale = 0)
	public double getOutMoney() {
		return outMoney;
	}

	public void setOutMoney(double outMoney) {
		this.outMoney = outMoney;
	}

	@Column(name = "safeLoanRecordId")
	public String getSafeLoanRecordId() {
		return safeLoanRecordId;
	}

	public void setSafeLoanRecordId(String safeLoanRecordId) {
		this.safeLoanRecordId = safeLoanRecordId;
	}

	@Column(name = "safeLoanId")
	public String getSafeLoanId() {
		return safeLoanId;
	}

	public void setSafeLoanId(String safeLoanId) {
		this.safeLoanId = safeLoanId;
	}

	@Column(name = "type")
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
