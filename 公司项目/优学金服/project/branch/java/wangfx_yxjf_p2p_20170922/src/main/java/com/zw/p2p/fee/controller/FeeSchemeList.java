package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.fee.model.FeeScheme;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemeList extends EntityQuery<FeeScheme> implements java.io.Serializable{
}
