package com.zw.p2p.baidumap.service;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;



public interface BaiDuMapService {
	/**
	*@Description: TODO(根据ip查询地址) 
	* @author cuihang   
	*@date 2016-11-8 下午2:04:45 
	*@param realip
	*@param qterm
	*@return
	 */
	public JSONObject getLocationByIp(String realip,String qterm);
	 public String getQterm(HttpServletRequest request);
	 public  String getIpAddress(HttpServletRequest request);
}
