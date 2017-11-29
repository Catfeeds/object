package com.zw.p2p.privilege.controller;

import java.util.Arrays;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.archer.invite.model.invite;
import com.zw.archer.menu.MenuConstants;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;
import com.zw.p2p.privilege.model.Privilege;

@Component
@Scope(ScopeType.VIEW)
public class PrivilegeList extends EntitySQLQuery<Privilege>{
	
	@Logger
	private static Log log;
	
	private static StringManager sm = StringManager
			.getManager(MenuConstants.Package);
	@Resource
	HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUserInfo;
	
	private String userid;
	
    private static final String lazyModelCountHql = "select count(*) from (select * from privilege) aaa";
	
	private static final String lazyModelHql ="select * from privilege aaa";
	
	public PrivilegeList(){
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		
		final String[] RESTRICTIONS = {
				" aaa.user_id like #{privilegeList.userid}"
				
		};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		addOrder("id", "desc");
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	

}
