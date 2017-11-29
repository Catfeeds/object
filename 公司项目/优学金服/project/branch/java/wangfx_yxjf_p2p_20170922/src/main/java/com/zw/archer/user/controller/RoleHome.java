package com.zw.archer.user.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class RoleHome extends EntityHome<Role> {

	// private static final long serialVersionUID = -5194410042254832100L;

	@Logger
	Log log;

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);
	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/user/roleList");

	public RoleHome() {
		// FIXME：保存角色的时候会执行一条更新User的语句
		setUpdateView(UPDATE_VIEW);
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteRole",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		List<User> userLists = getInstance().getUsers();
		if (userLists != null && userLists.size() > 0) {
			FacesUtil.addWarnMessage(sm
					.getString("canNotDeleteRole"));
			if (log.isInfoEnabled()) {
				log.info(sm.getString(
						"log.info.deleteRoleUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		}else{
			return super.delete();
		}
	}

}
