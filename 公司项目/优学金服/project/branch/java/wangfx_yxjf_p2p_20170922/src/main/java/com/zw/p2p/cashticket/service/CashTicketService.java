package com.zw.p2p.cashticket.service;

import java.util.List;

import com.zw.archer.user.model.User;
import com.zw.p2p.cashticket.model.CashTicket;

public interface CashTicketService {

	/**
	 * @Description: TODO(为某个用户发放现金券)
	 * @return
	 */
	public void giveCashTicketToUser(CashTicket cashTicket,User user) throws Exception;
	
	/**
	 * 用户使用现金券(只有方法  接汇付的时候加逻辑)
	 * @param id
	 */
	public void useCashTicket(CashTicket cashTicket);
	
	/**
	*@Description: TODO(获得用户可用现金券) 
	*@param userId
	*@return
	 */
	public List<CashTicket> getUnusedCashTicket(String userId);
	/**
	 * @return 根据指定用户获取已使用现金券
	 */
	public List<CashTicket> getAllCashTicket(String userId);
}
