package com.zw.p2p.risk.controller;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.risk.model.SystemBill;
import com.zw.p2p.risk.service.SystemBillService;

@Component
@Scope(ScopeType.VIEW)
public class SystemBillList extends EntityQuery<SystemBill> {

	@Resource
	private SystemBillService ssb;
	
	private Date startTime;
	private Date endTime;
	
	public SystemBillList() {
		final String[] RESTRICTIONS = {
				"id like #{systemBillList.example.id}",
				"type = #{systemBillList.example.type}",
				"reason = #{systemBillList.example.reason}",
				"moneyResource = #{systemBillList.example.moneyResource}",
				"time >= #{systemBillList.startTime}",
				"time <= #{systemBillList.endTime}",
				"money > 0"
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	public double getBalance(){
		return ssb.getBalance();
	}
	
	public double getSumInByType(String type) {
		String hql = "select sum(sb.money) from SystemBill sb where sb.type =?";
		Double sum = (Double) getHt().find(hql, type).get(0);
		if (sum == null) {
			return 0;
		}
		return sum.doubleValue();
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
