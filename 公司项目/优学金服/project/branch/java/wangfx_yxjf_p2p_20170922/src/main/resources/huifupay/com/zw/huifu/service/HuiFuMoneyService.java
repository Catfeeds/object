package com.zw.huifu.service;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.zw.core.bean.ZwJson;

/**
 * 汇付天下资金接口
 * @author majie
 * @date 2016年8月17日 下午1:36:44
 */
public interface HuiFuMoneyService {

	/**
	 * 冻结金额
	 * @author majie
	 * @param UsrCustId 用户客户号
	 * @param money 冻结金额
	 * @date 2016年8月17日 下午1:42:38
	 */
	public ZwJson freezeMoney(String usrCustId,Double money);
	
	/**
	 * 解冻金额
	 * @author majie
	 * @param UsrCustId 用户客户号
	 * @param trxId 唯一标示
	 * @date 2016年8月17日 下午1:42:38
	 */
	public ZwJson unfreezeMoney(String usrCustId,String freezeTrxId);
	/**
	 * 账户余额（后台接口）
	 * @author ch
	 * @param UsrCustId 用户客户号
	 * @date 2016年8月19日 下午17:12:38
	 */
	public JSONObject userBalance(String usrCustId);
	
	/**
	 * 商户子账户信息查询
	 * @author majie
	 * @return
	 * @date 2016年9月7日 下午5:18:48
	 */
	public JSONObject queryAccts();
	
	/**
	 * 交易状态查询
	 * @author majie
	 * @param ordId
	 * @param ordDate
	 * @param queryTransType( LOANS：放款交易查询  REPAYMENT：还款交易查 询 TENDER：投标交易查询  CASH：取现交易查询 FREEZE：冻结解冻交易查询)
	 * @return
	 * @date 2016年9月7日 下午8:08:09
	 */
	public JSONObject queryTransStat(String ordId,String ordDate,String queryTransType);
	
	/**
	 * 
	*@Description: TODO(充值对账) 
	* @author cuihang   
	*@date 2016-9-7 下午8:14:29 
	*@return
	 */
	public JSONObject SaveReconciliation(Date beginDate,Date endDate,int pageNum,int pageSize);
	/**
	 * 
	 *@Description: TODO(取现对账) 
	 * @author cuihang   
	 *@date 2016-9-7 下午8:14:29 
	 *@return
	 */
	public JSONObject CashReconciliation(Date beginDate,Date endDate,int pageNum,int pageSize);
	/**
	 * 
	*@Description: TODO(放还款对账) 
	* @author cuihang   
	*@date 2016-9-7 下午8:39:40 
	*@param beginDate
	*@param endDate
	*@param pageNum
	*@param pageSize
	*@param queryTransType LOANS：放款交易查询 REPAYMENT：还款交易查询
	*@return
	 */
	public JSONObject Reconciliation(Date beginDate,Date endDate,int pageNum,int pageSize,String queryTransType);
	/**
	 * 
	*@Description: TODO(商户扣款对账) 
	* @author cuihang   
	*@date 2016-9-7 下午8:43:12 
	*@param beginDate
	*@param endDate
	*@param pageNum
	*@param pageSize
	*@return
	 */
	public JSONObject TrfReconciliation(Date beginDate,Date endDate,int pageNum,int pageSize);
}
