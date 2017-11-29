package com.zw.p2p.area.model;

// default package

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bank_branch")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Branch implements java.io.Serializable {
	private int id;
	private String bank_no;
	private String branch;
	private String address;

	/** default constructor */
	public Branch() {
	}

	@Column(name = "bank_no", length = 128)
	public String getBank_no() {
		return bank_no;
	}

	public void setBank_no(String bank_no) {
		this.bank_no = bank_no;
	}

	@Column(name = "branch", length = 100)
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Column(name = "address", length = 100)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 6)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

}