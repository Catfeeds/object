package com.zw.archer.user.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.pay.service.PayService;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.HttpClientUtil;
import com.zw.core.util.SpringBeanUtil;
import com.zw.huifu.service.HuifuPayService;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.user.service.RechargeService;

@Component
@Scope(ScopeType.VIEW)
public class RechargeHome extends EntityHome<Recharge> {

	@Logger
	static Log log;

	@Resource
	private LoginUserInfo loginUserInfo;

	@Resource
	private RechargeService rechargeService;
	
	@Resource
	private HuifuPayService huifuPayService;

	@Override
	protected Recharge createInstance() {
		Recharge recharge = new Recharge();
		recharge.setFee(0D);
		recharge.setUser(new User(loginUserInfo.getLoginUserId()));
		return recharge;
	}

	public void calculateFee() {
		double fee = rechargeService.calculateFee(this.getInstance()
				.getActualMoney());
		this.getInstance().setFee(fee);
	}
	
	public String offlineRecharge(){
		rechargeService.createOfflineRechargeOrder(getInstance());
		FacesUtil.addInfoMessage("您的线下充值记录已经提交，请等待管理员审核。请勿重复提交！");
		return "pretty:userCenter";
	}
	
	/**
	 * 充值
	 */
	public void recharge() {
		/*if (StringUtils.isEmpty(this.getInstance().getRechargeWay())) {
			FacesUtil.addErrorMessage("请选择充值方式！");
			return;
		}*/
		Recharge recharge = getInstance();
		String orderNo = OrderNoService.getOrderNo();
		recharge = rechargeService.createHuiFuRechargeOrder(recharge,orderNo);
		String html = huifuPayService.recharge(recharge, loginUserInfo.getUser().getUsrCustId(),orderNo);
		if(html==null || html.trim().length()==0)
			return;
		try {
			FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
			FacesUtil.getHttpServletResponse().getWriter().print(html);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 接收充值回调 */
	public String receiveRechargeReturnWeb() {
		HttpServletRequest request = FacesUtil.getHttpServletRequest();
		String respCode = (String) request.getParameter("RespCode");
		String respDesc = (String) request.getParameter("RespDesc");
		if("000".equals(respCode)){
			FacesUtil.addInfoMessage(respDesc);
		}else{
			FacesUtil.addErrorMessage(respDesc);
		}
		return "pretty:userCenter";
	}

	public void receiveRechargeReturnS2S() {
		if (log.isInfoEnabled()) {
			log.info("recharge return s2s:"+HttpClientUtil.requestParametersToString(FacesUtil.getHttpServletRequest()));
		}
		PayService payService = (PayService) SpringBeanUtil
				.getBeanByName(getInstance().getRechargeWay() + "PayService");
		payService.receiveReturnS2S(FacesUtil.getHttpServletRequest(), FacesUtil.getHttpServletResponse());
		FacesUtil.getCurrentInstance().responseComplete();
	}

	/** 去往充值支付方 */
	public void toRecharge() {
		Recharge recharge = getInstance();
		if (recharge == null) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("payWay:"+getPayWay(recharge)+", bankNo:"+getBankNo(recharge));
		}
		PayService payService = (PayService) SpringBeanUtil
				.getBeanByName(getPayWay(recharge) + "PayService");
		payService.recharge(FacesUtil.getCurrentInstance(), recharge,
				getBankNo(recharge));
	}
	
	/**
	 * 管理员充值
	 * @param rechargeId
	 */
	public void rechargeByAdmin(String rechargeId){
		rechargeService.rechargeByAdmin(rechargeId);
		FacesUtil.addInfoMessage("充值成功，相应款项已经充入到用户账户中");
	}
	
	/**
	 * Pos机充值
	 * @author majie
	 * @date 2016年5月9日 上午10:24:04
	 */
	public void addPosRecharge(){
		Double actualMoney = this.getInstance().getActualMoney();
		String id = this.getInstance().getId();
		Recharge recharge = createInstance();
		recharge.setActualMoney(actualMoney);
		recharge.setId(id);
		rechargeService.createPosRechargeOrder(recharge);
	}

	/**
	 * 获取支付方式
	 * 
	 * @param recharge
	 * @return
	 */
	private static String getPayWay(Recharge recharge) {
		return recharge.getRechargeWay().split("_")[0];
	}

	/**
	 * 获取充值银行号码
	 * 
	 * @param recharge
	 * @return
	 */
	private static String getBankNo(Recharge recharge) {
//		String[] strs = recharge.getRechargeWay().split("_");
//		if (strs.length > 1) {
//			return strs[1];
//		}
//		return null;
		return recharge.getBankNo();
	}
}
