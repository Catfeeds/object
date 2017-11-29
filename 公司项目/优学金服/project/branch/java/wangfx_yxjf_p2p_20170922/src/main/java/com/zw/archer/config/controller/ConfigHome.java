package com.zw.archer.config.controller;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.Config;
import com.zw.archer.config.model.ConfigType;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class ConfigHome extends EntityHome<Config> implements
		java.io.Serializable {
	@Logger
	static Log log;
	private final static StringManager sm = StringManager
			.getManager(ConfigConstants.Package);

	@Resource
	private LoginUserInfo loginUserInfo;

	public ConfigHome() {
		setUpdateView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
		setDeleteView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
	}

	public Config createInstance() {
		Config config = new Config();
		config.setConfigType(new ConfigType());
		return config;
	}

	@Transactional(readOnly = false)
	public String save() {
		if (getInstance().getConfigType() == null
				|| StringUtils.isEmpty(getInstance().getConfigType().getId())) {
			getInstance().setConfigType(null);
		}
		return super.save();
	}

	/**
	 * 通过配置编号得到配置的值
	 * 
	 * @param configId
	 * @return
	 */
	public String getConfigValue(String configId) {
		Config config = getBaseService().get(Config.class, configId);
		if (config != null) {
			return config.getValue();
		}
		return "";
	}
	public void printConfigValue(String configId){
		String result="";

		Config config = getBaseService().get(Config.class, configId);
		if (config != null) {
			result= config.getValue();
		}
			try {
					FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
					FacesUtil.getHttpServletResponse().getWriter().print(result);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	@Override
	@Transactional(readOnly = false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteConfig",
					loginUserInfo.getLoginUserId(), new Date(), getId()));
		}
		return super.delete();
	}
}
