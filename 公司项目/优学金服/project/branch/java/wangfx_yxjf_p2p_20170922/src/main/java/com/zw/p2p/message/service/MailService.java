package com.zw.p2p.message.service;

import com.zw.p2p.message.exception.MailSendErrorException;

/**
 * 发送邮件
 * 
 * @author Administrator
 * 
 */
public interface MailService {
	
	/**
	 * 发送邮件
	 * @param toAddress 收件人地址
	 * @param personal 发件人称呼
	 * @param title 邮件标题
	 * @param content 邮件内容
	 * 
	 * @throws MailSendErrorException 邮件发送出错
	 */
	public void sendMail(String toAddress, String personal, String title,
			String content) throws MailSendErrorException;
	
	/**
	 * 发送邮件
	 * @param toAddress 收件人地址
	 * @param title 邮件标题
	 * @param content 邮件内容
	 * 
	 * @throws MailSendErrorException 邮件发送出错
	 */
	public void sendMail(String toAddress, String title,
			String content) throws MailSendErrorException;

	/**
	 * 发送邮件返回参数判断发送是否成功
	 * @param toAddress  收件人地址
	 * @param title  邮件主题
	 * @param content  邮件正文内容
	 * @return
	 */
	public String getResult(String toAddress, String title,String content) throws Exception;
}
