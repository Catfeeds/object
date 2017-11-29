package com.zw.archer.user.exception;

/**
 * 发送短信失败
 * @author lijin
 *
 */
public class SendSMSException extends Exception{
	public SendSMSException(String message) {
		super(message);
	}
}
