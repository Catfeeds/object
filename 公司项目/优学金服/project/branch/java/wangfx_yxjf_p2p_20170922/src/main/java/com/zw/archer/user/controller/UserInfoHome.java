package com.zw.archer.user.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.primefaces.context.RequestContext;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.common.exception.AuthInfoAlreadyActivedException;
import com.zw.archer.common.exception.AuthInfoOutOfDateException;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.common.service.AuthService;
import com.zw.archer.common.service.impl.AuthInfoBO;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.exception.SendSMSException;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.archer.user.service.impl.UserBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.message.MessageConstants;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.message.service.impl.MessageBO;

import org.springframework.transaction.annotation.Transactional;

/**
 * Filename: UserHome.java Description: Copyright: Copyright (c)2013
 * Company:p2p
 * 
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-9 上午10:16:53
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-9 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class UserInfoHome extends EntityHome<User> implements Serializable {

	@Logger
	static Log log;
	
	/**
	 * 步骤
	 */
	private int step = 1;

	// 新密码
	private String newPassword;
	// 用于找回密码的邮箱
	private String findPwdEmail;

	//用户基本信息接口
	@Resource
	private AuthInfoBO authInfoBO;

	@Resource
	private UserService userService;
	//信息认证service，生成和验证认证信息。例如：手机短信认证，邮箱认证等
	@Resource
	private AuthService authService;

	//用户登录信息
	@Resource
	private LoginUserInfo loginUserInfo;
	
	@Resource
	private MessageBO messageBO;
	
	@Resource
	private UserBO userBO;
	
	//用于绑定的新邮箱
	private String newEmail;
	// 新手机号
	private String newMobileNumber;
	// 认证码
	private String authCode;

	@Resource
	private HibernateTemplate ht;

	@Resource
	private SmsService smsService;


	public String getFindPwdEmail() {
		return findPwdEmail;
	}
	public void setFindPwdEmail(String findPwdEmail) {
		this.findPwdEmail = findPwdEmail;
	}
	public String getNewMobileNumber() {
		return newMobileNumber;
	}
	public void setNewMobileNumber(String newMobileNumber) {
		this.newMobileNumber = newMobileNumber;
	}
	public String getNewEmail() {
		return newEmail;
	}
	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}
	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}


	// /////////////////////////////////////////通过邮箱找回密码--开始////////////////////////////////////

	/**
	 * 通过邮箱找回密码，第一步：发送认证码到用户邮箱
	 * 
	 * @return
	 */
	public String findPwdByEmail() {
		// 发送找回密码的邮件
		String result = null;
		String message = null;
		try {
			result = userService.sendFindLoginPasswordLinkEmail(findPwdEmail);
		} catch (UserNotFoundException e) {
			message = "对不起，邮箱验证失败！" + findPwdEmail + "尚未注册";
			FacesUtil.addErrorMessage(message);
			FacesUtil.getCurrentInstance().validationFailed();
			return null;
		}
		FacesUtil.setRequestAttribute("email", findPwdEmail);
		// 跳转到第二步，验证邮箱验证码
		if (!"true".equals(result)){
			message = "邮件发送失败，请稍后再试！";
			FacesUtil.addErrorMessage(message);
			return null;
		}
		return "pretty:findPwdByEmail2";
	}

	/**
	 * 通过邮箱找回密码，第二步：检验认证码
	 * 
	 * @return
	 */
	public boolean findPwdByEmail2(String findPwdEmailActiveCode) {
		try {
			User user = userService
					.verifyFindLoginPasswordActiveCode(findPwdEmailActiveCode);
			this.setFindPwdEmail(user.getEmail());
			return true;
		} catch (UserNotFoundException e) {
		} catch (NoMatchingObjectsException e) {
		} catch (AuthInfoOutOfDateException e) {
		} catch (AuthInfoAlreadyActivedException e) {
			
		}
		return false;
	}

	/**
	 * 根据邮箱找回密码，第三步：修改密码，指定新密码
	 * 
	 * @return
	 */
	public String findPwdByEmail3() {
		try {
			User user = userService.getUserByEmail(findPwdEmail);
			userService.modifyPassword(user.getId(), newPassword);
			authInfoBO.activate(user.getId(), findPwdEmail,
					CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL);
			FacesUtil.addInfoMessage("修改密码成功！");
			return "pretty:memberLogin";
		} catch (UserNotFoundException e) {
			String message = "对不起，邮箱验证失败！" + findPwdEmail + "尚未注册";
			FacesUtil.addErrorMessage(message);
			return null;
		}
	}

	/**
	 * 重新发送通过邮箱找回密码的验证码邮件
	 * 
	 * @return
	 */
	public void resendFindPwdEmail() {
		try {
			userService.sendFindLoginPasswordEmail(findPwdEmail);
			FacesUtil.addInfoMessage("邮件已发送");
		} catch (UserNotFoundException e) {
			String message = "对不起，邮箱验证失败！" + findPwdEmail + "尚未注册";
			FacesUtil.getCurrentInstance().validationFailed();
			FacesUtil.addErrorMessage(message);
		}
	}
///////////////////////////////////////////通过邮箱找回密码--结束////////////////////////////////////

	///////////////////////////////////////////通过手机找回密码--结束////////////////////////////////////
	/**
	 * 
	 * @param mobileNumber
	 */
	@Deprecated
	public void sentVerifyAuthCodeToMobile(String mobileNumber){
		User l = userBO.getUserByMobileNumber(mobileNumber);
		if(l == null ){
			FacesUtil.getCurrentInstance().validationFailed();
			FacesUtil.addErrorMessage("该手机号未注册");
			return;
		}
		this.setInstance(l);
		// 发送手机验证码
		Map<String, String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("authCode", authService.createAuthInfo(l.getId(), mobileNumber, null, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE).getAuthCode());
		messageBO.sendSMS(getBaseService().get(UserMessageTemplate.class, MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_MOBILE + "_sms"), params, mobileNumber);
	}
	
	/**
	 * 发送手机认证码
	 * @param mobileNumber
	 * @param jsCode 方法执行完以后要执行的js
	 */
	public void sentVerifyAuthCodeToMobile(String mobileNumber, String jsCode){
		User l = userBO.getUserByMobileNumber(mobileNumber);
		if(l == null ){
			FacesUtil.getCurrentInstance().validationFailed();
			FacesUtil.addErrorMessage("该手机号未注册");
			return;
		}
		this.setInstance(l);
		String content = ht.get(UserMessageTemplate.class, MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_MOBILE + "_sms").getTemplate();
		try {
			authCode = smsService.sendCM(content,mobileNumber);
		}catch (Exception e){
			log.error("手机验证码信息发送错误");
			FacesUtil.addErrorMessage("手机验证码信息发送失败,请和管理员联系");
			return;
		}
		authService.createAuthInfo(l.getId(), mobileNumber, authCode, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE);
		/*Map<String, String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("authCode", authService.createAuthInfo(l.getId(), mobileNumber, null, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE).getAuthCode());
		// 发送手机验证码
		messageBO.sendSMS(getBaseService().get(UserMessageTemplate.class, MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_MOBILE + "_sms"), params, mobileNumber);*/
		FacesUtil.addInfoMessage("验证码已经发送至手机！");
		RequestContext.getCurrentInstance().execute(jsCode);
	}
	
	public void findPwdByMobile1(){
		try {
			authService.verifyAuthInfo(getInstance().getId(), getInstance().getMobileNumber(),
					authCode,
					CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_MOBILE);
			this.step = 2;
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("验证码输入有误！");
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("验证码输入有误！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("验证码输入有误！");
		}
	}
	
	public String findPwdByMobile2() {
		try {
			User user = this.getInstance();
			userService.modifyPassword(user.getId(), newPassword);
			FacesUtil.addInfoMessage("修改密码成功！");
			if(StringUtils.isNotEmpty(super.getUpdateView())){
				return super.getUpdateView();
			}
			return "pretty:memberLogin";
		} catch (UserNotFoundException e) {
			String message = "对不起，手机验证失败！";
			FacesUtil.addErrorMessage(message);
			return null;
		}
	}
	
	
/////////////////////////////////修改绑定手机--开始////////////////////////////////////
	/**
	 * 更改绑定手机号第一步 给用户当前手机发送认证码
	 */
	@Deprecated
	public void sendCurrentBindingMobileNumberSMS() {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.sendChangeBindingMobileNumberSMS(user.getId(),
					user.getMobileNumber());
			FacesUtil.addInfoMessage("验证码已经发送至手机！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改绑定手机号第一步 给用户当前手机发送认证码
	 * @param jsCode 发送成功后，执行的js代码
	 */
	public void sendCurrentBindingMobileNumberSMS(String jsCode) {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.sendChangeBindingMobileNumberSMS(user.getId(),
					user.getMobileNumber());
			FacesUtil.addInfoMessage("验证码已经发送至手机！");
			RequestContext.getCurrentInstance().execute(jsCode);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改绑定手机号第一步 通过收到手机认证码验证用户当前手机
	 * 
	 * @return
	 */
	public void checkCurrentMobileNumber() {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			authService.verifyAuthInfo(user.getId(), user.getMobileNumber(),
					authCode,
					CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER);
			this.authCode = null;
			step = 2;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			
		}
	}

	/**
	 * 更改绑定手机号第3步 给新手机发送验证码
	 */
	public void sendNewBindingMobileNumber() {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.sendBindingMobileNumberSMS(user.getId(),
					newMobileNumber);
			FacesUtil.addInfoMessage("验证码已经发送至手机！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}

	/**
	 * 更改绑定手机号第3步 给新手机发送验证码
	 * @param jsCode 发送成功后，执行的js代码
	 */
	public void sendNewBindingMobileNumber(String jsCode) {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.sendBindingMobileNumberSMS(user.getId(),
					newMobileNumber);
			FacesUtil.addInfoMessage("验证码已经发送至手机！");
			RequestContext.getCurrentInstance().execute(jsCode);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}

	/**
	 * 更改绑定手机第4步 验证认证码并更改绑定手机
	 * 
	 * @return
	 */

	public String changeBindingMobileNumber() {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.bindingMobileNumber(user.getId(), newMobileNumber,
					authCode);
			// 给提示:修改绑定手机成功
			FacesUtil.addInfoMessage("绑定手机修改成功!");
			// 跳转到个人中心
			return "pretty:userCenter";
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("认证码已经使用！");
		}
		return null;
	}


///////////////////////////////////////////修改绑定手机--结束////////////////////////////////////
	
///////////////////////////////////////////通过手机找回交易密码--开始////////////////////////////////////
	/**
	 * 通过手机找回交易密码第一步 给用户当前手机发送认证码
	 * @param jsCode 发送成功后，执行的js代码
	 */

	public void sendFindCashPwdSMS(String jsCode) {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			userService.sendFindCashPwdSMS(user.getId(), user.getMobileNumber());
			step = 2;
			FacesUtil.addInfoMessage("验证码已经发送至手机！");
			RequestContext.getCurrentInstance().execute(jsCode);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}catch (SendSMSException e){
			FacesUtil.addErrorMessage("手机验证码信息发送失败,请和管理员联系");
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过手机找回交易密码第一步 通过收到手机认证码验证用户当前手机
	 * 
	 * @return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void checkFindCashPwdCode() {
		User user;
		try {
			user = userService.getUserById(loginUserInfo.getLoginUserId());
			authService.verifyAuthInfo(user.getId(), user.getMobileNumber(), authCode, CommonConstants.AuthInfoType.FIND_CASH_PASSWORD_BY_MOBILE);
			this.authCode = null;
			step = 3;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			
		}
	}
///////////////////////////////////////////通过手机找回交易密码--结束////////////////////////////////////
	
	
///////////////////////////////////////////修改绑定邮箱--开始////////////////////////////////////
	/**
	 *更改绑定邮箱第一步 给用户当前邮箱发送认证码
	 */
	@Deprecated
	public void sendCurrentBindingEmail(){
		try {
			User user=userService.getUserById(loginUserInfo.getLoginUserId());
			//发邮件(认证码)给原邮箱
			userService.sendChangeBindingEmail(user.getId(), user.getEmail());
			FacesUtil.addInfoMessage("验证码已经发送至邮箱！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	/**
	 *更改绑定邮箱第一步 给用户当前邮箱发送认证码
	 *@param jsCode 发送成功后执行的js代码
	 */
	public void sendCurrentBindingEmail(String jsCode){
		try {
			User user=userService.getUserById(loginUserInfo.getLoginUserId());
			//发邮件(认证码)给原邮箱
			String result = userService.sendChangeBindingEmail(user.getId(), user.getEmail());
			FacesUtil.addInfoMessage("验证码已经发送至邮箱！");
			if (!"true".equals(result)){
				FacesUtil.addErrorMessage("验证码发送失败！");
			}
			RequestContext.getCurrentInstance().execute(jsCode);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	/**
	 * 更改绑定邮箱第二步 通过收到邮件认证码验证用户当前邮箱
	 */
	public void checkCurrentEmail(){
			try {
				User user=userService.getUserById(loginUserInfo.getLoginUserId());
				//验证认证码        CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL(类型) 修改绑定邮箱
				authService.verifyAuthInfo(user.getId(), user.getEmail(), authCode, CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
				this.step = 2;
				this.authCode = null;
			} catch (UserNotFoundException e) {
				FacesUtil.addErrorMessage("用户未登录");
				e.printStackTrace();
			} catch (NoMatchingObjectsException e) {
				FacesUtil.addErrorMessage("输入验证码错误，请重新输入");
			} catch (AuthInfoOutOfDateException e) {
				FacesUtil.addErrorMessage("验证码已过期");
			} catch (AuthInfoAlreadyActivedException e) {
				FacesUtil.addErrorMessage("认证码已经使用！");
			}
	}
	
	
	/**
	 * 更改绑定邮箱第三步 给新邮箱发送验证码
	 */
	@Deprecated
	public void sendNewBindingEmail() {
		try {
			User user=userService.getUserById(loginUserInfo.getLoginUserId());
			//发送绑定新邮箱接口 、 新邮箱需要验证唯一性()    发邮件(认证码)给新邮箱
			userService.sendBindingEmail(user.getId(), newEmail);
			FacesUtil.addInfoMessage("验证码已经发送至新邮箱！");
			
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改绑定邮箱第三步 给新邮箱发送验证码
	 * @param jsCode 成功发送后执行的js代码
	 */
	public void sendNewBindingEmail(String jsCode) {
		try {
			//根据当前登录用户的id,获得当前用户
			User user=userService.getUserById(loginUserInfo.getLoginUserId());
			//发送绑定新邮箱接口 、 新邮箱需要验证唯一性()    发邮件(认证码)给新邮箱
			String result = userService.sendBindingEmail(user.getId(), newEmail);
			FacesUtil.addInfoMessage("验证码已经发送至新邮箱！");
			if (!"true".equals(result)){
				FacesUtil.addErrorMessage("验证码发送失败！");
			}
			RequestContext.getCurrentInstance().execute(jsCode);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改绑定邮箱第四步 验证认证码并更改绑定邮箱
	 * 
	 * @return
	 */
	public String changeBindingEmail() {
		try {
			User user=userService.getUserById(loginUserInfo.getLoginUserId());
			authService.verifyAuthInfo(user.getId(), newEmail, authCode,
					CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
			userService.bindingEmail(user.getId(), newEmail, authCode);
			FacesUtil.addInfoMessage("绑定新邮箱成功！");
			return "pretty:userCenter";
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("认证码已经使用！");
		}
		return null;
	}
///////////////////////////////////////////修改绑定邮箱--结束////////////////////////////////////
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	
}
	