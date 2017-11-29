package com.zw.p2p.loan.controller;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.impl.StdScheduler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.notice.model.NoticePool;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.RegistCash;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.loan.model.VirtualLoanRecord;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.loan.service.VirtualLoanRecordService;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;

/**
 * Filename: LoanHome.java Description: Copyright: Copyright (c)2013 Company:
 * jdp2p
 * c
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:28:49
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class LoanNewHome extends EntityHome<RegistCash> implements Serializable {

	@Resource
	NoticePool noticePool;
	@Resource
	LoanCalculator loanCalculator;
	@Resource
	ConfigService configService;
	
	@Logger
	Log log;

	@Resource
	StdScheduler scheduler;

	@Resource
	SafeLoanTaskService safeLoanTaskService;
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	private VirtualLoanRecordService virtualLoanRecordService;
	/**
	*@Description: TODO(获得当前登录用户可用体验金) 
	* @author cuihang   
	*@date 2016-11-1 下午4:34:09 
	*@return
	 */
	public Double getEnableMoney(){
		User user=loginUserInfo.getUser();
		if(null!=user){
			RegistCash registCash=	virtualLoanRecordService.getRegistCashByUser(user.getId());
			if((null!=registCash)&&(1==registCash.getIsused())&&(registCash.getEndtime().compareTo(new Date())>0)){
				//未使用并且结束时间在今天之后
				return registCash.getCash();
			}
		}
		return 0d;
	}
	/**
	 * 
	*@Description: TODO(可用体验金) 
	* @author cuihang   
	*@date 2016-11-2 上午11:37:43 
	*@return
	 */
	public RegistCash getEnableRegistCash(){

		User user=loginUserInfo.getUser();
		if(null!=user){
			RegistCash registCash=	virtualLoanRecordService.getRegistCashByUser(user.getId());
			if((null!=registCash)&&(1==registCash.getIsused())&&(registCash.getEndtime().compareTo(new Date())>0)){
				//未使用并且结束时间在今天之后
				return registCash;
			}
		}
		return null;
	
	}
	/**
	 * 
	*@Description: TODO(已投资体验金) 
	* @author cuihang   
	*@date 2016-11-2 上午11:37:43 
	*@return
	 */
	public VirtualLoanRecord getVirtualLoanRecord(){
		
		User user=loginUserInfo.getUser();
		if(null!=user){
			VirtualLoanRecord virtualLoanRecord=	virtualLoanRecordService.getVirtualLoanRecordByUser(user.getId());
			return virtualLoanRecord;
		}
		return null;
		
	}
	
	/**
	 * 投资体验标
	 */
	public String save(){
		User user=loginUserInfo.getUser();
		if(null==user){
			FacesUtil.addInfoMessage("请登录后投资！");
			return null;
		}
		String result=virtualLoanRecordService.saveIn(user.getId());
		if(result!=null){
			FacesUtil.addInfoMessage("投资成功！");
			return "pretty:p2p_loan_new";
		}else{
			FacesUtil.addInfoMessage("投资失败！");
		}
		
		return null;
	}
	
}
