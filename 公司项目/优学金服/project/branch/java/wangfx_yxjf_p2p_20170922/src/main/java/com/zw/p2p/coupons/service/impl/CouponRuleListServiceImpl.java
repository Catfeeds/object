package com.zw.p2p.coupons.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.service.CouponRuleListService;

@Service
public class CouponRuleListServiceImpl implements CouponRuleListService {
	@Resource
	private HibernateTemplate ht;

	@Override
	public List<CouponRule> listByType(String resource) {
		return ht.find("from CouponRule where resource='" + resource + "' order by moneyCanUse desc ");
	}

}
