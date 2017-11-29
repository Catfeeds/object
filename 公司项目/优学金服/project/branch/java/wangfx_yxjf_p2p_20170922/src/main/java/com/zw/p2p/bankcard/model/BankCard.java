package com.zw.p2p.bankcard.model;

// default package

import java.sql.Timestamp;
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
 * BankCard entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "bank_card")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class BankCard implements java.io.Serializable {

	// Fields

	private String id;
	private User user;
	// 分行
	private String name;
	//支行
	private String branch;
	// 银行名称
	private String bank;
	/** 银行账户服务类型，对公、对私 */
	private String bankServiceType;
	// 银行编号
	private String bankNo;
	// 银行所在省份
	private String bankProvince;
	// 银行所在城市
	private String bankCity;
	// 账户类型
	private String bankCardType;
	// 开户行地址
	private String bankArea;
	// 银行卡账户名称
	private String accountName;
	// 银行卡号
	private String cardNo;
	// 绑定金额
	private Double bindingprice;
	private Date time;
	private String status;
	private String payType; //fast 汇付快捷支付 qx 取现卡
	/** default constructor */
	public BankCard() {
	}
	/** full constructor */
	public BankCard(String id, User user, String name, String bank,
			String bankArea, String cardNo, Date time, String status) {
		this.id = id;
		this.user = user;
		this.name = name;
		this.bank = bank;
		this.bankArea = bankArea;
		this.cardNo = cardNo;
		this.time = time;
		this.status = status;
	}

	// Constructors

	@Column(name = "branch", length = 100)
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Column(name = "bank", length = 100)
	public String getBank() {
		return this.bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	@Column(name = "bank_area", length = 512)
	public String getBankArea() {
		return this.bankArea;
	}

	public void setBankArea(String bankArea) {
		this.bankArea = bankArea;
	}

	@Column(name = "city", length = 100)
	public String getBankCity() {
		return bankCity;
	}

	public void setBankCity(String bankCity) {
		this.bankCity = bankCity;
	}

	@Column(name = "bank_no", length = 128)
	public String getBankNo() {
		return bankNo;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	@Column(name = "province", length = 100)
	public String getBankProvince() {
		return bankProvince;
	}

	public void setBankProvince(String bankProvince) {
		this.bankProvince = bankProvince;
	}

	@Column(name = "bank_card_type", length = 100)
	public String getBankCardType() {
		return bankCardType;
	}

	public void setBankCardType(String bankCardType) {
		this.bankCardType = bankCardType;
	}

	@Column(name = "account_name", length = 200)
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@Column(name = "binding_price")
	public Double getBindingprice() {
		return bindingprice;
	}

	public void setBindingprice(Double bindingprice) {
		this.bindingprice = bindingprice;
	}

	@Column(name = "card_no", length = 100)
	public String getCardNo() {
		return this.cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	// Property accessors
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "name", length = 100)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "time", nullable = false, length = 19)
	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "bank_service_type", length = 100)
	public String getBankServiceType() {
		return bankServiceType;
	}

	public void setBankServiceType(String bankServiceType) {
		this.bankServiceType = bankServiceType;
	}
	@Column(name = "paytype", length = 100)
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}

}