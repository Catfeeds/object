package com.zw.p2p.message.controller;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.message.MessageConstants;
import com.zw.p2p.message.model.UserMessageWay;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageWayList extends EntityQuery<UserMessageWay>{
	
	@Resource
	private HibernateTemplate ht ;
	
	@Logger 
	private static Log log ;
	
	
	@Transactional(readOnly=false)
	public void changeStatus(UserMessageWay umw){
		if (umw.getStatus().equals(MessageConstants.UserMessageNodeStatus.OPEN)) {
			umw.setStatus(MessageConstants.UserMessageNodeStatus.CLOSED);
		} else {
			umw.setStatus(MessageConstants.UserMessageNodeStatus.OPEN);
		} 
		getHt().update(umw);
	}
}
