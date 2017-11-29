package com.zw.huifu.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 交易类接口
 */
public interface HuiFuTradeService {

	/**
	 * 取现（页面）
	 * @param userCustId 用户客户号
	 * @param transAmt 交易金额
	 * @param servFee 商户收取服务费金额(可选)
	 * @param servFeeAcctId 商户子账户号(可选)
	 * @param openAcctId 开户银行账号(可选)
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月15日 下午4:33:46
	 */
	public String tradeCash(String UsrCustId, String TransAmt,String ServFee,String OpenAcctId,String MerPriv);
	
	/**
	 * 提现校验
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月17日 上午11:02:47
	 */
	public String tradeCashReturn(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 提现复合接口通过
	 * @param UsrCustId HuiFu用户id
	 * @param TransAmt 交易金额
	 * @param AuditFlag 符合状态 R-拒绝 S-复合通过
	 * @param MerPriv 用户私有域用来存储支付公司的id
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月17日 下午2:11:03
	 */
	public String tradeCashAudit(String UsrCustId,String TransAmt,String AuditFlag,String OrderId);
	
	/**
	 * 复核回调接口通过
	 * @param request
	 * @param response
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月18日 下午2:35:08
	 */
	public String tradeCashAuditReturn(HttpServletRequest request, HttpServletResponse response);
	/**
	 * 复核异步对账回调接口通过
	 * @param request
	 * @param response
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月18日 下午2:35:08
	 */
	public String tradeAsynCashAuditReturn(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 自动扣款（还款）
	 * @param OutCustId
	 * @param TransAmt
	 * @param Fee
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月17日 下午6:38:24
	 */
	public String tradeRepayment(String orderId,String ProId,String OutCustId,String SubOrdId,String SubOrdDate,String PrincipalAmt,String InterestAmt,String Fee,String InCustId,String MerPriv);
	
	/**
	 * 自动扣款回调
	 * @param request
	 * @param response
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月19日 下午1:15:50
	 */
	public String tradeRepaymentReturn(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 投标撤销接口
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月23日 下午7:41:54
	 */
	public String tradeTenderCancle(String OrdId,String OrdDate,String TransAmt,String UsrCustId,String UnFreezeOrdId,String MerPriv,String FreezeTrxId);
	
	/**
	 * 投标撤销回调
	 * @param request
	 * @param response
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月19日 下午1:15:50
	 */
	public String tradeTenderCancleReturn(HttpServletRequest request, HttpServletResponse response);
}
