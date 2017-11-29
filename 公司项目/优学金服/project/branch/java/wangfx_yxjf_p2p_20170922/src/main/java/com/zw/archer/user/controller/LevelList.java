package com.zw.archer.user.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.LevelForUser;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class LevelList extends EntityQuery<LevelForUser> {

	public LevelList() {
		final String[] RESTRICTIONS = {
				"level like #{levelList.example.level}",
				"name like #{levelList.example.name}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

}
