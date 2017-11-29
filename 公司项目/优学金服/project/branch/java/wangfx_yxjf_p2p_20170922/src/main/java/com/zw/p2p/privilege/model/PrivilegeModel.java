package com.zw.p2p.privilege.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 特权本金管理模型
 *
 */
public class PrivilegeModel implements Serializable{

	private static final long serialVersionUID = 8833629605460866433L;
	private String user_id;          /* 用户id，表user主键 */
	private String user_name;        /* 用户名称 */
	private Integer invite_number;   /* 成功邀请人数 */
	private Double totle_fee;        /* 有效特权本金总额 */
	private Double totle_income;     /* 累积特权收益 */
	private Double yesterday_income; /* 昨日特权收益 */
	private String status;           /* 状态 */
	private Date last_job_time;      /* 上次计算时间 */
	private Date create_time;        /* 创建时间 */
	private String mobile_number;    /* 用户手机号 */
	
	public PrivilegeModel(){}
	
	public PrivilegeModel(String user_id, String user_name, String mobile_number,Double totle_fee,Integer invite_number,Double totle_income,Date create_time){
		this.user_id=user_id;
		this.user_name=user_name;
		this.mobile_number=mobile_number;
		this.totle_fee=totle_fee;
		this.invite_number=invite_number;
		this.totle_income=totle_income;
		this.create_time=create_time;
	}
	
	public String getUserId() {
		return this.user_id;
	}
	public void setUserId(String userId) {
		this.user_id = userId;
	}
	
	public String getUserName() {
		return this.user_name;
	}
	public void setUserName(String userName) {
		this.user_name = userName;
	}
	
	public Integer getInviteNumber() {
		return this.invite_number;
	}
	public void setInviteNumber(Integer inviteNumber) {
		this.invite_number = inviteNumber;
	}
	
	public Double getTotleFee() {
		if(this.totle_fee == null){
			this.totle_fee = 0.00D;
		}
		return totle_fee;
	}
	public void setTotleFee(Double totleFee) {
		this.totle_fee = totleFee;
	}
	
	public Double getTotleIncome() {
		if(this.totle_income == null){
			this.totle_income = 0.00D;
		}
		return totle_income;
	}
	public void setTotleIncome(Double totleIncome) {
		this.totle_income = totleIncome;
	}
	
	public Double getYesterdayIncome() {
		if(this.yesterday_income == null){
			this.yesterday_income = 0.00D;
		}
		return yesterday_income;
	}
	public void setYesterdayIncome(Double yesterdayIncome) {
		this.yesterday_income = yesterdayIncome;
	}
	
	public String getStatus() {
		return this.status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getLastJobTime() {
		return this.last_job_time;
	}
	public void setLastJobTime(Date lastJobTime) {
		this.last_job_time = lastJobTime;
	}
	
	public Date getCreateTime() {
		return this.create_time;
	}
	public void setCreateTime(Date createTime) {
		this.create_time = createTime;
	}
	
	public String getMobileNumber() {
		return this.mobile_number;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobile_number = mobileNumber;
	}
}
