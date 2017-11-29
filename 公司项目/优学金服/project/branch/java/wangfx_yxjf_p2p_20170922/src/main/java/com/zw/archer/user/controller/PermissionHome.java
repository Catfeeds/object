package com.zw.archer.user.controller;

import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.menu.model.Menu;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.model.Permission;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.REQUEST)
public class PermissionHome extends EntityHome<Permission> {

	@Logger
	static Log log;
	private static StringManager userSM = StringManager
			.getManager(UserConstants.Package);
	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/user/permissionList");

	public PermissionHome() {
		setUpdateView(UPDATE_VIEW);
	}
	
	@Transactional(readOnly=false)
	public void ajaxDelete() {
		super.delete();
		FacesUtil.addInfoMessage(commonSM.getString("deleteLabel")
				+ commonSM.getString("success"));
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(userSM.getString("log.info.deletePermission",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		Set<Role> roleSets = getInstance().getRoles();
		Set<Menu> menuSets = getInstance().getMenus();
		if (roleSets != null && roleSets.size() > 0) {
			FacesUtil.addWarnMessage(userSM
					.getString("canNotDeletePermissionByRole"));
			if (log.isInfoEnabled()) {
				log.info(userSM.getString(
						"log.info.deletePermissionByRoleUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		} else if (menuSets != null && menuSets.size() > 0) {
			FacesUtil.addWarnMessage(userSM
					.getString("canNotDeletePermissionByMenu"));
			if (log.isInfoEnabled()) {
				log.info(userSM.getString(
						"log.info.deletePermissionByMenuUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		} else {
			return super.delete();
		}
	}
}
