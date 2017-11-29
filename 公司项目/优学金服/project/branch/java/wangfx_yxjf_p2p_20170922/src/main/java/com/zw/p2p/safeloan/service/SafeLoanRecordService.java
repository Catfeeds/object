package com.zw.p2p.safeloan.service;

import java.util.Date;

import com.zw.p2p.safeloan.model.SafeLoanRecord;

public interface SafeLoanRecordService {
	/**
	 * 根据产品id返回投资记录数量
	 * @param safeLoanId
	 * @return
	 */
public int recordNumBySafeLoanId(String safeLoanId);
/**
 * 创建id
 * @return
 */

public String createSafeLoanRecordId();
/**
 * 
*@Description: TODO(根据投资金额起止日期及利率计算实际收益) 
* @author cuihang   
*@date 2016-2-17 下午6:15:46 
*@param money
*@param starttime
*@param rate
*@return
 */
public double getrealIncome(double money,Date starttime,Date endTime,double rate);
/**
 * 
*@Description: TODO(返回用戶无忧保投资债权，债权在投资中和复核中的总和) 
* @author cuihang   
*@date 2016-2-29 下午3:39:46 
*@param userid
*@return
 */
public double getgetFrozenMoney(String userid);
/**
 * 
*@Description: TODO(获得无忧保投资金额，封闭期+结算期投资总和) 
* @author cuihang   
*@date 2016-2-29 下午4:26:46 
*@param userid
*@return
 */
public double getInvestMoney(String userid);

/**
 * 查询当前用户的无忧宝最近的一次投资记录
 */

 public SafeLoanRecord getLastSafeLoanRecordByUser(String userid);
}
