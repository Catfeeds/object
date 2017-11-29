package com.zw.p2p.rateticket.service;

import java.util.List;

import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.model.RateTicketRule;

public interface RateTicketService {

	/**
	 * @Description: TODO(为某个用户发放某个规则的红包)
	 * @author cuihang
	 * @date 2017-3-21 上午10:44:54
	 * @param couponRule
	 * @param user
	 * @return
	 */
	public void giveRateTicketToUser(RateTicketRule rateTicketRule, User user) throws Exception;
	/**
	*@Description: TODO(发送由投资产生的投资红包和邀请红包) 
	* @author cuihang   
	*@date 2017-3-21 下午2:37:09 
	*@param invest
	*@throws Exception
	 */
	public void giveRateTicketToUser(Invest invest) throws Exception;
	
	/**
	 * 获取使用中状态的加息券
	 * */
	public List<RateTicket> getRateTicketByStatus();
	
	/**
	 * 更新红包状态
	 * */
	public void updateRateTicket(RateTicket rateTicket);
	
	/**
	 * @Description: TODO(用户某个类型红包的列表)
	 * @author cuihang
	 * @date 2017-3-21 上午11:05:27
	 * @param resource
	 * @param user
	 * @return
	 */
	public List<RateTicket> listByType(String resource, User user);
	/**
	*@Description: TODO(根据id获得红包信息) 
	* @author cuihang   
	*@date 2017-3-21 上午11:49:10 
	*@param id
	*@return
	 */
	public RateTicket getRateTicketById(String id);
	/**
	*@Description: TODO(使用红包) 
	* @author cuihang   
	*@date 2017-3-21 上午11:55:35 
	*@param couponsid
	 */
	public void userUserRateTicket(String ticketId)throws Exception;
	/**
	*@Description: TODO(列出用户可用红包) 
	* @author cuihang   
	*@date 2017-3-21 下午5:13:23 
	*@param user
	*@return
	 */
	public List<RateTicket> listCanUseRateTicketByUser(User user,double investMoney);
	
	/**
	 * 列出用户所有可用红包
	 * @param user
	 * @return
	 */
	public List<RateTicket> listAllUserRateTicket(User user);

	/**
	 * 列出用户当天已使用的加息券数量
	 * */
	public Long getTodayUsedRateTicket(User user);
}
