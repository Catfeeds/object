package com.zw.p2p.rateticket.model;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class RateTicketRuleList extends EntityQuery<RateTicketRule>  {
	public RateTicketRuleList() {
		
		final String[] RESTRICTIONS = { "resource = #{rateTicketRuleList.example.resource}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}
}
