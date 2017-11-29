package com.zw.p2p.message.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.message.model.UserMessage;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.service.MessageService;

@Component
@Scope(ScopeType.VIEW)
public class UserMessageHome extends EntityHome<UserMessage> implements
		java.io.Serializable {

	@Logger
	private static Log log;

	@Resource
	private LoginUserInfo loginUser;
	
	@Resource
	private MessageService messageService;

	private List<UserMessageTemplate> umts;

	public String saveUserMessages() {
		if(umts!=null){
			for (UserMessageTemplate userMessageTemplate : umts) {
				if(userMessageTemplate.getUserMessageWay().getId().equals("email")){
					if(loginUser.getUser().getEmail()==null||loginUser.getUser().getEmail().length()==0){
						FacesUtil.addErrorMessage("当前用户暂未绑定邮箱");
						return "pretty:userMessageTypeSet";
					}
				}
			}
		}
		messageService.saveUserMessages(loginUser.getLoginUserId(), umts);
		FacesUtil.addInfoMessage("设置成功！");
		return "pretty:userMessageTypeSet";
	}

	public void initUmts(String userId) {
		this.umts = messageService.getUserMessagesTemplatesByUserId(userId);
	}

	public List<UserMessageTemplate> getUmts() {
		return umts;
	}

	public void setUmts(List<UserMessageTemplate> umts) {
		this.umts = umts;
	}

}
