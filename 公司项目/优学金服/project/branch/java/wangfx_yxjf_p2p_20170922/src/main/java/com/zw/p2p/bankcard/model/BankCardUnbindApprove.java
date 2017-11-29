package com.zw.p2p.bankcard.model;

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

import com.zw.archer.user.model.User;

/**
 * BankCard entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "bankcardunbindapprove")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class BankCardUnbindApprove implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private User userid;//申请人id
	private String idCardsImgStr;//身份证照片
	private String bankProveStr;//银行证明
	private String statementTextStr;//解绑声明
	private String approveDes;//同意或驳回原因
	private int resouse;
	private int status;
	private Date commitTime;//申请提交时间
	private Date approveTime;//审核时间
	private User approveUser;//审核人
	private int approvetype;//审批类型
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userid")
	public User getUserid() {
		return userid;
	}
	public void setUserid(User userid) {
		this.userid = userid;
	}
	@Column(name = "idCardsImgStr")
	public String getIdCardsImgStr() {
		return idCardsImgStr;
	}
	public void setIdCardsImgStr(String idCardsImgStr) {
		this.idCardsImgStr = idCardsImgStr;
	}
	@Column(name = "bankProveStr")
	public String getBankProveStr() {
		return bankProveStr;
	}
	public void setBankProveStr(String bankProveStr) {
		this.bankProveStr = bankProveStr;
	}
	@Column(name = "statementTextStr")
	public String getStatementTextStr() {
		return statementTextStr;
	}
	public void setStatementTextStr(String statementTextStr) {
		this.statementTextStr = statementTextStr;
	}
	@Column(name = "resouse")
	public int getResouse() {
		return resouse;
	}
	public void setResouse(int resouse) {
		this.resouse = resouse;
	}
	@Column(name = "status")
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	@Column(name = "commitTime",length = 19)
	public Date getCommitTime() {
		return commitTime;
	}
	public void setCommitTime(Date commitTime) {
		this.commitTime = commitTime;
	}
	@Column(name = "approveTime",length = 19)
	public Date getApproveTime() {
		return approveTime;
	}
	public void setApproveTime(Date approveTime) {
		this.approveTime = approveTime;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approveUser")
	public User getApproveUser() {
		return approveUser;
	}
	public void setApproveUser(User approveUser) {
		this.approveUser = approveUser;
	}
	@Column(name = "approvetype")
	public int getApprovetype() {
		return approvetype;
	}
	public void setApprovetype(int approvetype) {
		this.approvetype = approvetype;
	}
	@Column(name = "approveDes")
	public String getApproveDes() {
		return approveDes;
	}
	
	public void setApproveDes(String approveDes) {
		this.approveDes = approveDes;
	}
	
	
}