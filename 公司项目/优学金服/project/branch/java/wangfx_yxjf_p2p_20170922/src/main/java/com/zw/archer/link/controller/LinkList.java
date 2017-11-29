package com.zw.archer.link.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.language.model.Language;
import com.zw.archer.link.LinkConstants;
import com.zw.archer.link.model.Link;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
@Component
@Scope(ScopeType.VIEW)
public class LinkList extends EntityQuery<Link> implements Serializable{
	@Logger
	static Log log;
	public LinkList() {
		final String[] RESTRICTIONS = {
				"id like #{linkList.example.id}",
				"name like #{linkList.example.name}",
				"url like #{linkList.example.url}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

	}
	
	public List<Link> getAllLinks(){
		return getHt().find("from Link order by seqNum");
	}
	
	/**
	 * 获取首页面所有的链接
	 * @return
	 */
	public List<Link> getFrontLinks(){
		return getLinks(LinkConstants.LinkPosition.FRONT);
	}
	
	/**
	 * 获取内页所有链接
	 * @return
	 */
	public List<Link> getInnerLinks(){
		return getLinks(LinkConstants.LinkPosition.INNER);
		
	}
	
	
	public List<Link> getLinks(final String type){
		return getHt().findByNamedQuery("Link.findLinkByPositionOrderBySeqNumAndName", type);
	}
	
	
}
