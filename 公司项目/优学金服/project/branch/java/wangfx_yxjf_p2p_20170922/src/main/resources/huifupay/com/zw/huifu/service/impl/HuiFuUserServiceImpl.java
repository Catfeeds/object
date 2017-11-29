package com.zw.huifu.service.impl;

import java.util.LinkedHashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuUserService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;

@Service("huiFuUserService")
public class HuiFuUserServiceImpl implements HuiFuUserService {
	@Resource
	private HibernateTemplate ht;
	@Resource
	private UserService userService;
	@Resource
	private HuiFuHttpUtilService huiFuHttpUtilService;
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String userRegister(User user) {
		// TODO Auto-generated method stub
		JSONObject jso=QueryUsrInfo(user.getIdCard());
		//boolean huifuCheckPass=false;
		if("000".equals(jso.getString("RespCode"))){
			//String UsrId=jso.getString("UsrId");
			String UsrCustId=jso.getString("UsrCustId");
			user.setUsrCustId(UsrCustId);
			userService.hfRealName(user);
			ht.update(user);
		}
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		String Version="10";//版本号 --目前固定为 10，如版本升级，能向前兼容
		String CmdId="UserRegister";//消息类型--每一种消息类型代表一种交易
		String BgRetUrl=HuiFuConstants.Config.BACK_URL;//商户后台应答地址--通过后台异步通知，商户网站都应在应答接收页面 输出 RECV_ORD_ID 字样的字符串，表明商户已经 收到该笔交易结果
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrId=user.getId();//用户号商户下的平台用户号，在每个商户下唯一
		String UsrName=user.getRealname();
		String IdType="00";//00-- 身份证
		String IdNo=user.getIdCard();
		String RetUrl=request.getScheme() + "://" + request.getServerName()+HuiFuHttpUtil.getPort(request) +request.getContextPath()+"/user/get_investor_permission";//实名后跳转的地址
		String UsrMp=user.getMobileNumber();//用户手机号
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("BgRetUrl",BgRetUrl);
		params.put("RetUrl",RetUrl);
		params.put("UsrId", UsrId);
		params.put("UsrName", UsrName);
		params.put("IdType", IdType);
		params.put("IdNo", IdNo);
		params.put("UsrMp", UsrMp);
		if(!"000".equals(jso.getString("RespCode"))){
			user.setIdCard(null);
			user.setRealname(null);
		}
		try {
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String bindCard(String usrCustId) {
		// TODO Auto-generated method stub
		String Version="10";//版本号 --目前固定为 10，如版本升级，能向前兼容
		String CmdId="UserBindCard";//消息类型--每一种消息类型代表一种交易
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrCustId=usrCustId;//用户客户号 
		String BgRetUrl=HuiFuConstants.Config.BACK_URL;//商户后台应答地址--通过后台异步通知，商户网站都应在应答接收页面 输出 RECV_ORD_ID 字样的字符串，表明商户已经 收到该笔交易结果
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		params.put("BgRetUrl",BgRetUrl);
		
		try {
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public JSONObject delCard(String usrCustId,String cardId) {
		// TODO Auto-generated method stub
		String Version="10";//版本号 --目前固定为 10，如版本升级，能向前兼容
		String CmdId="DelCard";//消息类型--每一种消息类型代表一种交易
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrCustId=usrCustId;//用户客户号 
		String CardId=cardId;
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		params.put("CardId",CardId);
		JSONObject jso=new JSONObject();
		try {
		String delStr=	huiFuHttpUtilService.doPost(params);
		 jso=JSONObject.parseObject(delStr);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jso;
	}

	@Override
	public JSONArray QueryCardInfo(String usrCustId) {
		// TODO Auto-generated method stub
		String Version="10";
		String CmdId="QueryCardInfo";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrCustId=usrCustId;//用户客户号 
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		JSONArray result=new JSONArray();
		String resultStr;
		try {
			resultStr = huiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		JSONObject jsonObj = JSONObject.parseObject(resultStr);
		String respCode = (String) jsonObj.get("RespCode");
		if("000".equals(respCode)){
			return 	(JSONArray) JSONArray.parse(jsonObj.getString("UsrCardInfolist"));
		}
		return result;
	}

	@Override
	public String uerLogin(String usrCustId) {
		// TODO Auto-generated method stub
		
		String Version="10";
		String CmdId="UserLogin";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrCustId=usrCustId;//用户客户号 
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		try {
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String acctModify(String usrCustId) {
		// TODO Auto-generated method stub
		
		String Version="10";
		String CmdId="AcctModify";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户的唯一性标识
		String UsrCustId=usrCustId;//用户客户号 
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("UsrCustId", UsrCustId);
		try {
			return huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public JSONObject QueryUsrInfo(String certId) {
		// TODO Auto-generated method stub
		
		String Version="10";
		String CmdId="QueryUsrInfo";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String CertId=certId;
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("CertId", CertId);
		String jsonStr="";
		try {
			jsonStr = huiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSONObject.parseObject(jsonStr);
	}

	
}
