package com.zw.p2p.coupons.model;

// default package

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 红包规则 Coupon entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "coupon_rule")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class CouponRule implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7079861900888037733L;

	/**
	 * 规则：使用节点+类型+金额+使用下限
	 */
	private String id;

	/**
	 * 红包金额
	 */
	private double money;
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

	private double getCouponCondition;
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

	@Column(name = "money", precision = 12, scale = 2)
	public Double getMoney() {
		return this.money;
	}

	public void setMoney(Double money) {
		this.money = money;
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

	public double getGetCouponCondition() {
		return getCouponCondition;
	}

	public void setGetCouponCondition(double getCouponCondition) {
		this.getCouponCondition = getCouponCondition;
	}

	public int getUseLine() {
		return useLine;
	}

	public void setUseLine(int useLine) {
		this.useLine = useLine;
	}

}