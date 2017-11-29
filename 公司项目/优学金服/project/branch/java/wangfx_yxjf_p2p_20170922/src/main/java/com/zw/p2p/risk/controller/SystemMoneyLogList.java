package com.zw.p2p.risk.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.risk.model.SystemMoneyLog;

@Component
@Scope(ScopeType.VIEW)
public class SystemMoneyLogList extends EntityQuery<SystemMoneyLog> {

	public SystemMoneyLogList() {
		final String[] RESTRICTIONS = { "id like #{loanerBillList.example.id}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

}
