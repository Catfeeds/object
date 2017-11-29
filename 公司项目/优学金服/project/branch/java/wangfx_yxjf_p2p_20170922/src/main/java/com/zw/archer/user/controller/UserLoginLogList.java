package com.zw.archer.user.controller;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.model.UserLoginLog;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class UserLoginLogList extends EntityQuery<UserLoginLog> {

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Logger
	private static Log log ;
	
	public UserLoginLogList() {
		final String[] RESTRICTIONS = { 
				"username like #{userLoginLogList.example.username}",
				"loginIp like #{userLoginLogList.example.loginIp}",
				"isSuccess = #{userLoginLogList.example.isSuccess}",
				"loginTime >= #{userLoginLogList.loginTimeStart}",
				"loginTime <= #{userLoginLogList.loginTimeEnd}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	private Date loginTimeStart;
	private Date loginTimeEnd;

	public Date getLoginTimeStart() {
		return loginTimeStart;
	}
	public void setLoginTimeStart(Date loginTimeStart) {
		this.loginTimeStart = loginTimeStart;
	}
	public Date getLoginTimeEnd() {
		return loginTimeEnd;
	}
	public void setLoginTimeEnd(Date loginTimeEnd) {
		this.loginTimeEnd = loginTimeEnd;
	}

	
	


}
