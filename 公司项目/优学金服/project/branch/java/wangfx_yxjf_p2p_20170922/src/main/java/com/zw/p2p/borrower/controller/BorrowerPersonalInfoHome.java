package com.zw.p2p.borrower.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.borrower.BorrowerConstant;
import com.zw.p2p.borrower.model.BorrowerPersonalInfo;
import com.zw.p2p.borrower.service.BorrowerService;

/**  
 * Description:   
 * Copyright:   Copyright (c)2013 
 * Company:    p2p 
 * @author:     yinjunlu  
 * @version:    1.0  
 * Create at:   2014-1-21 下午5:25:43  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014-1-21    yinjunlu             1.0        1.0 Version  
 */
@Component
@Scope(ScopeType.VIEW)
public class BorrowerPersonalInfoHome extends EntityHome<BorrowerPersonalInfo> {
	private boolean ispass = false;
	private String verifyMessage;
	@Resource
	private BorrowerService borrowService;
	@Resource
	private LoginUserInfo loginUser;
	
	@Override
	protected BorrowerPersonalInfo createInstance() {
		BorrowerPersonalInfo personInfo = new BorrowerPersonalInfo();
		try {
			personInfo = borrowService.initBorrowerPersonalInfo(loginUser.getLoginUserId());
		} catch (UserNotFoundException e) {
			e.printStackTrace();
		}
		return personInfo;
	}

	@Override
	public String save() {
//		getInstance().setUserId(loginUser.getLoginUserId());
		//保存BorrowerPersonalInfo，借款人普通信息
		borrowService.saveOrUpdateBorrowerPersonalInfo(getInstance());
//		FacesUtil.addInfoMessage("保存成功，请填写“工作及财务信息”。");
		return "pretty:loanerAdditionInfo";
	}

	/**
	 * 审核借款用户个人基本信息
	 * @return
	 */
	public String verify(BorrowerPersonalInfo borrowerPersonalInfo) {
		borrowService.verifyBorrowerPersonalInfo(borrowerPersonalInfo.getUserId() , ispass,
				borrowerPersonalInfo.getVerifiedMessage(), loginUser.getLoginUserId());
		FacesUtil.addInfoMessage("保存成功");
		return null;
	}

	public void initVerify(BorrowerPersonalInfo borrowerPersonalInfo){
		this.setInstance(borrowerPersonalInfo);
		if((BorrowerConstant.Verify.passed).equals(this.getInstance().getVerified())){
			ispass = true;
		}
	}
	
	public boolean isIspass() {
		return ispass;
	}

	public void setIspass(boolean ispass) {
		this.ispass = ispass;
	}

	public String getVerifyMessage() {
		return verifyMessage;
	}

	public void setVerifyMessage(String verifyMessage) {
		this.verifyMessage = verifyMessage;
	}

}
