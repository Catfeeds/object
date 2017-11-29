package com.zw.p2p.coupons.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.coupons.model.CouponRule;

@Component
@Scope(ScopeType.VIEW)
public class CouponRuleList extends EntityQuery<CouponRule> {
	

	public CouponRuleList() {
		final String[] RESTRICTIONS = { "resource = #{couponRuleList.example.resource}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}




}
