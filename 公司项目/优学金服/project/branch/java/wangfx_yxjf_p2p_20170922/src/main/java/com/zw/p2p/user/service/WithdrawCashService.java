package com.zw.p2p.user.service;

import java.util.List;

import com.zw.archer.user.model.UserBill;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.WithdrawCash;

/**
 * Filename:AtmFeesService.java Description:提现接口 Copyright: Copyright(c)2013
 * Company:p2p
 * 
 * @author:gongph version:1.0 Create at: 2014-1-4 下午04:33:57
 */
public interface WithdrawCashService {

	/**
	 * 计算提现费用
	 * 
	 * @param amount
	 *            提现金额
	 * @return double 提现费用
	 */
	public double calculateFee(double amount,String userid);

	/**
	 * 通过提现申请
	 * 
	 * @param withdrawCash
	 *            提现instance
	 */
	public void passWithdrawCashApply(WithdrawCash withdrawCash);
	
	/**
	 * 通过提现复核
	 * 
	 * @param withdrawCash
	 *            提现instance
	 */
	public void passWithdrawCashRecheck(WithdrawCash withdrawCash);

	/**
	 * 拒绝提现申请
	 * 
	 * @param withdrawCash
	 *            提现instance
	 */
	public void refuseWithdrawCashApply(WithdrawCash withdrawCash);
	/**
	*@Description: TODO(提现失败) 
	* @author cuihang   
	*@date 2017-4-22 下午5:23:37 
	*@param withdrawCash
	 */
	public void failWithdrawCashApply(String  withdrawCashid,String RespDesc);
	

	/**
	 * 申请提现
	 * 
	 * @param withdrawCash
	 * @throws InsufficientBalance 
	 */
	public void applyWithdrawCash(WithdrawCash withdraw) throws InsufficientBalance;
	
	/**
	 * 管理员提现
	 * @throws InsufficientBalance 
	 */
	public void withdrawByAdmin(UserBill ub) throws InsufficientBalance;
	/**
	 * 	
	*@Description: TODO(根据userid获得提现申请进行中的list) 
	*@author cuihang   
	*@date 2016-3-3 下午6:05:31 
	*@param userid
	*@return
	 */
	public List<WithdrawCash> getApproving(String userid);
	/**
	*@Description: TODO(获得用户体现中的总和) 
	* @author cuihang   
	*@date 2016-3-16 下午7:35:43 
	*@param userid
	*@return
	 */
	public double countWithdraw(String userid);
	
	/**
	 * 根据HuiFu订单号获得id
	 * @param huiFuOrderId
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月18日 下午5:48:39
	 */
	public String getIdByHuiFuOrderId(String huiFuOrderId);

}
