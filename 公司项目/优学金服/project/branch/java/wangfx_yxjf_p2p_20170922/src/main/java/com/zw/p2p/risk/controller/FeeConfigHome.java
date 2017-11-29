package com.zw.p2p.risk.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.risk.model.FeeConfig;
import com.zw.p2p.risk.model.RiskRank;

@Component
@Scope(ScopeType.VIEW)
public class FeeConfigHome extends EntityHome<FeeConfig> {

	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/risk/feeConfigList");

	public FeeConfigHome() {
		setUpdateView(UPDATE_VIEW);
	}
	
}
