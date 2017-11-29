package com.zw.p2p.coupons.model;

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
 * 红包记录 Coupon entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "coupons")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Coupons implements java.io.Serializable {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = -28725040105640778L;

	/**
	 * 规则：使用节点+类型+金额+使用下限
	 */
	private String id;

	/**
	 * 使用状态 
	 */
	private String status;
	/**
	 * 用户
	 */
	private User user;
	/**
	 * 面值金额
	 */
	private double money;
	/**
	 * 获得日期
	 */
	private  Date createTime;
	/**
	 * 有效期日期
	 */
	private  Date endTime;
	/**
	 * 使用日期
	 */
	private Date usedTime;
	/**
	 * 投资使用门槛金额
	 */
	private double moneyCanUse;
	/**
	 * 获取来源类型 注册，投资达标，邀请，管理员操作 
	 */
	private String resource;
	


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
	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "used_time", nullable = true)
	public Date getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(Date usedTime) {
		this.usedTime = usedTime;
	}


	@Column(name = "resource", nullable = false)
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public double getMoneyCanUse() {
		return moneyCanUse;
	}

	public void setMoneyCanUse(double moneyCanUse) {
		this.moneyCanUse = moneyCanUse;
	}
	
}