package com.zw.p2p.message.service;

import com.zw.p2p.message.exception.SmsSendErrorException;


/**
 * 发短信
 * 返回信息详见文档。
 * @author Administrator
 * 
 */
public abstract class SmsService {

	/**
	 * 发送短信
	 * @param content
	 * @param mobileNumber
	 * @throws SmsSendErrorException
	 */
	public abstract boolean send(String content, String mobileNumber) throws SmsSendErrorException;

	/**
	 * 发送短信验证码
	 * @param content
	 * @param mobileNumber
	 * @return
	 * @throws Exception
	 */
	public abstract String sendCM(String content, String mobileNumber) throws Exception;

	/**
	 * 发送普通短信
	 * @param content
	 * @param mobileNumber
	 * @return
	 * @throws Exception
	 */
	public abstract boolean sendMsg(String content, String mobileNumber) throws Exception;
	/**
	 * 群发短信
	 * @param content
	 * @param mobileNumber
	 * @return
	 * @throws Exception
	 */
	public abstract String sendSms(String content, String mobileNumber[]) throws Exception;
}
