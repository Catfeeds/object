package com.zw.archer.menu.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.menu.model.MenuType;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;

/**
 * 菜单类型查询
 * @author wanghm
 *
 */
@Component
@Scope(ScopeType.VIEW)
public class MenuTypeList extends EntityQuery<MenuType> implements java.io.Serializable{
	
	static StringManager sm = StringManager.getManager(CommonConstants.Package);
	
//	private static 
	
	public MenuTypeList(){
		final String[] RESTRICTIONS = {
				"id like #{menuTypeList.example.id}",
				"name like #{menuTypeList.example.name}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
	
	
}
