package com.zw.archer.node.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.node.NodeConstants;
import com.zw.archer.node.model.NodeType;
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
public class NodeTypeList extends EntityQuery<NodeType> implements java.io.Serializable{
	
	private static final long serialVersionUID = 9057256450216810237L;
	
	static StringManager sm = StringManager.getManager(NodeConstants.Package);
	@Logger static Log log ;
	
	public NodeTypeList(){
		final String[] RESTRICTIONS = 
				{"id like #{nodeTypeList.example.id}",
				"name like #{nodeTypeList.example.name}",
				"description like #{nodeTypeList.example.description}"};
				
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
	
	
}
