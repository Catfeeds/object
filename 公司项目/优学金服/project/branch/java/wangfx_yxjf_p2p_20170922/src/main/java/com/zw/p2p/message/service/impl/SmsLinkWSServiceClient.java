package com.zw.p2p.message.service.impl;

import java.util.Properties;
import java.util.Random;

import com.zw.p2p.message.exception.SmsSendErrorException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


/**
 * @author YTH
 *凌凱短信工具類
 */
public class SmsLinkWSServiceClient {
	
	private static Properties props = new Properties();
	static{
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("url.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	static String MsgUrl = props.getProperty("MsgUrl");
	static String sn = props.getProperty("sn");
	static String pwd = props.getProperty("pwd");
	static String sn2 = props.getProperty("sn2");
	static String pwd2 = props.getProperty("pwd2");
	
  public static void SnOrPwd(){
		
		if (sn == null || pwd == null) {
			throw new SmsSendErrorException("短信发送失败，sn或password未定义");
		}
  }

	/**
	 * 群发短信
	 * @param phoneNum
	 * @param content
	 * @return
	 */
	public static String sendSms(String phoneNum[] ,String content){
		SnOrPwd();
		for(int i=0;i<phoneNum.length; i++){
			sendSMS2(phoneNum[i],content);
		}
		return "ok";
	}
	
	/**
	 * 发送验证码信息
	 * @author Songlin Li
	 * @date 2016年5月11日
	 * @param mob
	 * @param content
	 * @return
	 */
	public static boolean sendSMS(String mob, String content) {
		SnOrPwd();
		boolean flag = false;
		URL url = null;
		try {
			// URLEncoder.encode 地址栏以gb2312加密 （编码）
			String strUrl = MsgUrl+"CorpID="
		            + URLEncoder.encode(sn, "GB2312")
					+"&Pwd=" 
		            +java.net.URLEncoder.encode(pwd, "GB2312")
					+"&Mobile=" 
		            +java.net.URLEncoder.encode(mob, "GB2312")
					+"&Content=" 
		            +java.net.URLEncoder.encode(content, "GB2312");
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			url = new URL(strUrl);
			// 建立URLConnection对象
			URLConnection UConn = url.openConnection();
			// 把服务器返回的字节流 读取 转成 BufferedReader
			BufferedReader breader = new BufferedReader(new InputStreamReader(UConn.getInputStream()));
			// 按行读取
			String str = breader.readLine();
			// 如果不为空
			while (str != null) {
				// 解码
				str = URLDecoder.decode(str, "GB2312");
				// 按照 &劈分
				String[] strs = str.split("&");
				// 把 result= 替换成 空
				// trim() 去掉前后空格
				// 如果是>=0
				int num = Integer.parseInt(strs[0].replace("result=", "").trim());
				//if (strs[0].replace("result=", "").trim().equals("0")) {
				if(num >= 0){
					flag = true;
				} else {
					flag = false;
				}
				str = breader.readLine();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 发送营销信息
	 * @author Songlin Li
	 * @date 2016年5月11日
	 * @param mob
	 * @param content
	 * @return
	 */
	public static boolean sendSMS2(String mob, String content) {
		SnOrPwd();
		boolean flag = false;
		URL url = null;
		try {
			// URLEncoder.encode 地址栏以gb2312加密 （编码）
			String strUrl = MsgUrl+"CorpID="
		            + URLEncoder.encode(sn, "GB2312")
					+"&Pwd=" 
		            +java.net.URLEncoder.encode(pwd, "GB2312")
					+"&Mobile=" 
		            +java.net.URLEncoder.encode(mob, "GB2312")
					+"&Content=" 
		            +java.net.URLEncoder.encode(content, "GB2312");
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			url = new URL(strUrl);
			// 建立URLConnection对象
			URLConnection UConn = url.openConnection();
			// 把服务器返回的字节流 读取 转成 BufferedReader
			BufferedReader breader = new BufferedReader(new InputStreamReader(UConn.getInputStream()));
			// 按行读取
			String str = breader.readLine();
			// 如果不为空
			while (str != null) {
				// 解码
				str = URLDecoder.decode(str, "GB2312");
				// 按照 &劈分
				String[] strs = str.split("&");
				// 把 result= 替换成 空
				// trim() 去掉前后空格
				// 如果是>=0
				int num = Integer.parseInt(strs[0].replace("result=", "").trim());
				//if (strs[0].replace("result=", "").trim().equals("0")) {
				if(num >= 0){
					flag = true;
				} else {
					flag = false;
				}
				str = breader.readLine();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * 发送验证码
	 * @param phoneNum
	 * @param content
	 * @return
	 */
	public static String sendCM(String phoneNum){
		SnOrPwd();
		String cm=creatCM(6);
		boolean flag = sendSMS(phoneNum, "尊敬的用户，您的验证码为："+cm+"，如非本人操作请忽略！");
		if(flag){
			return cm;
		}
		return null;
	}
	/**
	 * 生成验证码
	 * @param passLenth 验证码长度
	 * @return
	 */
	
	private static String creatCM(int passLenth){
		StringBuffer buffer = new StringBuffer("0123456789");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int range = buffer.length();
		for (int i = 0; i < passLenth; i++) {
			// 生成指定范围类的随机数0—字符串长度(包括0、不包括字符串长度)
			sb.append(buffer.charAt(r.nextInt(range)));
		}
		return sb.toString();
	}
	
	public static void trustAllHttpsCertificates() throws Exception {

		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];

		javax.net.ssl.TrustManager tm = new miTM();

		trustAllCerts[0] = tm;

		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");

		sc.init(null, trustAllCerts, null);

		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());

	}
	
	
	public static class miTM implements javax.net.ssl.TrustManager,javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}
		
		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs) {
			return true;
		}
		
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
		
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
	
	static HostnameVerifier hv = new HostnameVerifier() {
		public boolean verify(String urlHostName, SSLSession session) {
			System.out.println("Warning: URL Host: " + urlHostName + " vs. "
					+ session.getPeerHost());
			return true;
		}
	};
}
