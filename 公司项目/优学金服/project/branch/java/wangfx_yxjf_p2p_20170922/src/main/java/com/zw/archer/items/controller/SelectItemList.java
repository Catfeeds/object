package com.zw.archer.items.controller;
// default package

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.items.model.SelectItem;
import com.zw.archer.items.model.SelectItemGroup;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.REQUEST)
public class SelectItemList extends EntityQuery<SelectItem> {

	private static final String[] RESTRICTIONS = {
		"selectItem.id like #{selectItemList.example.id}",
		"selectItem.name like #{selectItemList.example.name}",
		"selectItem.selectItemGroup.id like #{selectItemList.example.selectItemGroup.id}"
	};
	
	public SelectItemList(){
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	@Override
	protected void initExample() {
		SelectItem selectItem = new SelectItem();
		selectItem.setSelectItemGroup(new SelectItemGroup());
		setExample(selectItem);
	}
}