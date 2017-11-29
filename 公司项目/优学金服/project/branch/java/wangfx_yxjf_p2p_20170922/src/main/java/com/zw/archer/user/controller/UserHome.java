package com.zw.archer.user.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.common.exception.AuthInfoAlreadyActivedException;
import com.zw.archer.common.exception.AuthInfoOutOfDateException;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.common.model.AuthInfo;
import com.zw.archer.common.service.AuthService;
import com.zw.archer.invite.InviteService;
import com.zw.archer.invite.model.invite;
import com.zw.archer.openauth.OpenAuthConstants;
import com.zw.archer.openauth.service.OpenAuthService;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.exception.ConfigNotFoundException;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.archer.user.service.impl.MyAuthenticationManager;
import com.zw.archer.user.service.impl.UserBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.HashCrypt;
import com.zw.core.util.ImageUploadUtil;
import com.zw.core.util.SpringBeanUtil;
import com.zw.core.util.StringManager;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuUserService;
import com.zw.p2p.message.controller.UserMessageHome;
import com.zw.p2p.message.model.UserMessageTemplate;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.MessageService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.message.service.impl.MessageBO;

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
public class UserHome extends EntityHome<User> implements java.io.Serializable {

	@Resource
	private UserService userService;

	@Resource
	private AuthService authService;

	@Resource
	private LoginUserInfo loginUser;

	@Resource
	private UserBO userBO;

	@Resource
	private SmsService smsService;

	@Resource
	private HibernateTemplate ht;

	@Resource
	private MessageBO messageBO;

	@Resource
	private AutoMsgService autoMsgService;

	@Resource
	private MessageService messageService;

	@Resource
	private UserMessageHome userMessageHome;

	@Resource
	private InviteService inviteService;
	
	@Resource
	private HuiFuUserService huiFuUserService;
	@Resource
	private HuiFuLoanService huiFuLoanService;
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	@Resource
	UserDetailsService userDetailsService;
	@Autowired
	SessionRegistry sessionRegistry;
	/**
	 * 推荐人
	 */
	@Deprecated
	private String referrer;

	// 验证认证码是否正确
	private boolean correctAuthCode = false;
	@Logger
	static Log log;
	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);
	// 认证码
	private String authCode;
	// 实名认证绑定所要手机号
	private String mobileNumber;
	// 旧密码
	private String oldPassword;
	// 旧交易密码
	private String oldCashPassword;
	// 新邮箱
	private String newEmail;
	// 新手机号
	private String newMobileNumber;
	/**
	 * 动态验证码
	 */
	private String dynvalcode;
	/**
	 * 更改绑定邮箱第二步 验证认证码并更改绑定邮箱
	 *
	 * @return
	 */
	public String changeBindingEmail() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			userService.bindingEmail(user.getId(), newEmail, authCode);
			correctAuthCode = true;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("认证码已激活！");
		}
		return null;
	}

	/**
	 * 更改绑定手机第二步 验证认证码并更改绑定手机
	 *
	 * @return
	 */
	public String changeBindingMobileNumber() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			userService.bindingMobileNumber(user.getId(), newMobileNumber,
					authCode);
			correctAuthCode = true;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入认证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("认证码已激活！");
		}
		return null;
	}

	/**
	 * 更改绑定邮箱第一步 通过收到邮件认证码验证用户当前邮箱
	 *
	 * @return
	 */
	public String checkCurrentEmail() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			authService.verifyAuthInfo(user.getId(), user.getEmail(), authCode,
					CommonConstants.AuthInfoType.CHANGE_BINDING_EMAIL);
			correctAuthCode = true;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("输入认证码错误，认证码已经使用！");
		}
		return null;
	}

	/**
	 * 更改绑定手机号第一步 通过收到手机认证码验证用户当前手机
	 *
	 * @return
	 * @throws AuthInfoAlreadyActivedException
	 */
	public String checkCurrentMobileNumber()
			throws AuthInfoAlreadyActivedException {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			authService.verifyAuthInfo(user.getId(), user.getMobileNumber(),
					authCode,
					CommonConstants.AuthInfoType.CHANGE_BINDING_MOBILE_NUMBER);
			correctAuthCode = true;
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入验证码错误，请重新输入！");
		}
		return null;
	}

	/**
	 * 禁止用户
	 *
	 * @return
	 */
	public String forbid(String userId) {
		// if (log.isInfoEnabled()) {
		// log.info(sm.getString("log.info.forbidUser",
		// userId, new Date(), userId));
		// }
		try {
			userService.changeUserStatus(userId,
					UserConstants.UserStatus.DISABLE);
		} catch (ConfigNotFoundException e) {
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("该用户不存在");
		}
		return getSaveView();
	}

	public String getAuthCode() {
		return authCode;
	}

	/**
	 * 获取投资权限,即实名认证----投资
	 *
	 * @return
	 */
	public String getInvestorPermission() {

		/*if (StringUtils.equals(
				HashCrypt.getDigestHash(getInstance().getCashPassword()),
				getInstance().getPassword())) {
			FacesUtil.addErrorMessage("交易密码不能与登录密码相同");
			return null;
		}*/
		try {
			userService.realNameCertification(getInstance());
		}catch (NoMatchingObjectsException e){
			FacesUtil.addErrorMessage(e.getMessage());
			return null;
		}

		FacesUtil.addInfoMessage("保存成功，你已通过了实名认证！");
		if (FacesUtil.isMobileRequest()) {
			return "pretty:mobile_user_center";
		}
		//被邀请人实名认证后修改进行状态给予邀请人奖励
		String loginUserId=loginUser.getLoginUserId();
		String hql="from invite i where i.touserid=?";
		List<invite> invite=getBaseService().find(hql,loginUserId);
		if(null==invite||invite.size()<=0){
			return "pretty:bankCardList";
		}
		invite in=invite.get(0);
		in.setRealnamestatus(1);
		inviteService.save(in);
		
		return "pretty:userCenter";
	}



    /**
     * 获取投资权限,即实名认证---------------后台使用
     *
     * @return
     */
    public String getInvestorPermissionBackStage() {

       /* if (StringUtils.equals(
                HashCrypt.getDigestHash(getInstance().getCashPassword()),
                getInstance().getPassword())) {
            FacesUtil.addErrorMessage("交易密码不能与登录密码相同");
            return null;
        }*/
        try {
            userService.realNameCertification(getInstance());
        }catch (NoMatchingObjectsException e){
            FacesUtil.addErrorMessage(e.getMessage());
            return null;
        }

        FacesUtil.addInfoMessage("保存成功，你已通过了实名认证！");
        if (FacesUtil.isMobileRequest()) {
            return "pretty:mobile_user_center";
        }
        return "/admin/loaner/loanerList";
    }
	public String getMobileNumber() {
		return mobileNumber;
	}

	public String getNewEmail() {
		return newEmail;
	}

	public String getNewMobileNumber() {
		return newMobileNumber;
	}

	public String getOldCashPassword() {
		return oldCashPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getReferrer() {
		return referrer;
	}

	public boolean isCorrectAuthCode() {
		return correctAuthCode;
	}

	/**
	 * 修改交易密码
	 *
	 * @return
	 */
	public String modifycashPassword() {
		User user = ht.get(User.class,loginUser.getLoginUserId());
		try {
			if (oldCashPassword != null && !"".equals(oldCashPassword) && !userService.verifyOldCashPassword(user.getId(), oldCashPassword)) {
				FacesUtil.addErrorMessage("输入旧交易密码错误，请重新输入！");
				return null;
			}
			String cashPassword = HashCrypt.getDigestHash(getInstance().getCashPassword());
			String password = user.getPassword();
			if (cashPassword.equals(password)) {
				FacesUtil.addErrorMessage("交易密码不能与登录密码相同");
				return null;
			}
			userService.modifyCashPassword(user.getId(), getInstance()
					.getCashPassword());
			if(oldCashPassword == null || "".equals(oldCashPassword)){
				FacesUtil.addInfoMessage("设置交易密码成功。");
				sendPwdMsg(user,"交易","设置");
			}else{
				FacesUtil.addInfoMessage("修改交易密码成功。");
				sendPwdMsg(user,"交易","修改");
			}
			return "pretty:userCenter";
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送密码变更信息
	 * @param user
	 * @param type
	 * @param operation
	 */
	public void sendPwdMsg(User user,String type,String operation){
		UserMessageTemplate umt = ht.get(UserMessageTemplate.class, "update_pwd_sms");
		Map<String, String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("username",user.getUsername());
		params.put("type",type);
		params.put("operation",operation);
		String mobile = user.getMobileNumber();
		String content  = messageBO.replaceParams(umt,params);
		try {
			smsService.sendMsg(content,mobile);

		}catch (Exception e){
			log.error("密码变更信息发送错误");
		}
	}

	/**
	 * 修改密码
	 *
	 * @return
	 */
	public String modifyPassword() {
		String userId = loginUser.getLoginUserId();
		try {
			if (!userService.verifyOldPassword(userId, oldPassword)) {
				FacesUtil.addErrorMessage("输入旧密码错误，请重新输入！");
				return null;
			}
			userService.modifyPassword(loginUser.getLoginUserId(),
					getInstance().getPassword());
			FacesUtil.addInfoMessage("密码修改成功！");
			User user = ht.get(User.class,userId);
			sendPwdMsg(user,"登录","修改");
			return "pretty:userCenter";
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用户注册
	 *
	 * @deprecated
	 * @see UserHome.registerByEmail()
	 * @return
	 */
	@Deprecated
	public String register() {
		// 保存用户
		userService.register(getInstance(), referrer);
		// regSuccess = true;
		FacesUtil.setSessionAttribute("userEmail", getInstance().getEmail());
		return "pretty:userRegActiveuser";
	}

	/**
	 * 通过邮箱注册用户
	 *
	 * @since 2.0
	 * @return
	 */
	public String registerByEmail() {
		// 保存用户
		userService.register(getInstance(), referrer);
		// 跳转到“提示通过邮箱激活页面”
		FacesUtil.setRequestAttribute("email", getInstance().getEmail());
		return "pretty:emailActiveNotice";
	}

	/**
	 * 通过手机注册
	 *
	 * @return
	 */
	public String registerByMobileNumber() {
		if(getInstance().getReferrer()!=null&&getInstance().getReferrer()!=""&&getInstance().getReferrer().length()>0){
			try {
				if(userService.getUserById(getInstance().getReferrer())==null){
					FacesUtil.addErrorMessage("推荐人不存在");
					return null;
				}
			} catch (UserNotFoundException e) {
				// TODO Auto-generated catch block
				FacesUtil.addErrorMessage("推荐人不存在");
				return null;
			}
			getInstance().setReferrerId(getInstance().getReferrer());
		}
		try {
			userService.registerByMobileNumber(getInstance(), authCode,
					referrer);
			List<UserMessageTemplate> umts = autoMsgService.getUmts();
			messageService.saveUserMessages(getInstance().getUsername(), umts);
			
			HttpSession session= FacesUtil.getHttpSession();
			UserDetails userDetails = userDetailsService
					.loadUserByUsername(getInstance().getUsername());
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					getInstance().getUsername(), userDetails.getPassword(),
					userDetails.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(token);

			session.setAttribute(
					HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext());
			sessionRegistry.registerNewSession(session.getId(), userDetails);
			MyAuthenticationManager mu=	(MyAuthenticationManager)SpringBeanUtil.getBeanByName("myAuthenticationManager");
			mu.handleLoginSuccess(getInstance(), FacesUtil.getHttpServletRequest());
			if (FacesUtil.isMobileRequest()) {
				return "pretty:mobile_user_center";
			}
			if(StringUtils.isEmpty(super.getSaveView())){
				return "pretty:userRegSuccess";
			}else{
				FacesUtil.addInfoMessage("注册成功");
				return super.getSaveView();
			}
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入的验证码错误，验证失败！");
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("验证码已过期！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("验证码已被使用！");
		}catch (Exception e){
			return "pretty:userRegister";
		}
		return null;
	}

	/**
	 * 第三方登录 绑定账号注册 QQ、新浪微博
	 *
	 * @return
	 */
	public String registerByOpenAuth() {
		try {
			userService.registerByMobileNumber(getInstance(), authCode, referrer);
		} catch (NoMatchingObjectsException e) {
			e.printStackTrace();
		} catch (AuthInfoOutOfDateException e) {
			e.printStackTrace();
		} catch (AuthInfoAlreadyActivedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		};
		Object openId = FacesUtil
				.getSessionAttribute(OpenAuthConstants.OPEN_ID_SESSION_KEY);
		Object openAutyType = FacesUtil
				.getSessionAttribute(OpenAuthConstants.OPEN_AUTH_TYPE_SESSION_KEY);
		Object accessToken = FacesUtil
				.getSessionAttribute(OpenAuthConstants.ACCESS_TOKEN_SESSION_KEY);
		if (openId != null && openAutyType != null && accessToken != null) {
			OpenAuthService oas = null;
			// QQ
			if (OpenAuthConstants.Type.QQ.equals((String) openAutyType)) {
				oas = (OpenAuthService) SpringBeanUtil
						.getBeanByName("qqOpenAuthService");
				// weibo
			} else if (OpenAuthConstants.Type.SINA_WEIBO
					.equals((String) openAutyType)) {
				oas = (OpenAuthService) SpringBeanUtil
						.getBeanByName("sinaWeiboOpenAuthService");
			}
			// 找不到应该抛异常
			if (oas != null) {
				oas.binding(getInstance().getId(), (String) openId,
						(String) accessToken);
			}
		}
		FacesUtil.addInfoMessage("注册成功！");
		return "pretty:home";
	}

	/**
	 * 解禁用户
	 *
	 * @return
	 */
	public String release(String userId) {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.releaseUser", FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"),
					new Date(), userId));
		}
		try {
			userService.changeUserStatus(userId,
					UserConstants.UserStatus.ENABLE);
			// FIXME:下面异常不合理
		} catch (ConfigNotFoundException e) {
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("该用户不存在");
		}
		return getSaveView();
	}

	/**
	 * 后台保存非借款用户
	 */
	@Override
	@Transactional(readOnly = false , rollbackFor = Exception.class)
	public String save() {
		// FIXME:放在service中

		if (StringUtils.isEmpty(getInstance().getId())) {
			getInstance().setId(getInstance().getUsername());
			getInstance().setPassword(HashCrypt.getDigestHash("123abc"));
			FacesUtil.addInfoMessage("用户创建成功，初始密码为123abc，请及时通知用户修改密码！");
			getInstance().setRegisterTime(new Date());
		}

		setUpdateView(FacesUtil.redirect("/admin/user/userList"));

		return super.saveUser();
	}
    /**
     * 后台保存借款用户
     */
    @Transactional(readOnly = false)
    public String saveLoaner() {
        // FIXME:放在service中
        if (StringUtils.isEmpty(getInstance().getId())) {
            getInstance().setId(getInstance().getUsername());
            getInstance().setPassword(HashCrypt.getDigestHash("123abc"));
            FacesUtil.addInfoMessage("后台借款用户创建成功，初始密码为123abc，请及时通知用户修改密码！");
            getInstance().setRegisterTime(new Date());
            List<Role> roles=getLoanersRoles();
            getInstance().setRoles(roles);
        }

        setUpdateView(FacesUtil.redirect("/admin/loaner/loanerList"));
        return super.saveUser();
    }
    public List<Role> getLoanersRoles(){
        List<Role> roles=new ArrayList<Role>();
        Role role =new Role();
        role=userBO.getRoleByRoleId("MEMBER");//用户角色
        if(null!=role){
            roles.add(role);
        }
        role=new Role();
        role=userBO.getRoleByRoleId("LOANER");//借款者角色
        if(null!=role){
            roles.add(role);
        }
        return  roles;
    }
	public void setUserMsg(){
		save();
		List<UserMessageTemplate> umts = autoMsgService.getUmts();
		messageService.saveUserMessages(getInstance().getId(),umts);
	}

	/**
	 * 管理员修改用户，修改普通信息和邮箱、手机，不可修改密码等
	 *
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public String modifyByAdmin() {
		if (StringUtils.isNotEmpty(getInstance().getEmail())) {
			User user = userBO.getUserByEmail(getInstance().getEmail());
			if (user != null && !user.getId().equals(getInstance().getId())) {
				FacesUtil.addErrorMessage("该邮箱已存在！");
				return null;
			}
		}
		if (StringUtils.isNotEmpty(getInstance().getMobileNumber())) {
			User user = userBO.getUserByMobileNumber(getInstance()
					.getMobileNumber());
			if (user != null && !user.getId().equals(getInstance().getId())) {
				FacesUtil.addErrorMessage("该手机号已存在！");
				return null;
			}
		}
		getBaseService().merge(getInstance());

		messageService.saveUserMessages(getInstance().getId(), userMessageHome.getUmts());

		FacesUtil.addInfoMessage("用户信息修改成功！");
		return FacesUtil.redirect("/admin/user/userList");
	}
    /**
     * 资本端修改用户，修改普通信息和邮箱、手机，不可修改密码等
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String modifyByZiben() {
//        if (StringUtils.isNotEmpty(getInstance().getEmail())) {
//            User user = userBO.getUserByEmail(getInstance().getEmail());
//            if (user != null && !user.getId().equals(getInstance().getId())) {
//                FacesUtil.addErrorMessage("该邮箱已存在！");
//                return null;
//            }
//        }
//        if (StringUtils.isNotEmpty(getInstance().getMobileNumber())) {
//            User user = userBO.getUserByMobileNumber(getInstance()
//                    .getMobileNumber());
//            if (user != null && !user.getId().equals(getInstance().getId())) {
//                FacesUtil.addErrorMessage("该手机号已存在！");
//                return null;
//            }
//        }
        getBaseService().merge(getInstance());

		messageService.saveUserMessages(getInstance().getId(), userMessageHome.getUmts());

		FacesUtil.addInfoMessage("借款用户信息修改成功！");
        return FacesUtil.redirect("/admin/loaner/loanerList");
    }
	/**
	 * 后台创建或修改借款者
	 *
	 * @return
	 */
	public String saveBorrower() {
		// TODO:用户风险等级
		this.getInstance().setPassword("123456");
		userService.createBorrowerByAdmin(this.getInstance());
		FacesUtil.addInfoMessage("借款者创建成功，初始密码为123456");
		return FacesUtil.redirect("/admin/user/userList");
	}

	/**
	 * 再次发送激活邮件
	 *
	 * @author wangxiao 5-6
	 * @return
	 */
	public void sendActiveEmailAgain() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			userService.sendActiveEmailAgain(user);
			FacesUtil
					.setSessionAttribute("userEmail", getInstance().getEmail());

		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
		FacesUtil.addInfoMessage("邮件已发送请登录邮箱激活");
	}

	/**
	 * 给绑定手机发送认证码
	 */
	public void sendBdMobileNumber() {
		try {
			userService.sendBindingMobileNumberSMS(loginUser.getLoginUserId(),
					getInstance().getMobileNumber());
			FacesUtil.addInfoMessage("验证码短信已经发送！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户尚未登录！");
			e.printStackTrace();
		}
	}

	/**
	 * 更改绑定邮箱第一步 给用户当前邮箱发送认证码
	 */
	public void sendCurrentBindingEmail() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			userService.sendChangeBindingEmail(user.getId(), user.getEmail());
			FacesUtil.addInfoMessage("验证码已经发送至邮箱！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}

	}

	/**
	 * 更改绑定手机号第一步 给用户当前手机发送认证码
	 */
	public void sendCurrentBindingMobileNumberSMS() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			userService.sendChangeBindingMobileNumberSMS(user.getId(),
					user.getMobileNumber());
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}

	/**
	 * 更改绑定邮箱第二步 给新邮箱发送验证码
	 */
	public void sendNewBindingEmail() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			if (user.getEmail().equals(newEmail)) {
				FacesUtil.addErrorMessage("新邮箱不能与当前邮箱相同！");
				return;
			}
			// FIXME 缺发送绑定新邮箱接口 、 新邮箱需要验证唯一性
			userService.sendBindingEmail(user.getId(), newEmail);
			FacesUtil.addInfoMessage("验证码已经发送至新邮箱！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}

	/**
	 * 更改绑定手机号第二步 给新手机发送验证码（修改绑定手机）
	 */
	public void sendNewBindingMobileNumber() {
		User user;
		try {
			user = userService.getUserById(loginUser.getLoginUserId());
			if (user.getMobileNumber().equals(newMobileNumber)) {
				FacesUtil.addErrorMessage("新手机号不能与当前手机相同！");
				return;
			}
			// FIXME 缺发送绑定新手机接口 、 新手机需要验证唯一性
			userService.sendBindingMobileNumberSMS(user.getId(),
					newMobileNumber);
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录");
			e.printStackTrace();
		}
	}

	/**
	 * 用户注册操作，发送手机验证码验证（用户注册时）
	 */
	@Deprecated
	public void sendRegisterAuthCodeToMobile(String mobileNumber) {
		/*userService.sendRegisterByMobileNumberSMS(mobileNumber);
		FacesUtil.addInfoMessage("短信已发送，请注意查收！");*/
	}
	//是否汇付注册
public boolean hasUsrCustId(){
	User user=loginUser.getUser();
	String usrCustId= user.getUsrCustId();
	if(null!=usrCustId&&usrCustId.length()>0){
		return true;
	}
	return false;
	
}
	//获得汇付用户开户注册页面
	public void turnUserRegister(){
		User user=loginUser.getUser();
		user.setIdCard(getInstance().getIdCard());
		user.setRealname(getInstance().getRealname());
		try {
			User userbyidcard=userService.getUserByIdCard(getInstance().getIdCard());
			if(null!=userbyidcard){
				FacesUtil.addErrorMessage("请输入正确身份证号！");
			}else{
				String html =huiFuUserService.userRegister(user);
				FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
				FacesUtil.getHttpServletResponse().getWriter().print(html);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 用户注册操作，发送手机验证码验证（用户注册时）
	 *
	 * @param mobileNumber
	 * @param jsCode
	 *            成功后执行的js代码
	 */
	public void sendRegisterAuthCodeToMobile(String mobileNumber, String jsCode) {
		if(null!=getDynvalcode()&&getDynvalcode().toUpperCase().equals(FacesUtil.getSessionAttribute(CommonConstants.CaptchaFlag.CAPTCHA_SESSION))){
			userService.sendRegisterByMobileNumberSMS(mobileNumber);
			FacesUtil.addInfoMessage("短信已发送，请注意查收！");
			RequestContext.getCurrentInstance().execute(jsCode);
			FacesUtil.setSessionAttribute(CommonConstants.CaptchaFlag.CAPTCHA_SESSION, null);
		}else{
			FacesUtil.addErrorMessage("请输入正确动态验证码！");
		}
	
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public void setCorrectAuthCode(boolean correctAuthCode) {
		this.correctAuthCode = correctAuthCode;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

	public void setNewMobileNumber(String newMobileNumber) {
		this.newMobileNumber = newMobileNumber;
	}

	public void setOldCashPassword(String oldCashPassword) {
		this.oldCashPassword = oldCashPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getDynvalcode() {
		return dynvalcode;
	}

	public void setDynvalcode(String dynvalcode) {
		this.dynvalcode = dynvalcode;
	}

	/**
	 * 上传图片
	 *
	 * @return
	 */
	@Transactional(readOnly = false)
	public void uploadPhoto(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		InputStream is = null;
		try {
			is = file.getInputstream();
			this.getInstance().setPhoto(
					ImageUploadUtil.upload(is, file.getFileName()));
			getBaseService().merge(getInstance());
			FacesUtil.addInfoMessage("上传成功！");
		} catch (IOException e) {
			FacesUtil.addErrorMessage("上传失败！");
			e.printStackTrace();
		}
	}

	/* =======================通过手机修改密码=============================== */

	/**
	 * 第一步： 给手机发送验证码
	 *
	 * @author liuchun
	 * @param mobileNumber
	 *            注册时候用的手机号码
	 *
	 */
	public String sendAuthCodeToMobile(String mobileNumber) {
		String hql = "from User u where u.mobileNumber = ?";
		if (0 != getBaseService().find(hql, mobileNumber).size()) {
			userService.sendRegisterByMobileNumberSMS(mobileNumber);
			RequestContext.getCurrentInstance().addCallbackParam("sendSuccess",
					true);
			FacesUtil.setSessionAttribute("mobileNumber", mobileNumber);
			FacesUtil.addInfoMessage("短信已发送，请注意查收！");
			// FIXME:专属页面没有做，直接在原来页面上修改的
			return "pretty:findPwdByEmail2";
		} else {
			FacesUtil.addErrorMessage("此用户没有注册!!");
			return null;
		}
	}

	/**
	 * 第二部： 检查发送的修改密码验证码是否和数据库收到的验证码一致
	 *
	 * @author liuchun
	 * @param authCode
	 *            验证码
	 * @param mobileNumber
	 *            手机号
	 * @return
	 */
	@Transactional
	public String checkAuthCode(String authCode, String mobileNumber) {
		String hql = "from AuthInfo ai where ai.authCode =? and ai.authTarget=?";
		ArrayList<AuthInfo> list = (ArrayList<AuthInfo>) getBaseService().find(
				hql, new String[] { authCode, mobileNumber });

		// FIXME:验证不够严谨，有可能出现重复数据
		if (list.size() > 0) {
			FacesUtil.setRequestAttribute("mobileNumber", mobileNumber);
			return "pretty:findPwdByEmail3";
		} else {
			FacesUtil.addErrorMessage("验证码输入错误！！");
			return null;
		}
	}

	/**
	 * 第三部：通过手机修改密码
	 *
	 * @param mobileNumber
	 * @param newPwd
	 * @return
	 */
	@Transactional
	public String ModificationPwdByMobileNum(String mobileNumber, String newPwd) {
		try {
			User user = userService.getUserByMobileNumber(mobileNumber);
			userService.modifyPassword(user.getId(), newPwd);
			FacesUtil.addInfoMessage("修改密码成功");
			return "pretty:memberLogin";
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("此号码未注册");
			e.printStackTrace();
		}
		return authCode;
	}

	/*
	 * =================================通过邮箱进行实名认证================================
	 */

	/**
	 * 第一步： 给邮箱发送验证码(实名认证的时候用)
	 *
	 * @author liuchun
	 */
	public void sendAuthCodeToEmail() {
		try {
			userService.sendBindingEmail(loginUser.getLoginUserId(),
					getInstance().getEmail());
			FacesUtil.addInfoMessage("验证码已经发送，请注意查收！");
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户尚未登录！");
			e.printStackTrace();
		}
	}

	/**
	 * 第二部： 获取投资权限,即实名认证(通过邮箱进行实名认证 )
	 *
	 * @return
	 */

	public String getInvestorPermissionByEmail() {
		try {
			userService.realNameCertificationByEmail(getInstance(), authCode);
			FacesUtil.addInfoMessage("保存成功，你已通过了实名认证！");
			return "pretty:userCenter";
		} catch (AuthInfoOutOfDateException e) {
			FacesUtil.addErrorMessage("认证码已过期！");
		} catch (NoMatchingObjectsException e) {
			FacesUtil.addErrorMessage("输入认证码错误，实名认证失败！");
		} catch (AuthInfoAlreadyActivedException e) {
			FacesUtil.addErrorMessage("输入认证码错误，实名认证失败！");
		}
		return null;
	}

	/**
	 * 导出所有用户信息
	 */
	public void exportUserList() {
		String hql = "from User where 1=1 order by register_time desc";
		List<User> list = ht.find(hql);
		if (list != null && list.size() > 0) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String[] excelHeader = { "编号", "用户名", "真实姓名", "性别", "手机号", "电子邮箱","推荐人","注册时间","角色","状态" };
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("用户信息表");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < excelHeader.length; i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(excelHeader[i]);
				sheet.autoSizeColumn(i);
			}
			String roleName = "";
			for (int i = 0; i < list.size(); i++) {
				User user = list.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(user.getId());
				row.createCell(1).setCellValue(user.getUsername());
				row.createCell(2).setCellValue(user.getRealname());
				row.createCell(3).setCellValue(user.getSex());
				row.createCell(4).setCellValue(user.getMobileNumber());
				row.createCell(5).setCellValue(user.getEmail());
				row.createCell(6).setCellValue(user.getReferrer());
				row.createCell(7).setCellValue(format.format(user.getRegisterTime()));
				List<Role> roles = user.getRoles();
				for (Role role : roles){
					roleName += role.getName() + "  ";
				}
				row.createCell(8).setCellValue(roleName);
				row.createCell(9).setCellValue(user.getStatus().equals("1")?"正常":"禁止");
				roleName = "";
			}
			HttpServletResponse response = FacesUtil.getHttpServletResponse();
			response.setContentType("application/vnd.ms-excel");
			OutputStream stream = null;
			try {
				String filename = "用户信息表.xls";
				String agent = FacesUtil.getHttpServletRequest().getHeader(
						"USER-AGENT");
				if (null != agent && -1 != agent.indexOf("MSIE")) {
					filename = URLEncoder.encode(filename, "utf-8");
				} else {
					filename = new String(filename.getBytes("utf-8"),
							"iso8859-1");
				}
				response.setHeader("Content-disposition",
						"attachment;filename=" + filename);
				stream = response.getOutputStream();
				wb.write(stream);
				stream.flush();
				stream.close();
				stream = null;
				response.flushBuffer();
				FacesUtil.getCurrentInstance().responseComplete();
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public String rolesstr(List<Role> roles){
		StringBuffer result=new StringBuffer();
			for (Role role : roles) {
				result.append(role.getName()).append(";");
			}	
			return result.toString();
		}
	public void getReCode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, java.io.IOException {
	       // 告知浏览当作图片处理
	       response.setContentType("image/jpeg");
	       // 告诉浏览器不缓存
	       response.setHeader("pragma", "no-cache");
	       response.setHeader("cache-control", "no-cache");
	       response.setHeader("expires", "0");
	       // 产生由4位数字构成的验证码
	       int length = 4;
	       String valcode  = "";
	       Random rd =  new Random();
	       for(int i=0; i<length; i++)
	           valcode+=rd.nextInt(10);
	       // 把产生的验证码存入到Session中
	       HttpSession  session = request.getSession();
	       session.setAttribute("valcode", valcode);
	       // 产生图片
	       int width = 80;
	       int height = 25;
	       BufferedImage img = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
	       // 获取一个Graphics
	       Graphics g = img.getGraphics();
	       // 填充背景色
	       g.setColor(Color.WHITE);
	       g.fillRect(0, 0, width, height);
	       // 填充干扰线50
	       for(int i=0; i<50; i++){
	           g.setColor(new Color(rd.nextInt(100)+155,rd.nextInt(100)+155,rd.nextInt(100)+155));
	           g.drawLine(rd.nextInt(width), rd.nextInt(height),rd.nextInt(width), rd.nextInt(height));
	       }
	       // 绘制边框
	       g.setColor(Color.GRAY);
	       g.drawRect(0, 0, width-1, height-1);
	       // 绘制验证码
	       Font[] fonts = {new Font("隶书",Font.BOLD,18),new Font("楷体",Font.BOLD,18),new Font("宋体",Font.BOLD,18),new Font("幼圆",Font.BOLD,18)};
	       for(int i=0; i<length; i++){
	           g.setColor(new Color(rd.nextInt(150),rd.nextInt(150),rd.nextInt(150)));
	           g.setFont(fonts[rd.nextInt(fonts.length)]);
	           g.drawString(valcode.charAt(i)+"", width/valcode.length()*i+2, 18);
	       }
	       
	       // 输出图像
	       g.dispose();
	       ImageIO.write(img, "jpeg", response.getOutputStream());
	       
	   }
	/**
	 * 汇付登录
	 */
	public void huifuLogin(){
		User user=loginUser.getUser();
		try {
			FacesUtil.getHttpServletResponse().getWriter().print(huiFuUserService.uerLogin(user.getUsrCustId()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 汇付修改信息
	 */
	public void huifuAcctModify(){
		User user=loginUser.getUser();
		try {
			FacesUtil.getHttpServletResponse().getWriter().print(huiFuUserService.acctModify(user.getUsrCustId()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
