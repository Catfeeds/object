package com.zw.archer.invite.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 邀请关系
 * @author majie
 * @date 2016-1-14 15:48:17
 */
@Entity
@Table(name="invite")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class invite implements java.io.Serializable, Cloneable{

	private int id;
	private String from_phone;
	private String from_openId;
	private String to_phone;
	private String to_openId;
	private String succ_flag;
	private Date create_time;
	
	private String fromuserid;
	private String touserid;
	private int tyjstatus;
	private int realnamestatus;
	private int loantyjstatus;
	private int bandcardstatus;
	private int tyjrewmoney;
	private int realnamerewmoney;
	private int loantyjrewmoney;
	private int bandcardrewmoney;
	private int tyjlqstatus;
	private int realnamelqstatus;
	private int loantyjlqstatus;
	private int bandcardlqstatus;
	
	@Id
	@Column(name = "id", unique = true)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFrom_phone() {
		return from_phone;
	}
	public void setFrom_phone(String from_phone) {
		this.from_phone = from_phone;
	}
	public String getFrom_openId() {
		return from_openId;
	}
	public void setFrom_openId(String from_openId) {
		this.from_openId = from_openId;
	}
	public String getTo_phone() {
		return to_phone;
	}
	public void setTo_phone(String to_phone) {
		this.to_phone = to_phone;
	}
	public String getTo_openId() {
		return to_openId;
	}
	public void setTo_openId(String to_openId) {
		this.to_openId = to_openId;
	}
	public String getSucc_flag() {
		return succ_flag;
	}
	public void setSucc_flag(String succ_flag) {
		this.succ_flag = succ_flag;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	public int getTyjstatus() {
		return tyjstatus;
	}
	public void setTyjstatus(int tyjstatus) {
		this.tyjstatus = tyjstatus;
	}
	public int getRealnamestatus() {
		return realnamestatus;
	}
	public void setRealnamestatus(int realnamestatus) {
		this.realnamestatus = realnamestatus;
	}
	public int getLoantyjstatus() {
		return loantyjstatus;
	}
	public void setLoantyjstatus(int loantyjstatus) {
		this.loantyjstatus = loantyjstatus;
	}
	public int getBandcardstatus() {
		return bandcardstatus;
	}
	public void setBandcardstatus(int bandcardstatus) {
		this.bandcardstatus = bandcardstatus;
	}
	public int getTyjrewmoney() {
		return tyjrewmoney;
	}
	public void setTyjrewmoney(int tyjrewmoney) {
		this.tyjrewmoney = tyjrewmoney;
	}
	public int getRealnamerewmoney() {
		return realnamerewmoney;
	}
	public void setRealnamerewmoney(int realnamerewmoney) {
		this.realnamerewmoney = realnamerewmoney;
	}
	public int getLoantyjrewmoney() {
		return loantyjrewmoney;
	}
	public void setLoantyjrewmoney(int loantyjrewmoney) {
		this.loantyjrewmoney = loantyjrewmoney;
	}
	public int getBandcardrewmoney() {
		return bandcardrewmoney;
	}
	public void setBandcardrewmoney(int bandcardrewmoney) {
		this.bandcardrewmoney = bandcardrewmoney;
	}
	public int getTyjlqstatus() {
		return tyjlqstatus;
	}
	public void setTyjlqstatus(int tyjlqstatus) {
		this.tyjlqstatus = tyjlqstatus;
	}
	public int getRealnamelqstatus() {
		return realnamelqstatus;
	}
	public void setRealnamelqstatus(int realnamelqstatus) {
		this.realnamelqstatus = realnamelqstatus;
	}
	public int getLoantyjlqstatus() {
		return loantyjlqstatus;
	}
	public void setLoantyjlqstatus(int loantyjlqstatus) {
		this.loantyjlqstatus = loantyjlqstatus;
	}
	public int getBandcardlqstatus() {
		return bandcardlqstatus;
	}
	public void setBandcardlqstatus(int bandcardlqstatus) {
		this.bandcardlqstatus = bandcardlqstatus;
	}
	public String getFromuserid() {
		return fromuserid;
	}
	public void setFromuserid(String fromuserid) {
		this.fromuserid = fromuserid;
	}
	public String getTouserid() {
		return touserid;
	}
	public void setTouserid(String touserid) {
		this.touserid = touserid;
	}
	
}
