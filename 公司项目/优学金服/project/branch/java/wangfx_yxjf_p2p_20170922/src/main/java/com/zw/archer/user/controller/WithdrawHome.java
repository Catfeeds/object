package com.zw.archer.user.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.notice.model.Notice;
import com.zw.archer.notice.model.NoticePool;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserConstants.WithdrawStatus;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.archer.user.service.UserService;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateUtil;
import com.zw.core.util.HashCrypt;
import com.zw.core.util.StringManager;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.p2p.fee.service.impl.FeeSchemeServiceImpl;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.safeloan.model.SafeLoanRecord;
import com.zw.p2p.safeloan.service.SafeLoanRecordService;
import com.zw.p2p.user.service.RechargeService;
import com.zw.p2p.user.service.WithdrawCashService;
@Component
@Scope(ScopeType.VIEW)
public class WithdrawHome extends EntityHome<WithdrawCash> {

	@Logger
	static Log log;
	private static StringManager userSM = StringManager
			.getManager(UserConstants.Package);
	@Resource
	WithdrawCashService wcs;

	@Resource
	LoginUserInfo loginUserInfo;
	
	@Resource
	UserBillService userBillService;
	@Resource
	UserService userService;
	@Resource
	NoticePool noticePool;
	@Resource
	FeeSchemeServiceImpl feeSchemeService;
	@Resource
	private AutoMsgService autoMsgService;
	@Resource
	private InvestService investService;
	@Resource
	private SafeLoanRecordService safeLoanRecordService;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private HuiFuTradeService huiFuTradeService;
	
	private String status;
	private String feeMeg;
	/** 交易密码 */
	private String cashPassword;

	public WithdrawHome() {
		setUpdateView(FacesUtil.redirect("/admin/withdraw/withdrawList"));
	}

	public String getStatus() {
		return FacesUtil.getParameter("withdrawStatus");
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	protected WithdrawCash createInstance() {
		WithdrawCash withdraw = new WithdrawCash();
		withdraw.setAccount("借款账户");
		withdraw.setFee(0D);
		withdraw.setCashFine(0D);
		withdraw.setUser(new User(loginUserInfo.getLoginUserId()));
		return withdraw;
	}

	/**
	 * 计算手续费和罚金
	 */
	public boolean calculateFee() {
		double fee = wcs.calculateFee(this.getInstance().getMoney(),this.getInstance().getUser().getId());
		if (userBillService.getBalance(loginUserInfo.getLoginUserId())<fee+this.getInstance().getMoney()) {
			FacesUtil.addErrorMessage("余额不足！");
			FacesUtil.getCurrentInstance().validationFailed();
			this.getInstance().setMoney(0D);
			return false;
		} else {
			this.getInstance().setFee(fee);
			return true;
		}
	}

	/**
	 * 提现
	 */
	public void withdraw() {
		try {
		
			WithdrawCash withdraw = this.getInstance();
			Double money = withdraw.getMoney();
			DecimalFormat df = new DecimalFormat("0.00");
			String moneystr = "0.00";
			if(money != null){
				moneystr = df.format(money);
			}
			String OpenAcctId = withdraw.getBankCard().getCardNo();
			String MerPriv = withdraw.getBankCard().getId();
			String res = huiFuTradeService.tradeCash(loginUserInfo.getUser().getUsrCustId(), moneystr,df.format(withdraw.getFee()),OpenAcctId,MerPriv);
			FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
			FacesUtil.getHttpServletResponse().getWriter().print(res);
		} catch (Exception e) {
			FacesUtil.addErrorMessage("调用第三方平台出错！");
		}
	}
	/**
	 * 页面回掉
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月16日 下午8:03:42
	 */
	public String withdrawReturnWeb(){
		HttpServletRequest request = FacesUtil.getHttpServletRequest();
		String respCode = (String) request.getParameter("RespCode");
		String respDesc = (String) request.getParameter("RespDesc");
		if("000".equals(respCode)){
			FacesUtil.addInfoMessage(URLDecoder.decode(respDesc));
		}else{
			FacesUtil.addErrorMessage(URLDecoder.decode(respDesc));
		}
		return "pretty:myCashFlow";
	}

	/**
	 * 提现审核初审通过
	 */
	public String verifyPass() {
		setUpdateView(FacesUtil
				.redirect("/admin/withdraw/withdrawList")
				.concat("withdrawStatus=wait_verify"));
		getInstance().setVerifyUser(new User(loginUserInfo.getLoginUserId()));
		wcs.passWithdrawCashApply(this.getInstance());
		FacesUtil.addInfoMessage("审核通过，请等待系统复核");
		noticePool.add(new Notice("提现："+getInstance().getId()+"初审通过"));
		return getUpdateView();
	}

	/**
	 * 提现复审通过
	 */
	public String recheckPass() {
		setUpdateView(FacesUtil
				.redirect("/admin/withdraw/withdrawList")
				.concat("withdrawStatus=recheck"));
		FacesUtil.addInfoMessage("复核通过，用户账户资金会自动解冻并扣除");
		return getUpdateView();
	}
	/**
	*@Description: TODO(更新支付方式) 
	* @author cuihang   
	*@date 2016-6-27 下午2:52:35 
	*@return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String saveWith() {
		setUpdateView(FacesUtil
				.redirect("/admin/withdraw/withdrawList")
				.concat("withdrawStatus=wait_verify	"));
		WithdrawCash withdrawCash=this.getInstance();
		WithdrawCash wdc = ht.get(WithdrawCash.class, withdrawCash.getId());
		ht.evict(wdc);
		wdc = ht.get(WithdrawCash.class, wdc.getId(), LockMode.UPGRADE);
		wdc.setPay(withdrawCash.getPay());
		ht.merge(wdc);
		FacesUtil.addInfoMessage("保存成功");
		return getUpdateView();
	}
	/**
	 * 发送提现通知
	 * nodeId: 成功:withdraw_success,失败:withdraw_fail
	 */
	public void sendWithdrawMsg(String nodeId) {
		User user = getInstance().getUser();
		Map<String,String> params = new HashMap<String, String>();
		params.put("username",user.getUsername());
		try {
			autoMsgService.sendMsg(user,nodeId,params);
		}catch (Exception e){
			log.error("提现信息发送失败");
		}
	}

	public void sendWithdrawMsg(){
		sendWithdrawMsg("withdraw_success");
	}

	public String getTime(WithdrawCash withdraw){
		String time = null;
		if (withdraw.getMoney() < 50000){
			time = DateUtil.getDate(new Date()) + "  23:59:59前";
		} else {
			if (DateUtil.getHour(new Date()) > 16 || (DateUtil.getHour(new Date())==16 && DateUtil.getMinute(new Date()) >= 30)){
				if (!withdraw.getBankCard().getName().equals("中国建设银行")||!withdraw.getBankCard().getName().equals("中国工商银行")
				||!withdraw.getBankCard().getName().equals("交通银行")){
					Date date = DateUtil.addDay(new Date(),1);
					time = DateUtil.getDate(date) + "  23:59:59前";
				}
			} else {
				time = DateUtil.getDate(new Date()) + "  23:59:59前";
			}
		}
		return time;
	}

	/**
	 * 提现审核初审不通过
	 */
	public String verifyFail() {
		setUpdateView(FacesUtil
				.redirect("/admin/withdraw/withdrawList")
				.concat("withdrawStatus=wait_verify"));

		getInstance().setVerifyUser(new User(loginUserInfo.getLoginUserId()));
		getInstance().setRecheckUser(new User(loginUserInfo.getLoginUserId()));
		wcs.refuseWithdrawCashApply(this.getInstance());
		FacesUtil.addInfoMessage("初审未通过，用户账户的资金会自动解冻");
		noticePool.add(new Notice("提现："+getInstance().getId()+"初审未通过"));
		return getUpdateView();
	}

	/**
	 * 提现审核复核不通过
	 */
	public String recheckFail() {
		setUpdateView(FacesUtil
				.redirect("/admin/withdraw/withdrawList")
				.concat("withdrawStatus=recheck"));

		getInstance().setVerifyUser(new User(loginUserInfo.getLoginUserId()));
		wcs.refuseWithdrawCashApply(this.getInstance());
		FacesUtil.addInfoMessage("复核未通过，用户账户的资金会自动解冻");
		noticePool.add(new Notice("提现："+getInstance().getId()+"复审未通过"));
		sendWithdrawMsg("withdraw_fail");
		return getUpdateView();
	}

	public String getCashPassword() {
		return cashPassword;
	}

	public void setCashPassword(String cashPassword) {
		this.cashPassword = cashPassword;
	}

	public String recheck(){
		getInstance().setVerifyUser(new User(loginUserInfo.getLoginUserId()));
		String state = "";
		DecimalFormat df = new DecimalFormat("0.00");
		String money = df.format(this.getInstance().getApplyMoney());
		String orderId = this.getInstance().getHuiFuOrderId();
		state = "复核通过".equals(this.getInstance().getRecheckMessage())==true?"S":"R";
		String res = huiFuTradeService.tradeCashAudit(this.getInstance().getUser().getUsrCustId(), money, state,orderId);
		JSONObject resJson = JSONObject.parseObject(res);
		String rescode = resJson.getString("RespCode");
		if("999".equals(rescode) || "000".equals(rescode)){
			if("R".equals(state)){
				return recheckFail();
			}else{
				return recheckPass();
			}
		}else{
			
		}
		return getUpdateView();
	}
	
	public String getFeeMeg() {
		return feeMeg;
	}

	public void setFeeMeg(String feeMeg) {
		this.feeMeg = feeMeg;
	}
	public boolean judge(){
		boolean flag = false ;
		String userid =  getInstance().getUser().getId();
		Recharge recharge = rechargeService.getLastRechargeByUser(userid);
		Invest invest = investService.getLastInvestByUser(userid);
		Date d1 = recharge.getTime();
		Date d2 = invest.getTime();
		SafeLoanRecord safeloan = safeLoanRecordService.getLastSafeLoanRecordByUser(userid);
		Date d3 = safeloan.getBeforeInvestTime();
		if(null!=d1){
			if (compareTo(d1, d2) && compareTo(d1, d3)) {
				flag = true;
			} else if (getInstance().getMoney() < 999.0) {
				flag = false;
			} else {
				flag = false;
			}
		}
		return flag;
	}
	public String initFeemes(){
		feeMeg="0";
	 	String userid =  getInstance().getUser().getId();
	 	Boolean isInvest=false;
		boolean loaner=false;
	 	try {
			User user=	userService.getUserById(userid);
			List<Role> listRole=user.getRoles();
			for (Role roles : listRole) {
				if(roles.getId().equals("INVESTOR")){
					isInvest=true;
				}
				if(roles.getId().equals("LOANER")){
					loaner=true;
				}
			}
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 	if(!isInvest||loaner){
	 		return feeMeg;
	 	}
		Recharge recharge = rechargeService.getLastRechargeByUser(userid);
		Invest invest = investService.getLastInvestByUser(userid);
		Date d1 = recharge != null ? recharge.getTime() : null;
		Date d2 = invest != null ? invest.getTime() : null;
		
		SafeLoanRecord safeloan = safeLoanRecordService.getLastSafeLoanRecordByUser(userid);
		Date d3 = safeloan != null ? safeloan.getBeforeInvestTime() : null;
		//Date d3 = safeloan.getBeforeInvestTime();
		if(null!=d1){
			if (compareTo(d1, d2) && compareTo(d1, d3)) {
				//未进行任何投资
				feeMeg="1";
			}
		}
		
		return feeMeg;
	}
	
	/**
	 *比较 param1 param2 param1>param2 return true;(当前时间与投资时间作比较)
	 * @param param1
	 * @param param2
	 * @return
	 */
	private boolean compareTo(Date param1,Date param2){
		if(null==param2){
			return true;
		}else{
			return(param1.compareTo(param2)>0);
		}
	}
}
