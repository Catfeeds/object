package com.zw.huifu.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zw.core.bean.ZwJson;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.service.HuiFuTransferService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;

@Service("huiFuMoneyService")
public class HuiFuMoneyServiceImpl implements HuiFuMoneyService {

	@Resource
	private HuiFuLoanService huiFuLoanService;
	
	@Resource
	private HuiFuTransferService huiFuTransferService;
	
	@Resource 
	private HuiFuHttpUtilService HuiFuHttpUtilService;
	@Override
	public ZwJson freezeMoney(String usrCustId, Double money) {
		ZwJson json = new ZwJson();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "UsrFreezeBg");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("UsrCustId", usrCustId);
		params.put("OrdId",OrderNoService.getOrderNo());
		params.put("OrdDate", OrderNoService.getNowDate());
		params.put("TransAmt", new DecimalFormat("#.00").format(money));
		params.put("BgRetUrl",HuiFuConstants.Config.BACK_URL);
		try {
			String result = HuiFuHttpUtilService.doPost(params);
			JSONObject jsonObj = JSONObject.parseObject(result);
			String respCode = (String) jsonObj.get("RespCode");
			String respDesc = (String) jsonObj.get("RespDesc");
			//冻结唯一标示
			String trxId = (String) jsonObj.get("TrxId");
			if("000".equals(respCode)){
				json.setSuccess(true);
				json.setObj(trxId);
			}else{
				json.setSuccess(false);
			}
			json.setMsg(respDesc);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.setSuccess(false);
			json.setMsg("调用资金冻结接口异常");
			return json;
		}
	}
	
	
	/**
	 * 解冻金额
	 * @author majie
	 * @param UsrCustId 用户客户号
	 * @param trxId 唯一标示
	 * @date 2016年8月17日 下午1:42:38
	 */
	public ZwJson unfreezeMoney(String usrCustId,String freezeTrxId){
		ZwJson json = new ZwJson();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "UsrUnFreeze");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("OrdId",OrderNoService.getOrderNo());
		params.put("OrdDate", OrderNoService.getNowDate());
		params.put("TrxId", freezeTrxId);
		params.put("BgRetUrl",HuiFuConstants.Config.BACK_URL);
		try {
			String result = HuiFuHttpUtilService.doPost(params);
			JSONObject jsonObj = JSONObject.parseObject(result);
			String respCode = (String) jsonObj.get("RespCode");
			String respDesc = (String) jsonObj.get("RespDesc");
			if("000".equals(respCode)){
				json.setSuccess(true);
			}else{
				json.setSuccess(false);
			}
			json.setMsg(respDesc);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.setSuccess(false);
			json.setMsg("调用资金解冻接口异常");
			return json;
		}
	}


	@Override
	public JSONObject userBalance(String usrCustId) {
		String Version="10";
		String CmdId="QueryBalanceBg";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String UsrCustId=usrCustId;
		JSONObject json = new JSONObject();
		json.put("AvlBal", "0.00");//可用余额账户可以支取的余额
		json.put("AcctBal", "0.00");//账户余额 账户资金余额，该余额能真正反映账户的资金量
		json.put("FrzBal","0.00");//冻结余额
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		String respCode = (String) jsonObj.get("RespCode");
		if("000".equals(respCode)){
			jsonObj.remove("ChkValue");
			jsonObj.put("des", "账户总额:"+jsonObj.getString("AcctBal")+"可用余额:"+jsonObj.getString("AvlBal")+"冻结余额:"+jsonObj.getString("FrzBal")+"");
			return jsonObj;
		}
		return json;
	}
	
	public void huifuTest(){
		//标的审核状态查询接口
		JSONObject jsstr=huiFuLoanService.QueryBidInfo("1609011208239850");
		System.out.println(jsstr);
		/*//充值对账
		JSONObject jsstr1= SaveReconciliation(new Date(), new Date(), 1, 100);
		System.out.println(jsstr1);
		//取现对账
		JSONObject jsstr2=CashReconciliation(new Date(), new Date(), 1, 100);
		System.out.println(jsstr2);
		//放还款对账
		JSONObject jsstr3=Reconciliation(new Date(), new Date(), 1, 100,"LOANS");
		System.out.println(jsstr3);
		//商户扣款对账
		JSONObject jsstr4=TrfReconciliation(new Date(), new Date(), 1, 100);
		System.out.println(jsstr4);*/
		
		
	}

	/**
	 * 商户子账户信息查询
	 * @author majie
	 * @return
	 * @date 2016年9月7日 下午5:18:48
	 */
	public JSONObject queryAccts(){
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "QueryAccts");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		String respCode = (String) jsonObj.get("RespCode");
		if("000".equals(respCode)){
			jsonObj.remove("ChkValue");
			return jsonObj;
		}
		
		return json;
	}
	
	
	/**
	 * 交易状态查询
	 * @author majie
	 * @param ordId
	 * @param ordDate
	 * @param queryTransType
	 * @return
	 * @date 2016年9月7日 下午8:08:09
	 */
	public JSONObject queryTransStat(String ordId,String ordDate,String queryTransType){
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "10");
		params.put("CmdId", "QueryTransStat");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("OrdId", ordId);
		params.put("OrdDate", ordDate);
		params.put("QueryTransType", queryTransType);
		
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		String respCode = (String) jsonObj.get("RespCode");
		if("000".equals(respCode)){
			jsonObj.remove("ChkValue");
			return jsonObj;
		}
		
		return json;
	}

	@Override
	public JSONObject SaveReconciliation(Date beginDate, Date endDate, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		String Version="10";
		String CmdId="SaveReconciliation";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String BeginDate=sdfymd.format(beginDate);
		String EndDate=sdfymd.format(endDate);
		String PageNum=pageNum+"";
		String PageSize=pageSize+"";
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("BeginDate", BeginDate);
		params.put("EndDate", EndDate);
		params.put("PageNum", PageNum);
		params.put("PageSize", PageSize);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		return jsonObj;
		
	}


	@Override
	public JSONObject CashReconciliation(Date beginDate, Date endDate, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		String Version="10";
		String CmdId="CashReconciliation";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String BeginDate=sdfymd.format(beginDate);
		String EndDate=sdfymd.format(endDate);
		String PageNum=pageNum+"";
		String PageSize=pageSize+"";
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("BeginDate", BeginDate);
		params.put("EndDate", EndDate);
		params.put("PageNum", PageNum);
		params.put("PageSize", PageSize);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		return jsonObj;
		
	}


	@Override
	public JSONObject Reconciliation(Date beginDate,Date endDate,int pageNum,int pageSize,String queryTransType) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		String Version="10";
		String CmdId="Reconciliation";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String BeginDate=sdfymd.format(beginDate);
		String EndDate=sdfymd.format(endDate);
		String PageNum=pageNum+"";
		String PageSize=pageSize+"";
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("BeginDate", BeginDate);
		params.put("EndDate", EndDate);
		params.put("PageNum", PageNum);
		params.put("PageSize", PageSize);
		params.put("QueryTransType", queryTransType);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		return jsonObj;
		
	}


	@Override
	public JSONObject TrfReconciliation(Date beginDate, Date endDate, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		String Version="10";
		String CmdId="TrfReconciliation";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String BeginDate=sdfymd.format(beginDate);
		String EndDate=sdfymd.format(endDate);
		String PageNum=pageNum+"";
		String PageSize=pageSize+"";
		JSONObject json = new JSONObject();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("BeginDate", BeginDate);
		params.put("EndDate", EndDate);
		params.put("PageNum", PageNum);
		params.put("PageSize", PageSize);
		String result;
		try {
			result = HuiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return json;
		}
		JSONObject jsonObj = JSONObject.parseObject(result);
		return jsonObj;
		
	}

}
