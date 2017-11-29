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
 * Loan entity. // TODO:虚拟标投标记录
 */
@Entity
@Table(name = "virtualloanrecord")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class VirtualLoanRecord implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields

	private String vlrid;
	/** 投入的虚拟金额 */
	private Double money;
	// 发起时间
	private Date vlrTime;
	//收息时间
	private Date endTime;
	//利息金额保留两位小数
	private Double income;
	//利率
	private Double ratebak;
	// 启用停用 1启用2停用
	private Integer enableStatus; 
	//投标人ID
	private String userid;
	//虚拟标id
	private String virtualloanid;
	//收益时间
	private Date getEndPayTime;
		
	@Id
	@Column(name = "vlrid", unique = true, nullable = false, length = 32)
	public String getVlrid() {
		return vlrid;
	}

	public void setVlrid(String vlrid) {
		this.vlrid = vlrid;
	}

	@Column(name = "money", precision = 22, scale = 0)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	@Column(name = "vlrTime", nullable = false, length = 19)
	public Date getVlrTime() {
		return vlrTime;
	}

	public void setVlrTime(Date vlrTime) {
		this.vlrTime = vlrTime;
	}

	@Column(name = "enableStatus")
	public Integer getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(Integer enableStatus) {
		this.enableStatus = enableStatus;
	}

	@Column(name = "userid")
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	@Column(name = "virtualloanid")
	public String getVirtualloanid() {
		return virtualloanid;
	}

	public void setVirtualloanid(String virtualloanid) {
		this.virtualloanid = virtualloanid;
	}
	@Column(name = "endTime", nullable = false, length = 19)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	@Column(name = "income", precision = 22, scale = 0)
	public Double getIncome() {
		return income;
	}

	public void setIncome(Double income) {
		this.income = income;
	}
	@Column(name = "ratebak", precision = 22, scale = 0)
	public Double getRatebak() {
		return ratebak;
	}

	public void setRatebak(Double ratebak) {
		this.ratebak = ratebak;
	}

	public Date getGetEndPayTime() {
		return getEndPayTime;
	}

	public void setGetEndPayTime(Date getEndPayTime) {
		this.getEndPayTime = getEndPayTime;
	}
	
}
