package com.zw.p2p.baidumap.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zw.core.util.service.HttpUtilService;
import com.zw.p2p.baidumap.service.BaiDuMapService;

@Service("baiDuMapService")
public class BaiDuMapServiceImp implements BaiDuMapService {

	private String baiduak = "mVjh30t81Orknf4grG0m0yN3cG0GG0Gq";
	@Resource
	HttpUtilService httpUtilService;

	@Override
	public JSONObject getLocationByIp(String realip, String qterm) {
		// TODO Auto-generated method stub
		JSONObject jso = new JSONObject();
		Map<String, String> params = new HashMap<String, String>();
		params.put("qcip", realip);
		params.put("qterm", qterm);
		params.put("ak", baiduak);
		params.put("extensions", "1");
		params.put("callback_type", "json");
		try {
			jso = httpUtilService.doGet("http://api.map.baidu.com/highacciploc/v1", params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jso;
	}
	 public  String getIpAddress(HttpServletRequest request) { 
		    String ip = request.getHeader("x-forwarded-for"); 
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		      ip = request.getHeader("Proxy-Client-IP"); 
		    } 
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		      ip = request.getHeader("WL-Proxy-Client-IP"); 
		    } 
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		      ip = request.getHeader("HTTP_CLIENT_IP"); 
		    } 
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		      ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
		    } 
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		      ip = request.getRemoteAddr(); 
		    } 
		    return ip; 
		  } 
	 /**
	 *@Description: TODO(获得终端类型) 
	 * @author cuihang   
	 *@date 2016-11-8 上午11:54:12 
	 *@return
	  */
	 public String getQterm(HttpServletRequest request){
		 String userAgent = request.getHeader("user-agent");
		 if( userAgent.contains("Mobile")||userAgent.contains("iOS")||userAgent.contains("Android")){
			 return "mb";
		 }
		 return "pc";
	 }
}
