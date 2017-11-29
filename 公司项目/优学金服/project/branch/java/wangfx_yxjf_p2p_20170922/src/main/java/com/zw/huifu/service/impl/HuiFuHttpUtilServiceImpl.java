package com.zw.huifu.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.bean.model.HuiFuLog;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuLogService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;

import sun.print.resources.serviceui_pt_BR;
@Service("HuiFuHttpUtilService")
public class HuiFuHttpUtilServiceImpl implements HuiFuHttpUtilService{

	@Resource
	private HuiFuLogService huiFuLogService;
	@Override
	public String doPost(Map<String, String> params) {
		String result = null;
		CloseableHttpClient httpclient = null;
		try {
			httpclient = HuiFuHttpUtil.createSSLInsecureClient();
		} catch (GeneralSecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			params=HuiFuHttpUtil.getParams(params);
			List<NameValuePair> nvps =HuiFuHttpUtil.buildNameValuePair(params);
			UrlEncodedFormEntity formEntity = null;
			formEntity = new UrlEncodedFormEntity(nvps,"UTF-8");
			HttpPost httpPost = new HttpPost(HuiFuConstants.Config.HUIFU_HTTP_URL);
			httpPost.setEntity(formEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getReasonPhrase().equals("OK")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(entity, "UTF-8");
               }
			EntityUtils.consume(entity);
			}finally {
				response.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(result.startsWith("<")){
			SaveHuiFuLog(params, null);
			return result;
		}
		SaveHuiFuLog(params, JSONObject.parseObject(result));
        return result;
	}
	@Transactional(readOnly = false)
	public void SaveHuiFuLog(Map<String, String> params,JSONObject jsonObject){
    	//生成记录
		HuiFuLog huiFuLog=new HuiFuLog();
		huiFuLog.setVersion(params.get("Version"));
		huiFuLog.setCmdIds(params.get("CmdId")==null?"":params.get("CmdId").toString());
		huiFuLog.setOrderId(params.get("OrdId")==null?"":params.get("OrdId").toString());
		huiFuLog.setMerCustId(params.get("MerCustId")==null?"":params.get("MerCustId").toString());
		huiFuLog.setMerPriv(params.get("MerPriv")==null?"":params.get("MerPriv").toString());
		Map<String, Object> map=new HashMap<String, Object>();
		for (String  str : params.keySet()) {
			map.put(str, params.get(str));
		}
		huiFuLog.setParam(JSONArray.toJSONString(map));
		if(jsonObject!=null){
			huiFuLog.setRespData(jsonObject.toJSONString());
			huiFuLog.setRespCode(jsonObject.getString("RespCode"));
			huiFuLog.setRespDesc(jsonObject.getString("RespDesc"));
		}
		huiFuLog.setId(IdGenerator.randomUUID());
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		huiFuLog.setCreateDate(format.format(new java.util.Date()));
		huiFuLogService.SaveHuiFuLog(huiFuLog);
    }
	@Override
	public String doFormPost(Map<String, String> params) {
		HuiFuLog huiFuLog=new HuiFuLog();
    	huiFuLog.setVersion(params.get("Version"));
    	huiFuLog.setCmdIds(params.get("CmdId")==null?"":params.get("CmdId").toString());
    	huiFuLog.setMerCustId(params.get("MerCustId")==null?"":params.get("MerCustId").toString());
    	Map<String, Object> map=new HashMap<String, Object>();
    	for (String  str : params.keySet()) {
    		map.put(str, params.get(str));
		}
    	huiFuLog.setParam(JSONArray.toJSONString(map));
    	huiFuLog.setId(IdGenerator.randomUUID());
    	if(params.get("OrdId")==null){
    		huiFuLog.setMerPriv(IdGenerator.randomUUID());
    		params.put("MerPriv", huiFuLog.getMerPriv());
    	}else{
    		huiFuLog.setOrderId(params.get("OrdId"));
    	}
    	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	huiFuLog.setCreateDate(format.format(new java.util.Date()));
    	huiFuLogService.SaveHuiFuLog(huiFuLog);
    	
		StringBuffer sb = new StringBuffer();
		try {
			params=HuiFuHttpUtil.getParams(params);
			sb.append("<html><head>");
			sb.append("<title>跳转......</title>");
			sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=\"UTF-8\">");
			sb.append("</head><body>");
			sb.append("<form action=\"" + HuiFuConstants.Config.HUIFU_HTTP_URL
					+ "\" method=\"post\" id=\"tform\" style=\"display:none;\">");
			for (String key : params.keySet()) {
				sb.append("<input type=\"hidden\" name=\"" + key + "\" value=\""
						+ StringEscapeUtils.escapeHtml(params.get(key)) + "\">");
			}
			sb.append("</form>");
			sb.append("<script type=\"text/javascript\">document.getElementById(\"tform\").submit()</script>");
			sb.append("</body></html>");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString() ;
	}
	
}
