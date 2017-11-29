	package com.zw.p2p.safeloan.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.zw.core.util.DateUtil;


// default package


public class SafeSum implements java.io.Serializable, Cloneable {
	private String id;
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	//无忧宝编号
	private String safeLoanId;
	//无忧宝名
	private String name;
	//无忧宝投资期限
	private Integer deadline;
	//收益率
	private Double rate;
	//无忧宝状态
	private Integer status;
	//无忧宝到期日
	private Date endTime;
	//无忧宝投资金额
	private Double money;
	//投资生效日期
	private Date salTime;
	//散标名
	private String loanname;
	//散标年利率
	private Double loanrate;
	//散标编号
	private String loanid;
	//借款金额
	private Double investmoney;
	//无忧宝状态
	private String safestatus;
	//散标状态
	private String loanstatus;
	//借款人姓名
	private String loanusername;
	//投资人姓名
	private String investusername;
	//无忧宝投资姓名
	private String safeusername;
	//还款日
	private Date loanrepayday;

public String getName() {
	return name;
}	

public void setName(String name) {
	if (!StringUtils.isEmpty(name))
		this.name = "%"+name+"%";
	else
		this.name=null;
}

public String getSafeLoanId() {
	return safeLoanId;
}

public void setSafeLoanId(String safeLoanId) {

	if (!StringUtils.isEmpty(safeLoanId))
		this.safeLoanId = "%"+safeLoanId+"%";
	else
		this.safeLoanId=null;

}

public Integer getDeadline() {
	return deadline;
}
public void setDeadline(Integer deadline) {
	this.deadline = deadline;
}
public Double getRate() {
	return rate;
}
public void setRate(Double rate) {
	
		this.rate=rate;
}
public Integer getStatus() {
	return status;
}
public void setStatus(Integer status) {
	this.status = status;
}
public Date getEndTime() {
	return endTime;
}
public void setEndTime(Date endTime) {
	this.endTime = endTime;
}
public Double getMoney() {
	return money;
}
public void setMoney(Double money) {
	this.money = money;
}
public Date getSalTime() {
	return salTime;
}
public void setSalTime(Date salTime) {
	
	this.salTime = salTime;
}
public String getLoanname() {
	return loanname;
}
public void setLoanname(String loanname) {
	if (!StringUtils.isEmpty(loanname))
		this.loanname = "%"+loanname+"%";
	else
		this.loanname=null;
}
public Double getLoanrate() {
	return loanrate;
}
public void setLoanrate(Double loanrate) {
	this.loanrate = loanrate;
}
public String getLoanid() {
	return loanid;
}
public void setLoanid(String loanid) {
	if (!StringUtils.isEmpty(loanid))
		this.loanid = "%"+loanid+"%";
	else
		this.loanid=null;
}
public Double getInvestmoney() {
	return investmoney;
}
public void setInvestmoney(Double investmoney) {
	this.investmoney = investmoney;
}
public String getSafestatus() {
	return safestatus;
}
public void setSafestatus(String safestatus) {
	this.safestatus = safestatus;
}
public String getLoanstatus() {
	return loanstatus;
}
public void setLoanstatus(String loanstatus) {
	if(null!=loanstatus&&loanstatus.equals("2")){
		loanstatus="(2,3)";
	}else if(null!=loanstatus){
		loanstatus="("+loanstatus+")";
	}
	
	this.loanstatus = loanstatus;
}
public String getLoanusername() {
	return loanusername;
}
public void setLoanusername(String loanusername) {
	if (!StringUtils.isEmpty(loanusername))
		this.loanusername = "%"+loanusername+"%";
	else
		this.loanusername=null;
}
public String getInvestusername() {
	return investusername;
}
public void setInvestusername(String investusername) {
	if (!StringUtils.isEmpty(investusername))
		this.investusername = "%"+investusername+"%";
	else
		this.investusername=null;
}
public String getSafeusername() {
	return safeusername;
}
public void setSafeusername(String safeusername) {
	if (!StringUtils.isEmpty(safeusername))
		this.safeusername = "%"+safeusername+"%";
	else
		this.safeusername=null;
}
public Date getLoanrepayday() {
	
	
	
		
	return loanrepayday;
}
public void setLoanrepayday(Date loanrepayday) {

	this.loanrepayday = loanrepayday;

}

}

