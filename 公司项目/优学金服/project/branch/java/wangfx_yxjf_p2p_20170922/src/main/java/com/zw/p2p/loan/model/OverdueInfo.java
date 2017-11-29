package com.zw.p2p.loan.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 15-2-26
 * Time: 下午1:50
 * To change this template use File | Settings | File Templates.
 */

/**
 * 借款逾期记录表
 */
 @Entity
 @Table(name = "overdue_info")
public class OverdueInfo implements Serializable{

	private  String  id;
	/**
	 * 用户id
	 */
	private  String  userId;

	/**
	 * 借款还款信息id
	 */
	private  String  loanRepayId;

	public OverdueInfo() {
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "user_id", nullable = false, length = 32)
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "loan_repay_id",nullable = false, length = 32)
	public String getLoanRepayId() {
		return loanRepayId;
	}

	public void setLoanRepayId(String loanRepayId) {
		this.loanRepayId = loanRepayId;
	}
}
