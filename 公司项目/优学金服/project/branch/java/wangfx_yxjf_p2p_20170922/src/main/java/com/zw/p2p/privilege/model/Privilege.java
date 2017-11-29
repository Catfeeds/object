package com.zw.p2p.privilege.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 特权本金
 * 
 * @author zhenghaifeng
 * @date 2015-12-14 上午11:31:18
 */
@Entity
@Table(name = "privilege")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Privilege implements java.io.Serializable {
	private static final long serialVersionUID = 8833629605460866433L;

	private Integer id;              /* 主键，自增长 */
	private String user_id;          /* 用户id，表user主键 */
	private Integer invite_number;   /* 成功邀请人数 */
	private Double totle_fee;        /* 有效特权本金总额 */
	private Double totle_income;     /* 累积特权收益 */
	private Double yesterday_income; /* 昨日特权收益 */
	private String status;           /* 状态 */
	private Date last_job_time;      /* 上次计算时间 */
	private Date create_time;        /* 创建时间 */

	public Privilege(){
		
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "user_id", length = 32)
	public String getUserId() {
		return this.user_id;
	}
	public void setUserId(String userId) {
		this.user_id = userId;
	}
	
	@Column(name = "invite_number")
	public Integer getInviteNumber() {
		return this.invite_number;
	}
	public void setInviteNumber(Integer inviteNumber) {
		this.invite_number = inviteNumber;
	}
	
	@Column(name = "totle_fee")
	public Double getTotleFee() {
		if(this.totle_fee == null){
			this.totle_fee = 0.00D;
		}
		return totle_fee;
	}
	public void setTotleFee(Double totleFee) {
		this.totle_fee = totleFee;
	}
	
	@Column(name = "totle_income")
	public Double getTotleIncome() {
		if(this.totle_income == null){
			this.totle_income = 0.00D;
		}
		return totle_income;
	}
	public void setTotleIncome(Double totleIncome) {
		this.totle_income = totleIncome;
	}
	
	@Column(name = "yesterday_income")
	public Double getYesterdayIncome() {
		if(this.yesterday_income == null){
			this.yesterday_income = 0.00D;
		}
		return yesterday_income;
	}
	public void setYesterdayIncome(Double yesterdayIncome) {
		this.yesterday_income = yesterdayIncome;
	}
	
	@Column(name = "status")
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Column(name = "last_job_time")
	public Date getLastJobTime() {
		return this.last_job_time;
	}
	public void setLastJobTime(Date lastJobTime) {
		this.last_job_time = lastJobTime;
	}
	
	@Column(name = "create_time")
	public Date getCreateTime() {
		return this.create_time;
	}
	public void setCreateTime(Date createTime) {
		this.create_time = createTime;
	}
}