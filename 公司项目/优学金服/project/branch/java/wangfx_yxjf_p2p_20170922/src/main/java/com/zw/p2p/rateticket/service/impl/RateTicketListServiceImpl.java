package com.zw.p2p.rateticket.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.rateticket.service.RateTicketListService;

@Service
public class RateTicketListServiceImpl implements RateTicketListService{

	@Resource
	private HibernateTemplate ht;
	
	@Override
	public Double getUnusedRateTicket(String id) {
		if (id != null && id.length() > 0) {
			StringBuilder sql = new StringBuilder("select sum(r.money) from RateTicket r where r.user.id=? and r.status='un_used'");
			Double sum = (Double) ht.find(sql.toString(), id).get(0);
			if (sum != null) {
				return sum;
			} else {
				return 0d;
			}
		} else {
			return 0d;
		}
	}

	@Override
	public Double getUsedRateTicket(String id) {
		if (id != null && id.length() > 0) {
			StringBuilder sql = new StringBuilder("select sum(r.money) from RateTicket r where r.user.id=? and r.status='used'");
			Double sum = (Double) ht.find(sql.toString(), id).get(0);
			if (sum != null) {
				return sum;
			} else {
				return 0d;
			}
		} else {
			return 0d;
		}
	}

	@Override
	public Double getTodayUserRateTickey(String userId) {
		SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		
		String startDate = startFormat.format(new Date());
		String endDate = endFormat.format(new Date());
		
		if (userId != null && userId.length() > 0) {
			StringBuilder sql = new StringBuilder("select count(r.id) from RateTicket r where r.user.id=? and r.usedTime between "+startDate+" and "+endDate);
			Double sum = (Double) ht.find(sql.toString(), userId).get(0);
			if (sum != null) {
				return sum;
			} else {
				return 0d;
			}
		} else {
			return 0d;
		}
	}

}
