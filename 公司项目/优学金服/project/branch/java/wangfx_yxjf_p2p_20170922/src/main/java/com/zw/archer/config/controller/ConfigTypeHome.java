package com.zw.archer.config.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.Config;
import com.zw.archer.config.model.ConfigType;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class ConfigTypeHome extends EntityHome<ConfigType> implements
		java.io.Serializable {
	@Logger
	static Log log;
	private static final StringManager sm = StringManager
			.getManager(ConfigConstants.Package);

	public ConfigTypeHome() {
		setUpdateView(FacesUtil.redirect(ConfigConstants.View.CONFIG_TYPE_LIST));
		setDeleteView(FacesUtil.redirect(ConfigConstants.View.CONFIG_TYPE_LIST));
	}

	private List<Config> configs = new ArrayList<Config>();

	@PostConstruct
	public void init() {
		if (FacesUtil.getParameter("id") != null) {
			String id = FacesUtil.getParameter("id");
			super.setId(id);
			configs = new ArrayList<Config>();
			configs = getInstance().getConfigs();
		}
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

	@Transactional(readOnly = false)
	public String configDetail() {
		String id = FacesUtil.getParameter("typeId");
		super.setId(id);
		getInstance().setConfigs(configs);
		super.save();
		return null ;
	}

	@Override
	@Transactional(readOnly = false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteConfigType", FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"),
					new Date(), getId()));
		}
		return super.delete();
	}

	@Override
	@Transactional(readOnly = false)
	public String save() {
		return super.save();
	}
}
