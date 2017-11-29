package com.zw.p2p.area.model;

// default package

import com.zw.p2p.fee.model.FeeSchemePay;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.util.List;

@Entity
@Table(name = "code_province")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class Province implements java.io.Serializable {
	private int id;
	private String name;

	//支付公司列表
	private List<City> cities;

	public Province(int id) {
		this.id = id;
	}

	/** default constructor */
	public Province() {

	}

	public Province(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "province")
	public List<City> getCities() {
		return cities;
	}

	public void setCities(List<City> cities) {
		this.cities = cities;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 6)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "name", length = 32)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}