package com.zw.p2p.bankcode.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.bankcode.BankCodeConstant.Status;
import com.zw.p2p.bankcode.model.BankCode;

@Component
@Scope(ScopeType.VIEW)
public class BankCodeList extends EntityQuery<BankCode> {

	private static final String[] RESTRICTIONS = {
//			"bankCode.id like #{bankCodeList.example.id}",
			"bankCode.name like #{bankCodeList.example.name}",
			"bankCode.payCode like #{bankCodeList.example.payCode}",
//			"bankCode.status like #{bankCodeList.example.status}",
			"bankCode.hot like #{bankCodeList.example.hot}",
			"bankCode.category like #{bankCodeList.example.category}"};
//			"bankCode.type = #{bankCodeList.example.type}" };

	public BankCodeList() {
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	/** 获取某类型且可用的bankCode */
	public List<BankCode> getBankCodesByType(String type) {
		String hql = "from BankCode bc where bc.type=? and bc.status=?";
		return getHt().find(hql, new String[] { type, Status.ENABLE });
	}

}
