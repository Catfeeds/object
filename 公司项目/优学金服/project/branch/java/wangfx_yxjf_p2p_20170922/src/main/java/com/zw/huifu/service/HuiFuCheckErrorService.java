package com.zw.huifu.service;

import com.zw.archer.user.model.User;


/**
* @Description: TODO(汇付对账服务接口) 
* @author cuihang   
* @date 2017-8-2 下午2:43:36
 */
public interface HuiFuCheckErrorService {
	
	/**
	*@Description: TODO(检测用户余额) 
	* @author cuihang   
	*@date 2017-8-2 下午2:47:33 
	*@param user
	 */
	public String checkUserBalance(User user);
	/**
	*@Description: TODO(清空记录) 
	* @author cuihang   
	*@date 2017-8-2 下午5:51:54
	 */
	public void cleanHuiFuCheckError();
}


