package com.zw.p2p.area.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.area.model.Province;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;

@Component
@Scope(ScopeType.VIEW)
public class ProvinceList extends EntityQuery<Province> implements Serializable {
	public ProvinceList() {
		this.addOrder("id","asc");
	}
	
	@Override
	protected void initExample() {
		Province pv = new Province();
		setExample(pv);
	}

}
