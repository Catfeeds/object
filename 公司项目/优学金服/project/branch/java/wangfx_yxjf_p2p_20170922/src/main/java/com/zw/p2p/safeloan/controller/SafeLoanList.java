package com.zw.p2p.safeloan.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanRecordStatus;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanStatus;
import com.zw.p2p.safeloan.model.SafeLoan;

@Component
@Scope(ScopeType.VIEW)
public class SafeLoanList extends EntityQuery<SafeLoan> {
	@Logger
	static Log log;

	private String name;
	private Integer status;

	private static final String lazyModelCountHql = "select count(distinct safeLoan) from SafeLoan safeLoan";
	private static final String lazyModelHql = "select distinct safeLoan from SafeLoan safeLoan";
	
	public SafeLoanList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				
				" safeLoan.name like #{safeLoanList.name}",
				" safeLoan.status = #{safeLoanList.status}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		addOrder("safeLoan.approveBeginTime", "desc");
	}

	@Override
	protected void initExample() {
		SafeLoan example = new SafeLoan();
		example.setCreatUser(new User());
		setExample(example);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if (!StringUtils.isEmpty(name))
			this.name = "%"+name+"%";
		else
			this.name=null;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public List<SafeLoanStatus> getStautsList(){
		List<SafeLoanStatus> resultList =new ArrayList<SafeLoanStatus>();
		for (SafeLoanStatus c : SafeLoanStatus.values()) {
			resultList.add(c);
		}
		return resultList;
	}
	
	public String getStautsData(Integer index){
		return SafeLoanConstants.SafeLoanStatus.getName(index);
	}
}
