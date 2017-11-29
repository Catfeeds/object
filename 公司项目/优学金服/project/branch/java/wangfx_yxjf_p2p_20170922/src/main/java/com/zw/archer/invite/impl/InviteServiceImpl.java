package com.zw.archer.invite.impl;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.invite.InviteService;
import com.zw.archer.invite.model.invite;

@Service("inviteService")
public class InviteServiceImpl implements InviteService {

	@Resource
	private HibernateTemplate ht;
	
	@Override
	public void updateMobile(String oldMobile, String newMobile) {
		ht.bulkUpdate(" update invite set from_phone=? where from_phone=?",newMobile,oldMobile);
		ht.bulkUpdate(" update invite set to_phone=? where to_phone=?",newMobile,oldMobile);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void save(invite in) {
		ht.saveOrUpdate(in);
	}

	
}
