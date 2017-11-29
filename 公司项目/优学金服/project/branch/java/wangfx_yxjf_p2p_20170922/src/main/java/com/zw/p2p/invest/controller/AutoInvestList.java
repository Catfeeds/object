package com.zw.p2p.invest.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.invest.model.AutoInvest;

@Component
@Scope(ScopeType.VIEW)
public class AutoInvestList extends EntityQuery<AutoInvest> implements java.io.Serializable{
		
	public AutoInvestList(){
		addRestriction(" status='on' ");
		addOrder("lastAutoInvestTime", super.DIR_ASC);
		addOrder("seqNum", super.DIR_ASC);
	}
}
