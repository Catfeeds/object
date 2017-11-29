package com.zw.huifu.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import chinapnr.Base64;

/**
 * 汇付天下请求工具类
 * @author majie
 * @date 2016年8月13日 下午4:15:34
 */
public class HuiFuHttpUtil {
	
    /**
     * 汇付天下后台请求方法
     * @author majie
     * @param params
     * @param url
     * @return
     * @throws Exception
     * @date 2016年8月13日 下午4:22:52
     */
    public static String doPost(Map<String, String> params) throws Exception {
    	
        String result = null;
        params = getParams(params);
        List<NameValuePair> nvps = buildNameValuePair(params);
        UrlEncodedFormEntity formEntity = null;
        formEntity = new UrlEncodedFormEntity(nvps,"UTF-8");
//        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpClient httpclient = createSSLInsecureClient();
        //EntityBuilder builder = EntityBuilder.create();
        try {
            HttpPost httpPost = new HttpPost(HuiFuConstants.Config.HUIFU_HTTP_URL);
          //  builder.setParameters(nvps);
            httpPost.setEntity(formEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            try {
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getReasonPhrase().equals("OK")
                    && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(entity, "UTF-8");
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
     * 汇付天下页面请求方法
     * @author majie
     * @param params
     * @return
     * @throws Exception
     * @date 2016年8月15日 下午8:16:16
     */
    public static String doFormPost(Map<String, String> params) throws Exception {
    	params = getParams(params);
		StringBuffer sb = new StringBuffer();
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
		return sb.toString();
	}
    
    
    
    
    /**
     * 模拟放款接口请求参数
     * @throws Exception 
     * 
     */
    public static Map<String,String> needMd5=getNeedMd5();
    private static  Map<String,String> getNeedMd5(){
    	Map<String,String> result= new HashMap<String, String>();
    	result.put("AddBidInfo", "AddBidInfo");
    	result.put("Repayment", "Repayment");
    	result.put("CreditAssign", "CreditAssign");
    	result.put("QueryBidInfo", "QueryBidInfo");
    	return result;
    }
    //不参与加密字段
    private static final Map<String ,Map<String,String>> nocheckpara=getNoCheckPara();
	private static Map<String ,Map<String,String>> getNoCheckPara(){
		Map<String ,Map<String,String>> result=new HashMap<String, Map<String,String>>();
		String[] AddBidInfo=new String[]{"BidName","LoanPeriod","BorrName","BorrPurpose"};//录入标的
		String[] Loans=new String[]{"ProId"};//录入标的
		result.put("AddBidInfo", arrayToMap(AddBidInfo));
		result.put("Loans", arrayToMap(Loans));
		return result;
	}
	private static Map<String,String> arrayToMap(String[] str){
		Map<String,String> result=new HashMap<String, String>();
		for (String string : str) {
			result.put(string, string);
		}
	return result;
	}
    public static Map<String, String> getParams(Map<String, String> params) throws Exception {
        // 组装加签字符串原文
        // 注意加签字符串的组装顺序参 请照接口文档
        StringBuffer buffer = new StringBuffer();
        String CmdId=params.get("CmdId");
        Map<String,String> map=  nocheckpara.get(CmdId);
        Iterator<String> it = params.keySet().iterator();
        while(it.hasNext()){
        	String key=it.next();
        	if(null==map||!map.containsKey(key)){
        		buffer.append(StringUtils.trimToEmpty(params.get(key)));
        	}
        }
        String plainStr = buffer.toString();
        if(needMd5.containsKey(CmdId)){
        	plainStr=SignUtils.MD5Encode(plainStr);
        }
        System.out.println(plainStr);
        params.put("ChkValue", SignUtils.encryptByRSA(plainStr));
        
        return params;
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

    public static String getBase64Encode(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        try {
            byte[] bt = str.getBytes("UTF-8");
            str = String.valueOf(Base64.encode(bt));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getBase64Decode(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        char[] ch = str.toCharArray();
        byte[] bt = Base64.decode(ch);
        try {
            str = new String(bt,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
    
    /**
     * 对参数urlencode
     * @author majie
     * @param str
     * @return
     * @date 2016年8月15日 下午8:58:36
     */
    public static String getUrlencode(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return "";
    }
    
    /**
     * 对参数解密
     * @author majie
     * @param str
     * @return
     * @date 2016年8月15日 下午8:58:47
     */
    public static String getUrlDecode(String str) {
        if (str == null || "".equals(str)) {
            return "";
        }
        try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        return "";
    }
    
    
    /**
     * 创建 SSL连接
     * @return
     * @throws GeneralSecurityException
     */
    public static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                        public boolean isTrusted(X509Certificate[] chain,String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                        @Override
                        public boolean verify(String arg0, SSLSession arg1) {
                            return true;
                        }

                        @Override
                        public void verify(String host, SSLSocket ssl)
                                throws IOException {
                        }

                        @Override
                        public void verify(String host, X509Certificate cert)
                                throws SSLException {
                        }

                        @Override
                        public void verify(String host, String[] cns,
                                String[] subjectAlts) throws SSLException {
                        }

                    });
            
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
            
        } catch (GeneralSecurityException e) {
            throw e;
        }
    }
    public static String getPort(HttpServletRequest request){
    	int port=request.getServerPort();
    	if(80==port){
    		return "";
    	}
    	return ":"+port;
    }
}