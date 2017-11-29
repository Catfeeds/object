package com.zw.archer.config.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.ConfigType;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;
@Component
@Scope(ScopeType.VIEW)
public class ConfigTypeList extends EntityQuery<ConfigType> implements java.io.Serializable{

	private static final long serialVersionUID = 2809025189706426377L;
	static StringManager sm = StringManager.getManager(ConfigConstants.Package);
	@Logger static Log log ;
	
	public ConfigTypeList(){
		final String[] RESTRICTIONS = 
				{"id like #{configTypeList.example.id}",
				"name like #{configTypeList.example.name}",
				"description like #{configTypeList.example.description}"};
				
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
}
