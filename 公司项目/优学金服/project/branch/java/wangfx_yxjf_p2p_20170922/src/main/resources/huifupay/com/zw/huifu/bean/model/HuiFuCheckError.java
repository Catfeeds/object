package com.zw.huifu.bean.model;

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
* @Description: TODO(汇付对账异常人员,只记录异常的记录) 
* @author cuihang   
* @date 2017-8-2 下午1:49:28
 */
@Entity
@Table(name ="huifu_check_error")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class HuiFuCheckError {
	private String id;
	private User user;

	/**
	 * 冻结金额
	 */
	private Double frozenMoney;
	/**
	 * 余额
	 */
	private Double balance;
	/**
	 * 汇付冻结金额
	 */
	private Double hffrozenMoney;
	/**
	 * 汇付余额
	 */
	private Double hfbalance;
	/**
	 * 备注
	 */
	private String userId;
	private String memo;
	private Date createTime;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Double getFrozenMoney() {
		return frozenMoney;
	}
	public void setFrozenMoney(Double frozenMoney) {
		this.frozenMoney = frozenMoney;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public Double getHffrozenMoney() {
		return hffrozenMoney;
	}
	public void setHffrozenMoney(Double hffrozenMoney) {
		this.hffrozenMoney = hffrozenMoney;
	}
	public Double getHfbalance() {
		return hfbalance;
	}
	public void setHfbalance(Double hfbalance) {
		this.hfbalance = hfbalance;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
