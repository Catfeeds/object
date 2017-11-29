package com.zw.core.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

public class HttpClientUtil {
		
	public final static int SUCCESS = 1 ;
	
	public final static int FAIL = 0 ;
	
	public static String getResponseBodyAsString(String url ){
		GetMethod get = new GetMethod(url);
		
		HttpClient client = new HttpClient();
		try {
			client.executeMethod(get);
//			System.out.println(url);
//			System.out.println( get.getResponseCharSet() );
			return get.getResponseBodyAsString();
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null ;
	}
	
	public static int saveHtmlFromRemoteSite(String url ,String filePath){
		File file = new File(filePath);
			
		return saveHtmlFromRemoteSite(url ,file) ;
		
	}
	
	
	public static int saveHtmlFromRemoteSite(String url ,File file){
		if(!file.exists()){
			//file.mkdirs();
			try {
				File temp = file.getParentFile();
				if(!temp.exists()){
					temp.mkdirs();
				}
				file.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
				return FAIL ;
			}
		}
		
		try {
			final String response = getResponseBodyAsString(url);
			//System.out.println(response);
//			FileUtils.writeStringToFile(file, response , EncodingUtil.UTF8);
			FileUtils.writeByteArrayToFile(file, response.getBytes( "utf-8" ));
		} catch (IOException e) {
			e.printStackTrace();
			return FAIL ;
		}
		
		return SUCCESS ;
	}
	
	public static String requestParametersToString(ServletRequest request) {
		StringBuffer sb = new StringBuffer();
		Map map = request.getParameterMap();
		for (Object str : map.keySet()) {
			sb.append(str);
			sb.append(":");
			sb.append(request.getParameter(str.toString()));
			sb.append("  ");
		}
		return sb.toString();
	}

	/**
	 * Map<String,String[]> 转 Map<String,String>
	 *@Description: TODO(用一句话描述该文件做什么) 
	 * @author cuihang   
	 *@date 二〇一五年十二月二十八日 12:05:40
	 *@param param
	 *@return
	 */
	public static Map<String,String> translateMap(Map<String,String[]> param){
		Map<String,String> result = new HashMap<String,String>();
		if(param == null)
			return result;
		Iterator it = param.keySet().iterator();
		String key = null;
		while(it.hasNext()){
			key = String.valueOf(it.next());
			String[] values = param.get(key);
			if(values != null && values.length > 0){
				result.put(key, values[0]);
			}
		}
		
		return result;
	} 
}
