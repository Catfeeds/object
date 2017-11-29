package com.zw.p2p.loan.service;

import java.util.Date;
import java.util.List;

import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.user.model.User;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.exception.*;
import com.zw.p2p.loan.model.ApplyEnterpriseLoan;
import com.zw.p2p.loan.model.Loan;

/**
 * Filename: LoanService.java Description: 借款serviceCopyright: Copyright (c)2013
 * Company: p2p
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-4 下午3:36:30
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-4 wangzhi 1.0
 */
public interface LoanService {

	
	/**
	 * 流标
	 * @param loanId 借款id
	 * @param operatorId 操作用户id
	 * @throws ExistWaitAffirmInvests 
	 */
	public boolean fail(String loanId, String operatorId) throws ExistWaitAffirmInvests;
	
	/**
	 * 流标返回
	 * @param loanId
	 * @param operatorId
	 * @Auth Songli Li
	 * @Date 2016年8月23日 下午8:07:53
	 */
	public void failReturn(String loanId, String operatorId) throws ExistWaitAffirmInvests;


	/**
	 * 延长借款募集时间，即延长预计执行时间
	 * 
	 * @param loanId
	 *            借款id
	 * @param newExpectTime
	 *            新的预计执行时间
	 * @throws InvalidExpectTimeException  预计执行时间设置错误
	 */
	public void delayExpectTime(String loanId, Date newExpectTime) throws InvalidExpectTimeException;

	/**
	 * 借款放款，即借款执行，转钱给借款者。
	 * @param loanId
	 * @throws ExistWaitAffirmInvests 存在等待第三方资金托管确认的投资，不能放款。
	 * @throws BorrowedMoneyTooLittle 募集到的资金太少，为0、或者不足以支付借款保证金
	 */
	public void giveMoneyToBorrower(String loanId) throws ExistWaitAffirmInvests, BorrowedMoneyTooLittle;


	/**
	 * 借款复核通过，即可进入放款流程
	 * @param loanId
	 */
	public void recheckLoan(String loanId) throws Exception;
	/**
	 * 借款申请，通过审核
	 * 
	 * @param loanId
	 *            借款id
	 * @param auditInfo
	 *            审核信息
	 * @param verifyUserId
	 *            审核人编号
	 * @throws InvalidExpectTimeException  预计执行时间设置错误
	 */
	public void passApply(Loan loan) throws InvalidExpectTimeException, InsufficientBalance;
	/**
	 * 汇付审核通过更新状态
	 * @param loan
	 * @throws InvalidExpectTimeException
	 * @throws InsufficientBalance
	 */
	public void huiFuPassApply(String  ProId);
	/**
	 * 初审通过后，自动给所有人发送短信
	 */
	public void autoSendLoanMessage(Loan loan);
	/**
	 * 借款申请，未通过审核，即拒绝借款申请
	 * 
	 * @param loanId
	 *            借款id
	 * @param refuseInfo
	 *            审核信息
	 * @param verifyUserId
	 *            审核人编号
	 */
	public void refuseApply(String loanId, String refuseInfo,
			String verifyUserId);

	/**
	 * 判断借款是否已完成，即是否所有的还款都还了
	 * 
	 * @param loanId
	 *            借款id
	 * @return 是否已完成
	 */
	public boolean isCompleted(String loanId);

	/**
	 * 处理借款完成工作，即改借款状态、与之相关的投资状态等。
	 * @param loanId 借款id
	 * @param isInvest 是否需要考虑投资记录 true为考虑
	 * @return
	 */
	public boolean dealComplete(String loanId,boolean isInvest);

	/**
	 * 检查借款是否募集完成
	 * 
	 * @param loanId
	 *            借款编号
	 * @return
	 * @throws NoMatchingObjectsException 找不到loan
	 */
	public boolean isRaiseCompleted(String loanId) throws NoMatchingObjectsException;

	/**
	 * 处理借款募集完成。
	 * 
	 * @param loanId
	 *            借款编号
	 * @throws NoMatchingObjectsException 找不到loan
	 */
	public void dealRaiseComplete(String loanId) throws NoMatchingObjectsException;

	/**
	 * 检查借款是否到预计执行时间
	 * 
	 * @param loanId
	 *            借款id
	 */
	public boolean isOverExpectTime(String loanId);

	/**
	 * 借款到预计执行时间，处理借款
	 * 
	 * @param loanId
	 *            借款id
	 */
	public void dealOverExpectTime(String loanId);

	/**
	 * 申请借款
	 * 
	 * @param loan
	 * @throws InsufficientBalance
	 *             余额不足以支付保证金
	 */
	public void applyLoan(Loan loan) throws InsufficientBalance;

	
	/**
	 * 申请借款
	 * @param ael 借款对象
	 */
	public void applyEnterpriseLoan(ApplyEnterpriseLoan ael);
	
	/**
	 * 审核借款---借款申请
	 * @param ael 借款对象
	 */
	public void verifyEnterpriseLoan(ApplyEnterpriseLoan ael);
	
	/**
	 * 更新loan
	 * @param loan
	 */
	public void update(Loan loan);

	/**
	 * 管理员添加借款
	 * @param loan
	 * @throws InsufficientBalance
	 * @throws InvalidExpectTimeException
	 */
	public void createLoanByAdmin(Loan loan) throws InvalidExpectTimeException,InsufficientBalance,LoanException;

	
	/**
	 * 获取某笔借款里成功的投资
	 * @param loanId
	 */
	public List<Invest> getSuccessfulInvests(String loanId);
	/**
	*@Description: TODO(返回债权预期完成时间，已完成则返回完成时间) 
	* @author cuihang   
	*@date 2016-3-30 下午4:59:45 
	*@param loanId
	*@return
	 */
	public Date loanFinishTime(String loanId);
	/**
	*@Description: TODO(获得第一个推荐散标) 
	* @author cuihang   
	*@date 2016-4-5 下午5:14:16 
	*@return
	 */
	public Loan getFirstTuiJian();
	/**
	*@Description: TODO(根据id获取散标信息) 
	* @author cuihang   
	*@date 2016-5-5 下午3:59:44 
	*@param id
	*@return
	 */
	public Loan findLoanById(String id);
	
	/**
	 * 放款后发送信息
	 */
	public void sendInvestMsg(String loanId);
	/**
	 * 
	*@Description: TODO(获得复核中的loan) 
	* @author cuihang   
	*@date 2016-9-6 下午2:43:08 
	*@return
	 */
	public List<Loan> getRecheckLoanList();
	/**
	 * 
	*@Description: TODO(流标发送短信给借款人) 
	* @author cuihang   
	*@date 2016-9-8 上午10:22:00 
	*@param user
	 */
	public void sendVerifyMsg(User user);
	/**
	 * 
	*@Description: TODO(流标发送短信给投资人) 
	* @author cuihang   
	*@date 2016-9-8 上午10:22:39 
	*@param loan
	 */
	public void sendFailMsg(Loan loan);
	/**
	 * 
	*@Description: TODO(获得这个用户待审核的借款) 
	* @author cuihang   
	*@date 2016-9-18 下午5:47:07 
	*@return
	 */
	public List<Loan> getUserVerify(User user);
}
