package com.zw.p2p.bankcard.service;

import com.zw.archer.user.model.User;
import com.zw.p2p.bankcard.model.BankCardUnbindApprove;

/**
 * @Description: TODO(解绑申请接口)
 * @author cuihang
 * @date 2016-3-5 下午3:20:59
 */
public interface BankCardUnbindApproveService {
	/**
	 * @Description: TODO(提交解绑申请)
	 * @author cuihang
	 * @date 2016-3-5 下午3:22:45
	 * @param bca
	 */

	public void creatApprove(BankCardUnbindApprove bca);

	/**
	*@Description: TODO(根据user和审核状态返回数量） 
	* @author cuihang   
	*@date 2016-3-5 下午3:53:21 
	*@param userid
	*@param status
	*@return
	 */
	public int countApproveByUser(String userid,int status);
	/**
	 * 
	*@Description: TODO(审核通过) 
	* @author cuihang   
	*@date 2016-3-7 上午11:26:08 
	*@param bcua
	*@return
	 */
	public String ApproveAgree(BankCardUnbindApprove bcua);
	
	/**
	 * 
	*@Description: TODO(审核拒绝) 
	* @author cuihang   
	*@date 2016-3-7 上午11:26:31 
	*@param bcua
	*@return
	 */
	public String ApproveResuse(BankCardUnbindApprove bcua);
}
