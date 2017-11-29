package com.zw.p2p.statistics.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.ScopeType;
import com.zw.huifu.service.HuiFuMoneyService;

/**
 * 账户（单）统计
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.REQUEST)
public class BillStatistics {
	@Resource
	private HibernateTemplate ht;
	@Resource
	private UserBillService ubs;
	
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	
	/**
	 * 获取用户账户余额
	 * 
	 * @return
	 */
	public double getBalanceByUserId(String userId) {
		User user=ht.get(User.class, userId);
		if (null!=user.getUsrCustId()&&user.getUsrCustId().length()>0) {
			System.out.println(getHuiFuBalanceByUserId(user.getUsrCustId()));
		}
		return ubs.getBalance(userId);
	}
	/**
	 * 获取汇付用户账户余额
	 * 
	 * @return
	 */
	public JSONObject getHuiFuBalanceByUserId(String usrCustId) {
		return huiFuMoneyService.userBalance(usrCustId);
	}

	/**
	 * 获取用户账户冻结金额
	 * @param userId
	 * @return
	 */
	public double getFrozenMoneyByUserId(String userId) {
		return ubs.getFrozenMoney(userId)+ubs.getSafeLoanFrozenMoney(userId);
	}
	
}
