package com.zw.archer.user.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author Hch
 * 推荐人模型
 *
 */
public class RefereeModel implements Serializable{

	private static final long serialVersionUID = 8833629605460866433L;
	private String referee;//推荐人名称
	private Date startTime,endTime;//日期
	private Double sumMoney;//总金额
	
	private String refereePhone;//推荐人手机号
	private String referrals;//被推荐人名称
	private String referralsPhone;//被推荐人手机号
	private String status;//标的状态
	

	public RefereeModel(){
	}
	
	public RefereeModel(String referee,Double sumMoney,Date startTime,Date endTime,String refereePhone,String referrals,String referralsPhone,String status){
		this.referee=referee;
		this.sumMoney=sumMoney;
		this.startTime=startTime;
		this.endTime=endTime;
		this.refereePhone=refereePhone;
		this.referrals=referrals;
		this.referralsPhone=referralsPhone;
		this.status=status;
	}
	
	public String getReferee() {
		return referee;
	}
	public void setReferee(String referee) {
		this.referee = referee;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Double getSumMoney() {
		return sumMoney;
	}
	public void setSumMoney(Double sumMoney) {
		this.sumMoney = sumMoney;
	}
	public String getRefereePhone() {
		return refereePhone;
	}
	public void setRefereePhone(String refereePhone) {
		this.refereePhone = refereePhone;
	}
	public String getReferrals() {
		return referrals;
	}
	public void setReferrals(String referrals) {
		this.referrals = referrals;
	}
	public String getReferralsPhone() {
		return referralsPhone;
	}
	public void setReferralsPhone(String referralsPhone) {
		this.referralsPhone = referralsPhone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
