package com.zw.p2p.message.service;

import com.zw.archer.user.model.User;
import com.zw.p2p.message.model.UserMessageTemplate;

import java.util.List;
import java.util.Map;

/**
 * 发送信息(短信、邮件、或者站内信)
 * 
 * @author Administrator
 * 
 */
public interface AutoMsgService {
	/**
	 * 发送信息
	 * @param user  接收信息的用户
	 * @param nodeId 信息发送节点
	 * @return
	 */
	public void sendMsg(User user,String nodeId,Map<String,String> params) throws Exception;

	/**
	 * 获取要绑定的模板
	 * @return
	 */
	public List<UserMessageTemplate> getUmts();

}
