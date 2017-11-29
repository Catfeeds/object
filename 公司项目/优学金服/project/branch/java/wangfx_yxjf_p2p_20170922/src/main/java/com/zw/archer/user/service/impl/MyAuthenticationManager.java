package com.zw.archer.user.service.impl;

import com.zw.archer.common.CommonConstants;
import com.zw.archer.common.service.BaseService;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.Config;
import com.zw.archer.openauth.OpenAuthConstants;
import com.zw.archer.openauth.service.OpenAuthService;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.exception.IllegalLoginIPException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.model.UserLoginLog;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.*;
import com.zw.p2p.coupons.model.Coupons;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service("myAuthenticationManager")
public class MyAuthenticationManager extends DaoAuthenticationProvider {
	@Resource
	private HibernateTemplate ht;
	private BaseService<User> userService;

	private BaseService<UserLoginLog> loginLogService;
	private BaseService<Coupons> couponSService;

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Override
	@Resource(name = "jdbcUserDetailsManager")
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		super.setUserDetailsService(userDetailsService);
	}

	@Autowired
	private HttpServletRequest request;

	// 直接用@resource注入ht会有问题，在初始化ht之前，就会加载此filter，所以注不进来。
	private HibernateTemplate getHt() {
		return (HibernateTemplate) SpringBeanUtil.getBeanByName("ht");

	}

	/**
	 * 验证用户名
	 */
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		// 是否需要验证码
		Boolean needValidateCode = (Boolean) request.getSession(true)
				.getAttribute(
						UserConstants.AuthenticationManager.NEED_VALIDATE_CODE);
		int loginFailLimit = Integer.parseInt(getHt().get(Config.class,
				ConfigConstants.UserSafe.LOGIN_FAIL_MAX_TIMES).getValue());

		if (needValidateCode == null) {
			needValidateCode = false;
		}

		// check validate code
		if (needValidateCode || loginFailLimit == 0) {
			checkValidateCode(request);
		}
		try {
			return super.authenticate(authentication);
		} catch (final AuthenticationException ae) {
			handleLoginFail(null, request);

			int passwordFailLimit = Integer.parseInt(getHt().get(Config.class,
					ConfigConstants.UserSafe.PASSWORD_FAIL_MAX_TIMES).getValue());

			Integer loginFailTime = (Integer) request
					.getSession(true)
					.getAttribute(
							UserConstants.AuthenticationManager.LOGIN_FAIL_TIME);

			String errMessage;
			if (ae.getMessage().equals("用户已失效"))
				errMessage= ae.getMessage();
			else {
				if (loginFailTime<3){
					errMessage = "您输入的用户名或密码有误，<br/><br/>forget";
				}else {
					if (passwordFailLimit - loginFailTime > 0)
						errMessage = String.format("您已多次输入错误的用户名或密码，<br/>错误超过%d次账户将被锁定，<br/>您还剩余%d次，<br/><br/>forget", passwordFailLimit,passwordFailLimit - loginFailTime);
					else
						errMessage = "因您的登陆密码输入次数太多，账号已经被锁定，请致电客服。";
				}
			}

			// 方法additionalAuthenticationChecks中会捕获此异常并进行异常处理，因此无需再次对此异常进行处理
			AuthenticationException tae = new AuthenticationException(errMessage) {
				@Override
				public void setAuthentication(Authentication authentication) {
					super.setAuthentication(ae.getAuthentication());
				}
			};
			throw tae;
		}
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		this.setPasswordEncoder(new ShaPasswordEncoder());

		User user = (User) getHt().findByNamedQuery("User.findUserByUsername",
				userDetails.getUsername()).get(0);
//		user=getInverstOrLoanStatusByUserId(user);
//		HttpSession session= FacesUtil.getHttpSession();
//		FacesUtil.setSessionAttribute("abc","abc");
////		ServletContext session= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
////		session.setAttribute("myuser", "1111111");
////		session.setAttribute("myuser","1111111");
		//判断用户是否在绑定的ip下登录，为空则不限制登录ip
		String bindIPs = user.getBindIP();
		if (StringUtils.isNotEmpty(bindIPs)) {
			String loginIp = FacesUtil.getRequestIp(request);
			if (!bindIPs.contains(loginIp)) {
				throw new IllegalLoginIPException("当前登录IP未绑定！");
			}
		}
		try {
			super.additionalAuthenticationChecks(userDetails, authentication);
			handleLoginSuccess(user, request);
		} catch (AuthenticationException ae) {
			handleLoginFail(user, request);
			throw ae;
		}
	}

	public User  getInverstOrLoanStatusByUserId(User user){
		String result=null;
		if(null!=user&&StringUtils.isNotEmpty(user.getId())){
			String inverstHql = "Select id from Invest where status  != 'complete' and user_id =?";

			List<Object> inverstResult = ht.find(inverstHql, user.getId());

			if (inverstResult != null && inverstResult.size()>0) {
				result="1";
			}else{
				result="0";
			}
			String loanHql = "Select id from Loan where status  != 'complete' and user_id =?";

			List<Object> loanResult = ht.find(loanHql, user.getId());
			if (loanResult != null && loanResult.size()>0) {
				if(null!=result&&"1".equals(result)){
					result="3";
				}else{
				result="2";
				}
			}
		}
		user.setInverstOrLoanStatus(result);
		return  user;
	}

	/**
	 * 处理登录成功
	 * 
	 * @param user
	 * @param request
	 */
	public void handleLoginSuccess(User user, HttpServletRequest request) {
		request.getSession(true).setAttribute(
				UserConstants.AuthenticationManager.LOGIN_FAIL_TIME, 0);
		request.getSession(true).setAttribute(
				UserConstants.AuthenticationManager.NEED_VALIDATE_CODE, false);

		String openAuthBidding = request
				.getParameter("open_auth_bidding_login");
		if (StringUtils.isNotEmpty(openAuthBidding)
				&& openAuthBidding.trim().equals("true")) {
			// 第三方首次登录，绑定已有账号
			Object openId = request.getSession().getAttribute(
					OpenAuthConstants.OPEN_ID_SESSION_KEY);
			Object openAutyType = request.getSession().getAttribute(
					OpenAuthConstants.OPEN_AUTH_TYPE_SESSION_KEY);
			Object accessToken = request.getSession().getAttribute(
					OpenAuthConstants.ACCESS_TOKEN_SESSION_KEY);
			if (openId != null && openAutyType != null && accessToken != null) {
				OpenAuthService oas = null;
				if (OpenAuthConstants.Type.QQ.equals((String) openAutyType)) {
					oas = (OpenAuthService) SpringBeanUtil
							.getBeanByName("qqOpenAuthService");
				} else if (OpenAuthConstants.Type.SINA_WEIBO
						.equals((String) openAutyType)) {
					oas = (OpenAuthService) SpringBeanUtil
							.getBeanByName("sinaWeiboOpenAuthService");
				}
				// 找不到应该抛异常
				if (oas != null) {
					oas.binding(user.getId(), (String) openId,
							(String) accessToken);
				}
			}
		}

		addUserLoginLog(user, request, UserConstants.UserLoginLog.SUCCESS);

		//处理优惠券过期
		processCouponsExpire(user);
	}

	/**
	 * 处理优惠券过期
	 *
	 * @param user
	 */
	private void processCouponsExpire(User user) {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(Coupons.class);
		criteria.add(Restrictions.eq("user.id", user.getId()));

		couponSService = (BaseService<Coupons>) SpringBeanUtil
		.getBeanByName("baseService");

		List<Coupons> couponsList = getHt().findByCriteria(criteria);
		for (Coupons coupons: couponsList){
			if (coupons.getEndTime()==null)
				continue;

			if (coupons.getStatus().equalsIgnoreCase("enable")) {
				Date expireDay = DateUtil.addDay(
						DateUtil.StringToDate(DateUtil.DateToString(
								coupons.getEndTime(), DateStyle.YYYY_MM_DD_CN)), 1);
				if (expireDay.before(new Date())) {
					coupons.setStatus("expired");
					couponSService.save(coupons);
				}
			}
		}
	}

	/**
	 * 处理登录失败
	 * 
	 * @param user
	 * @param request
	 */
	private void handleLoginFail(User user, HttpServletRequest request) {
		if (user == null) {
			// 连续登录失败，达到一定次数，就出验证码
			int loginFailLimit = Integer.parseInt(getHt().get(Config.class,
					ConfigConstants.UserSafe.LOGIN_FAIL_MAX_TIMES).getValue());

			Integer loginFailTime = (Integer) request
					.getSession(true)
					.getAttribute(
							UserConstants.AuthenticationManager.LOGIN_FAIL_TIME);
			if (loginFailTime == null) {
				loginFailTime = 0;
			}
			loginFailTime++;
			request.getSession(true).setAttribute(
					UserConstants.AuthenticationManager.LOGIN_FAIL_TIME,
					loginFailTime);
			if (loginFailLimit <= loginFailTime) {
				request.getSession(true).setAttribute(
						UserConstants.AuthenticationManager.NEED_VALIDATE_CODE,
						true);
			}
		} else {
			handleUserState(user);
			addUserLoginLog(user, request, UserConstants.UserLoginLog.FAIL);
		}
	}

	/**
	 * 处理用户状态，是否锁定用户之类的
	 * 
	 * @param user
	 */
	private void handleUserState(User user) {
		// 同一个用户名，密码一直输入错误，达到一定次数，就锁定账号。
		int passwordFailLimit = Integer.parseInt(getHt().get(Config.class,
				ConfigConstants.UserSafe.PASSWORD_FAIL_MAX_TIMES).getValue());
		DetachedCriteria criteria = DetachedCriteria
				.forClass(UserLoginLog.class);
		criteria.addOrder(Order.desc("loginTime"));
		criteria.add(Restrictions.eq("username", user.getUsername()));

		List<UserLoginLog> ulls = getHt().findByCriteria(criteria, 0,
				passwordFailLimit);
		if (ulls.size() == passwordFailLimit) {
			// 达到最大次数了
			// 有一次登录成功的，就不锁定
			for (UserLoginLog ull : ulls) {
				if (ull.getIsSuccess().equals(
						UserConstants.UserLoginLog.SUCCESS)) {
					return;
				}
			}
			// 锁定账号
			user.setStatus(UserConstants.UserStatus.DISABLE);
			userService = (BaseService<User>) SpringBeanUtil
					.getBeanByName("baseService");
			userService.save(user);
		}
	}

	/**
	 * 添加用户登录记录
	 * 
	 * @param user
	 * @param request
	 * @param isSuccess
	 *            登录是否成功
	 */
	private void addUserLoginLog(User user, HttpServletRequest request,
			String isSuccess) {
		// 记录user登录信息
		UserLoginLog ull = new UserLoginLog();
		ull.setId(IdGenerator.randomUUID());
		ull.setIsSuccess(isSuccess);
		ull.setLoginIp(FacesUtil.getRequestIp(request));
		ull.setLoginTime(new Timestamp(System.currentTimeMillis()));
		ull.setUsername(user.getUsername());
		loginLogService = (BaseService<UserLoginLog>) SpringBeanUtil
				.getBeanByName("baseService");
		loginLogService.save(ull);
	}

	/**
	 * 
	 * <li>比较session中的验证码和用户输入的验证码是否相等</li>
	 * 
	 */
	protected void checkValidateCode(HttpServletRequest request) {
		String sessionValidateCode = obtainSessionValidateCode(request);
		String validateCodeParameter = obtainValidateCodeParameter(request);
		if (StringUtils.isEmpty(validateCodeParameter)
				|| !sessionValidateCode.equalsIgnoreCase(validateCodeParameter)) {
			throw new AuthenticationServiceException(
					sm.getString("verificationCodeError"));
		}
	}

	private String obtainValidateCodeParameter(HttpServletRequest request) {
		return request.getParameter(CommonConstants.CaptchaFlag.CAPTCHA_INPUT);
	}

	protected String obtainSessionValidateCode(HttpServletRequest request) {
		Object obj = request.getSession().getAttribute(
				CommonConstants.CaptchaFlag.CAPTCHA_SESSION);
		return null == obj ? "" : obj.toString();
	}

}