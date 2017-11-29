	package com.zw.p2p.safeloan.model;


// default package

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

import com.zw.archer.node.model.Node;
import com.zw.archer.user.model.User;

/**
 * Loan entity. // TODO:无忧宝产品
 */
@Entity
@Table(name = "safeloan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class SafeLoan implements java.io.Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Fields

	private String id;
	// 标名称
	private String name;
	// 描述 模版id
	private Node description;
	// 常见问题 模版id
	private Node normalPro;
	//合同 模版id
	private Node contract ;
	/** 实际借到的金额 */
	private Double money;
	/** 预计的借款金额 */
	private Double loanMoney;

	/**
	 * 起投金额
	 */
	private Double minInvestMoney;
	/**
	 * 递增金额
	 */
	private Double incMoney;
	/**
	 * 最大投资金额
	 */
	private Double maxInvestMoney;

	// 利率0.12
	private Double rate;
	//提前退出扣款比例
	private Double fine;

	// 预计项目开始计息时间 如果提前投满或者复核需更新开始和截至时间
	private Date commitTime;
	// 预计项目结束时间
	private Date endTime;
	// 投资期  月（31天每月）封闭期
	private Integer deadline;
	// 投资 锁定期小时
	private Integer deadhours;
	// 状态 无忧宝使用审核状态   审核期0 →审核通过→  投资期1→ （复核中2 ）  →已满标3
	private Integer status;
	/**
	 * 投资期开始时间
	 */
	private Date approveBeginTime;
	/**
	 * 投资期结束时间
	 */
	private Date approveEndTime;
	/**
	 * 是否允许提前退出 1允许 2不允许
	 */
	private Integer enableExit;
	private Integer enableStatus; // 启用停用 1启用2停用 只有在新建初始状态可进行停用操作
	/**
	 * 创建人
	 */
	private User creatUser;
	/**
	 * 审核意见
	 */
	private String approveMsg;
	private int inteertype;//计息方式
	private String unit;//计息单位
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "description")
	public Node getDescription() {
		return description;
	}

	public void setDescription(Node description) {
		this.description = description;
	}

	@Column(name = "money", precision = 22, scale = 0)
	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	@Column(name = "loanMoney", precision = 22, scale = 0)
	public Double getLoanMoney() {
		return loanMoney;
	}

	public void setLoanMoney(Double loanMoney) {
		this.loanMoney = loanMoney;
	}

	@Column(name = "minInvestMoney", precision = 22, scale = 0)
	public Double getMinInvestMoney() {
		return minInvestMoney;
	}

	public void setMinInvestMoney(Double minInvestMoney) {
		this.minInvestMoney = minInvestMoney;
	}

	@Column(name = "maxInvestMoney", precision = 22, scale = 0)
	public Double getMaxInvestMoney() {
		return maxInvestMoney;
	}

	public void setMaxInvestMoney(Double maxInvestMoney) {
		this.maxInvestMoney = maxInvestMoney;
	}

	@Column(name = "rate", precision = 22, scale = 0)
	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}
	@Column(name = "fine", precision = 22, scale = 0)
	public Double getFine() {
		return fine;
	}

	public void setFine(Double fine) {
		this.fine = fine;
	}

	@Column(name = "commitTime", length = 19)
	public Date getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}

	@Column(name = "deadline")
	public Integer getDeadline() {
		return deadline;
	}

	public void setDeadline(Integer deadline) {
		this.deadline = deadline;
	}

	@Column(name = "enableStatus")
	public Integer getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(Integer enableStatus) {
		this.enableStatus = enableStatus;
	}

	@Column(name = "endTime",length = 19)
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	@Column(name = "incMoney", precision = 22, scale = 0)
	public Double getIncMoney() {
		return incMoney;
	}

	public void setIncMoney(Double incMoney) {
		this.incMoney = incMoney;
	}
	@Column(name = "deadhours")
	public Integer getDeadhours() {
		return deadhours;
	}

	public void setDeadhours(Integer deadhours) {
		this.deadhours = deadhours;
	}
	@Column(name = "enableExit")
	public Integer getEnableExit() {
		return enableExit;
	}

	public void setEnableExit(Integer enableExit) {
		this.enableExit = enableExit;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creatUser", nullable = false)
	public User getCreatUser() {
		return creatUser;
	}

	public void setCreatUser(User creatUser) {
		this.creatUser = creatUser;
	}
	@Column(name = "approveBeginTime",length = 19)
	public Date getApproveBeginTime() {
		return approveBeginTime;
	}

	public void setApproveBeginTime(Date approveBeginTime) {
		this.approveBeginTime = approveBeginTime;
	}
	@Column(name = "approveEndTime",length = 19)
	public Date getApproveEndTime() {
		return approveEndTime;
	}

	public void setApproveEndTime(Date approveEndTime) {
		this.approveEndTime = approveEndTime;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "normalPro")
	public Node getNormalPro() {
		return normalPro;
	}

	public void setNormalPro(Node normalPro) {
		this.normalPro = normalPro;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract")
	public Node getContract() {
		return contract;
	}

	public void setContract(Node contract) {
		this.contract = contract;
	}
	@Column(name = "approveMsg")
	public String getApproveMsg() {
		return approveMsg;
	}

	public void setApproveMsg(String approveMsg) {
		this.approveMsg = approveMsg;
	}
	
	@Column(name = "inteertype")
	public int getInteertype() {
		return inteertype;
	}

	public void setInteertype(int inteertype) {
		this.inteertype = inteertype;
	}
	@Column(name = "unit")
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	

}
