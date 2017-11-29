package com.zw.archer.system.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import com.zw.archer.menu.model.Menu;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.el.SpringSecurityELLibrary;
import com.zw.core.jsf.util.FacesUtil;

@Component
@Scope(ScopeType.SESSION)
public class LoginUserInfo implements Serializable {

	private static final long serialVersionUID = 2359599087254308891L;

	@Logger
	static Log log;

	@Resource
	HibernateTemplate ht;

	@Autowired
	SessionRegistry sessionRegistry;

	private String loginUserId;

	private String selectMenuId;

	private String theme = "redmond";// redmond ;bluesky;aristo;bootstrap

	public String getLoginUserId() {
		if (loginUserId == null) {
			//获取当前登录用户的id
			SecurityContextImpl securityContextImpl = (SecurityContextImpl) FacesUtil
					.getSessionAttribute("SPRING_SECURITY_CONTEXT");
			if (securityContextImpl != null) {
				loginUserId = securityContextImpl.getAuthentication().getName();
			}
		}
		return loginUserId;
	}

	public User getUser(){
		User user = null;
		if (getLoginUserId() != null){
			user = ht.get(User.class,getLoginUserId());
		}
		return user;
	}

	public void selectMenu(String selectMenuId) {
		setSelectMenuId(selectMenuId);
		Menu menu = getFirstGrantedMenu(ht.get(Menu.class, selectMenuId)
				.getChildren());
		if (menu != null) {
			FacesUtil.sendRedirect(FacesUtil.getHttpServletRequest()
					.getContextPath() + menu.getUrl());
		}
	}

	/** 递归找到menus中第一个当前用户可以访问的menu */
	private Menu getFirstGrantedMenu(List<Menu> menus) {
		for (Menu menu : menus) {
			if (menu.getChildren().size() > 0) {
				Menu menuMatched= getFirstGrantedMenu(menu.getChildren());
				if (menuMatched!=null){
					return menuMatched;
				}
			}else if (SpringSecurityELLibrary.ifAnyGranted(menu
					.getPermissionsCommaStr())
					|| menu.getPermissions().size() == 0) {
				return menu;
			}
		}
		return null;
	}
	
	/*
	 * 查询平台在线总人数
	 */
	 public int getNumberOfUsers() {
	    return sessionRegistry.getAllPrincipals().size();
	 }


	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getTheme() {
		return theme;
	}

	public String getSelectMenuId() {
		return selectMenuId;
	}

	public void setSelectMenuId(String selectMenuId) {
		this.selectMenuId = selectMenuId;
	}

}
