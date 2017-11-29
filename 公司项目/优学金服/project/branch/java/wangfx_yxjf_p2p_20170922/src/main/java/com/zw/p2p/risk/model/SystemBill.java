package com.zw.p2p.risk.model;
// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.privilege.model.Privilege;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.safeloan.model.SafeLoanRecord;

/**
 * 系统收益账户。
 */
@Entity
@Table(name = "system_bill")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class SystemBill implements java.io.Serializable {

	// Fields

	private String id;
	/**
	 * 时间
	 */
	private Date time;
	/**
	 * 收益类型（支出、收入）
	 */
	private String type;
	/**
	 * 操作类型
	 */
	private String reason;
	/**
	 * 收益费用
	 */
	private Double money;
	/**
	 * 描述
	 */
	private String detail;
	/**
	 * 系统收益额
	 */
	private Double balance;
	private Long seqNum;
	/**
	 * 优惠券
	 */
	private Coupons coupons;
	/**
	 * 提现记录
	 */
	private WithdrawCash withdrawCash;
	/**
	 * 债权投资记录
	 */
	private Invest invest;
	/**
	 * 无忧宝投资记录
	 */
	private SafeLoanRecord safeLoanRecord;
/*	*//**
	 * 特权本金
	 *//*
	private Privilege privilege;*/
	/**
	 * 用户
	 */
	private User user;
	/**
	 * 投资还款记录
	 */
	private InvestRepay investRepay;
	/**
	 * 借款还款记录
	 */
	private LoanRepay loanRepay;
	/**
	 * 资金来源
	 */
	private String moneyResource;
	/**
	 * 债权列表
	 */
	 private TransferApply transferApply;
	/**
	 * 充值记录
	 */
	 private Recharge recharge;

	// Constructors

	/**
	 * default constructor
	 */
	public SystemBill() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invest_id" , nullable = true)
	public Invest getInvest() {
		return invest;
	}

	public void setInvest(Invest invest) {
		this.invest = invest;
	}


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "withdraw_cash_id" , nullable = true)
	public WithdrawCash getWithdrawCash() {
		return withdrawCash;
	}

	public void setWithdrawCash(WithdrawCash withdrawCash) {
		this.withdrawCash = withdrawCash;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "loan_repay_id" ,nullable = true)
	public LoanRepay getLoanRepay() {
		return loanRepay;
	}

	public void setLoanRepay(LoanRepay loanRepay) {
		this.loanRepay = loanRepay;
	}

	@Column(name = "moneyResource", length = 40)
	public String getMoneyResource() {
		return moneyResource;
	}

	public void setMoneyResource(String moneyResource) {
		this.moneyResource = moneyResource;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transferApply_id" , nullable = true)
	public TransferApply getTransferApply() {
		return transferApply;
	}

	public void setTransferApply(TransferApply transferApply) {
		this.transferApply = transferApply;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recharge_id" , nullable = true)
	public Recharge getRecharge() {
		return recharge;
	}

	public void setRecharge(Recharge recharge) {
		this.recharge = recharge;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "investRepay_id" , nullable = true)
	public InvestRepay getInvestRepay() {
		return investRepay;
	}

	public void setInvestRepay(InvestRepay investRepay) {
		this.investRepay = investRepay;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id" , nullable = true)
	public Coupons getCoupons() {
		return coupons;
	}

	public void setCoupons(Coupons coupons) {
		this.coupons = coupons;
	}

	@Column(name = "seq_num", nullable = false)
	public Long getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(Long seqNum) {
		this.seqNum = seqNum;
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

	@Column(name = "time", nullable = false, length = 19)
	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Column(name = "type", nullable = false, length = 200)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "reason", nullable = false, length = 200)
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Column(name = "money", precision = 22, scale = 0)
	public Double getMoney() {
		return this.money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	@Column(name = "detail", length = 200)
	public String getDetail() {
		return this.detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	@Column(name = "balance", precision = 22, scale = 0)
	public Double getBalance() {
		return this.balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "salr_id" , nullable = true)
	public SafeLoanRecord getSafeLoanRecord() {
		return safeLoanRecord;
	}

	public void setSafeLoanRecord(SafeLoanRecord safeLoanRecord) {
		this.safeLoanRecord = safeLoanRecord;
	}
	/*@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pri_id" , nullable = true)
	public Privilege getPrivilege() {
		return privilege;
	}

	public void setPrivilege(Privilege privilege) {
		this.privilege = privilege;
	}*/
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id" , nullable = true)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}