package com.zw.p2p.message.service.impl;

import com.zw.archer.user.model.User;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.message.model.UserMessage;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.MailService;
import com.zw.p2p.message.service.MessageService;
import com.zw.p2p.message.service.SmsService;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("AutoMsgService")
public class AutoMsgServiceImpl implements AutoMsgService {
	@Resource
	private HibernateTemplate ht;

	@Resource
	private SmsService smsService;

	@Resource
	private MailService mailService;

	@Resource
	private  MessageBO messageBO;

	@Resource
	private MessageService messageService;

	public void sendMsg(User user, String nodeId, Map<String, String> params) throws Exception{
		List<String> types = messageService.getUserMessageTypes(user.getId());
		for (String type:types){
			UserMessageTemplate umt = ht.get(UserMessageTemplate.class,nodeId + "_" + type);
			String content = messageBO.replaceParams(umt,params);
			if ("email".equals(type)){
				mailService.getResult(user.getEmail(),umt.getName(),content);
			}else if ("sms".equals(type)){
				smsService.sendMsg(content,user.getMobileNumber());
			} else if ("letter".equals(type)){
				if(umt != null){
					messageBO.sendStationMsg(user,ht.get(User.class,"admin"),umt.getName(),content);
				}
			} else {

			}
		}
	}

	public List<UserMessageTemplate> getUmts(){
		List<UserMessageTemplate> umts = new ArrayList<UserMessageTemplate>();
		UserMessageTemplate smsumt = ht.get(UserMessageTemplate.class,"note_sms");
		UserMessageTemplate mailumt = ht.get(UserMessageTemplate.class,"mail_letter");
		umts.add(smsumt);
		umts.add(mailumt);
		return umts;
	}
}
