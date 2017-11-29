package com.zw.p2p.message.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.message.MessageConstants;
import com.zw.p2p.message.model.UserMessage;
import com.zw.p2p.message.model.UserMessageNode;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.model.UserMessageWay;
import com.zw.p2p.message.service.MailService;
import com.zw.p2p.message.service.MessageService;
import com.zw.p2p.message.service.SmsService;

@Service
public class MessageServiceImpl implements MessageService {
	@Logger
	private static Log log;
	@Resource
	MailService mailService;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private MessageBO messageBO;
	@Resource
	private SmsService smsService;

	private String getTemplateType(UserMessageTemplate template){
		String[] strings = template.getId().split("_");
		String type = strings[strings.length - 1];
		return type;
	}

	@Override
	public List<String> getUserMessageTypes(String userId) {
		List<String> types = new ArrayList<String>();

		List<UserMessageTemplate> templates= getUserMessagesTemplatesByUserId(userId);

		//对于新用户，从来没有设置过的，默认打开站内信和短信
//		if (templates.size()<1){
//			types.add("letter");
//			types.add("sms");
//		}else {
			for (UserMessageTemplate template : templates) {
//				if (!(template.getStatus()!=null && template.getStatus().equals("removed"))){
//					String[] strings = template.getId().split("_");
//					String type = strings[strings.length - 1];
					types.add(getTemplateType(template));
//				}
			}
//		}

		return types;
	}

	@Override
	public void sendMobileMsg(String userId, String msg)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		if (messageBO.isMessageWayenable(MessageConstants.UserMessageWayId.SMS)) {
			if (log.isDebugEnabled())
				log.debug("Send mobile message to User ," + "id = " + userId
						+ ",msg = " + msg);
			smsService.send(msg, user.getMobileNumber());
		}
	}

	@Override
	public void sendEmailMsg(String userId, String title, String msg)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		if (log.isDebugEnabled())
			log.debug("Send email message to User ," + "id = " + userId
					+ ",title=" + title + ",msg = " + msg);
		if (messageBO
				.isMessageWayenable(MessageConstants.UserMessageWayId.EMAIL)) {
			mailService.sendMail(user.getEmail(), title, msg);
		}
	}

	@Override
	public void sendStationMsg(String reveiverId, String senderId,
			String title, String msg) throws UserNotFoundException {
		User receiver = ht.get(User.class, reveiverId);
		if (receiver == null) {
			throw new UserNotFoundException("user.id:" + reveiverId);
		}
		User sender = ht.get(User.class, senderId);
		if (sender == null) {
			throw new UserNotFoundException("user.id:" + sender);
		}
		if (messageBO
				.isMessageWayenable(MessageConstants.UserMessageWayId.LETTER)) {
			if (log.isDebugEnabled())
				log.debug("Send private message to User ," + "reveiverid = "
						+ reveiverId + ",senderId=" + senderId + ",title="
						+ title + ",msg = " + msg);
			messageBO.sendStationMsg(receiver, sender, title, msg);
		}
	}

	//默认通知模板
	private List<UserMessageTemplate> defaultUserMessageTemplate(){
		List<UserMessageTemplate> messageTemplates=ht.find(
				"from UserMessageTemplate umt where umt.status='"
						+ MessageConstants.UserMessageTemplateStatus.OPEN
						+ "' and (umt.id='note_sms' or umt.id='mail_letter')");

		return messageTemplates;
	}

	@Override
	public List<UserMessageTemplate> getUserMessagesTemplatesByUserId(
			String userId) {
		if (getAllUserMessagesTemplates(userId).size()<1){
			return defaultUserMessageTemplate();
		}else{
			String hql = "select um.userMessageTemplate from UserMessage um where um.user.id=? and um.status is null";
			return ht.find(hql, userId);
		}
	}

	public List<UserMessage> getAllUserMessagesTemplates(
			String userId) {
		String hql = "select um from UserMessage um where um.user.id=?";
		return ht.find(hql, userId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor=Exception.class)
	public void saveUserMessages(String userId, List<UserMessageTemplate> umts) {
		List<UserMessage> oldUserMessages= getAllUserMessagesTemplates(userId);
		List<String> oldTmpTypes=new ArrayList<String>();
		for (UserMessage message:oldUserMessages){
			oldTmpTypes.add(getTemplateType(message.getUserMessageTemplate()));
		}

		//处理用户正在修改默认模板，而且把默认模板都勾掉的问题
		boolean modifyDefaultTemplate=false;
		if (umts.size()==0 && oldUserMessages.size()==0){
			umts= defaultUserMessageTemplate();
			modifyDefaultTemplate=true;
		}

//		ht.bulkUpdate("delete from UserMessage um where um.user.id=?", userId);
		for (UserMessageTemplate umt : umts) {
			String type= getTemplateType(umt);
			//添加新模板
			if (oldTmpTypes.indexOf(type)<0) {
				UserMessage um = new UserMessage();
				um.setId(IdGenerator.randomUUID());
				um.setUser(new User(userId));
				um.setUserMessageTemplate(umt);
				if (modifyDefaultTemplate)
					um.setStatus("removed");
				ht.save(um);
			}else{
				//老模板把 removed 标记去掉
				ht.bulkUpdate("update UserMessage um set um.status=null where um.id=?",
						oldUserMessages.get(oldTmpTypes.indexOf(type)).getId());
			}
		}

		List<String> newTmpTypes=new ArrayList<String>();
		for (UserMessageTemplate template:umts){
			newTmpTypes.add(getTemplateType(template));
		}
		for (UserMessage message:oldUserMessages){
			UserMessageTemplate umt= message.getUserMessageTemplate();
			String type= getTemplateType(umt);
			//被删除的模板标记为 removed
			if (newTmpTypes.indexOf(type)<0){
				ht.bulkUpdate("update UserMessage um set um.status='removed' where um.id=?",
						oldUserMessages.get(oldTmpTypes.indexOf(type)).getId());
			}
		}
	}

}
