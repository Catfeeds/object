package com.zw.p2p.coupons.service;

import java.util.List;

import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.model.Invest;

/**
 * Company:     p2p <br/> 
 * Copyright:   Copyright (c)2013 <br/>
 * Description:  优惠券service
 * @author:     wangzhi  
 * @version:    1.0
 * Create at:   2014-7-16 下午2:28:20  
 *  
 * Modification History: <br/>
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014-7-16      wangzhi      1.0          
 */
public interface CouponSService {
	/**
	 * @Description: TODO(为某个用户发放某个规则的红包)
	 * @author cuihang
	 * @date 2017-3-21 上午10:44:54
	 * @param couponRule
	 * @param user
	 * @return
	 */
	public void giveCouponToUser(CouponRule couponRule, User user) throws Exception;
	/**
	*@Description: TODO(发送由投资产生的投资红包和邀请红包) 
	* @author cuihang   
	*@date 2017-3-21 下午2:37:09 
	*@param invest
	*@throws Exception
	 */
	public void giveCouponToUser(Invest invest) throws Exception;

	/**
	 * @Description: TODO(用户某个类型红包的列表)
	 * @author cuihang
	 * @date 2017-3-21 上午11:05:27
	 * @param resource
	 * @param user
	 * @return
	 */
	public List<Coupons> listByType(String resource, User user);
	
	
	/**
	 * 获取使用中状态的红包
	 * */
	public List<Coupons> getCouPonsByStatus();
	
	/**
	 * 更新红包状态
	 * */
	public void updateCoupon(Coupons coupons);
	
	/**
	*@Description: TODO(根据id获得红包信息) 
	* @author cuihang   
	*@date 2017-3-21 上午11:49:10 
	*@param id
	*@return
	 */
	public Coupons getCouponsById(String id);
	/**
	*@Description: TODO(使用红包) 
	* @author cuihang   
	*@date 2017-3-21 上午11:55:35 
	*@param couponsid
	 */
	public void userUserCoupon(String couponsid)throws Exception;
	/**
	*@Description: TODO(列出用户可用红包) 
	* @author cuihang   
	*@date 2017-3-21 下午5:13:23 
	*@param user
	*@return
	 */
	public List<Coupons> listCanUseCouponsByUser(User user,double investMoney);
	
	/**
	 * 列出用户所有可用红包
	 * @param user
	 * @return
	 */
	public List<Coupons> listAllUserCoupons(User user);
	
	/**
	 * 列出用户当天已使用的红包数量
	 * */
	public Long getTodayUsedCoupon(User user);
}
