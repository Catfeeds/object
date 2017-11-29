package com.zw.archer.user.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.archer.user.service.UserService;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.bean.ZwJson;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.bean.model.HuiFuLog;
import com.zw.huifu.service.HuiFuLogService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.huifu.service.HuifuPayService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.SignUtils;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.InvestConstants.InvestStatus;
import com.zw.p2p.invest.InvestConstants.TransferStatus;
import com.zw.p2p.invest.exception.ExceedMoneyNeedRaised;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.service.RateTicketService;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.user.service.WithdrawCashService;
@Component
@Scope(ScopeType.VIEW)
public class HuiFuHome {

	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;
	
	@Resource
	UserBillService ubs;
	
	@Resource
	private UserService userService;
	@Resource
	private BankCardService bankCardService;
	@Resource
	LoanCalculator loanCalculator;
	@Resource
	private HuifuPayService huiFuPayService;
	@Resource
	private HuiFuMoneyService huiFuMoneyService;
	@Resource
	private LoanService loanService;
	@Resource
	private HuiFuTradeService huiFuTradeService;
	@Resource
	private TransferService transferService;
	@Resource
	UserBillBO userBillBO;
	@Resource
	SystemBillService sbs;
	@Resource
	private InvestService investService;
	@Resource
	private AutoMsgService autoMsgService;
	@Resource
	private WithdrawCashService withdrawCashService;
	@Resource
	private HuiFuLogService huiFuLogService;
	@Resource
	private CouponSService couponSService;
	@Resource
	private RateTicketService rateTicketService;
	/**
	 * 投资超标的 需要解冻暂存
	 */
	public static  Map<String ,String> hasInvestOut=new HashMap<String, String>();
	//private static final String useridb = "smt_";
	//存储各类型请求参数，对应参数（数组）
	private static final Map<String ,String[]> cmdIdPara=getCmdIdPara();
	private static Map<String ,String[]> getCmdIdPara(){
		Map<String ,String[]> result=new HashMap<String, String[]>();
		String[] UserRegister=new String[]{"CmdId","RespCode","MerCustId","UsrId","UsrCustId","BgRetUrl","TrxId","RetUrl","MerPriv"};
		String[] NetSave=new String[]{"CmdId","RespCode","MerCustId","UsrCustId","OrdId","OrdDate","TransAmt","TrxId","RetUrl","BgRetUrl","MerPriv"};
		String[] UserBindCard=new String[]{"CmdId","RespCode","MerCustId","OpenAcctId","OpenBankId","UsrCustId","TrxId","BgRetUrl","MerPriv"};
		String[] AddBidInfo=new String[]{"CmdId","RespCode","MerCustId","ProId","AuditStat","BgRetUrl","MerPriv","RespExt"};
		String[] CashInfo = new String[]{"CmdId","RespCode","MerCustId","OrdId","UsrCustId","TransAmt","OpenAcctId","OpenBankId","FeeAmt","FeeCustId","FeeAcctId","ServFee","ServFeeAcctId","RetUrl","BgRetUrl","MerPriv","RespExt"};
		String[] Loans = new String[]{"CmdId","RespCode","MerCustId","OrdId","OrdDate","OutCustId","OutAcctId","TransAmt","Fee","InCustId","InAcctId","SubOrdId","SubOrdDate","FeeObjFlag","IsDefault","IsUnFreeze","UnFreezeOrdId","FreezeTrxId","BgRetUrl","MerPriv","RespExt"};
		String[] UsrFreezeBg=new String[]{"CmdId","RespCode","MerCustId","UsrCustId","SubAcctType","SubAcctId","OrdId","OrdDate+TransAmt","RetUrl","BgRetUrl","TrxId","MerPriv"};
		String[] UsrUnFreeze=new String[]{"CmdId","RespCode","MerCustId","OrdId","OrdDate","TrxId","RetUrl","BgRetUrl","MerPriv"};
		String[] CashAudit = new String[]{"CmdId","RespCode","MerCustId","OrdId","UsrCustId","TransAmt","OpenAcctId","OpenBankId","AuditFlag","RetUrl","BgRetUrl","MerPriv"};
		String[] asynCashAudit = new String[]{"RespType","RespCode","MerCustId","OrdId","UsrCustId","TransAmt","OpenAcctId","OpenBankId","RetUrl","BgRetUrl","MerPriv","RespExt"};
		String[] Repayment = new String[]{"CmdId","RespCode","MerCustId","ProId","OrdId","OrdDate","OutCustId","SubOrdId","SubOrdDate","OutAcctId"," PrincipalAmt","InterestAmt","Fee","InCustId","InAcctId","FeeObjFlag","DzObject","BgRetUrl","MerPriv","RespExt"};
		String[] InitiativeTender = new String[]{"CmdId","RespCode","MerCustId","OrdId","OrdDate","TransAmt","UsrCustId","TrxId","IsFreeze"," FreezeOrdId","FreezeTrxId","RetUrl","BgRetUrl","MerPriv","RespExt"};
		String[] TenderCancle = new String[]{"Version","CmdId","MerCustId","OrdId","OrdDate","TransAmt","UsrCustId","IsUnFreeze","UnFreezeOrdId","FreezeTrxId","RetUrl","BgRetUrl","MerPriv","ReqExt","PageType"};
		String[] PnrUsrUnBindExpressCard = new String[]{"CmdId","RespCode","MerCustId","CustId","TrxId","BankId","CardId","ExpressFlag","BgRetUr"};
		String[] CreditAssign = new String[]{"CmdId","RespCode","MerCustId","SellCustId","CreditAmt","CreditDealAmt","Fee","BuyCustId","OrdId","OrdDate","RetUrl","BgRetUrl","MerPriv","RespExt","LcId","TotalLcAmt"};
		String[] Transfer = new String[]{"CmdId","RespCode","OrdId","OutCustId","OutAcctId","TransAmt","InCustId","InAcctId","RetUrl","BgRetUrl","MerPriv"};
		
		result.put("UserRegister", UserRegister);
		result.put("NetSave", NetSave);
		result.put("UserBindCard", UserBindCard);
		result.put("AddBidInfo", AddBidInfo);
		result.put("Cash", CashInfo);
		result.put("Loans", Loans);
		result.put("UsrFreezeBg", UsrFreezeBg);
		result.put("UsrUnFreeze", UsrUnFreeze);
		result.put("CashAudit", CashAudit);
		result.put("asynCashAudit", asynCashAudit);
		result.put("Repayment", Repayment);
		result.put("InitiativeTender", InitiativeTender);
		result.put("TenderCancle", TenderCancle);
		result.put("PnrUsrUnBindExpressCard", PnrUsrUnBindExpressCard);
		result.put("CreditAssign", CreditAssign);
		result.put("Transfer", Transfer);
		return result;
	}
	/**
	 * 汇付天下回调地址
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void hfReturn() {
		System.out.println("请求汇付天下回调接口");
		HttpServletRequest req = FacesUtil.getHttpServletRequest();
		HttpServletResponse response = FacesUtil.getHttpServletResponse();
		//更新huifuLog
		HuiFuLog huiFuLog=new HuiFuLog();
		String merPrv1=getValByKey(req,"MerPriv");
		if(getValByKey(req, "OrdId")==null||"".equals(getValByKey(req, "OrdId"))){
			huiFuLog= huiFuLogService.findHuiFuLogByMerPriv(merPrv1);
			huiFuLog.setRespCode(getValByKey(req, "RespCode"));
			huiFuLog.setRespDesc(getValByKey(req, "RespDesc"));
			StringBuffer sb=new StringBuffer();
			Map<String, Object> map= req.getParameterMap();
			sb.append("{");
			for (String str : map.keySet()) {
				sb.append("\"").append(str).append("\":");
				sb.append("\"").append(getValByKey(req, str)).append("\"").append(",");
			}
			sb.delete(sb.length()-1, sb.length());
			sb.append("}");
			huiFuLog.setRespData(sb.toString());
			huiFuLogService.updateHuiFuLog(huiFuLog);
		}else{
			huiFuLog= huiFuLogService.findHuiFuLogByOrderId(getValByKey(req, "OrdId"));
			huiFuLog.setRespCode(getValByKey(req, "RespCode"));
			huiFuLog.setRespDesc(getValByKey(req, "RespDesc"));
			StringBuffer sb=new StringBuffer();
			Map<String, Object> map= req.getParameterMap();
			sb.append("{");
			for (String str : map.keySet()) {
				sb.append("\"").append(str).append("\":");
				sb.append("\"").append(getValByKey(req, str)).append("\"").append(",");
			}
			sb.delete(sb.length()-1, sb.length());
			sb.append("}");
			
			huiFuLog.setRespData(sb.toString());
			huiFuLogService.updateHuiFuLog(huiFuLog);
		}
		
		//Map<String, String[]> map = req.getParameterMap();
		String RespCode = getValByKey(req, "RespCode");// 000--调用成功
		String CmdId = getValByKey(req, "CmdId");// 类 型 代 表 一 种 交 易
		String RespType = req.getParameter("RespType");
		if(RespType !=null && "Cash".equals(RespType)){
			//取现异步对账返回
			CmdId = "asynCashAudit";
		}
		System.out.println(RespCode);
		System.out.println(CmdId);
		boolean checkSignPass=checkSign(req, CmdId);
		if(!checkSignPass){
			log.error(CmdId+"回调签名验证失败");
		}
		if ("000".equals(RespCode)&&checkSignPass) {
			
			// 返回调用成功
			
			if ("UserRegister".equals(CmdId)) {
				// 用户开户回调接口
				String UsrCustId = getValByKey(req, "UsrCustId");// 用户客户号由汇付生成，用户的唯一性标识
				String IdType = getValByKey(req, "IdType");// 用户证件类型 00为身份证
				String IdNo = getValByKey(req, "IdNo");// 用户证件号
				String UsrName = getValByKey(req, "UsrName");// 用户真实姓名
				String UsrId = getValByKey(req, "UsrId");// 商户下的平台用户号，在每个商户下唯一
															// 注册时传的user id
				String UsrMp = getValByKey(req, "UsrMp");// 手机号
				User user = null;
				try {
					user = userService.getUserById(UsrId.substring(HuiFuConstants.Config.USER_BEF
							.length()));
				} catch (UserNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (null != user
						&& (null == user.getUsrCustId() || user.getUsrCustId()
								.length() == 0)) {
					// 系统中找到此用户
					if (null != IdType && IdType.equals("00")) {
						// 证件为身份证
						user.setIdCard(IdNo);
					}
					user.setRealname(UsrName);
					user.setMobileNumber(UsrMp);
					user.setUsrCustId(UsrCustId);
					userService.hfRealName(user);
				}
				try {
					response.getWriter().write("RECV_ORD_ID_".concat(getValByKey(req, "TrxId")));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if("NetSave".equals(CmdId)){//充值
				String result = huiFuPayService.receiveReturn(req, response);
				try {
					response.getWriter().print(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if ("UserBindCard".equals(CmdId)) {
				//绑卡
				String OpenAcctId=getValByKey(req, "OpenAcctId");//银行卡号
				String OpenBankId=getValByKey(req, "OpenBankId");//开户行代号
				String UsrCustId = getValByKey(req, "UsrCustId");// 商户下的平台用户号，在每个商户下唯一注册时传的user id
				try {
					User user = userService.getUserByUsrCustId(UsrCustId);
					if(null!=user){
						boolean cardHadBind=false;
						List<BankCard> list=bankCardService.getBankCardsByUserId(user.getId());
						for (BankCard bankCard : list) {
							if(OpenAcctId.equals(bankCard.getCardNo())){
								cardHadBind=true;
								break;
							}
						}
						if(!cardHadBind){
							BankCard bankCard=new BankCard();
							bankCard.setBankNo(OpenBankId);
							bankCard.setId(IdGenerator.randomUUID());
							bankCard.setStatus(BankCardConstants.BankCardStatus.BINDING);
							bankCard.setCardNo(OpenAcctId);
							bankCard.setUser(user);
							bankCard.setTime(new Date());
							bankCard.setPayType(BankCardConstants.BankCardPayType.QUXIAN);//取现卡
							bankCardService.bindBankCard(bankCard);
						}
					}
				} catch (UserNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else if("PnrUsrUnBindExpressCard".equals(CmdId)){
				String CardId=getValByKey(req, "CardId");//银行卡号
				bankCardService.delCardByCardId(CardId);
			}else if("Cash".equals(CmdId)){
				String result = huiFuTradeService.tradeCashReturn(req, response);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("asynCashAudit".equals(CmdId)){
				//异步对账 猜测与CashAudit区别在于异步对账有实际交易金额
				String result = huiFuTradeService.tradeAsynCashAuditReturn(req, response);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("CashAudit".equals(CmdId)){
				String result = huiFuTradeService.tradeCashAuditReturn(req, response);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("TenderCancle".equals(CmdId)){
				String result = huiFuTradeService.tradeTenderCancleReturn(req, response);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("Repayment".equals(CmdId)){
				String result = huiFuTradeService.tradeRepaymentReturn(req, response);
				try {
					response.getWriter().write(result);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("UsrFreezeBg".equals(CmdId)){
				String ordId = req.getParameter("OrdId");//订单号
				try {
					response.getWriter().write("RECV_ORD_ID_".concat(ordId));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("UsrUnFreeze".equals(CmdId)){
				String ordId = req.getParameter("OrdId");//订单号
				try {
					response.getWriter().write("RECV_ORD_ID_".concat(ordId));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if ("AddBidInfo".equals(CmdId)) {
				//标的信息录入接口回调
				
			String ProId=getValByKey(req, "ProId");//标的的唯一标识
			System.out.println("AddBidInfo:huiFuPassApply"+ProId);
			loanService.huiFuPassApply(ProId);
			}else if("InitiativeTender".equals(CmdId)){
					//主动投标
				String OrdId=getValByKey(req, "OrdId");//
				String FreezeTrxId=getValByKey(req, "FreezeTrxId");//
				String MerPriv=getValByKey(req, "MerPriv");//
				String TransAmt=getValByKey(req, "TransAmt");//交易金额
				String UsrCustId=getValByKey(req, "UsrCustId");//用户客户号
				String OrdDate=getValByKey(req, "OrdDate");//
				String FreezeOrdId=getValByKey(req, "FreezeOrdId");//
				String TrxId=getValByKey(req, "TrxId");//
				String RespExt=getValByKey(req, "RespExt");//代金券
				String[] divMer = MerPriv.split(",");
				String rateTicketId = null;
				String couponsId=null;
				if(divMer != null && divMer.length>1){
					//rateTicketId = divMer[1];
					if(divMer[1].startsWith("c")){
						couponsId=divMer[1].substring(1);
					}else if(divMer[1].startsWith("r")){
						rateTicketId=divMer[1].substring(1);
					}
				}
				
				Double VocherAmt = null;
				if(RespExt != null && RespExt.length()>0){
					JSONObject j = (JSONObject) JSONObject.parse(RespExt);
					JSONObject json = (JSONObject) JSONObject.parse(j.get("Vocher").toString());
					VocherAmt = json.getDouble("VocherAmt");
				}
				Invest invest=investService.getInvstByOrdId(OrdId);
				if(null==invest){
				String loanId=divMer[0];
				//是否标的已投满
				boolean iscom=false;
				double remainMoney=0;
				try {
					remainMoney = loanCalculator.calculateMoneyNeedRaised(loanId);
				} catch (NoMatchingObjectsException e) {
					iscom=true;
				}
				//输入的投资金额 大于 可投资的金额（尚未认购的金额） 抛出异常
				if (new Double(TransAmt) > remainMoney) {
					iscom=true;
				}
				/*try {
					iscom = loanService.isRaiseCompleted(loanId);
				} catch (NoMatchingObjectsException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					iscom=true;
				}*/
			
				User user=null;
				try {
					 user=userService.getUserByUsrCustId(UsrCustId);
				} catch (UserNotFoundException e1) {
					e1.printStackTrace();
				}
				Loan loan=	loanService.findLoanById(loanId);
				if(iscom){
					//如果已经投满，该投资需解冻
					ZwJson zj=huiFuMoneyService.unfreezeMoney(UsrCustId,FreezeTrxId);
					if(zj.isSuccess()){
					System.out.println(UsrCustId+"所投标的已满，被解冻");
						//解冻成功
						
						try {
							double realname=(VocherAmt == null?new Double(TransAmt):ArithUtil.sub(new Double(TransAmt), VocherAmt));
							ubs.freezeMoney(user.getId(),realname , OperatorInfo.INVEST_SUCCESS,
									"投资成功：冻结金额,项目：" + loan.getName(),loanId,"");
							ubs.unfreezeMoney(user.getId(),
									realname, OperatorInfo.CANCEL_LOAN,
									"项目:" + loan.getName() + "投资失败，资金退回",loanId);
							
						} catch (InsufficientBalance e) {
							e.printStackTrace();
						}
						hasInvestOut.put(OrdId, OrdId);
					}
					try {
						response.getWriter().write("RECV_ORD_ID_".concat(OrdId));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
						invest=new Invest();
						invest.setOrdId(OrdId);
						invest.setUser(user);
						invest.setLoan(loan);
						invest.setIsAutoInvest(false);
						invest.setRate(loan.getRate());
						invest.setTime(new Date());
						invest.setIsSafeLoanInvest(false);
						invest.setDitch("PC");
						invest.setId(investService.generateId(invest.getLoan().getId()));
						invest.setInvestMoney(new Double(TransAmt));
						invest.setMoney(new Double(TransAmt));
						invest.setStatus(InvestConstants.InvestStatus.BID_SUCCESS);
						//invest.setRate(loan.getRate());
						invest.setOrdDate(OrdDate);
						invest.setFreezeOrdId(FreezeOrdId);
						invest.setTrxId(TrxId);
						String otherMoney=null;
						// 投资成功以后，判断项目是否有尚未认购的金额，如果没有，则更改项目状态。
						if (invest.getTransferApply() == null || StringUtils.isEmpty(invest.getTransferApply().getId())) {
							invest.setTransferApply(null);
						}
						if(couponsId!=null&&couponsId.length()>0){
							Coupons coupons= couponSService.getCouponsById(couponsId);
							if(coupons!=null){
								invest.setCoupons(coupons);
								coupons.setStatus("useruse");
								coupons.setUsedTime(new Date());
								ht.save(coupons);
								otherMoney="c"+coupons.getMoney();
							}
						}
						
						if(rateTicketId != null && rateTicketId.length()>0){
							RateTicket rateTicket = rateTicketService.getRateTicketById(rateTicketId);
							if(rateTicket != null){
								invest.setRateTicket(rateTicket);
								rateTicket.setStatus("useruse");
								rateTicket.setUsedTime(new Date());
								ht.save(rateTicket);
								otherMoney="r"+rateTicket.getRate();
							}
						}
						invest.setFreezeTrxId(FreezeTrxId);
						ht.save(invest);
						try {
							//处理借款募集完成
							loanService.dealRaiseComplete(invest.getLoan().getId());
						} catch (NoMatchingObjectsException e) {
							throw new RuntimeException(e);
						}
							try {
								ubs.freezeMoney(invest.getUser().getId(),VocherAmt == null?invest.getMoney():ArithUtil.sub(invest.getMoney(), VocherAmt) , OperatorInfo.INVEST_SUCCESS,
										"投资成功：冻结金额,项目：" + invest.getLoan().getName(),invest.getLoan().getId(),otherMoney);
							} catch (InsufficientBalance e) {
								e.printStackTrace();
							}
							
							try {
								sendInitiativeTenderMsg(invest);
								couponSService.giveCouponToUser(invest);
								rateTicketService.giveRateTicketToUser(invest);
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
				}
				
			}else if ("Loans".equals(CmdId)) {//放款
				System.out.println("-------------------开始放款");
				String ordId = req.getParameter("OrdId");//订单号
				//merPriv为"investId,标的投资数量"
				String merPriv = req.getParameter("MerPriv");//投资id
				String[] mers = merPriv.split(",");
				//标的投资记录数
				int investNum = Integer.valueOf(mers[1]);
				Invest invest = investService.getInvstById(mers[0]);
				String loanId = invest.getLoan().getId();
				if(invest != null && invest.getStatus().equals(InvestConstants.InvestStatus.BID_SUCCESS)){
					try {
						int num = HuiFuConstants.getLoanNum(loanId, ordId);
						//如果是最后一条投资记录放款就处理系统标的还款业务
						if(num==investNum){
							loanService.giveMoneyToBorrower(loanId);
							loanService.sendInvestMsg(loanId);
							HuiFuConstants.LOAN_PAY_NUM.remove(loanId);
						}
						response.getWriter().write("RECV_ORD_ID_".concat(ordId));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if ("CreditAssign".equals(CmdId)){
				String CreditDeal=getValByKey(req, "CreditDealAmt");//承接金额 债权价值
				String CreditAmt=getValByKey(req, "CreditAmt");//转让金额 债权价值
				String BuyCustId=getValByKey(req, "BuyCustId");//承接人客户号
				String OrdId=getValByKey(req, "OrdId");//
				String OrdDate=getValByKey(req, "OrdDate");//
				String RespExt=getValByKey(req, "MerPriv");//当前存债权转让id
				Double transferCorpus=new Double(CreditAmt);//债权本金
				Double transferMoeny=new Double(CreditDeal);//承接金额 本金+利息
				User user=null;
				try {
					user = userService.getUserByUsrCustId(BuyCustId);
				} catch (UserNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				TransferApply ta =transferService.getTransferApplyById(RespExt);
				if(null!=ta&&TransferStatus.TRANSFERING.equals(ta.getStatus())){
					ht.evict(ta);
					ta = ht.get(TransferApply.class, ta.getId(), LockMode.UPGRADE);
					/*double remainCorpus = ta.getInvest().getRepayRoadmap().getUnPaidCorpus();//transferService.calculateRemainCorpus(transferApplyId);
					// 购买的本金占所有转让本金的比例。
					double corpusRateInAll = ArithUtil.div(transferCorpus,remainCorpus);
					if(corpusRateInAll==0){
						return ;
					}
					//转让本金占持有本金的本例
					double transferCorpusRate = ArithUtil.div(transferCorpus, ta.getInvest().getMoney());

					// 判断ta是否都被购买了
					if (remainCorpus == transferCorpus) {
						// 债权全部被购买，债权转让完成
						ta.setStatus(TransferStatus.TRANSFED);
					}*/
					ta.setStatus(TransferStatus.TRANSFED);//当前业务必须一次性购买
					Invest investNew = new Invest();
					investNew.setUser(user);
					investNew.setInvestMoney(ta.getInvest().getMoney());
					investNew.setIsAutoInvest(false);
					investNew.setMoney(ta.getInvest().getMoney());
					investNew.setStatus(InvestConstants.InvestStatus.REPAYING);
					investNew.setRate(ta.getInvest().getRate());
					investNew.setTime(new Date());
					investNew.setTransferApply(ta);
					investNew.setLoan(ta.getInvest().getLoan());
					investNew.setId(IdGenerator.randomUUID());
					investNew.setIsSafeLoanInvest(false);
					investNew.setDitch("PC");
					investNew.setOrdDate(OrdDate);
					investNew.setOrdId(OrdId);
					ht.save(investNew);

				/*	// 减去invest中持有的本金
					ta.getInvest().setMoney(
							ArithUtil.sub(ta.getInvest().getMoney(), ta.getInvest().getMoney()));
					if (ta.getInvest().getMoney() == 0) {*/
						// 投资全部被转让，则投资状态变为“完成”。
					ta.getInvest().setStatus(InvestStatus.COMPLETE);
					//}


					// 债权的购买金额：债权的价格*corpusRate
					//double buyPrice = ArithUtil.mul(ta.getPrice(), corpusRateInAll, 2);
					try {
						userBillBO.transferOutFromBalance(user.getId(), transferMoeny,
								OperatorInfo.TRANSFER, "购买债权，项目：" + ta.getInvest().getLoan().getName(),ta.getInvest().getLoan().getId());
					
					// 购买时候，扣除手续费，从转让人收到的金额中扣除。费用根据购买价格计算
					double fee = ta.getFee();
					// 购买人转出，原持有人转入，手续费转入系统
					sbs.transferInto(ArithUtil.round(fee,2), "transfer_fee",
							"债权转让手续费，项目：" + ta.getInvest().getLoan().getName(),null,null,null,null,investNew,null,null,null);
					userBillBO
							.transferIntoBalance(ta.getInvest().getUser().getId(),
									transferMoeny, "transfered", "债权转让成功，项目："
											+ ta.getInvest().getLoan().getName(),ta.getInvest().getLoan().getId());
					userBillBO.transferOutFromFrozen(ta.getInvest().getUser().getId(), 
							ArithUtil.round(fee,2), "transfered", "债权转让成功手续费，项目：" + ta.getInvest().getLoan().getName(), ta.getInvest().getLoan().getId());
					
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					sendTranMsg( ta);
					sendTranMsgToInvest(user, ta);
					// 生成购买债权后的还款数据，调整之前的还款数据
					for (Iterator iterator = ta.getInvest().getInvestRepays().iterator(); iterator.hasNext();) {
						InvestRepay ir =  (InvestRepay) iterator.next();
						if (ir.getStatus().equals(RepayStatus.WAIT_REPAY_VERIFY)
								|| ir.getStatus().equals(RepayStatus.OVERDUE)
								|| ir.getStatus().equals(RepayStatus.BAD_DEBT)) {
							throw new RuntimeException("investRepay with status "
									+ RepayStatus.WAIT_REPAY_VERIFY + "exist!");
						} else if (ir.getStatus().equals(RepayStatus.REPAYING)) {
							// 根据购买本金比例，生成债权还款信息
							InvestRepay irNew = new InvestRepay();
							try {
								BeanUtils.copyProperties(irNew, ir);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						    irNew.setId(IdGenerator.randomUUID());
//						    还款本金*转让本金比例*购买本金比例
							irNew.setCorpus(ArithUtil.mul(ir.getCorpus(),1, 2));
							irNew.setDefaultInterest(ArithUtil.mul(ir.getDefaultInterest(),1,2));
							irNew.setFee(ArithUtil.mul(ir.getFee(),1, 2));
							irNew.setInterest(ArithUtil.mul(ir.getInterest(),1, 2));
							irNew.setInvest(investNew);
							// 修改原投资的还款信息
							ir.setCorpus(ArithUtil.sub(ir.getCorpus(),
									irNew.getCorpus()));
							ir.setDefaultInterest(ArithUtil.sub(
									ir.getDefaultInterest(), irNew.getDefaultInterest()));
							ir.setFee(ArithUtil.sub(ir.getFee(), irNew.getFee()));
							ir.setInterest(ArithUtil.sub(ir.getInterest(),
									irNew.getInterest()));
							ht.merge(irNew);
							if (ir.getCorpus()+ir.getInterest() == 0 && !ir.getStatus().equals("complete")) {
								ht.delete(ir);
								iterator.remove();
							}else{
							    ht.update(ir);
							}
						}
					}
				
				
				}
				}else if("Transfer".equals(CmdId)){
				String ordId = req.getParameter("OrdId");//订单号
				try {
					response.getWriter().write("RECV_ORD_ID_".concat(ordId));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// 返回错误
			String RespDesc=getValByKey(req, "RespDesc");
			String errorStr = RespCode + ":" + RespDesc;
			log.error(errorStr);
			if("395".equals(RespCode)){
				String ProId=getValByKey(req, "ProId");
				loanService.huiFuPassApply(ProId);
			}
			if("400".equals(RespCode)){
				//取现失败解冻提现金额
				String OrdId=getValByKey(req, "OrdId");
				String withdrawId = withdrawCashService.getIdByHuiFuOrderId(OrdId);
				withdrawCashService.failWithdrawCashApply(withdrawId,RespDesc);
			}
		}
		// String MerCustId= map.get("MerCustId");//返回码的对应中文描述
		Map<String, String[]> map =req.getParameterMap();
		Map<String, String> mapstr =new HashMap<String, String>();
		for (String key : map.keySet()) {
			mapstr.put(key, map.get(key)[0]);
		}
		System.out.println(mapstr);
		
	}
	/**
	 * 验证签名
	 * @param map
	 * @param CmdId
	 * @return
	 */
	private boolean checkSign(HttpServletRequest req,String CmdId){
		String para[]=cmdIdPara.get(CmdId);
		StringBuffer plainStr=new StringBuffer();
		for (String string : para) {
			plainStr.append(getValByKey(req,StringUtils.trimToEmpty(string)));
		}
		String plainstr=plainStr.toString();
		if(HuiFuHttpUtil.needMd5.containsKey(CmdId)){
			plainstr=SignUtils.MD5Encode(plainstr);
		}
		try {
			return SignUtils.verifyByRSA(plainstr,getValByKey(req, "ChkValue"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	private String getValByKey(HttpServletRequest req, String key) {
		String arrstr=req.getParameter(key);
		if (null != arrstr && arrstr.length() > 0) {
			return arrstr;
		}
		return "";
	}
	/**
	 * 债权转让通知(转让人)
	 */
	public void sendTranMsg(TransferApply ta){
		User user = ht.get(User.class,ta.getInvest().getUser().getId());
		Map<String,String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("username",user.getId());
		params.put("dealNumber","'" + ta.getInvest().getLoan().getName() + "'");
		try{
			autoMsgService.sendMsg(user,"assignment_debt",params);
		}catch (Exception e){
			log.error("债权转让信息通知发送错误");
		}
	}
	/**
	 * 投资成功短信
	 */
	public void sendInitiativeTenderMsg(Invest invest){
		User user = ht.get(User.class,invest.getUser().getId());
		Map<String,String> params = new HashMap<String, String>();
		params.put("dealNumber",invest.getLoan().getName());
		params.put("username",user.getId());
		params.put("money","'" +invest.getMoney() + "'");
		try{
			autoMsgService.sendMsg(user,"invest_InitiativeTender",params);
		}catch (Exception e){
			log.error("投资成功信息通知发送错误");
		}
	}
	/**
	 * 债权转让通知(投资人，接收人)
	 */
	public void sendTranMsgToInvest(User investserid,TransferApply ta){
		Map<String,String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("username",investserid.getId());
		params.put("dealNumber","'" + ta.getInvest().getLoan().getName() + "'");
		try{
			autoMsgService.sendMsg(investserid,"assignment_debt_invest",params);
		}catch (Exception e){
			log.error("债权转让信息通知发送错误");
		}
	}
}
