package com.zw.huifu.service;

import com.zw.huifu.bean.model.HuiFuLog;

public interface HuiFuLogService {
	/**
	 * 保存HuiFulog
	 * */
	public void SaveHuiFuLog(HuiFuLog huiFuLog);
	
	/**
	 * 根据MerPriv查找HuiFuLog
	 * */
	public HuiFuLog findHuiFuLogByMerPriv(String MerPriv);
	
	/**
	 * 更新HuiFuLog
	 * */
	public void updateHuiFuLog(HuiFuLog huiFuLog);
	
	/**
	 * 根据OrderId查找HuiFuLg
	 * */
	public HuiFuLog findHuiFuLogByOrderId(String orderId);
}
