package com.zw.archer.user.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.Permission;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class PermissionList extends EntityQuery<Permission> {

	public PermissionList() {
		final String[] RESTRICTIONS = {"id like #{permissionList.example.id}",
				"name like #{permissionList.example.name}",
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

		addOrder("name","asc");
	}

}
