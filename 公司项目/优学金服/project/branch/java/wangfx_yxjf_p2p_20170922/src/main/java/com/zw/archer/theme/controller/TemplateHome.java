package com.zw.archer.theme.controller;


import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.theme.ThemeConstants;
import com.zw.archer.theme.model.Template;
import com.zw.archer.theme.model.Theme;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;

@Component
@Scope(ScopeType.REQUEST)
public class TemplateHome extends EntityHome<Template>{
	
	@Logger static Log log ;
	
	public TemplateHome(){
		setUpdateView( FacesUtil.redirect(ThemeConstants.View.TEMPLATE_LIST) );
	}

	@Override
	protected Template createInstance() {
		Template template = new Template();
		template.setTheme(new Theme());
		return template ;
	}
}
