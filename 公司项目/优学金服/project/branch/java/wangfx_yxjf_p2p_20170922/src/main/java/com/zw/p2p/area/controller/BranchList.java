package com.zw.p2p.area.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.area.model.Branch;
import com.zw.p2p.area.model.City;
import com.zw.p2p.area.model.Province;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Scope(ScopeType.VIEW)
public class BranchList extends EntityQuery<Branch> {
	public BranchList() {
		final String[] RESTRICTIONS = { 
//				"city.province.id like #{cityList.example.province.id}"
				"branch.bank_no like #{bankCardHome.instance.bankNo}"
//				"branch.address like #{bankCardHome.instance.bankCity}%"
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

		this.initLazyModel();
	}
	
	@Override
	protected void initExample() {
		Branch branch = new Branch();
		setExample(branch);
	}

}
