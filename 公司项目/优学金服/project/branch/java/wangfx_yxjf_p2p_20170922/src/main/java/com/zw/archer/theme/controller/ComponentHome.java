package com.zw.archer.theme.controller;


import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.theme.ThemeConstants;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;

@Component
@Scope(ScopeType.REQUEST)
public class ComponentHome extends EntityHome<com.zw.archer.theme.model.Component>{
	
	@Logger static Log log ;
	
	public ComponentHome(){
		setUpdateView( FacesUtil.redirect(ThemeConstants.View.COMPONENT_LIST) );
	}

	
}
