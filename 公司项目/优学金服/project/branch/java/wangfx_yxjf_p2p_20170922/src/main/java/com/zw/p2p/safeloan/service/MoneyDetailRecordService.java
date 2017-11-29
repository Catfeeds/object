package com.zw.p2p.safeloan.service;

public interface MoneyDetailRecordService {
	/**
	 * @Description: TODO(新增资金明细)
	 * @author cuihang
	 * @date 2016年1月19日 17:37:09
	 * @param loanid
	 * @param loanName
	 * @param safeloanid
	 * @param safeloanrecordid
	 * @param type
	 * @param inmoney
	 * @param outmoney
	 */
	public void addRecord(String loanid,String safeloanid, String safeloanrecordid, int type, double inmoney, double outmoney);
}
