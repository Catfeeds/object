package com.zw.p2p.risk.service;

import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.privilege.model.Privilege;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.safeloan.model.SafeLoanRecord;

/**
 * 系统收益账户service
 * @author Administrator
 *
 */
public interface SystemBillService {

	/**
	 * 获取最新一条数据
	 * @return
	 */
//	public SystemBill getLastestBill();
	
	/**
	 * 获取账户余额
	 * @return
	 */
	public double getBalance();

	/**
	 * 转出
	 * @param money 金额
	 * @param reason 操作类型
	 * @param detail 操作详情
	 * 
	 * @throws InsufficientBalance 余额不足
	 */
	public void transferOut(double money, String reason, String detail,LoanRepay loanRepay ,WithdrawCash withdrawCash , InvestRepay investRepay ,Recharge recharge,Invest invest,SafeLoanRecord slr,Coupons coupons,Privilege privilege,User user) throws InsufficientBalance;
	
	/**
	 * 转入.
	 * @param money 金额
	 * @param reason 操作类型
	 * @param detail 操作详情
	 */
	public void transferInto(double money, String reason, String detail , LoanRepay loanRepay ,WithdrawCash withdrawCash , InvestRepay investRepay , Recharge recharge,Invest invest,SafeLoanRecord slr,Privilege privilege,User user);

	/**
	 * 手续费调整
	 */
	public void adjustFee(double money,String reason,String detail);
}
