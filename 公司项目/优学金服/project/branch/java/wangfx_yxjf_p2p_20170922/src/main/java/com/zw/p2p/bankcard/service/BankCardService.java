package com.zw.p2p.bankcard.service;

import java.util.List;

import com.zw.archer.user.model.User;
import com.zw.p2p.bankcard.model.BankCard;


/**  
 * Filename:    BankService.java <br/>  
 * Company:     p2p <br/> 
 * Copyright:   Copyright (c)2013 <br/>
 * Description:  
 * @author:     wangzhi  
 * @version:    1.0
 * Create at:   2014-1-6 下午10:54:14  
 *  
 * Modification History: <br/>
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014-1-6      wangzhi      1.0          
 */
public interface BankCardService {
	/**
	 * 获取当前用户绑定的银行卡
	 * @param userId 用户id
	 * @return
	 */
	public List<BankCard> getBankCardsByUserId(String userId);
	/**
	*@Description: TODO(不需要登录就可以查询) 
	* @author cuihang   
	*@date 2017-4-17 上午10:25:18 
	*@param userId
	*@return
	 */
	public List<BankCard> getBankCardsByUserIdNoLogin(String userId);
	/**
	 * 获得userId汇付快捷支付卡
	 * @param userId
	 * @return
	 */
	public List<BankCard> getHuiFuFastBankCardsByUserId(String userId);
	/**
	*@Description: TODO(用户修改银行卡显示状态) 
	*@author cuihang   
	*@date 2016-3-3 下午9:25:56 
	*@param userId
	*@return
	 */
	public int userShowBankCardStatus(String userId);
	/**
	 * 新增银行卡 为汇付提供
	 * @param bankCard
	 */
	public void bindBankCard(BankCard bankCard);
	/**
	 * 根据卡号删除银行
	 * @param cardNo
	 */
	public void delCardByCardId(String cardNo);
	
	/**
	 * 批量删除银行
	 * @param cardNo
	 */
	public void delCard(List<BankCard> bankCards);
	/**
	*@Description: TODO(同步用户汇付银行卡) 
	* @author cuihang   
	*@date 2017-4-17 上午10:04:28 
	*@param user
	 */
	public void synBankCadrByUser(User user);
}
