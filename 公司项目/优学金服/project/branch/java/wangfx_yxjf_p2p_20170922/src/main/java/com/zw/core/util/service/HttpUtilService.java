package com.zw.core.util.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface HttpUtilService {
	/**
	*@Description: TODO(后台直连请求方法) 
	* @author cuihang   
	*@date 二〇一六年十一月四日 11:15:56
	*@param serviceName
	*@param json
	*@return
	*@throws Exception
	 */
	 public  JSONObject doPost(String url, Map<String, String> params) throws Exception ;
	 public  JSONObject doGet(String url, Map<String, String> params) throws Exception ;
}