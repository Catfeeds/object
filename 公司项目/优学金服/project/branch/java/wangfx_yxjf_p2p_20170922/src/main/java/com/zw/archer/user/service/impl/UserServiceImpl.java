package com.zw.archer.user.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.exception.AuthInfoAlreadyActivedException;
import com.zw.archer.common.exception.AuthInfoOutOfDateException;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.common.service.AuthService;
import com.zw.archer.common.service.impl.AuthInfoBO;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.invite.InviteService;
import com.zw.archer.openauth.OpenAuthConstants;
import com.zw.archer.openauth.service.OpenAuthService;
import com.zw.archer.system.SystemConstants;
import com.zw.archer.system.model.MotionTracking;
import com.zw.archer.system.service.SpringSecurityService;
import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserConstants.CashISUse;
import com.zw.archer.user.exception.ConfigNotFoundException;
import com.zw.archer.user.exception.NotConformRuleException;
import com.zw.archer.user.exception.RoleNotFoundException;
import com.zw.archer.user.exception.SendSMSException;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.RegistCash;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserInfoService;
import com.zw.archer.user.service.UserService;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.jsf.util.RestUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.HashCrypt;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.controller.CouponList;
import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.service.CouponListService;
import com.zw.p2p.coupons.service.CouponRuleListService;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.message.MessageConstants;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.MailService;
import com.zw.p2p.message.service.MessageService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.message.service.impl.MessageBO;
import com.zw.p2p.rateticket.RateTicketConstants;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.model.RateTicketRule;
import com.zw.p2p.rateticket.service.RateTicketListService;
import com.zw.p2p.rateticket.service.RateTicketRuleListService;
import com.zw.p2p.rateticket.service.RateTicketService;

/**
 * <p>
 * Title: UserServiceImpl.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: p2p
 * </p>
 *
 * @author wangzhi
 * @date 2014-1-3
 * @version 1.0
 */
@Service("userService")
public class UserServiceImpl implements UserService {
	@Resource
	private HibernateTemplate ht;

	@Resource
	private AuthService authService;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private UserBO userBO;

	@Resource(name = "qqOpenAuthService")
	private OpenAuthService qqOAS;

	@Resource(name = "sinaWeiboOpenAuthService")
	private OpenAuthService sinaWeiboOAS;

	@Resource
	private MessageBO messageBO;

	@Resource
	private AuthInfoBO authInfoBO;

	@Resource
	private SpringSecurityService springSecurityService;

	@Resource
	private MessageService messageService;

	@Resource
	private SmsService smsService;

	@Resource
	private MailService mailService;
	
	@Resource
	private ConfigService configService;

	private String authCode;

	@Logger
	Log log;

	@Resource
	private UserBillBO userBillBO;
	@Resource
	CouponListService couponListService;

	@Resource
	private AutoMsgService autoMsgService;
	
	@Resource
	private InviteService inviteService;

	@Resource
	private CouponSService couponSService;
	
	@Resource
	private CouponRuleListService couponRuleListService;
	
	@Resource
	private RateTicketRuleListService rateTicketRuleService;
	
	@Resource
	private RateTicketService rateTicketService;

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void registerByMobileNumber(User user, String authCode)
			throws NoMatchingObjectsException, AuthInfoOutOfDateException,
			AuthInfoAlreadyActivedException {
		//验证手机认证码是否正确
		authService.verifyAuthInfo(null, user.getMobileNumber(), authCode,
				CommonConstants.AuthInfoType.REGISTER_BY_MOBILE_NUMBER);
		user.setRegisterTime(new Date());
		//用户密码通过sha加密
		user.setPassword(HashCrypt.getDigestHash(user.getPassword()));
		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		user.setStatus(UserConstants.UserStatus.ENABLE);
		userBO.save(user);
		// 添加普通用户权限
		Role role = new Role("MEMBER");
		userBO.addRole(user, role);
	}

	/*

	 */

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void register(User user) {
		// FIXME:缺验证
		user.setRegisterTime(new Date());
		user.setPassword(HashCrypt.getDigestHash(user.getPassword()));
		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		user.setStatus(UserConstants.UserStatus.ENABLE);
		// /////////
		// user.setStatus(UserConstants.UserStatus.NOACTIVE);
		userBO.save(user);
		// 添加普通用户权限
		Role role = new Role("MEMBER");
		// 添加inactive角色（inactive role has inactive permission）
		Role role2 = new Role("INACTIVE");
		userBO.addRole(user, role2);
		// //////////
		userBO.addRole(user, role);
		sendActiveEmail(
				user,
				authService.createAuthInfo(user.getId(), user.getEmail(), null,
						CommonConstants.AuthInfoType.ACTIVATE_USER_BY_EMAIL)
						.getAuthCode());
	}

	private void sendActiveEmail(User user, String authCode) {
		final String email = user.getEmail();
		// 发送账号激活邮件
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", user.getUsername());
		params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		String activeCode = email + "&" + authCode;
		// base64编码
		activeCode = Base64.encodeBase64URLSafeString(activeCode.getBytes());
		String activeLink = FacesUtil.getCurrentAppUrl()
				+ "/activateAccount?activeCode=" + activeCode;
		params.put("active_url", activeLink);
		messageBO.sendEmail(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.REGISTER_ACTIVE + "_email"),
				params, email);
	}

	@Override
	public void sendActiveEmail(String userId, String authCode)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("userId:" + userId);
		}
		sendActiveEmail(user, authCode);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public User verifyFindLoginPasswordActiveCode(String activeCode)
			throws UserNotFoundException, NoMatchingObjectsException,
			AuthInfoOutOfDateException, AuthInfoAlreadyActivedException {
		// base64解码
		activeCode = new String(Base64.decodeBase64(activeCode));
		String[] aCodes = activeCode.split("&");
		if (aCodes.length != 2) {
			throw new UserNotFoundException("activeCode has error" + activeCode);
		}
		String email = aCodes[0];
		String code = aCodes[1];

		User user = getUserByEmail(email);
		authService.verifyAuthInfo(user.getId(), email, code,
				CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL);
		return user;
	}

	// ///////////////////
	/**
	 * 再次发送激活邮件
	 *
	 * @author wangxiao 5-6
	 * @param user
	 * @param
	 */
	@Override
	public void sendActiveEmailAgain(User user) {
		sendActiveEmail(
				user,
				authService.createAuthInfo(user.getId(), user.getEmail(), authCode,
						CommonConstants.AuthInfoType.ACTIVATE_USER_BY_EMAIL)
						.getAuthCode());
	}

	// ////////////////////

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void activateUserByEmailActiveCode(String activeCode)
			throws UserNotFoundException, NoMatchingObjectsException,
			AuthInfoOutOfDateException, AuthInfoAlreadyActivedException {
		// base64解码
		activeCode = new String(Base64.decodeBase64(activeCode));
		String[] aCodes = activeCode.split("&");
		if (aCodes.length != 2) {
			throw new UserNotFoundException("activeCode has error" + activeCode);
		}
		String email = aCodes[0];
		String code = aCodes[1];

		User user = userBO.getUserByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("user.email:" + email);
		}
		authService.verifyAuthInfo(user.getId(), email, code,
				CommonConstants.AuthInfoType.ACTIVATE_USER_BY_EMAIL);
		userBO.enableUser(user);
		// 去掉inactive角色（inactive role has inactive permission）
		Role role = new Role("INACTIVE");
		userBO.removeRole(user, role);
		// /////////////////
		authInfoBO.activate(user.getId(), email,
				CommonConstants.AuthInfoType.ACTIVATE_USER_BY_EMAIL);
		// 刷新用户权限
		springSecurityService.refreshLoginUserAuthorities(user.getId());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void disableUser(String userId) throws UserNotFoundException {
		try {
			changeUserStatus(userId, CommonConstants.DISABLE);
		} catch (ConfigNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void enableUser(String userId) throws UserNotFoundException {
		try {
			changeUserStatus(userId, CommonConstants.ENABLE);
		} catch (ConfigNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void changeUserStatus(String userId, String status)
			throws UserNotFoundException, ConfigNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("userId:" + userId);
		}
		user.setStatus(status);
		userBO.update(user);
	}

	@Override
	public boolean verifyPasswordRule(String password)
			throws NotConformRuleException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verifyUsernameRule(String username)
			throws NotConformRuleException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void addRole(String userId, String roleId)
			throws UserNotFoundException, RoleNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("userId:" + userId);
		}
		Role role = ht.get(Role.class, roleId);
		if (role == null) {
			throw new RoleNotFoundException("roleId:" + roleId);
		}
		userBO.addRole(user, role);
	}

	/**
	 * 修改登录密码
	 * 密码通过sha加密
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void modifyPassword(String userId, String newPassword)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("id:" + userId);
		}
		//用户密码通过sha加密保存
		user.setPassword(HashCrypt.getDigestHash(newPassword));
		userBO.update(user);
	}

	/**
	 * 修改交易密码
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void modifyCashPassword(String userId, String newCashPassword)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("id:" + userId);
		}
		//密码用sha加密
		String cashPwd=HashCrypt.getDigestHash(newCashPassword);
//		String pass1=HashCrypt.getDigestHash("123abc");
//		user.setCashPassword(HashCrypt.getDigestHash(newCashPassword));
		user.setCashPassword(cashPwd);
		userBO.updateForCashPwd(user);
	}

	@Override
	public void resetPassword(String userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean verifyOldPassword(String userId, String oldPassword)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("id:" + userId);
		}
		//用户密码是通过加密保存的
		if (user.getPassword().equals(HashCrypt.getDigestHash(oldPassword))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean verifyOldCashPassword(String userId, String oldCashPassword)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("id:" + userId);
		}
		if (user.getCashPassword() != null
				&& user.getCashPassword().equals(
				HashCrypt.getDigestHash(oldCashPassword))) {
			return true;
		}
		return false;
	}

	@Override
	public void sendFindLoginPasswordEmail(String email)
			throws UserNotFoundException {
		User user = userBO.getUserByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("user.email:" + email);
		}
		// 发送登录密码找回邮件
		Map<String, String> params = new HashMap<String, String>();
		//String content = ht.get(UserMessageTemplate.class, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL + "_email").getTemplate();
		authCode = RandomStringUtils.random(6, false, true);
		authService.createAuthInfo(user.getId(), email, authCode, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL);
		params.put("authCode",authCode);
		messageBO.sendEmail(ht.get(UserMessageTemplate.class,CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL
				+ "_email"), params, email);
	}

	@Override
	public String sendFindLoginPasswordLinkEmail(String email)
			throws UserNotFoundException {
		User user = userBO.getUserByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("user.email:" + email);
		}
		// 发送登录密码找回邮件
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", user.getUsername());
		params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		//生成认证码
		authCode = RandomStringUtils.random(6, false, true);
		authService.createAuthInfo(user.getId(), email, authCode, CommonConstants.AuthInfoType.FIND_LOGIN_PASSWORD_BY_EMAIL);
		String activeCode = email + "&" + authCode;
		// base64编码
		activeCode = Base64.encodeBase64URLSafeString(activeCode.getBytes());
		String resetPasswrodUrl = FacesUtil.getCurrentAppUrl()
				+ "/find_pwd_by_email3/" + activeCode;
		params.put("reset_password_url", resetPasswrodUrl);
		//发送邮件
		UserMessageTemplate umt = ht.get(UserMessageTemplate.class,MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_EMAIL+ "_email");
		String content = messageBO.replaceParams(umt,params);
		String result = null;
		try {

			result = mailService.getResult(email,umt.getName(),content);
		}catch (Exception e){
			log.error("找回密码邮件发送错误");
		}
		return result;
		/*messageBO.sendEmail(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.FIND_LOGIN_PASSWORD_BY_EMAIL
						+ "_email"), params, email);*/
	}

	@Override
	public String sendBindingEmail(String userId, String email)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		// 发送绑定邮箱确认邮件
		Map<String, String> params = new HashMap<String, String>();
		// TODO:实现模板
		params.put("username", user.getUsername());
		authCode = RandomStringUtils.random(6, false, true);
		authService.createAuthInfo(userId, email, authCode, CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
		params.put("username", user.getUsername());
		params.put("authCode", authCode);
		UserMessageTemplate umt = ht.get(UserMessageTemplate.class,MessageConstants.UserMessageNodeId.CHANGE_BINDING_EMAIL+ "_email");
		String content = messageBO.replaceParams(umt,params);
		String result = null;
		try {

			result = mailService.getResult(email,umt.getName(),content);
		}catch (Exception e){
			log.error("绑定邮箱确认邮件发送错误");
		}
		return result;
		/*params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put(
				"authCode",
				authService.createAuthInfo(userId, email, authCode,
						CommonConstants.AuthInfoType.BINDING_EMAIL)
						.getAuthCode());
		messageBO.sendEmail(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.BINDING_EMAIL + "_email"),
				params, email);*/
	}

	/**
	 * 发送认证码邮件指定邮箱
	 */
	@Override
	public String sendChangeBindingEmail(String userId, String oriEmail)
			throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		Map<String, String> params = new HashMap<String, String>();
		// TODO:实现模板
		authCode = RandomStringUtils.random(6, false, true);
		authService.createAuthInfo(userId, oriEmail, authCode, CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
		params.put("username", user.getUsername());
		params.put("authCode", authCode);
		UserMessageTemplate umt = ht.get(UserMessageTemplate.class,MessageConstants.UserMessageNodeId.CHANGE_BINDING_EMAIL+ "_email");
		String content = messageBO.replaceParams(umt,params);
		String result = null;
		try {

			result = mailService.getResult(oriEmail,umt.getName(),content);
		}catch (Exception e){
			log.error("邮箱验证码信息发送错误");
		}
		return result;
		/*params.put("username", user.getUsername());
		params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put(
				"authCode",
				authService.createAuthInfo(userId, oriEmail, authCode,
						CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL)
						.getAuthCode());
		messageBO.sendEmail(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.CHANGE_BINDING_EMAIL
						+ "_email"), params, oriEmail);*/
	}

	/**
	 * 绑定邮箱
	 * @param userId 用户id
	 * @param email 邮箱
	 * @param authCode 认证码
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void bindingEmail(String userId, String email, String authCode)
			throws UserNotFoundException, NoMatchingObjectsException,
			AuthInfoOutOfDateException, AuthInfoAlreadyActivedException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		//如果认证码输入正确，更改此认证码的状态为已激活
		authInfoBO.activate(userId, email,
				CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
		user.setEmail(email);
		userBO.update(user);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void bindingMobileNumber(String userId, String mobileNumber,
									String authCode) throws UserNotFoundException,
			NoMatchingObjectsException, AuthInfoOutOfDateException,
			AuthInfoAlreadyActivedException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		authService.verifyAuthInfo(userId, mobileNumber, authCode,
				CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER);
		authInfoBO.activate(userId, mobileNumber,
				CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER);
		//变更邀请关系
		inviteService.updateMobile(user.getMobileNumber(), mobileNumber);
		user.setMobileNumber(mobileNumber);
		userBO.update(user);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void realNameCertification(User user, String authCode)
			throws NoMatchingObjectsException, AuthInfoOutOfDateException,
			AuthInfoAlreadyActivedException {
		// FIXME:缺验证
//		verifyIdCard(user);

		authService.verifyAuthInfo(user.getId(), user.getMobileNumber(),
				authCode, CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER);
		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		userBO.update(user);
		userBO.addRole(user, new Role("INVESTOR"));

		// 刷新登录用户权限
		springSecurityService.refreshLoginUserAuthorities(user.getId());
	}

	/**
	 * 通过邮箱进行实名认证
	 *
	 * @throws AuthInfoAlreadyActivedException
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void realNameCertificationByEmail(User user, String authCode)
			throws NoMatchingObjectsException, AuthInfoOutOfDateException,
			AuthInfoAlreadyActivedException {
		// FIXME:缺验证
//		verifyIdCard(user);

		authService.verifyAuthInfo(user.getId(), user.getEmail(), authCode,
				CommonConstants.AuthInfoType.BINDING_EMAIL);
		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		userBO.update(user);
		userBO.addRole(user, new Role("INVESTOR"));

		// 刷新登录用户权限
		springSecurityService.refreshLoginUserAuthorities(user.getId());
	}

	private void verifyIdCard(User user) throws NoMatchingObjectsException{
		String idCard=user.getIdCard();

//		int sexCode;//性别码
//		String strBirthday= DateUtil.getDate(user.getBirthday()).replace("-","");
//		boolean birthdayMatch;

//		if (idCard.length()==15) {
//			sexCode = Integer.valueOf(idCard.substring(14));
//			birthdayMatch= idCard.substring(6, 12).equals(strBirthday);
//		}else {
//			sexCode = Integer.valueOf(idCard.substring(16, 17));
//			birthdayMatch= idCard.substring(6, 14).equals(strBirthday);
//		}
//		if (sexCode % 2==1){
//			if (!user.getSex().equals("M"))
//				throw new NoMatchingObjectsException(this.getClass(),"身份证号码和性别不匹配");
//		}else{
//			if (!user.getSex().equals("F"))
//				throw new NoMatchingObjectsException(this.getClass(),"身份证号码和性别不匹配");
//		}
//
//		if (!birthdayMatch){
//			throw new NoMatchingObjectsException(this.getClass(),"身份证号码和出生日期不匹配");
//		}

		String name=user.getRealname();
		try {
			name = com.zw.archer.util.Base64.encodeBytes(name.getBytes("UTF-8"));
		}catch (Exception e){
			e.printStackTrace();
		}

		String appId = props.getProperty("appId");
		NameValuePair[] data = {
				new NameValuePair("appId",appId),
				new NameValuePair("idCardNo",idCard),
				new NameValuePair("name",name)
		};
		String url = props.getProperty("restAddr")+props.getProperty("nciicCheck");
		String restData = RestUtil.getRestData(url, data);

		if (restData==null){
			throw new NoMatchingObjectsException(this.getClass(),"实名认证服务错误，请联系管理员");
		}else{
			JSONObject jsonObject= JSONObject.fromObject(restData);
			if (!jsonObject.getBoolean("xm")){
				throw new NoMatchingObjectsException(this.getClass(),"实名认证失败!");
			}
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void realNameCertification(User user) throws NoMatchingObjectsException{
		verifyIdCard(user);

		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		String idCard=user.getIdCard();
		Date birthday=null;
		String birthdayStr="";
		Integer sexCode ;
		if (idCard.length()==15) {
			sexCode = Integer.valueOf(idCard.substring(14));
			birthdayStr= idCard.substring(6, 12);
			Integer birthYear=Integer.valueOf(idCard.substring(6,8));
			if(birthYear<10){
				birthdayStr="20"+birthdayStr;
			}else {
				birthdayStr="19"+birthdayStr;
			}
		}else {
			sexCode = Integer.valueOf(idCard.substring(16, 17));
			birthdayStr= idCard.substring(6, 14);
		}
		if (sexCode % 2==1){
			user.setSex("M");;
		}else{
			user.setSex("F");
		}
		birthday=DateUtil.StringToDate(birthdayStr);
		user.setBirthday(birthday);
		userBO.update(user);
		userBO.addRole(user, new Role("INVESTOR"));

		// 刷新登录用户权限
		springSecurityService.refreshLoginUserAuthorities(user.getId());
	}

	@Override
	public void sendBindingMobileNumberSMS(String userId, String mobileNumber)
			throws UserNotFoundException {
		// FIXME:验证手机号码的合法性
		// 发送手机验证码
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		String content = ht.get(UserMessageTemplate.class,CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER+ "_sms").getTemplate();
		try {

			authCode = smsService.sendCM(content,mobileNumber);
		}catch (Exception e){
			log.error("短信验证码发送错误");
			FacesUtil.addErrorMessage("手机验证码信息发送失败,请和管理员联系");
			return;
		}
		authService.createAuthInfo(userId, mobileNumber, authCode,CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER);
		/*Map<String, String> params = new HashMap<String, String>();
		// TODO:实现模板
		params.put("username", user.getUsername());
		params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put(
				"authCode",
				authService.createAuthInfo(userId, mobileNumber, authCode,
						CommonConstants.AuthInfoType.BINDING_MOBILE_NUMBER)
						.getAuthCode());
		messageBO.sendSMS(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.BINDING_MOBILE_NUMBER
						+ "_sms"), params, mobileNumber);*/
	}
	private static Properties props = new Properties();
	static{
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("url.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void sendRegisterByMobileNumberSMS(String mobileNumber) {
		// FIXME:验证手机号码的合法性
		// 发送手机验证码
		Map<String, String> params = new HashMap<String, String>();
		// TODO:实现模板
		String content = ht.get(UserMessageTemplate.class,CommonConstants.AuthInfoType.REGISTER_BY_MOBILE_NUMBER+ "_sms").getTemplate();
		try {

			authCode = smsService.sendCM(content,mobileNumber);
		}catch (Exception e){
			log.error("短信注册验证码发送错误");
			FacesUtil.addErrorMessage("手机验证码信息发送失败,请和管理员联系");
			return;
		}
		authService.createAuthInfo(null, mobileNumber, authCode,CommonConstants.AuthInfoType.REGISTER_BY_MOBILE_NUMBER);
		/*UserMessageTemplate umt =  ht.get(UserMessageTemplate.class,
						MessageConstants.UserMessageNodeId.REGISTER_BY_MOBILE_NUMBER+ "_sms");
				String appId = props.getProperty("appId");
				String senderTitle = props.getProperty("senderTitle");
				String content = umt.getTemplate();
				NameValuePair[] data = {
					new NameValuePair("appId",appId),
					new NameValuePair("senderTitle",senderTitle),
					new NameValuePair("content",content),
					new NameValuePair("receiver",mobileNumber)
				};
				String url = "http://localhost:8080/messagecenter/rest/sendCaptchaMessage";*/
//				RestUtil.post(url,data);

	}

	@Override
	public void sendChangeBindingMobileNumberSMS(String userId,
												 String oriMobileNumber) throws UserNotFoundException {
		String content = ht.get(UserMessageTemplate.class,CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER+ "_sms").getTemplate();
		try {

			authCode = smsService.sendCM(content,oriMobileNumber);
		}catch (Exception e){
			log.error("修改绑定手机信息发送错误");
			FacesUtil.addErrorMessage("手机验证码信息发送失败,请和管理员联系");
			return;
		}
		authService.createAuthInfo(userId, oriMobileNumber, authCode,CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER);
	    /*
	    //FIXME:验证手机号码的合法性
		// 发送手机验证码
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		Map<String, String> params = new HashMap<String, String>();
		//TODO:实现模板
		params.put("username", user.getUsername());
		params.put("time", DateUtil.DateToString(new Date(),
				DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put(
				"authCode",
				authService
						.createAuthInfo(
								userId,
								oriMobileNumber,
								null,
								CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER)
						.getAuthCode());
		messageBO.sendSMS(ht.get(UserMessageTemplate.class,
				MessageConstants.UserMessageNodeId.CHANGE_BINDING_MOBILE_NUMBER
						+ "_sms"), params, oriMobileNumber);*/
	}

	/**
	 * 根据邮箱查找用户
	 */
	@Override
	public User getUserByEmail(String email) throws UserNotFoundException {
		User user = userBO.getUserByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("user.email:" + email);
		}
		return user;
	}

	/**
	 * 根据手机号查找用户
	 */
	@Override
	public User getUserByMobileNumber(String mobileNumber)
			throws UserNotFoundException {
		User user = userBO.getUserByMobileNumber(mobileNumber);
		if (user == null) {
			throw new UserNotFoundException("user.mobileNumber:" + mobileNumber);
		}
		return user;
	}
	/**
	 * 根据用户id查找用户
	 */
	@Override
	public User getUserById(String userId) throws UserNotFoundException {
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		return user;
	}
	@Override
	public User getUserByIdCard(String idcard)  {
		String hql="from User where idCard=?";
		List<User> user = ht.find(hql, idcard);
		if (user == null||user.size()==0) {
			return null;
		}
		return user.get(0);
	}
	/**
	 * 根据汇付用户客户号查找用户
	 */
	@Override
	public User getUserByUsrCustId(String srCustId) throws UserNotFoundException {
		String hql="from User where usrCustId=?";
		List<User> user = ht.find(hql, srCustId);
		if (user == null||user.size()==0) {
			throw new UserNotFoundException("user.id:" + srCustId);
		}
		return user.get(0);
	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void registerByOpenAuth(User user) {
		register(user);

		// 第三方首次登录，绑定已有账号
		Object openId = FacesUtil
				.getSessionAttribute(OpenAuthConstants.OPEN_ID_SESSION_KEY);
		Object openAutyType = FacesUtil
				.getSessionAttribute(OpenAuthConstants.OPEN_AUTH_TYPE_SESSION_KEY);
		Object accessToken = FacesUtil
				.getSessionAttribute(OpenAuthConstants.ACCESS_TOKEN_SESSION_KEY);
		if (openId != null && openAutyType != null && accessToken != null) {
			if (OpenAuthConstants.Type.QQ.equals((String) openAutyType)) {
				qqOAS.binding(user.getId(), (String) openId,
						(String) accessToken);
			} else if (OpenAuthConstants.Type.SINA_WEIBO
					.equals((String) openAutyType)) {
				sinaWeiboOAS.binding(user.getId(), (String) openId,
						(String) accessToken);
			}
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void createBorrowerByAdmin(User user) {
		// FIXME:缺验证，这里应该同时创建borrowerInfo，或者该方法应该放在borrowerInfoService中。
		user.setRegisterTime(new Date());
		user.setPassword(HashCrypt.getDigestHash(user.getPassword()));
		user.setStatus(UserConstants.UserStatus.ENABLE);
		userBO.save(user);
		// 添加普通用户权限
		userBO.addRole(user, new Role("MEMBER"));
		// 添加借款者权限
		userBO.addRole(user, new Role("LONAER"));
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void register(User user, String referrer) {
		register(user);
		saveReferrerInfo(user.getId(), referrer);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void registerByMobileNumber(User user, String authCode,
									   String referrer) throws NoMatchingObjectsException,
			AuthInfoOutOfDateException, AuthInfoAlreadyActivedException,Exception{
		/**
		 * 通过手机号查找用户名，如果找到则用找到的用户名,如果找不到则不用修改
		 * by lijin,2015-04-08
		 */
		if(null==user.getUsername()||user.getUsername().length()==0){
			user.setUsername(user.getMobileNumber());
		}
		String referId= referrer;
		try {
			if (StringUtils.isNotEmpty(referId)) {
				User user1 = getUserByMobileNumber(referrer);
				referId = user1.getId();
			}
		}catch (Exception e){
			//忽略错误
		}

		user.setReferrerId(referId);
		//registerByMobileNumber(user, authCode);
		//验证手机认证码是否正确
		authService.verifyAuthInfo(null, user.getMobileNumber(), authCode,
				CommonConstants.AuthInfoType.REGISTER_BY_MOBILE_NUMBER);
		user.setRegisterTime(new Date());
		//用户密码通过sha加密
		user.setPassword(HashCrypt.getDigestHash(user.getPassword()));
		user.setCashPassword(HashCrypt.getDigestHash(user.getCashPassword()));
		user.setStatus(UserConstants.UserStatus.ENABLE);
		if (StringUtils.isNotEmpty(referrer))
			user.setReferrer(referrer);
		userBO.save(user);
		// 添加普通用户权限
		Role role = new Role("MEMBER");
		userBO.addRole(user, role);
		//userBO.sendCoupons(user);
		saveReferrerInfo(user.getId(), referrer);
		/*userBillBO.transferIntoCoupon(user,
				couponList.getUnusedCoupons(user.getId()), UserBillConstants.OperatorInfo.COUPON,"赠送注册红包");*/

		/*List<UserMessageTemplate> umts = autoMsgService.getUmts();
		messageService.saveUserMessages(user.getUsername(),umts);*/
		
		String registSendTime = configService.getConfigValue("registerCreateTime");//每种红包的发放开始日期和结束日期可单独配置
		String registerEndTime = configService.getConfigValue("registerEndTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		if(now.compareTo(sdf.parse(registSendTime))>0 && sdf.parse(registerEndTime).compareTo(now)>0){
			List<CouponRule> zhuceRule=couponRuleListService.listByType(CouponConstants.RESOURCETYPE.zhuce.getName());
			for (CouponRule couponRule : zhuceRule) {
				couponSService.giveCouponToUser(couponRule, user);
			}
			
		}
		String registSendTime1 = configService.getConfigValue("rateticketRegisterCreateTime");//每种红包的发放开始日期和结束日期可单独配置
		String registerEndTime1 = configService.getConfigValue("rateticketRegisterInvestEndTime");
		if(now.compareTo(sdf.parse(registSendTime1))>0 && sdf.parse(registerEndTime1).compareTo(now)>0){
			List<RateTicketRule> rateTicketRule=rateTicketRuleService.listByType(RateTicketConstants.RESOURCETYPE.zhuce.getName());
			for (RateTicketRule rateTicketRule2 : rateTicketRule) {
				rateTicketService.giveRateTicketToUser(rateTicketRule2, user);
			}
		}
		//赠送体验金
		RegistCash cash =new RegistCash();
		cash.setPk_user(user.getId());
		String size = configService.getConfigValue("regestcash_size");
		String timeout = configService.getConfigValue("registcash_outtime");
		double cashsize = Double.parseDouble(size);
		int cashtime = Integer.parseInt(timeout);
		cash.setCash(cashsize);
		cash.setStarttime(new Date());
		cash.setEndtime(DateUtil.addDay(new Date(), cashtime));
		cash.setIsused(CashISUse.NOTUSE);
		this.userBO.ht.save(cash);
	}

	/**
	 * 保存注册时候的推荐人信息
	 *
	 * @param userId
	 * @param referrer
	 */
	private void saveReferrerInfo(String userId, String referrer) {
		if (StringUtils.isNotEmpty(referrer)) {
			// 保存推荐人信息
			MotionTracking mt = new MotionTracking();
			mt.setId(IdGenerator.randomUUID());
			mt.setFromWhere(referrer.trim());
			mt.setFromTime(new Date());
			mt.setFromType(SystemConstants.MotionTrackingConstants.FromType.REFERRER);
			mt.setWho(userId);
			mt.setWhoType(SystemConstants.MotionTrackingConstants.WhoType.USER);
			mt.setActionType(SystemConstants.MotionTrackingConstants.ActionType.REGISTER);
			ht.save(mt);
		}
	}

	@Override
	public boolean hasRole(String userId, String roleId) {
		String hql = "select user from User user left join user.roles r where user.id=? and r.id = ?";
		return ht.find(hql, new String[] { userId, roleId }).size() > 0;
	}

	@Override
	public void sendFindCashPwdSMS(String userId, String mobileNumber) throws SendSMSException {
		String content = ht.get(UserMessageTemplate.class, CommonConstants.AuthInfoType.FIND_CASH_PASSWORD_BY_MOBILE + "_sms").getTemplate();
		try {

			authCode = smsService.sendCM(content,mobileNumber);
		}catch (Exception e){
			log.error("找回交易密码信息发送错误");
//			throw new SendSMSException("找回交易密码信息发送错误");
//			FacesUtil.addErrorMessage("手机认证码信息发送失败,请和管理员联系");
			return;
		}
		authService.createAuthInfo(userId, mobileNumber, authCode, CommonConstants.AuthInfoType.FIND_CASH_PASSWORD_BY_MOBILE);
		/*
		User user = ht.get(User.class, userId);
		if (user == null) {
			throw new UserNotFoundException("user.id:" + userId);
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("authCode",authService.createAuthInfo(userId, mobileNumber, null, CommonConstants.AuthInfoType.FIND_CASH_PASSWORD_BY_MOBILE).getAuthCode());
		messageBO.sendSMS(ht.get(UserMessageTemplate.class, MessageConstants.UserMessageNodeId.FIND_CASH_PASSWORD_BY_MOBILE + "_sms"), params, mobileNumber);
		*/
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void hfRealName(User user) {

		String idCard=user.getIdCard();
		Date birthday=null;
		String birthdayStr="";
		Integer sexCode ;
		if (idCard.length()==15) {
			sexCode = Integer.valueOf(idCard.substring(14));
			birthdayStr= idCard.substring(6, 12);
			Integer birthYear=Integer.valueOf(idCard.substring(6,8));
			if(birthYear<10){
				birthdayStr="20"+birthdayStr;
			}else {
				birthdayStr="19"+birthdayStr;
			}
		}else {
			sexCode = Integer.valueOf(idCard.substring(16, 17));
			birthdayStr= idCard.substring(6, 14);
		}
		if (sexCode % 2==1){
			user.setSex("M");;
		}else{
			user.setSex("F");
		}
		birthday=DateUtil.StringToDate(birthdayStr);
		user.setBirthday(birthday);
		userBO.update(user);
		userBO.addRole(user, new Role("INVESTOR"));

		// 刷新登录用户权限
		springSecurityService.refreshLoginUserAuthorities(user.getId());
	}

	@Override
	public List<User> synBankCardUser() {
		// TODO Auto-generated method stub
		String hql="from User where usrCustId is not null";
		return ht.find(hql);
	}

}
