package com.zw.p2p.message.controller;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.message.model.UserMessageTemplate;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageTemplateHome extends EntityHome<UserMessageTemplate>
		implements java.io.Serializable {

	@Resource
	private HibernateTemplate ht;

	@Logger
	private static Log log;

	@Resource
	private LoginUserInfo loginUser;

	public UserMessageTemplateHome() {
		setUpdateView(FacesUtil
				.redirect("/admin/userMessage/userMessageTemplateList"));
	}

	/**
	 * 生成编号
	 */
	public void generateId(){
		if (this.getInstance().getUserMessageNode() != null && this.getInstance().getUserMessageWay() != null) {
			this.getInstance().setId(
					this.getInstance().getUserMessageNode().getId() + "_"
							+ this.getInstance().getUserMessageWay().getId());
		} else {
			this.getInstance().setId(null);
		}
	}

	@Transactional(readOnly = false)
	public String save() {
		return super.save();
	}

}
