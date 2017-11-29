package com.zw.p2p.fee.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;

/**
 * 费用方案（费率）
 *
 */
@Entity
@Table(name = "fee_scheme")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class FeeScheme implements java.io.Serializable {
	private String id;
	private String name;
	private String description;
	//是否是当前方案
	private Boolean active;
	//当前方案字体颜色
	private String color = "black";
	//当前方案背景颜色
	private String bgColor = "black";
	//支付公司列表
	private List<FeeSchemePay> schemePays;
	//客户相关设置
	private List<FeeSchemeCustomer> schemeCustomers;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "feeScheme")
	public List<FeeSchemeCustomer> getSchemeCustomers() {
		return schemeCustomers;
	}

	public void setSchemeCustomers(List<FeeSchemeCustomer> schemeCustomers) {
		this.schemeCustomers = schemeCustomers;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "feeScheme")
	@OrderBy(value="id")
	public List<FeeSchemePay> getSchemePays() {
		return schemePays;
	}

	public void setSchemePays(List<FeeSchemePay> schemePays) {
		this.schemePays = schemePays;
	}

	@Transient
	public String getBgColor() {
		if(this.active != null && active )
			this.bgColor = "#A7BDE8";
		else
			this.bgColor = "white";
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	@Transient
	public String getColor() {
		if(this.active != null && active )
			this.color = "white";
		else
			this.color = "black";
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}


	@Column(name = "active")
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Column(name = "name", length = 32)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", length = 200)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}


}