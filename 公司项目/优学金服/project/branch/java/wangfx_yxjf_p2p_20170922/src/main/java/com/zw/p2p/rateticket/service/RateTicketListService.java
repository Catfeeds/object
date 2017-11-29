package com.zw.p2p.rateticket.service;

public interface RateTicketListService {
	/**
	*@Description: TODO(获得用户可用加息券) 
	* @author cuihang   
	*@date 2016-9-7 下午3:35:05 
	*@param id
	*@return
	 */
	public Double getUnusedRateTicket(String id);
	/**
	 * @return 根据指定用户获取已使用加息券
	 */
	public Double getUsedRateTicket(String id);
	
	/**
	 * 查询用户当日所用加息券数量
	 * @param userId
	 * @return
	 */
	public Double getTodayUserRateTickey(String userId);
}
