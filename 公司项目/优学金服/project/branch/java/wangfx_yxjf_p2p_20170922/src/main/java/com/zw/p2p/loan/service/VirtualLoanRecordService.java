package com.zw.p2p.loan.service;

import java.util.List;
import java.util.Map;

import com.zw.archer.user.model.RegistCash;
import com.zw.p2p.loan.model.VirtualLoanRecord;

public interface VirtualLoanRecordService {
	/**
	 * @Description: TODO(根据虚拟标及条件返回要计算的利息的投标记录)
	 * @author cuihang
	 * @date 2015-12-16 下午3:29:47
	 * @param virtualloan
	 * @param param
	 * @return
	 */
	public List<VirtualLoanRecord> getVLRListByVlId(String virtualloanid, Map<String, String> param);
	public void updateVlr(VirtualLoanRecord vlr);
	/**
	*@Description: TODO(获得用户体验金详情) 
	* @author cuihang   
	*@date 2016-11-1 下午4:42:12 
	*@param userid
	*@return
	 */
	public RegistCash getRegistCashByUser(String userid);
	/**
	 * 
	*@Description: TODO(获得用户已投资体验金) 
	* @author cuihang   
	*@date 2016-11-2 上午11:59:25 
	*@param userid
	*@return
	 */
	
	public VirtualLoanRecord getVirtualLoanRecordByUser(String userid);
	/**
	 * 
	*@Description: TODO(新增体验标投资) 
	* @author cuihang   
	*@date 2016-11-1 下午6:46:19 
	*@param virtualLoanRecord
	*@return
	 */
	public String saveIn(String userid);
}
