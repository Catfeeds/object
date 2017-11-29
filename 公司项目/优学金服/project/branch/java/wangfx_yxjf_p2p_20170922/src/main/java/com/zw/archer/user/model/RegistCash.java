package com.zw.archer.user.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * registfreecash 注册赠送试用金
 */
@Entity
@Table(name = "registfreecash")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class RegistCash {
	private int pk_rid;
	private Double cash;
	private Date starttime;
	private Date endtime;
	private int isused; // 1 没用 2 用了
	private String pk_user;

	public RegistCash() {
	}

	public RegistCash(int pk_rid) {
		super();
		this.pk_rid = pk_rid;
	}

	@Id
	public int getPk_rid() {
		return pk_rid;
	}

	public void setPk_rid(int pk_rid) {
		this.pk_rid = pk_rid;
	}

	

	public Double getCash() {
		return cash;
	}

	public void setCash(Double cash) {
		this.cash = cash;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public int getIsused() {
		return isused;
	}

	public void setIsused(int isused) {
		this.isused = isused;
	}

	public String getPk_user() {
		return pk_user;
	}

	public void setPk_user(String pk_user) {
		this.pk_user = pk_user;
	}

}
