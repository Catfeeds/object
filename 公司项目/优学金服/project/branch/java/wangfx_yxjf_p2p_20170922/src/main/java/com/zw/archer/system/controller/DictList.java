package com.zw.archer.system.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.system.model.Dict;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class DictList extends EntityQuery<Dict> implements java.io.Serializable{
	
	public DictList() {
		final String[] RESTRICTIONS = { 
				"parent.key like #{dictList.example.parent.key}",
				"key like #{dictList.example.key}",
				"value like #{dictList.example.value}"
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	
	@Override
	protected void initExample() {
		Dict example =new Dict();
		example.setParent(new Dict());
		setExample(example);
	}
}
