package com.zw.archer.user.service;

import com.zw.archer.invite.model.invite;
import com.zw.archer.user.model.UserBill;
import com.zw.p2p.loan.exception.InsufficientBalance;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:用户账户service
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-4 下午3:36:30
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-4 wangzhi 1.0
 */
public interface UserBillService {

	/**
	 * 转入到余额，例如借款成功时候。
	 * 
	 * @param userId 用户id
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 * 
	 */
	public void transferIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail);
	/**
	*@Description: TODO(无忧宝收益到余额) 
	* @author cuihang   
	*@date 2016-6-7 下午3:42:44 
	*@param userId
	*@param money
	*@param operatorInfo
	*@param operatorDetail
	*@param safeloanid
	 */
	public void transferSafeLoanIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
/**
 * 增加无忧宝记录
 * @param userId
 * @param money
 * @param type
 * @param operatorInfo
 * @param operatorDetail
 */
	/*public void addSafeLoanRecord(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);*/
	/**
	*@Description: TODO(冻结无忧保冻结金额) 
	* @author cuihang   
	*@date 2016-3-26 下午6:27:09 
	*@param userId
	*@param money
	*@param operatorInfo
	*@param operatorDetail
	*@param safeloanid
	 */
	public void freezeSafeLoanMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	*@Description: TODO(解冻无忧宝冻结金额转入余额) 
	* @author cuihang   
	*@date 2016-3-26 下午5:53:53 
	*@param userId
	*@param money
	*@param operatorInfo
	*@param operatorDetail
	*@param safeloanid
	 */
	public void unfreezeSafeLoanMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	*@Description: TODO(从无忧宝冻结金额中转出) 
	* @author cuihang   
	*@date 2016-3-26 下午5:57:22 
	*@param userId
	*@param money
	*@param operatorInfo
	*@param operatorDetail
	*@param safeloanid
	 */
	
	public void transferOutFromsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	 *@Description: TODO(从无忧宝冻结金额中转到理财冻结) 
	 * @author cuihang   
	 *@date 2016-3-26 下午5:57:22 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	
	public void transferOutFromsafeloanFrozenToFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	 *@Description: TODO(从理财冻结 中转到无忧宝冻结金额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午5:57:22 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	
	public void transferOutFromFrozenTosafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	 *@Description: TODO(转入无忧宝冻结金额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午5:57:22 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	
	public void transferInsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid);
	/**
	 * 退还服务费
	 *
	 * @param userId 用户id
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 *
	 */
	public void paybackIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail);

	/**
	 * 从余额中转出
	 * 
	 * @param userId 用户id
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 * @throws InsufficientBalance 余额不足
	 */
	public void transferOutFromBalance(String userId, double money,
			String operatorInfo, String operatorDetail) throws InsufficientBalance;
	
	/**
	 * 从冻结金额中转出
	 * 
	 * @param userId 用户id
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 * @throws InsufficientBalance 余额不足
	 */
	public void transferOutFromFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String loanId) throws InsufficientBalance;

	/**
	 * 冻结金额.
	 * 
	 * @param userId
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 * @throws InsufficientBalance 余额不足
	 */
	public void freezeMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String loanId,String otherMoney) throws InsufficientBalance;

	/**
	 * 解冻金额.
	 * 
	 * @param userId 用户id
	 * @param money
	 *            金额
	 * @param operatorInfo
	 *            操作信息
	 * @param operatorDetail
	 *            操作详情
	 * 
	 * @throws InsufficientBalance 余额不足
	 */
	public void unfreezeMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String loanId) throws InsufficientBalance;

	/**
	 * 获取用户账户余额
	 * @param userId 用户id
	 * @return 余额
	 */
	public double getBalance(String userId);
	
	/**
	 * 获取用户账户冻结金额
	 * @param userId 用户id
	 * @return 余额
	 */
	public double getFrozenMoney(String userId);
	/**
	 * 获取用户账户无忧宝冻结金额
	 * @param userId 用户id
	 * @return 余额
	 */
	public double getSafeLoanFrozenMoney(String userId);
	
	public void save(UserBill ub);
}
