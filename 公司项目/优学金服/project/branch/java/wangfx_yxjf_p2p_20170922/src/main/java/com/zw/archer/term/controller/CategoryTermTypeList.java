package com.zw.archer.term.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.term.TermConstants;
import com.zw.archer.term.model.CategoryTermType;
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
public class CategoryTermTypeList extends EntityQuery<CategoryTermType> {
	
	static StringManager sm = StringManager.getManager(TermConstants.Package);
	@Logger static Log log ;
	
	public CategoryTermTypeList(){
		final String[] RESTRICTIONS = 
				{"id like #{categoryTermTypeList.example.id}",
				"name like #{categoryTermTypeList.example.name}",
				"description like #{categoryTermTypeList.example.description}"};
				
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
	
	
}
