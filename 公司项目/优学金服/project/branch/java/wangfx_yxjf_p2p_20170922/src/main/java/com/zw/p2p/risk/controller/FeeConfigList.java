package com.zw.p2p.risk.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.risk.model.FeeConfig;

import java.io.Serializable;

@Component
@Scope(ScopeType.VIEW)
public class FeeConfigList extends EntityQuery<FeeConfig> implements Serializable {

}
