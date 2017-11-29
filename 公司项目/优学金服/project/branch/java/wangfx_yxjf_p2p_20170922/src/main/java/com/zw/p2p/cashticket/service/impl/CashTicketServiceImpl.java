package com.zw.p2p.cashticket.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.p2p.cashticket.CashticketConstants;
import com.zw.p2p.cashticket.model.CashTicket;
import com.zw.p2p.cashticket.service.CashTicketService;
import com.zw.p2p.coupons.CouponConstants;

@Service
public class CashTicketServiceImpl implements CashTicketService{

	@Resource
	private HibernateTemplate ht;
	@Resource
	private ConfigService configService;
	@Override
	public void giveCashTicketToUser(CashTicket cashTicket,User user) throws Exception {
		cashTicket.setId( UUID.randomUUID().toString().replaceAll("-", ""));
		cashTicket.setResource("admin");
		cashTicket.setEndTime(configService.getConfigValue("zcashEndTime"));
		cashTicket.setUser(user);
		cashTicket.setStatus(CouponConstants.CouponStatus.ENABLE);
		cashTicket.setCreateTime(configService.getConfigValue("zcashSendTime"));
		ht.save(cashTicket);
	}
	@Override
	public void useCashTicket(CashTicket cashTicket) {
		cashTicket.setStatus(CashticketConstants.CashticketStatus.USERUSE);
		cashTicket.setUsedTime(new Date());
		ht.save(cashTicket);
	}
	@Override
	public List<CashTicket> getUnusedCashTicket(String userId) {
		return ht.find("from CashTicket where  user.id='" + userId + "' and status='"+CouponConstants.CouponStatus.ENABLE+"' and endTime > now()");
	}
	@Override
	public List<CashTicket> getAllCashTicket(String userId) {
		return ht.find("from CashTicket where  user.id='" + userId + "' and endTime > now()");
	}

}
