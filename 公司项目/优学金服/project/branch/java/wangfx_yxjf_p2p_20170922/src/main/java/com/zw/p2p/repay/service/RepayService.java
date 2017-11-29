package com.zw.p2p.repay.service;

import java.util.Date;
import java.util.List;

import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.exception.AdvancedRepayException;
import com.zw.p2p.repay.exception.NormalRepayException;
import com.zw.p2p.repay.exception.OverdueRepayException;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;

/**
 * Filename: RepayManage.java Description: 还款管理接口 Copyright: Copyright (c)2013
 * Company: p2p
 * 
 * @author: bizhibo
 * @version: 1.0 Create at: 2014-1-4 上午11:42:20
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-4 bizhibo 1.0 1.0 Version
 */
public interface RepayService {

	/**
	 * 通过借款交易的id生成还款信息
	 * 
	 * @param loan
	 *            借款交易
	 */
	public void generateRepay(Loan loan);

	/**
	 * 正常还款
	 * 
	 * @param repayId
	 *            还款编号
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 */
	public void normalRepay(String repayId) throws InsufficientBalance,
			NormalRepayException;

	/**
	 * 正常还款
	 * 
	 * @param loanRepay 
	 *            还款
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 */
	public boolean normalRepay(LoanRepay loanRepay) throws InsufficientBalance,
			NormalRepayException;
	
	public boolean normalRepayUpdateAcct(LoanRepay loanRepay) throws InsufficientBalance;
	
	public boolean normalInvestRepay(LoanRepay loanRepay,String investId)throws InsufficientBalance;

	/**
	 * 提前还款
	 * 
	 * @param loanId
	 *            借款id
	 * @param amount
	 *            提前还款金额
	 * @throws AdvancedRepayException
	 *             不符合提前还款条件
	 * 
	 * @throws InsufficientBalance
	 *             余额不足
	 * 
	 */
	public void advanceRepay(String loanId) throws InsufficientBalance,
			AdvancedRepayException;
	
	/**
	 * 提前还款回掉
	 * @param loanId
	 * @Auth Songli Li
	 * @Date 2016年8月19日 下午3:15:08
	 */
	public void advanceRepayUpdateAcct(String loanId) throws InsufficientBalance;

	/**
	 * 逾期还款
	 * 
	 * @param repayId
	 *            还款id
	 * @throws InsufficientBalance
	 *             余额不足
	 * @throws OverdueRepayException 
	 */
	public void overdueRepay(String repayId) throws InsufficientBalance, OverdueRepayException;
	
	public void overdueInvestRepay(String investId,String repayId)throws InsufficientBalance,OverdueRepayException;
	
	/**
	 * 逾期还款回掉
	 * 
	 * @param repayId
	 *            还款id
	 * @throws InsufficientBalance
	 *             余额不足
	 * @throws OverdueRepayException 
	 */
	public void overdueRepayUpdateAcct(String repayId) throws InsufficientBalance, OverdueRepayException;

	/**
	 * 管理员进行逾期还款
	 * 
	 * @param repayId
	 *            还款id
	 * @param adminUserId
	 *            管理员用户id
	 */
	public void overdueRepayByAdmin(String repayId, String adminUserId);

	/**
	 * 还款到期，自动扣款，否则项目状态为逾期，还款和借款也变为逾期，锁定用户账号；如果还款已经逾期了，那么每天都计算该还款的逾期费用，
	 * 加入到还款的本金和利息中。逾期超过一年，则变为坏账。
	 */
	public void autoRepay();
	
	/**
	 * 判断是否在还款期
	 * 
	 * @param repayDate
	 *            还款日
	 */
	public boolean isInRepayPeriod(Date repayDate);

	/**
	 * 还款提醒，n天以内到还款日的还款，或者逾期的。给还款人发短信
	 */
	public void repayAlert();

	/**
	 * 检查所有项目，是否有逾期，如果逾期，则做相应处理
	 */
	public void checkLoanOverdue();

	/**处理还款延迟
	 *
	 */
	public void investDelay() throws InsufficientBalance,
			NormalRepayException;

	/**
	 * 根据投资记录还款
	 * @author majie
	 * @throws InsufficientBalance
	 * @throws NormalRepayException
	 * @date 2016年8月23日 下午12:21:11
	 */
	public void investRepay(String investId) throws InsufficientBalance,
			NormalRepayException;

	
	/**
	 * 处理还款延迟短信
	 */
	public void investSendSms() throws InsufficientBalance,
			NormalRepayException;
	
	/**
	 * 根据类型发送短信
	 * @author majie
	 * @param loanRepay
	 * @param type
	 * @date 2016年11月11日 下午12:00:00
	 */
	public void sendRepayMsg(LoanRepay loanRepay, String type);
	
	public void giveUserRateMoney(Invest invest) throws Exception;
	
	/**
	 * 得到所有没有转账的投资记录
	 * @return
	 */
	public List<Invest> getUnRepayInvest();
}
