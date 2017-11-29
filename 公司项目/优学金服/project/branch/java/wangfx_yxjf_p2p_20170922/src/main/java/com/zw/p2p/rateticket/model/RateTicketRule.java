package com.zw.p2p.rateticket.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "rateticket_rule")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class RateTicketRule implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7079861900888037733L;

	/**
	 * 规则：使用节点+类型+金额+使用下限
	 */
	private String id;

	/**
	 * 加息券利率
	 */
	private double rate;
	/**
	 * 投资使用门槛金额
	 */
	private double moneyCanUse;
	/**
	 * 获取来源类型 注册，投资达标，邀请，管理员操作
	 */
	private String resource;
	/**
	 * 获得红包的金额条件金额
	 */

	private double getRateTicketCondition;
	/**
	 * 有效期（天）
	 */
	private int useLine;
	// Property accessors
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}


	@Column(name = "resource", nullable = false)
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public double getMoneyCanUse() {
		return moneyCanUse;
	}

	public void setMoneyCanUse(double moneyCanUse) {
		this.moneyCanUse = moneyCanUse;
	}

	public int getUseLine() {
		return useLine;
	}

	public void setUseLine(int useLine) {
		this.useLine = useLine;
	}

	@Column(name = "rate", precision = 12, scale = 2)
	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getGetRateTicketCondition() {
		return getRateTicketCondition;
	}

	public void setGetRateTicketCondition(double getRateTicketCondition) {
		this.getRateTicketCondition = getRateTicketCondition;
	}

}
