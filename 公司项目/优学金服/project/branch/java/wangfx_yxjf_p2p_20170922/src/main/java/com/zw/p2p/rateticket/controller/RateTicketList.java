package com.zw.p2p.rateticket.controller;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponListService;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.service.RateTicketListService;

@Component
@Scope(ScopeType.VIEW)
public class RateTicketList extends EntityQuery<RateTicket> {
	@Resource
	RateTicketListService rateTicketListService;
	
	private static final String lazyModelCountHql = "select count(distinct rateTicket) from RateTicket rateTicket";
	private static final String lazyModelHql = "select distinct rateTicket from RateTicket rateTicket";
	private String userid;
	public RateTicketList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = { "rateTicket.status = #{rateTicketList.example.status}",
				"rateTicket.user.id = #{rateTicketList.userid}",
				"rateTicket.resource = #{rateTicketList.example.resource}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}

	@Override
	protected void initExample() {
		RateTicket rateTicket=new RateTicket();
		rateTicket.setUser(new User());
		setExample(rateTicket);
	}

	/**
	 * @return 根据指定用户获取可用加息券
	 */
	public Double getUnusedRateTicket(String id) {
	return 	rateTicketListService.getUnusedRateTicket(id);
		
	}

	/**
	 * @return 根据指定用户获取已使用加息券
	 */
	public Double getUsedCoupons(String id) {
		return 	rateTicketListService.getUsedRateTicket(id);
		
	}

}
