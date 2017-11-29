package com.zw.p2p.privilege.controller;

import java.util.Arrays;
import java.util.Date;

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
public class PrivilegeDetailList extends EntitySQLQuery<Privilege>{
	
	@Logger
	private static Log log;
	
	private static StringManager sm = StringManager
			.getManager(MenuConstants.Package);
	@Resource
	HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUserInfo;
	
	private int id;
	private int privilege_id;
	private String invited_user_id;
	private String change_flag;
	private Double fee;
	private String context;
	private String status;
	private Date create_time;
	private String userid;
	private String username;
	
    private static final String lazyModelCountHql = "select count(*) from (select pd.id,pd.privilege_id,pd.invited_user_id,pd.change_flag,pd.fee,pd.context,pd.status,pd.create_time,p.user_id,u.username from privilege_detail pd left JOIN privilege p on pd.privilege_id=p.id LEFT JOIN user u on p.user_id=u.id) aaa";
	
	private static final String lazyModelHql ="select * from (select pd.id,pd.privilege_id,pd.invited_user_id,pd.change_flag,pd.fee,pd.context,pd.status,pd.create_time,p.user_id,u.username from privilege_detail pd left JOIN privilege p on pd.privilege_id=p.id LEFT JOIN user u on p.user_id=u.id order by pd.create_time  desc) aaa ";

	
	public PrivilegeDetailList(){
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		
		final String[] RESTRICTIONS = {
				" aaa.user_id like #{privilegeDetailList.userid}"
				
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


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getPrivilege_id() {
		return privilege_id;
	}


	public void setPrivilege_id(int privilege_id) {
		this.privilege_id = privilege_id;
	}


	public String getInvited_user_id() {
		return invited_user_id;
	}


	public void setInvited_user_id(String invited_user_id) {
		this.invited_user_id = invited_user_id;
	}


	public String getChange_flag() {
		return change_flag;
	}


	public void setChange_flag(String change_flag) {
		this.change_flag = change_flag;
	}


	public Double getFee() {
		return fee;
	}


	public void setFee(Double fee) {
		this.fee = fee;
	}


	public String getContext() {
		return context;
	}


	public void setContext(String context) {
		this.context = context;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Date getCreate_time() {
		return create_time;
	}


	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}
	

}
