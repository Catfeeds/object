package com.zw.p2p.area.model;

// default package

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "code_city")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class City implements java.io.Serializable {
	private String id;
	private String name;
	private Province province;

	/** default constructor */
	public City() {
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "province_id")
	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 6)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
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