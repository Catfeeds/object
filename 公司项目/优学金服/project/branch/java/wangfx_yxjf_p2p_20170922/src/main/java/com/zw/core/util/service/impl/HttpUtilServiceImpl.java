package com.zw.core.util.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zw.core.util.service.HttpUtilService;

@Service("httpUtilService")
public class HttpUtilServiceImpl implements HttpUtilService {
    /**
     * 后台请求方法
     * @author majie
     * @param params
     * @param url
     * @return
     * @throws Exception
     * @date 2016年8月13日 下午4:22:52
     */
	@Override
    public  JSONObject doPost(String url, Map<String, String> params) throws Exception {
		JSONObject result = null;
      
        List<NameValuePair> nvps = buildNameValuePair(params);
        UrlEncodedFormEntity formEntity = null;
        formEntity = new UrlEncodedFormEntity(nvps,"UTF-8");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //EntityBuilder builder = EntityBuilder.create();
        try {
            HttpPost httpPost = new HttpPost(url);
          //  builder.setParameters(nvps);
            httpPost.setEntity(formEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getReasonPhrase().equals("OK")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                  String  resultstr = EntityUtils.toString(entity, "UTF-8");
                  result=JSONObject.parseObject(resultstr);
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }
	@Override
    public  JSONObject doGet(String url, Map<String, String> params) throws Exception {
		JSONObject result = null;
		StringBuffer paraStringBuffer=new StringBuffer("?");
      for (String  key : params.keySet()) {
    	  paraStringBuffer.append(key).append("=").append(params.get(key)).append("&");
	}
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //EntityBuilder builder = EntityBuilder.create();
        try {
            HttpPost httpPost = new HttpPost(url+paraStringBuffer);
          //  builder.setParameters(nvps);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getReasonPhrase().equals("OK")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                  String  resultstr = EntityUtils.toString(entity, "UTF-8");
                  result=JSONObject.parseObject(resultstr);
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }
    
    
    /**
     * MAP类型数组转换成NameValuePair类型
     * 
     */
    public static List<NameValuePair> buildNameValuePair(Map<String, String> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        return nvps;
    }


    
}
