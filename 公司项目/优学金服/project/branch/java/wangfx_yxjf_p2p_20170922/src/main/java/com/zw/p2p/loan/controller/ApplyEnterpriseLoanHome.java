package com.zw.p2p.loan.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.loan.model.ApplyEnterpriseLoan;
import com.zw.p2p.loan.service.LoanService;

/**
 * Description:企业借款申请Home Copyright: Copyright (c)2013 Company: p2p
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-11 下午4:28:49
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 wangzhi 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class ApplyEnterpriseLoanHome extends EntityHome<ApplyEnterpriseLoan> implements java.io.Serializable {
	@Resource
	LoginUserInfo loginUserInfo;

	@Resource
	LoanService loanService;

	/**
	 * 企业借款申请
	 */
	@Override
	public String save() {
		//获取登录用户
		User loginUser = null;
		if (null!=loginUserInfo&&null!=loginUserInfo.getLoginUserId()){
			loginUser=getBaseService().get(User.class, loginUserInfo.getLoginUserId());
		}
		if(null!=loginUser){
			this.getInstance().setUser(loginUser);
		}else{
			this.getInstance().setUser(null);
		}
		this.getInstance().setUser(loginUser);
		loanService.applyEnterpriseLoan(this.getInstance());
		FacesUtil.addInfoMessage("您的融资申请已经提交，我们会尽快与您联系！");
		if (loginUser == null) {
//			FacesUtil.addErrorMessage("请先登陆");
//			return FacesUtil.redirect("/");
			return "pretty:home";
		}
		return "pretty:user_loan_applying-p2c";
	}

	/**
	 * 审核企业借款
	 */
	public void verify(ApplyEnterpriseLoan ael) {
		loanService.verifyEnterpriseLoan(ael);
	}
	
	@Override
	public Class<ApplyEnterpriseLoan> getEntityClass() {
		return ApplyEnterpriseLoan.class;
	}

}
