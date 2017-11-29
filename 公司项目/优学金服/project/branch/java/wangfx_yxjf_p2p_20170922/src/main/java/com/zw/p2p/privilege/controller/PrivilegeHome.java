package com.zw.p2p.privilege.controller;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.invite.InviteService;
import com.zw.archer.invite.model.invite;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class PrivilegeHome extends EntityHome<invite> implements java.io.Serializable{

	@Logger
	static Log log;
	@Resource
	private HibernateTemplate ht;

	@Resource
	private LoginUserInfo loginUserInfo;
	
	public Double getNextRate(Integer number){
		double num=number*10+50;
		
		return num;
	}
	
	
}
