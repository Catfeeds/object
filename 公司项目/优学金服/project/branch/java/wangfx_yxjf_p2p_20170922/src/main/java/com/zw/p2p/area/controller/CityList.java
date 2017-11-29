package com.zw.p2p.area.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.area.model.City;
import com.zw.p2p.area.model.Province;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Scope(ScopeType.VIEW)
public class CityList extends EntityQuery<City> {
	public CityList() {
		this.setPageSize(999);

		final String[] RESTRICTIONS = { 
//				"city.province.id like #{cityList.example.province.id}"
				"city.province.name like #{bankCardHome.instance.bankProvince}"
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	@Override
	protected void initExample() {
		City city = new City();
		city.setProvince(new Province(2,"北京市"));
		setExample(city);
	}

}
