package com.zw.p2p.invest.model;

// default package

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.zw.archer.user.model.User;
import com.zw.core.util.ArithUtil;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.RepayRoadmap;

/**
 * Investment entity. 投资实体
 * 
 * @author MyEclipse Persistence Tools
 * @author Administrator
 * 
 */
@Entity
@Table(name = "invest")
// @Inheritance(strategy = InheritanceType.JOINED)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Invest implements java.io.Serializable {

	// Fields

	private String id;
	private User user;
	private Loan loan;
	private Date time;
	private Boolean isAutoInvest;
	private Boolean isSafeLoanInvest;
	private String status;
	// 注意，此处存储的不是百分比利率
	private Double rate;
	// 利率百分比形式
	private Double ratePercent;
	// 投资类型(本金保障计划之类)
	private String type;
	private String ditch;//投资渠道 APP 微信 PC
	private String freezeOrdId;//冻结订单号
	private String ordDate;//订单时间
	private String freezeTrxId;//冻结标识
	private String ordId;//汇付订单号
	private String trxId;//汇付订单号
	/**
	 * 持有本金，依据此金额计算还款时应得金额。
	 */
	private Double money;

	/**
	 * 投资的金额
	 */
	private Double investMoney;

//	/**
//	 * 优惠券
//	 */
//	private UserCoupon userCoupon;
	/**
	 * 红包
	 */
	private Coupons coupons;
	/**
	 * 红包
	 */
	private RateTicket rateTicket;
	
	/**
	 * 加息券金额 (还款时  商户转账给用户 转多少就从这个字段里减去  达到限额后就根据状态加定时任务 第二天继续转)
	 */
	private double rateMoney;

	/**如果该投资是购买的债权，则此字段记录申请债权转让的信息。*/
	private TransferApply transferApply;

	private List<TransferApply> transferApplies = new ArrayList<TransferApply>(
			0);

	private List<InvestRepay> investRepays = new ArrayList<InvestRepay>(0);

	// Constructors

	/** default constructor */
	public Invest() {
	}

	// Property accessors
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "invest")
	@OrderBy(value = "period")
	public List<InvestRepay> getInvestRepays() {
		return investRepays;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "invest")
	public List<TransferApply> getTransferApplies() {
		return transferApplies;
	}

	public void setTransferApplies(List<TransferApply> transferApplies) {
		this.transferApplies = transferApplies;
	}

	public void setInvestRepays(List<InvestRepay> investRepays) {
		this.investRepays = investRepays;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "loan_id", nullable = false)
	public Loan getLoan() {
		return this.loan;
	}

	@Column(name = "money", nullable = false, precision = 22, scale = 0)
	public Double getMoney() {
		return this.money;
	}

	@Column(name = "invest_money", nullable = false, precision = 22, scale = 0)
	public Double getInvestMoney() {
		return investMoney;
	}

	public void setInvestMoney(Double investMoney) {
		this.investMoney = investMoney;
	}

	@Column(name = "rate", precision = 22, scale = 0)
	public Double getRate() {
		return this.rate;
	}

	@Transient
	public Double getRatePercent() {
		if (this.ratePercent == null && this.getRate() != null) {
			return this.getRate() * 100;
		}
		return ratePercent;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return this.status;
	}

	@Column(name = "time", nullable = false, length = 19)
	public Date getTime() {
		return this.time;
	}

	@Column(name = "type", length = 100)
	public String getType() {
		return type;
	}
	@Column(name = "ditch", length = 100)
	public String getDitch() {
		return ditch;
	}
	
	

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return this.user;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transfer_apply")
	public TransferApply getTransferApply() {
		return transferApply;
	}

	public void setTransferApply(TransferApply transferApply) {
		this.transferApply = transferApply;
	}

	@Column(name = "is_auto_invest", columnDefinition = "BOOLEAN")
	public Boolean getIsAutoInvest() {
		return isAutoInvest;
	}
	@Column(name = "isSafeLoanInvest", columnDefinition = "BOOLEAN")
	public Boolean getIsSafeLoanInvest() {
		return isSafeLoanInvest;
	}

	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_coupon")
	public Coupons getCoupons() {
		return coupons;
	}

	public void setCoupons(Coupons coupons) {
		this.coupons = coupons;
	}

//	@OneToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "user_coupon")
//	public UserCoupon getUserCoupon() {
//		return userCoupon;
//	}
//
//	public void setUserCoupon(UserCoupon userCoupon) {
//		this.userCoupon = userCoupon;
//	}

	public void setIsAutoInvest(Boolean isAutoInvest) {
		this.isAutoInvest = isAutoInvest;
	}
	public void setIsSafeLoanInvest(Boolean isSafeLoanInvest) {
		this.isSafeLoanInvest = isSafeLoanInvest;
	}
	public void setId(String id) {
		this.id = id;
	}

	public void setLoan(Loan loan) {
		this.loan = loan;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public void setRatePercent(Double ratePercent) {
		if (ratePercent != null) {
			this.rate = ArithUtil.div(ratePercent, 100, 4);
		}
		this.ratePercent = ratePercent;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setType(String type) {
		this.type = type;
	}
	public void setDitch(String ditch) {
		this.ditch = ditch;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	
	
	@Column(name = "ordDate", length = 100)
	public String getOrdDate() {
		return ordDate;
	}
	@Column(name = "freezeOrdId", length = 100)
	public String getFreezeOrdId() {
		return freezeOrdId;
	}

	public void setFreezeOrdId(String freezeOrdId) {
		this.freezeOrdId = freezeOrdId;
	}

	public void setOrdDate(String ordDate) {
		this.ordDate = ordDate;
	}
	
	@Column(name = "freezeTrxId", length = 18)
	public String getFreezeTrxId() {
		return freezeTrxId;
	}

	public void setFreezeTrxId(String freezeTrxId) {
		this.freezeTrxId = freezeTrxId;
	}

	@Column(name = "ordId", length = 23)
	public String getOrdId() {
		return ordId;
	}

	public void setOrdId(String ordId) {
		this.ordId = ordId;
	}

	@Column(name = "trxId", length = 18)
	public String getTrxId() {
		return trxId;
	}

	public void setTrxId(String trxId) {
		this.trxId = trxId;
	}


	/**
	 * 还款路标（待还、已还金额之类）
	 */
	private RepayRoadmap repayRoadmap;

	@Transient
	public RepayRoadmap getRepayRoadmap() {
		if (this.repayRoadmap == null) {
			// 未还本金
			Double unPaidCorpus = 0D;
			// 未还利息
			Double unPaidInterest = 0D;
			// 未还手续费
			Double unPaidFee = 0D;
			// 未还罚息
			Double unPaidDefaultInterest = 0D;
			// 已还本金
			Double paidCorpus = 0D;
			// 已还利息
			Double paidInterest = 0D;
			// 已还手续费
			Double paidFee = 0D;
			// 已还罚息
			Double paidDefaultInterest = 0D;
			// 下个还款日
			Date nextRepayDate = null;
			// 下个还款本金
			Double nextRepayCorpus = null;
			// 下个还款利息
			Double nextRepayInterest = null;
			// 下个还款手续费
			Double nextRepayFee = null;
			// 下个还款罚息
			Double nextRepayDefaultInterest = null;
			// 已还期数
			int paidPeriod = 0;
			for (int i = 0; i < getInvestRepays().size(); i++) {
				InvestRepay ir = getInvestRepays().get(i);
				if (ir.getStatus().equals(RepayStatus.BAD_DEBT)
						|| ir.getStatus().equals(RepayStatus.OVERDUE)
						|| ir.getStatus().equals(RepayStatus.REPAYING)
						|| ir.getStatus().equals(RepayStatus.INTERESTING)
						) {
					unPaidCorpus = ArithUtil.add(unPaidCorpus, ir.getCorpus());
					unPaidInterest = ArithUtil.add(unPaidInterest,
							ir.getInterest());
					unPaidFee = ArithUtil.add(unPaidFee, ir.getFee());
					unPaidDefaultInterest = ArithUtil.add(
							unPaidDefaultInterest, ir.getDefaultInterest());
					if (i == 0
							|| getInvestRepays().get(i - 1).getStatus()
									.equals(RepayStatus.COMPLETE)) {
						// 下一期待还款
						nextRepayDate = ir.getRepayDay();
						nextRepayCorpus = ir.getCorpus();
						nextRepayInterest = ir.getInterest();
						nextRepayFee = ir.getFee();
						nextRepayDefaultInterest = ir.getDefaultInterest();
					}
				} else if (ir.getStatus().equals(RepayStatus.COMPLETE)) {
					paidCorpus = ArithUtil.add(paidCorpus, ir.getCorpus());
					paidInterest = ArithUtil
							.add(paidInterest, ir.getInterest());
					paidFee = ArithUtil.add(paidFee, ir.getFee());
					paidDefaultInterest = ArithUtil.add(paidDefaultInterest,
							ir.getDefaultInterest());
					paidPeriod++;
				}
			}

			this.repayRoadmap = new RepayRoadmap(unPaidCorpus, unPaidInterest,
					unPaidFee, unPaidDefaultInterest, paidCorpus, paidInterest,
					paidFee, paidDefaultInterest, nextRepayDate,
					nextRepayCorpus, nextRepayInterest, nextRepayFee,
					nextRepayDefaultInterest, paidPeriod, getInvestRepays()
							.size() - paidPeriod, false);
		}
		return this.repayRoadmap;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_rateTicket")
	public RateTicket getRateTicket() {
		return rateTicket;
	}

	public void setRateTicket(RateTicket rateTicket) {
		this.rateTicket = rateTicket;
	}

	public double getRateMoney() {
		return rateMoney;
	}

	public void setRateMoney(double rateMoney) {
		this.rateMoney = rateMoney;
	}

	

}