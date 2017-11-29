package com.zw.p2p.bankcard.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApprovetype;
import com.zw.p2p.bankcard.model.BankCardUnbindApprove;

@Component
@Scope(ScopeType.VIEW)
public class BankCardUnbindApproveList  extends EntityQuery<BankCardUnbindApprove>  {
	private String username;
	private String realname;
	private int status=1;
	private int approvetype=0;
	private Date commitTimebegin;
	private Date commitTimeend;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public int getApprovetype() {
		return approvetype;
	}
	public void setApprovetype(int approvetype) {
		this.approvetype = approvetype;
	}
	public Date getCommitTimebegin() {
		return commitTimebegin;
	}
	public void setCommitTimebegin(Date commitTimebegin) {
		this.commitTimebegin = commitTimebegin;
	}
	public Date getCommitTimeend() {
		return commitTimeend;
	}
	public void setCommitTimeend(Date commitTimeend) {
		this.commitTimeend = commitTimeend;
	}


	private static final String lazyModelCountHql = "select count(distinct bcua) from BankCardUnbindApprove bcua";
	private static final String lazyModelHql = "select distinct bcua from BankCardUnbindApprove bcua";
	public BankCardUnbindApproveList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				" bcua.userid.username  like #{bankCardUnbindApproveList.username}",
				" bcua.userid.realname  like #{bankCardUnbindApproveList.realname}",
				" bcua.commitTime  >= #{bankCardUnbindApproveList.commitTimebegin}",
				" bcua.commitTime  <= #{bankCardUnbindApproveList.commitTimeend}",
				" bcua.status = #{bankCardUnbindApproveList.status}",
				" bcua.approvetype  = #{bankCardUnbindApproveList.approvetype}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		addOrder("bcua.commitTime", "desc");
	}
	@Override
	protected void initExample() {
		BankCardUnbindApprove example = new BankCardUnbindApprove();
		example.setApproveUser(new User());
		example.setUserid(new User());
		setExample(example);
	}
	public List<BankCardUnbindApproveStatus> getStautsList(){
		List<BankCardUnbindApproveStatus> resultList =new ArrayList<BankCardUnbindApproveStatus>();
		for (BankCardUnbindApproveStatus c : BankCardUnbindApproveStatus.values()) {
			resultList.add(c);
		}
		return resultList;
	}
	public String getStautsData(Integer index){
		return BankCardUnbindApproveStatus.getName(index);
	}
	public String getApproveData(Integer index){
		return BankCardUnbindApprovetype.getName(index);
	}
	public List<BankCardUnbindApprovetype> getApprovetypeList(){
		List<BankCardUnbindApprovetype> resultList =new ArrayList<BankCardUnbindApprovetype>();
		for (BankCardUnbindApprovetype c : BankCardUnbindApprovetype.values()) {
			resultList.add(c);
		}
		return resultList;
	}
	public String getApprovetypeStr(Integer index){
		return BankCardUnbindApprovetype.getName(index);
	}
}
