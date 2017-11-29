package com.zw.p2p.safeloan.controller;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.model.MoneyDetailRecord;
/**
 * 无忧宝资金明细
 * @author Administrator
 *2016年1月20日 14:17:20
 */
@Component
@Scope(ScopeType.VIEW)
public class MoneyDetailList extends EntityQuery<MoneyDetailRecord> {
	@Logger
	static Log log;

	private String safeLoanReId;
	private String userid;

	private static final String lazyModelCountHql = "select count(distinct mdr) from MoneyDetailRecord mdr";
	private static final String lazyModelHql = "select distinct mdr from MoneyDetailRecord mdr";
	
	public MoneyDetailList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				" mdr.safeLoanRecordId = #{moneyDetailList.safeLoanReId}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}


	public String getSafeLoanReId() {
		return safeLoanReId;
	}

	public void setSafeLoanReId(String safeLoanReId) {
		this.safeLoanReId = safeLoanReId;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	//返回类型名称
	public String getTypeStr(int key){
		return 	SafeLoanConstants.MoneyDetailRecordType.getName(key);
	}
}
