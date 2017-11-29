package com.zw.archer.theme.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.theme.model.Region;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;

/**
 * 菜单类型查询
 * @author wanghm
 *
 */
@Component
@Scope(ScopeType.VIEW)
public class RegionList extends EntityQuery<Region> implements java.io.Serializable{
	
	private static final long serialVersionUID = 3069029265084507807L;
	
	static StringManager sm = StringManager.getManager(CommonConstants.Package);
	@Logger static Log log ;
	
	public RegionList(){
		final String[] RESTRICTIONS = {"id like #{regionList.example.id}",
				"title like #{regionList.example.title}",
//				"description like #{regionList.example.description}",
//				"templates like #{regionList.example.templates}",
//				"components like #{regionList.example.components}",
				};
				
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
	
	
}
