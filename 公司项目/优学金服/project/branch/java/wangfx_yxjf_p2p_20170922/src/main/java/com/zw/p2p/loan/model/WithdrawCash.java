package com.zw.p2p.loan.model;

// default package

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.zw.archer.user.model.User;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.fee.model.FeeSchemePay;

/**
 * WithdrawCash entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "withdraw_cash")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class WithdrawCash implements java.io.Serializable {

	// Fields

	private String id;
	private User user;
	private Date time;
	private BankCard bankCard;
	
	/**
	 * 汇付天下提现订单号
	 */
	private String huiFuOrderId;
	/**
	 * 提现金额 提现金额-手续费=实际到账金额
	 */
	private Double money;
	/**
	 * 申请金额
	 */
	private Double applyMoney;
	/**
	 * 手续费
	 */
	private Double fee;
	
	/**
	 * 汇付收取手续费
	 */
	private Double huiFuFee;
	/**
	 * 是投资账户还是借款账户提现
	 */
	private String account;
	/**
	 * 提现罚金
	 */
	private Double cashFine;

	private String status;

	/**
	 * 审核人
	 */
	private User verifyUser;
	/**
	 * 审核信息
	 */
	private String verifyMessage;

	private Date verifyTime;

	private User recheckUser;

	private String recheckMessage;

	private Date recheckTime;

	/** 是否为管理员提现 */
	private Boolean isWithdrawByAdmin;

	/** 提现时选择的支付公司,by lijin,2015-03-10 **/
	private FeeSchemePay pay;

	/**	是否被选中*/
	private Boolean selected;

	/** default constructor */
	public WithdrawCash() {
	}

	@Transient
	public Boolean getSelected() {
		return selected;
	}

	@Transient
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pay",referencedColumnName="id", nullable = false)
	public FeeSchemePay getPay() {
//		if (pay==null)
//			pay=new FeeSchemePay();
//
		return this.pay;
	}
	// Constructors

	public void setPay(FeeSchemePay pay) {
		this.pay = pay;
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "time", nullable = false, length = 19)
	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bank_card_id")
	public BankCard getBankCard() {
		return this.bankCard;
	}

	public void setBankCard(BankCard bankCard) {
		this.bankCard = bankCard;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "verify_user_id")
	public User getVerifyUser() {
		return verifyUser;
	}

	public void setVerifyUser(User verifyUser) {
		this.verifyUser = verifyUser;
	}

	@Column(name = "verify_message", length = 500)
	public String getVerifyMessage() {
		return verifyMessage;
	}

	public void setVerifyMessage(String verifyMessage) {
		this.verifyMessage = verifyMessage;
	}

	@Column(name = "fee", nullable = false, precision = 22, scale = 0)
	public Double getFee() {
		return this.fee;
	}

	public void setFee(Double fee) {
		this.fee = fee;
	}

	public Double getHuiFuFee() {
		return huiFuFee;
	}
	@Column(name = "huifufee", nullable = false, precision = 22, scale = 0)
	public void setHuiFuFee(Double huiFuFee) {
		this.huiFuFee = huiFuFee;
	}

	@Column(name = "money", nullable = false, precision = 22, scale = 0)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}
	@Column(name = "applymoney", nullable = false, precision = 22, scale = 0)
	public Double getApplyMoney() {
		return applyMoney;
	}

	public void setApplyMoney(Double applyMoney) {
		this.applyMoney = applyMoney;
	}

	@Column(name = "account", length = 32)
	public String getAccount() {
		return this.account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Column(name = "cash_fine", nullable = false, precision = 22, scale = 0)
	public Double getCashFine() {
		return this.cashFine;
	}

	public void setCashFine(Double cashFine) {
		this.cashFine = cashFine;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recheck_user_id")
	public User getRecheckUser() {
		return recheckUser;
	}

	public void setRecheckUser(User recheckUser) {
		this.recheckUser = recheckUser;
	}

	@Column(name = "recheck_message", length = 500)
	public String getRecheckMessage() {
		return recheckMessage;
	}

	public void setRecheckMessage(String recheckMessage) {
		this.recheckMessage = recheckMessage;
	}

	@Column(name = "verify_time")
	public Date getVerifyTime() {
		return verifyTime;
	}

	public void setVerifyTime(Date verifyTime) {
		this.verifyTime = verifyTime;
	}

	@Column(name = "recheck_time")
	public Date getRecheckTime() {
		return recheckTime;
	}

	public void setRecheckTime(Date recheckTime) {
		this.recheckTime = recheckTime;
	}

	@Column(name = "is_withdraw_by_admin", columnDefinition = "BOOLEAN")
	public Boolean getIsWithdrawByAdmin() {
		return isWithdrawByAdmin;
	}

	public void setIsWithdrawByAdmin(Boolean isWithdrawByAdmin) {
		this.isWithdrawByAdmin = isWithdrawByAdmin;
	}

	public String getHuiFuOrderId() {
		return huiFuOrderId;
	}

	@Column(name = "HUIFU_ORDERID")
	public void setHuiFuOrderId(String huiFuOrderId) {
		this.huiFuOrderId = huiFuOrderId;
	}
	
	

}