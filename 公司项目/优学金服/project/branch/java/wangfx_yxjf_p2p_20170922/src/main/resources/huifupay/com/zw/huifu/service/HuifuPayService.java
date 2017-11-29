package com.zw.huifu.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zw.p2p.loan.model.Recharge;

/**
 * 充值接口
 * @author majie
 * @date 2016年8月15日 下午4:47:02
 */
public interface HuifuPayService {

	/**
	 * 充值
	 * @author majie
	 * @param recharge
	 * @param usrCustId 用户客户号
	 * @date 2016年8月15日 下午4:50:34
	 */
	public String recharge(Recharge recharge,String usrCustId,String orderNo);
	
	/**
	 * 接收充值成功的参数
	 * @author majie
	 * @param request
	 * @param response
	 * @date 2016年8月16日 下午3:03:55
	 */
	public String receiveReturn(HttpServletRequest request, HttpServletResponse response);
}
