package com.zw.p2p.safeloan.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.zw.p2p.safeloan.model.SafeLoan;
import com.zw.p2p.safeloan.model.SafeLoanRecord;

@Component
@Scope(ScopeType.VIEW)
public class SafeLoanRecordList extends EntityQuery<SafeLoanRecord> {
	@Logger
	static Log log;

	private String name;
	private String slid;
	private String username;
	private Integer status;
	private Integer restatus;
	private Date searchSalMinTime, searchSalMaxTime;
	
	private static final String lazyModelCountHql = "select count(distinct safeLoanRecord) from SafeLoanRecord safeLoanRecord";
	private static final String lazyModelHql = "select distinct safeLoanRecord from SafeLoanRecord safeLoanRecord";
	
	public SafeLoanRecordList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				" safeLoanRecord.safeloanid.name like #{safeLoanRecordList.name}",
				" safeLoanRecord.safeloanid.id = #{safeLoanRecordList.slid}",
				" safeLoanRecord.safeloanid.status like #{safeLoanRecordList.restatus}",
				" safeLoanRecord.userid.username = #{safeLoanRecordList.username}",
				" safeLoanRecord.status = #{safeLoanRecordList.status}",
				" safeLoanRecord.salTime >= #{safeLoanRecordList.searchSalMinTime}",
				" safeLoanRecord.salTime <= #{safeLoanRecordList.searchSalMaxTime}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		this.addOrder("safeLoanRecord.safeloanid.commitTime", "desc");
	}

	public String getSlid() {
		return slid;
	}

	public void setSlid(String slid) {
		this.slid = slid;
	}

	@Override
	protected void initExample() {
		SafeLoanRecord example = new SafeLoanRecord();
		example.setUserid(new User());
		example.setSafeloanid(new SafeLoan());
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

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public Date getSearchSalMinTime() {
		return searchSalMinTime;
	}
	public void setSearchSalMinTime(Date searchSalMinTime) {
		this.searchSalMinTime = searchSalMinTime;
	}

	public Date getSearchSalMaxTime() {
		return searchSalMaxTime;
	}
	public void setSearchSalMaxTime(Date searchSalMaxTime) {
		this.searchSalMaxTime = searchSalMaxTime;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public List<SafeLoanRecordStatus> getStautsList(){
		List<SafeLoanRecordStatus> resultList =new ArrayList<SafeLoanRecordStatus>();
		for (SafeLoanRecordStatus c : SafeLoanRecordStatus.values()) {
			resultList.add(c);
		}
		return resultList;
	}
	
	public String getStautsData(Integer index){
		return SafeLoanConstants.SafeLoanRecordStatus.getName(index);
	}
	
	public Integer getRestatus() {
		return restatus;
	}
	public void setRestatus(Integer restatus) {
		this.restatus = restatus;
	}
	
}
