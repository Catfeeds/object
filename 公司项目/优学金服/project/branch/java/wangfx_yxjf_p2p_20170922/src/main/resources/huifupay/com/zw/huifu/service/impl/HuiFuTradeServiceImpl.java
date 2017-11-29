package com.zw.huifu.service.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;
import com.zw.core.util.SpringBeanUtil;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.fee.model.FeeSchemePay;
import com.zw.p2p.fee.service.FeeSchemePayService;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.repay.service.RepayService;
import com.zw.p2p.user.service.WithdrawCashService;

@Service("huiFuTradeService")
public class HuiFuTradeServiceImpl implements HuiFuTradeService {
	@Resource
	private HibernateTemplate ht;
	
	@Resource
	private WithdrawCashService withdrawCashService;
	
	@Resource
	private FeeSchemePayService feeSchemePayService;
	
	@Resource
	private RepayService repayService;
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	@Resource
	private HuiFuHttpUtilService huiFuHttpUtilService;
	
	@Override
	public String tradeCash(String UsrCustId, String TransAmt,String ServFee,String OpenAcctId,String MerPriv) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("Version", "20");// 2.0 接口中此字段的值为 20，如版本升级，能向前兼容
		params.put("CmdId", "Cash");// 每一种消息类型代表一种交易，此处为 Cash
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);// 由汇付生成，商户的唯一性标识
		params.put("OrdId", OrderNoService.getOrderNo());// 由商户的系统生成，必须保证唯一，请使用纯数字
		params.put("UsrCustId", UsrCustId);// 由汇付生成，用户的唯一性标识
		params.put("TransAmt", TransAmt);// 泛指交易金额，如充值、支付、取现、冻结和解冻金额（金额格式必须是###.00）比如 2.00，2.0
//		params.put("ServFee", ServFee);
//		params.put("ServFeeAcctId", HuiFuConstants.Config.DIV_ACCT_ID);
//		params.put("OpenAcctId", OpenAcctId);//银行账户
		params.put("RetUrl", HuiFuConstants.Config.RETURN_URL+"withdraw_return_web");
		params.put("BgRetUrl", HuiFuConstants.Config.BACK_URL);// 通过后台异步通知，商户网站都应在应答接收页面输出 RECV_ORD_ID 字样的字符串， 表明商户已经收到该笔交易结果
		params.put("MerPriv", MerPriv);
		String ReqExt = "[{\"FeeObjFlag\":\"M\",\"FeeAcctId\":\"MDT000001\"}]";
		params.put("ReqExt", ReqExt);
		try {
			
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String tradeCashReturn(HttpServletRequest request, HttpServletResponse response) {
		String respCode = request.getParameter("RespCode");
		String UsrCustId = request.getParameter("UsrCustId");
		String TransAmt = request.getParameter("TransAmt");
		String RealTransAmt = request.getParameter("RealTransAmt");
		String OrdId = request.getParameter("OrdId");
		String FeeAmt = request.getParameter("FeeAmt");//汇付收取手续费金额
		String bankId = request.getParameter("MerPriv");
		System.out.println(OrdId);
		try{
			List list = ht.find("from WithdrawCash wh where wh.huiFuOrderId='"+OrdId+"'");
			if(list.size()==0){
				if("000".equals(respCode)){
					List<User> users = ht.find("from User u where u.usrCustId=?",new Object[]{UsrCustId});
					WithdrawCash withdraw = new WithdrawCash();
					withdraw.setUser(users.get(0));
					BankCard bankCard = ht.get(BankCard.class, bankId);
					withdraw.setBankCard(bankCard);
					withdraw.setMoney(Double.valueOf(RealTransAmt));
					withdraw.setApplyMoney(Double.valueOf(TransAmt));
					withdraw.setHuiFuOrderId(OrdId);
					withdraw.setHuiFuFee(Double.valueOf(FeeAmt));
					withdrawCashService.applyWithdrawCash(withdraw);
					return "RECV_ORD_ID_".concat(OrdId);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
				 try {
					response.getWriter().println("fail");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		return "fail";
	}

	@Override
	public String tradeCashAudit(String UsrCustId, String TransAmt, String AuditFlag,String OrderId) {
		try{
			JSONObject jso=	huiFuMoneyService.queryTransStat(OrderId, OrderNoService.getNowDate(), "CASH");
			if("000".equals(jso.getString("RespCode"))){
				String QueryTransType=jso.getString("QueryTransType");
				if("CASH".equals(QueryTransType)){
					//取现
					String TransStat=jso.getString("TransStat");
					if("S".equals(TransStat)){
						//成功
						String withdrawId = withdrawCashService.getIdByHuiFuOrderId(OrderId);
						List<User> users = ht.find("from User u where u.usrCustId=?",new Object[]{UsrCustId});
						FeeSchemePay pay = feeSchemePayService.getFeeSchemePayById("huifu_default");
						WithdrawCash withdrawCash = new WithdrawCash();
						withdrawCash.setId(withdrawId);
						withdrawCash.setRecheckMessage("审核通过");
						withdrawCash.setRecheckUser(users.get(0));
						withdrawCash.setPay(pay);
						withdrawCashService.passWithdrawCashRecheck(withdrawCash);
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		Map<String,String> params = new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "CashAudit");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);// 由汇付生成，商户的唯一性标识
		params.put("OrdId", OrderId);// 由商户的系统生成，必须保证唯一，请使用纯数字
		params.put("UsrCustId", UsrCustId);// 由汇付生成，用户的唯一性标识
		params.put("TransAmt", TransAmt);// 泛指交易金额，如充值、支付、取现、冻结和解冻金额（金额格式必须是###.00）比如 2.00，2.0
		params.put("AuditFlag", AuditFlag);//复合标志 R-拒绝 S-复合通过
//		params.put("RetUrl", HuiFuConstants.Config.RETURN_URL+"withdraw_return_CashAudit");
		params.put("BgRetUrl", HuiFuConstants.Config.BACK_URL);// 通过后台异步通知，商户网站都应在应答接收页面输出 RECV_ORD_ID 字样的字符串， 表明商户已经收到该笔交易结果
		try {
			return huiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String tradeCashAuditReturn(HttpServletRequest request, HttpServletResponse response){
		String RespCode = request.getParameter("RespCode");
		String OrdId = request.getParameter("OrdId");
		String UsrCustId = request.getParameter("UsrCustId");
		String AuditFlag = request.getParameter("AuditFlag");
		try {
			if("000".equals(RespCode)){
				if(("S").equals(AuditFlag)){
					String withdrawId = withdrawCashService.getIdByHuiFuOrderId(OrdId);
					List<User> users = ht.find("from User u where u.usrCustId=?",new Object[]{UsrCustId});
					FeeSchemePay pay = feeSchemePayService.getFeeSchemePayById("huifu_default");
					WithdrawCash withdrawCash = new WithdrawCash();
					withdrawCash.setId(withdrawId);
					withdrawCash.setRecheckMessage("审核通过");
					withdrawCash.setRecheckUser(users.get(0));
					withdrawCash.setPay(pay);
					withdrawCashService.passWithdrawCashRecheck(withdrawCash);
					
					return "RECV_ORD_ID_".concat(OrdId);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
		return "fail";
	}
	@Override
	public String tradeAsynCashAuditReturn(HttpServletRequest request, HttpServletResponse response){
		String RespCode = request.getParameter("RespCode");
		String OrdId = request.getParameter("OrdId");
		String UsrCustId = request.getParameter("UsrCustId");
		try {
			if("000".equals(RespCode)){
					String withdrawId = withdrawCashService.getIdByHuiFuOrderId(OrdId);
					List<User> users = ht.find("from User u where u.usrCustId=?",new Object[]{UsrCustId});
					FeeSchemePay pay = feeSchemePayService.getFeeSchemePayById("huifu_default");
					WithdrawCash withdrawCash = new WithdrawCash();
					withdrawCash.setId(withdrawId);
					withdrawCash.setRecheckMessage("审核通过");
					withdrawCash.setRecheckUser(users.get(0));
					withdrawCash.setPay(pay);
					withdrawCashService.passWithdrawCashRecheck(withdrawCash);
					return "RECV_ORD_ID_".concat(OrdId);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
		return "fail";
	}

	@Override
	public String tradeRepayment(String orderId,String ProId,String OutCustId,String SubOrdId,String SubOrdDate,String PrincipalAmt,String InterestAmt,String Fee,String InCustId,String MerPriv) {
		Map<String,String> params = new LinkedHashMap<String, String>();
		params.put("Version", "30");
		params.put("CmdId", "Repayment");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("ProId", ProId);
		params.put("OrdId", orderId);// 由商户的系统生成，必须保证唯一，请使用纯数字
		params.put("OrdDate", OrderNoService.getNowDate());
		params.put("OutCustId", OutCustId);
		params.put("SubOrdId", SubOrdId);
		params.put("SubOrdDate", SubOrdDate);
		params.put("PrincipalAmt", PrincipalAmt);
		params.put("InterestAmt", InterestAmt);
		params.put("Fee", Fee);
		params.put("InCustId", InCustId);
		if(Double.valueOf(Fee)>0){
			//分账帐户串
			StringBuffer DivDetails = new StringBuffer();
			DivDetails.append("[{");
			DivDetails.append("\"DivCustId\":\"").append(HuiFuConstants.Config.MERCUSTID).append("\",");
			DivDetails.append("\"DivAcctId\":\"").append(HuiFuConstants.Config.DIV_ACCT_ID).append("\",");
			DivDetails.append("\"DivAmt\":\"").append(Fee).append("\"");
			DivDetails.append("}]");
			params.put("DivDetails", DivDetails.toString());
			params.put("FeeObjFlag", "O");
		}
		params.put("BgRetUrl", HuiFuConstants.Config.BACK_URL);// 通过后台异步通知，商户网站都应在应答接收页面输出 RECV_ORD_ID 字样的字符串， 表明商户已经收到该笔交易结果
		params.put("MerPriv", MerPriv);
		try {
			return huiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String tradeRepaymentReturn(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("--------------还款开始-----------------");
		String RespCode = request.getParameter("RespCode");
		String OrdId = request.getParameter("OrdId");
		String MerPriv = request.getParameter("MerPriv");
		String[] MerPrivs = MerPriv.split(",");
		try{
			if("000".equals(RespCode)){
				LoanRepay repay = ht.get(LoanRepay.class, MerPrivs[1]);
				if(!RepayStatus.REPAYING.equals(repay.getStatus()) && !RepayStatus.OVERDUE.equals(repay.getStatus())){
					System.out.println("--------------"+MerPrivs[1]+"还款已结束，不再执行方法-----------------");
					return "fail";
				}
				if("normal".equals(MerPrivs[0])){
					//更新投资人账户信息
					String investId=MerPrivs[3];
					int loanNum = HuiFuConstants.getLoanNum(MerPrivs[1],OrdId);
					repayService.normalInvestRepay(repay, investId);
					if(loanNum==Integer.parseInt(MerPrivs[2])){
						repayService.normalInvestRepay(repay, investId);
						repayService.normalRepayUpdateAcct(repay);
						repayService.investRepay(repay.getLoan().getId());
						repayService.sendRepayMsg(repay, "normal");
						HuiFuConstants.LOAN_PAY_NUM.remove(MerPrivs[1]);
						System.out.println("--------------"+MerPrivs[1]+"还款成功-----------------");
					}
				}else if("advance".equals(MerPrivs[0])){
					//更新投资人账户信息
					String investId=MerPrivs[3];
					repayService.overdueInvestRepay(investId, MerPrivs[1]);
					int loanNum = HuiFuConstants.getLoanNum(MerPrivs[1],OrdId);
					if(loanNum==Integer.parseInt(MerPrivs[2])){
						repayService.advanceRepayUpdateAcct(MerPrivs[1]);
						repayService.investRepay(repay.getLoan().getId());
						repayService.sendRepayMsg(repay, "advance");
						HuiFuConstants.LOAN_PAY_NUM.remove(MerPrivs[1]);
						System.out.println("--------------"+MerPrivs[1]+"还款成功-----------------");
					}
					
				}else if("overdue".equals(MerPrivs[0])){
					//更新投资人账户信息
					int loanNum = HuiFuConstants.getLoanNum(MerPrivs[1],OrdId);
					if(loanNum==Integer.parseInt(MerPrivs[2])){
						repayService.overdueRepayUpdateAcct(MerPrivs[1]);
						repayService.investRepay(repay.getLoan().getId());
						repayService.sendRepayMsg(repay, "overdue");
						HuiFuConstants.LOAN_PAY_NUM.remove(MerPrivs[1]);
						System.out.println("--------------"+MerPrivs[1]+"还款成功-----------------");
					}
					
				}
				return "RECV_ORD_ID_".concat(OrdId);
			}
		}catch(Exception e){
			e.printStackTrace();
			return "fail";
		}
		System.out.println("--------------"+MerPrivs[1]+"还款失败-----------------");
		return "fail";
	}

	@Override
	public String tradeTenderCancle(String OrdId,String OrdDate,String TransAmt, String UsrCustId, String UnFreezeOrdId,String MerPriv,String FreezeTrxId) {
		Map<String,String> params = new LinkedHashMap<String, String>();
		params.put("Version", "20");
		params.put("CmdId", "TenderCancle");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("OrdId", OrdId);// 由商户的系统生成，必须保证唯一，请使用纯数字
		params.put("OrdDate", OrdDate);
		params.put("TransAmt", TransAmt);
		params.put("UsrCustId", UsrCustId);
		params.put("IsUnFreeze", "N");
		params.put("UnFreezeOrdId", UnFreezeOrdId);
		params.put("FreezeTrxId", FreezeTrxId);
		params.put("RetUrl", HuiFuConstants.Config.RETURN_URL+"fail_manager_return");
		params.put("BgRetUrl", HuiFuConstants.Config.BACK_URL);// 通过后台异步通知，商户网站都应在应答接收页面输出 RECV_ORD_ID 字样的字符串， 表明商户已经收到该笔交易结果
		params.put("MerPriv", MerPriv);
		try {
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String tradeTenderCancleReturn(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("--------------投标撤销开始-----------------");
		String RespCode = request.getParameter("RespCode");
		String OrdId = request.getParameter("OrdId");
		String MerPriv = request.getParameter("MerPriv");
		String[] MerPrivs = MerPriv.split(",");
		try {
			if("000".equals(RespCode)){
				int loanNum = HuiFuConstants.getLoanNum(MerPrivs[0],OrdId);
				if(loanNum==Integer.parseInt(MerPrivs[2])){
					LoanService loanService = (LoanService) SpringBeanUtil.getBeanByName("loanService");
					String loanId=MerPrivs[0];
					loanService.failReturn(loanId, MerPrivs[1]);
					Loan loan=ht.get(Loan.class, loanId);
					loanService.sendVerifyMsg(loan.getUser());//流标发给借款人
					loanService.sendFailMsg(loan);//流标发给投资人
					System.out.println("--------------投标撤销成功-----------------");
					return "RECV_ORD_ID_".concat(OrdId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("--------------投标撤销失败-----------------");
			return "fail";
		}
		System.out.println("--------------投标撤销失败-----------------");
		return "fail";
	}

}
