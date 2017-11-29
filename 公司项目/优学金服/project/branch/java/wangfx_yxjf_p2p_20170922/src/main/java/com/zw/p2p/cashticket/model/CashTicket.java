package com.zw.p2p.cashticket.model;

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
 * 现金券
 * 用户获得加息券后点击使用，使用后金额累加到账户余额中
 */
@Entity
@Table(name = "cash_ticket")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class CashTicket implements java.io.Serializable {
	// Fields

		/**
		 * 
		 */
		private static final long serialVersionUID = -28725040105640778L;

		/**
		 * 规则：使用节点+类型+金额+使用下限
		 */
		private String id;

		/**
		 * 使用状态 
		 */
		private String status;
		/**
		 * 用户
		 */
		private User user;
		/**
		 * 面值金额
		 */
		private double money;
		/**
		 * 获得日期
		 */
		private String createTime;
		/**
		 * 有效期日期
		 */
		private String endTime;
		/**
		 * 使用日期
		 */
		private Date usedTime;
		/**
		 * 获取来源类型 注册，投资达标，邀请，管理员操作 
		 */
		private String resource;


		// Property accessors
		@Id
		@Column(name = "id", unique = true, nullable = false, length = 32)
		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Column(name = "money", precision = 12, scale = 2)
		public double getMoney() {
			return money;
		}

		public void setMoney(double money) {
			this.money = money;
		}

		@Column(name = "status", nullable = false, length = 50)
		public String getStatus() {
			return this.status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@Column(name = "used_time", nullable = true)
		public Date getUsedTime() {
			return usedTime;
		}

		public void setUsedTime(Date usedTime) {
			this.usedTime = usedTime;
		}


		@Column(name = "resource", nullable = false)
		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "user_id")
		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public String getCreateTime() {
			return createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		

}
