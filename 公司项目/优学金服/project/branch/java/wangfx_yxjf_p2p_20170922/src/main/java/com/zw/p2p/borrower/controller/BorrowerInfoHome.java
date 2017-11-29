package com.zw.p2p.borrower.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.borrower.model.BorrowerInfo;
import com.zw.p2p.borrower.model.CreditRatingLog;
import com.zw.p2p.risk.service.RiskService;

/**  
 * Filename:    BorrowerInfoHome.java  
 * Description:   
 * Copyright:   Copyright (c)2013 
 * Company:    p2p 
 * @author:     yinjunlu  
 * @version:    1.0  
 * Create at:   2014-1-11 下午4:14:55  
 *  
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014-1-11    yinjunlu             1.0        1.0 Version  
 */
@Component
@Scope(ScopeType.VIEW)
public class BorrowerInfoHome extends EntityHome<BorrowerInfo> {
	
	@Logger
	private static Log log ;
	
	@Resource
	private LoginUserInfo loginUserInfo;
	
	@Resource
	HibernateTemplate ht;
	
	@Resource
	private RiskService riskService;
	
	
	private String reason;
	private final static String UPDATE_VIEW = FacesUtil
			.redirect("/admin/user/verifyLoanerList");

	public BorrowerInfoHome() {
		// FIXME：保存角色的时候会执行一条更新User的语句
		setUpdateView(UPDATE_VIEW);
	}
	
	@Override
	@Transactional(readOnly = false)
	public String save() {
		if(StringUtils.isNotEmpty(getInstance().getRiskLevel())){

			CreditRatingLog creditRatingLog = new CreditRatingLog();
			creditRatingLog.setId(IdGenerator.randomUUID());
			creditRatingLog.setOperator(loginUserInfo.getLoginUserId());
			creditRatingLog.setTime(new Date());
			creditRatingLog.setReason(reason);
			creditRatingLog.setUser(getInstance().getUser());
			creditRatingLog.setDetails("信用评级改为"+getInstance().getRiskLevel());
			getBaseService().save(creditRatingLog);
			if(log.isDebugEnabled())
				log.debug("用户["+loginUserInfo.getLoginUserId()+"]更改了["
						+getInstance().getUser().getId()+"]的信用评级记录，评级改为"
						+getInstance().getRiskLevel());
		}
		return super.save();
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	//计算风险准备金利率
	public Double getReserveRate(){
		BorrowerInfo loanerInfo = ht.get(BorrowerInfo.class, loginUserInfo.getLoginUserId());
		String riskLevel = loanerInfo.getRiskLevel();
		return riskService.getRPRateByRank(riskLevel);
	}

	/**
	 * 获取用户信用等级
	 * @param user
	 * @return
	 */
	public String getUserRiskLevel(User user) {
		BorrowerInfo loanerInfo = ht.get(BorrowerInfo.class, user.getId());
		String riskLevel = loanerInfo.getRiskLevel();
		return riskLevel;
	}
}
