package com.zw.p2p.safeloan.service;

import com.zw.p2p.safeloan.model.SafeLoan;

/**
 * 
 * @Description: TODO(无忧宝产品接口)
 * @author cuihang
 * @date 2016-1-8 下午12:24:35
 */
public interface SafeLoanService {
	/**
	 * 
	 * @Description: TODO(无忧宝产品新增获得递增id)
	 * @author cuihang
	 * @date 2016年1月19日 16:44:15
	 */
	public String createSafeLoanId();
	
	/**
	 * 删除无忧宝-只针对待审核状态产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:57:21
	 * @param id
	 * @return
	 */
	public String deleteSafeLoan(String id);
	
	/**
	 * 审核无忧宝-只针对待审核状态产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:57:21
	 * @param id
	 * @return
	 */
	public String verifySafeLoan(SafeLoan sl);
	/**
	 * 审核无忧宝-只针对待审核状态产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:57:21
	 * @param id
	 * @return
	 */
	public String disverifySafeLoan(SafeLoan sl);
	
	/**
	 * 复核无忧宝-只针对复核中状态产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:57:21
	 * @param id
	 * @return
	 */
	public String recheckSafeLoan(SafeLoan sl);
	/**
	 * 
	 * @Description: TODO(获得债权转让人id)
	 * @author cuihang
	 * @date 2016年1月20日 18:47:17
	 */
	public String getTUSER();
	/**
	 * 
	*@Description: TODO(无忧保金额不能大于当前平台所有未满标的剩余金额总和的百分比) 
	* @author cuihang   
	*@date 2016-2-29 下午5:10:39 
	*@param money
	*@return
	 */
	public boolean isPassHalf(Double money)throws Exception;
	/**
	*@Description: TODO(获得最新的投资中的无忧宝产品信息) 
	* @author cuihang   
	*@date 2016-3-24 下午12:40:43 
	*@return
	 */
	public SafeLoan getFirstSafeLoan();
	/**
	*@Description: TODO(根据id获得无忧宝信息) 
	* @author cuihang   
	*@date 2016-5-5 下午4:06:29 
	*@return
	 */
	public SafeLoan findSafeLoanById(String id);
}
