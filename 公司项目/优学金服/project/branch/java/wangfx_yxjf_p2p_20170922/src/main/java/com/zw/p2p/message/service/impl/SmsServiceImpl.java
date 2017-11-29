package com.zw.p2p.message.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.springframework.stereotype.Service;

import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.CacheData;
import com.zw.p2p.message.exception.SmsSendErrorException;
import com.zw.p2p.message.service.SmsService;


/**
 * 发短信 返回信息详见文档。
 * 
 * @author Administrator
 * 
 */
@Service("smsService")
public class SmsServiceImpl extends SmsService {
	private static Map<String,CacheData> smscodeCache=new HashMap<String,CacheData>();

	@Logger
	Log log;

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

	public boolean send(String content, String mobileNumber) {
		boolean flag = true;
		
		try {
			content = URLEncoder.encode(content, "utf-8");
			if(SmsYMRTServiceClient.sendSMS2(mobileNumber, content)){
			}
			else{
				throw new SmsSendErrorException("短信发送失败");
			}
			
		} catch (UnsupportedEncodingException e) {
			throw new SmsSendErrorException(null, e);
		}
		return flag;
	}

	/**
	 *发送手机验证码
	 * @param content
	 * @param mobileNumber
	 * @return
	 */
	public String sendCM(String content, String mobileNumber) throws Exception{
		return SmsYMRTServiceClient.sendCM(mobileNumber);
		}

	/**
	 *发送手机短信
	 * @param content
	 * @param mobileNumber
	 * @return
	 */
	public boolean sendMsg(String content, String mobileNumber) throws Exception{
		boolean flag = false;
		if(SmsYMRTServiceClient.sendSMS2(mobileNumber, content)){
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 群发短信
	 */
		@Override
		public String sendSms(String content, String mobileNumber[]) throws Exception {
			if(SmsYMRTServiceClient.sendGroupSMS(content,mobileNumber)){
				log.info("群发短信发送成功");
			}
			else{
				log.error("群发短信失败");
			}
			return null;
		}
}
