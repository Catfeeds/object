package com.zw.archer.items.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.items.model.SelectItemGroup;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class SelectItemGroupList extends EntityQuery<SelectItemGroup> {

	private static final String[] RESTRICTIONS = {
		"selectItemGroup.id like #{selectItemGroupList.example.id}",
		"selectItemGroup.name like #{selectItemGroupList.example.name}"
	};
	
	public SelectItemGroupList(){
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
}
