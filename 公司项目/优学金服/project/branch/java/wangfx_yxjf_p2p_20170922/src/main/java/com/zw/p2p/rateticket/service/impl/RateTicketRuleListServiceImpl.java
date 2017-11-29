package com.zw.p2p.rateticket.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.service.CouponRuleListService;
import com.zw.p2p.rateticket.model.RateTicketRule;
import com.zw.p2p.rateticket.service.RateTicketRuleListService;

@Service
public class RateTicketRuleListServiceImpl implements RateTicketRuleListService {
	@Resource
	private HibernateTemplate ht;

	@Override
	public List<RateTicketRule> listByType(String resource) {
		return ht.find("from RateTicketRule where resource='" + resource + "' order by moneyCanUse desc ");
	}

}
