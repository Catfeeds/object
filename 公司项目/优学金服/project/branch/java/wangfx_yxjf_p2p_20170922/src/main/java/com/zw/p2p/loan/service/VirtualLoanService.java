package com.zw.p2p.loan.service;

import com.zw.p2p.loan.model.VirtualLoan;

public interface VirtualLoanService {
	/**
	*@Description: TODO(根据id获得虚拟标) 
	* @author cuihang   
	*@date 2015-12-16 下午3:23:58 
	*@param userId
	*@return
	*@throws Exception
	 */
	public  VirtualLoan getVirtualLoanById(String virtualloan) throws Exception;
}
