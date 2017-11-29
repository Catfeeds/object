package com.zw.huifu.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuifuPayService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.user.service.RechargeService;

@Service("huifuPayService")
public class HuifuPayServiceImpl implements HuifuPayService{
	
	@Resource
	private RechargeService rechargeService;
	@Resource
	private BankCardService bankCardService;
	@Resource
	private UserService userService;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private HuiFuHttpUtilService huiFuHttpUtilService;
	
	@Override
	public String recharge(Recharge recharge,String usrCustId,String orderNo) {
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "NetSave");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("UsrCustId", usrCustId);
		params.put("OrdId", orderNo);
		params.put("OrdDate", OrderNoService.getNowDate());
		/**
		 * 网关的细分业务类型  
		 * B2C--B2C 网银支付
		 * B2B--B2B 网银支付
		 * QP--快捷支付
		 */
//		params.put("GateBusiId", "B2C");
		//交易金额
		params.put("TransAmt", new DecimalFormat("#.00").format(recharge.getActualMoney()));
		//页面返回 URL
		params.put("RetUrl", HuiFuConstants.Config.RETURN_URL+"recharge_return_web/"+recharge.getRechargeWay());
		params.put("BgRetUrl",HuiFuConstants.Config.BACK_URL);
		try {
			String result = huiFuHttpUtilService.doPost(params);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String receiveReturn(HttpServletRequest request, HttpServletResponse response) {
		String respCode = request.getParameter("RespCode");
		String respDesc = request.getParameter("RespDesc");
		String ordId = request.getParameter("OrdId");	//订单号
		String GateBusiId = request.getParameter("GateBusiId");	//支付网关业务代号
		String UsrCustId = request.getParameter("UsrCustId");	//用户客户号
		List list = ht.find("select id from Recharge where status='wait_pay' and ordId='"+ordId+"'");
		if(list.size()==0)
			return "RECV_ORD_ID_".concat(ordId);
		
		String id = (String) list.get(0);
		//充值失败时
		if(!"000".equals(respCode)){
			ht.bulkUpdate(" update Recharge set status=?,remark=? where id=?","fail",respDesc,id);
			return "fail";
		}
	   try {
		   boolean isSuccess = rechargeService.rechargePaySuccess(id);
		   if(isSuccess && "QP".equals(GateBusiId)){
		   //快捷支付
				User user = userService.getUserByUsrCustId(UsrCustId);
				List<BankCard> listquick=bankCardService.getHuiFuFastBankCardsByUserId(user.getId());
				if(null==listquick||listquick.size()==0){
					//没有快捷卡
					//先删除所有卡再绑定快捷卡
					List<BankCard> listbind= bankCardService.getBankCardsByUserId(user.getId());
					bankCardService.delCard(listbind);
					String GateBankId = request.getParameter("GateBankId");	//开户银行代号
					String CardId = request.getParameter("CardId");	//银行卡号
					BankCard bankCard=new BankCard();
					bankCard.setBankNo(GateBankId);
					bankCard.setId(IdGenerator.randomUUID());
					bankCard.setStatus(BankCardConstants.BankCardStatus.BINDING);
					bankCard.setCardNo(CardId);
					bankCard.setUser(user);
					bankCard.setTime(new Date());
					bankCard.setPayType(BankCardConstants.BankCardPayType.QUICKPAY);//取现卡
					bankCardService.bindBankCard(bankCard);
				}
		   }
			  
			  return "RECV_ORD_ID_".concat(ordId);
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
	
}
