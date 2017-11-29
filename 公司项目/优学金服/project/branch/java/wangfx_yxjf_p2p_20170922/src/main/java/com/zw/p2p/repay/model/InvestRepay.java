package com.zw.p2p.repay.model;

// default package

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.zw.p2p.invest.model.Invest;

/**
 * 投资的还款信息
 * 
 * @author wangzhi
 */
@Entity
@Table(name = "invest_repay")
public class InvestRepay implements java.io.Serializable {

	// Fields

	private String id;
	private Invest invest;

	/**
	 * 当前还款为第几期
	 */
	private Integer period;
	/**
	 * 还款时间
	 */
	private Date time;
	/**
	 * 还款日
	 */
	private Date repayDay;
	/**
	 * 本金
	 */
	private Double corpus;
	/**
	 * 利息
	 */
	private Double interest;

	/**
	 * 罚息（逾期利息）
	 */
	private Double defaultInterest;

	/**
	 * 手续费（从利息中扣除，给系统）
	 */
	private Double fee;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 本期长度
	 */
	private Integer length;

	/**
	 * 还款方式
	 */
	private String repayWay;
	/**
	 * 是否已经发送了还款提醒短信
	 */
	private Integer smsSended;
	/**
	 * loan_repay_id
	 */
	private String loanRepayId;
	
	/**
	 * 订单ID
	 * */
	private String orderId;
	
	/**
	 * 订单生成时间
	 * */
	private String orderDate;

	/** default constructor */
	public InvestRepay() {
	}

	@Column(name = "loan_repay_id")
	public String getLoanRepayId() {
		return loanRepayId;
	}

	// Constructors

	public void setLoanRepayId(String loanRepayId) {
		this.loanRepayId = loanRepayId;
	}

	@Column(name = "smsSended")
	public Integer getSmsSended() {
		return smsSended;
	}

	public void setSmsSended(Integer smsSended) {
		this.smsSended = smsSended;
	}

	/**
	 * 本金
	 */
	@Column(name = "corpus", nullable = false, precision = 22, scale = 0)
	public Double getCorpus() {
		return this.corpus;
	}

	/**
	 * 本金
	 *
	 * @param corpus
	 */
	public void setCorpus(Double corpus) {
		this.corpus = corpus;
	}

	/**
	 * 罚息
	 */
	@Column(name = "default_interest", precision = 22, scale = 0)
	public Double getDefaultInterest() {
		if (this.defaultInterest == null) {
			return 0D;
		}
		return this.defaultInterest;
	}

	/**
	 * 罚息
	 */
	public void setDefaultInterest(Double defaultInterest) {
		this.defaultInterest = defaultInterest;
	}

	@Column(name = "fee")
	public Double getFee() {
		return fee;
	}

	public void setFee(Double fee) {
		this.fee = fee;
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

	/**
	 * 利息
	 */
	@Column(name = "interest", nullable = false, precision = 22, scale = 0)
	public Double getInterest() {
		return this.interest;
	}

	/**
	 * 利息
	 *
	 * @param data
	 *            .corpus
	 */
	public void setInterest(Double interest) {
		this.interest = interest;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invest_id", nullable = false)
	public Invest getInvest() {
		return this.invest;
	}

	public void setInvest(Invest invest) {
		this.invest = invest;
	}

	@Column(name = "length")
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	@Column(name = "period", nullable = false)
	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	/**
	 * 还款日
	 *
	 * @return
	 */
	@Column(name = "repay_day", nullable = false)
	public Date getRepayDay() {
		return repayDay;
	}

	/**
	 * 还款日
	 */
	public void setRepayDay(Date repayDay) {
		this.repayDay = repayDay;
	}

	@Column(name = "status", nullable = false, length = 50)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 还款时间
	 */
	@Column(name = "time", length = 19)
	public Date getTime() {
		return this.time;
	}

	/**
	 * 还款时间
	 * 
	 * @param time
	 */
	public void setTime(Date time) {
		this.time = time;
	}
	@Column(name = "repay_way", length = 50)
	public String getRepayWay() {
		return repayWay;
	}

	public void setRepayWay(String repayWay) {
		this.repayWay = repayWay;
	}
	/**
	 * 订单号
	 * */
	@Column(name = "order_id" ,length = 32)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	/**
	 * 订单时间
	 * */
	@Column(name = "order_date")
	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	
	
}