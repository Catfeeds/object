package com.zw.archer.theme.controller;


import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.menu.MenuConstants;
import com.zw.archer.menu.model.MenuType;
import com.zw.archer.theme.ThemeConstants;
import com.zw.archer.theme.model.ComponentParameter;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;

@Component
@Scope(ScopeType.REQUEST)
public class ComponentParameterHome extends EntityHome<ComponentParameter>{
	
	@Logger static Log log ;
	
	public ComponentParameterHome(){
//		setUpdateView( FacesUtil.redirect(ThemeConstants.View.COMPONENT_EDIT+"?id="+getId()) );
	}
	
	@Override
	public String getUpdateView() {
		
		return FacesUtil.redirect(ThemeConstants.View.COMPONENT_EDIT) +"&id="+getInstance().getComponent().getId();
	}
	
	@Override
	protected ComponentParameter createInstance() {
		ComponentParameter instance = new ComponentParameter();
		instance.setComponent(new com.zw.archer.theme.model.Component());
		return instance;
	}
}
