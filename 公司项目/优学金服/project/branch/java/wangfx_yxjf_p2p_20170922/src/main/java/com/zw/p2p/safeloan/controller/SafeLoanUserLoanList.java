package com.zw.p2p.safeloan.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.model.SafeLoan_User_Loan;
/**
 * 无忧宝资金明细
 * @author Administrator
 *2016年1月20日 14:17:20
 */
@Component
@Scope(ScopeType.VIEW)
public class SafeLoanUserLoanList extends EntityQuery<SafeLoan_User_Loan> {
	@Logger
	static Log log;
	@Resource
	LoanService loanService;
	@Resource
	InvestService investService;
	private String safeLoanReId;
	private String userid;

	private static final String lazyModelCountHql = "select count(distinct slul) from SafeLoan_User_Loan slul";
	private static final String lazyModelHql = "select distinct slul from SafeLoan_User_Loan slul";
	
	public SafeLoanUserLoanList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				" slul.safeLoanRecordId = #{safeLoanUserLoanList.safeLoanReId}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	public void setParams(String safeLoanReId, Integer pageRows, String userid){
		this.safeLoanReId = safeLoanReId;
		this.userid = userid;
		if(null != pageRows){
			setPageSize(pageRows);
		}
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
	public String getStautsData(Integer index){
		return SafeLoanConstants.SafeLoanUserLoanStatus.getName(index);
	}
	public String finishtime(String loanId){
		String result="";
		Date dater= loanService.loanFinishTime(loanId);
		if(null!=dater){
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			 result= formatter.format(dater);
		}
		
		return result;
	}
	/**
	*@Description: TODO(根据状态和投资id判断是否显示合同) 
	* @author cuihang   
	*@date 2016-4-25 上午10:03:38 
	*@param status
	*@param invesid
	*@return
	 */
	public boolean isShowCon(int status,String invesid){
			Invest invest=investService.getInvstById(invesid);
			if(null==invest){
				return false;
			}
			if(status==0&&invest.getStatus()!=null&&invest.getStatus().equals("repaying")){
			return true;	
			}
			return false;
	}
}
