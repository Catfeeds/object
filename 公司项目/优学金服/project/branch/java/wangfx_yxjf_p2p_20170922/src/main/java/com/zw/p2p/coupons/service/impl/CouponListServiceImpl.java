package com.zw.p2p.coupons.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.coupons.service.CouponListService;

@Service
public class CouponListServiceImpl implements CouponListService {
	@Resource
	private HibernateTemplate ht;

	@Override
	public Double getUnusedCoupons(String id) {
		if (id != null && id.length() > 0) {
			StringBuilder sql = new StringBuilder("select sum(c.money) from Coupons c where c.user.id=? and c.status='un_used'");
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
	public Double getUsedCoupons(String id) {
		if (id != null && id.length() > 0) {
			StringBuilder sql = new StringBuilder("select sum(c.money) from Coupons c where c.user.id=? and c.status='used'");
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
	public Double getTodayUserCoupons(String userId) {
		SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		
		String startDate = startFormat.format(new Date());
		String endDate = endFormat.format(new Date());
		
		if (userId != null && userId.length() > 0) {
			StringBuilder sql = new StringBuilder("select count(c.id) from Coupons c where c.user.id=? and c.usedTime between "+startDate+" and "+endDate);
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
