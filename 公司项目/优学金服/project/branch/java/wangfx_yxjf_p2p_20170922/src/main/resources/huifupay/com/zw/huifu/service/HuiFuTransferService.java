package com.zw.huifu.service;

import com.zw.core.bean.ZwJson;

/**
 * 汇付转账接口
 * @author majie
 * @date 2016年8月29日 上午11:30:54
 */
public interface HuiFuTransferService {

	/**
	 * 给用户转账    (谨慎使用，一旦调用钱就立刻转账)
	 * @author majie
	 * @param inCustId 入账客户号
	 * @param transAmt 交易金额 
	 * @return
	 * @date 2016年8月29日 上午11:34:30
	 */
	public ZwJson transferMoney(String inCustId,Double transAmt);
	
}