package com.zw.p2p.coupons.service;


public interface CouponListService {
	/**
	*@Description: TODO(获得用户可用红包) 
	* @author cuihang   
	*@date 2016-9-7 下午3:35:05 
	*@param id
	*@return
	 */
	public Double getUnusedCoupons(String id);
	/**
	 * @return 根据指定用户获取已使用红包
	 */
	public Double getUsedCoupons(String id);
	/**
	 * 查询用户当日所用加息券数量
	 * @param userId
	 * @return
	 */
	public Double getTodayUserCoupons(String userId);
}
