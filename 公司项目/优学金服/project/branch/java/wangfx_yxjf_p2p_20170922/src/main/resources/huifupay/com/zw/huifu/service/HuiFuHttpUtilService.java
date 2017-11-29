package com.zw.huifu.service;

import java.util.Map;

public interface HuiFuHttpUtilService {
	/**
	 * 汇付天下后台请求方式
	 * */
	public String doPost(Map<String, String> params);
	
	
	/**
	 * 汇付天下页面请求方法
	 * */
	public String doFormPost(Map<String, String> params);
}
