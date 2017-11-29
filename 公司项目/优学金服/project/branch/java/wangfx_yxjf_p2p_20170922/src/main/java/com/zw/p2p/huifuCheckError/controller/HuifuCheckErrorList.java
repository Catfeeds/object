package com.zw.p2p.huifuCheckError.controller;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.huifu.bean.model.HuiFuCheckError;

@Component
@Scope(ScopeType.VIEW)
public class HuifuCheckErrorList extends EntityQuery<HuiFuCheckError> {
	private static final String lazyModelCountHql="select count(distinct huifuCheckError) from HuiFuCheckError huifuCheckError";
	private static final String lazyModelHql="select distinct huifuCheckError from HuiFuCheckError huifuCheckError";
	
	public HuifuCheckErrorList(){
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
	}
	
}
