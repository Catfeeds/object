package com.zw.p2p.message.controller;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.message.model.UserMessageNode;
import com.zw.p2p.message.model.UserMessageTemplate;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageNodeHome extends EntityHome<UserMessageNode>
		implements java.io.Serializable {

	@Resource
	private HibernateTemplate ht;

	@Logger
	private static Log log;

	@Resource
	private LoginUserInfo loginUser;

	public UserMessageNodeHome() {
		setUpdateView(FacesUtil
				.redirect("/admin/userMessage/userMessageNodeList"));
	}

	@Transactional(readOnly = false)
	public String save() {
		return super.save();
	}

}
