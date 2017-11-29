package com.zw.core.jsf.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-12-4
 * Time: 下午8:42
 * To change this template use File | Settings | File Templates.
 */
public class RestUtil {

	public static String getRestData(String url, NameValuePair[] data){
		String result = post(url, data);
		JSONObject retData  = JSONObject.fromObject(result);
		if(null != retData && retData.getInt("code") == 0){
			return retData.getString("data");
		}else{
			return null;
		}

	}
	public static String  post(String uri, NameValuePair[] data) {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(uri);
//		post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf8");//在头文件中设置转码
//		post.setRequestBody(data);
		post.setRequestBody(data);
		try {
			client.executeMethod(post);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Header[] headers = post.getResponseHeaders();
		int statusCode = post.getStatusCode();

		for(Header h : headers){
			System.out.println(h.toString());
		}
		String result = "";
		try {
			result = new String(post.getResponseBodyAsString().getBytes("utf-8"));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		post.releaseConnection();
		System.out.println("#########RestReturn#########:"+result);
		return result;
	}

}
