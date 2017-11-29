package com.zw.p2p.message.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.Config;
import com.zw.archer.util.Base64;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.RestUtil;
import com.zw.core.util.SpringBeanUtil;
import com.zw.p2p.message.exception.MailSendErrorException;
import com.zw.p2p.message.service.MailService;

@Service("mailService")
public class MailServiceImpl implements MailService {
	private static Properties props = new Properties();
	static{
		try {
			System.out.println(">>MailServiceImpl 00");
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("url.properties"));
			System.out.println(">>MailServiceImpl 11");
//			HibernateTemplate ht = (HibernateTemplate) SpringBeanUtil
//					.getBeanByName("ht");
//			System.out.println(">>MailServiceImpl 22");
//			host = ht.get(Config.class, ConfigConstants.Mail.MAIL_SMTP).getValue();
//			username = ht.get(Config.class, ConfigConstants.Mail.MAIL_USER_NAME)
//					.getValue();
//			password = ht.get(Config.class, ConfigConstants.Mail.MAIL_PASSWORD)
//					.getValue();
//			personal = ht.get(Config.class, ConfigConstants.Mail.MAIL_PERSONAL)
//					.getValue();
//			System.out.println(">>MailServiceImpl 33");
//			if (username != null && !"".equals(username)){
//				username = Base64.encodeBytes(username.getBytes("UTF-8"));
//			}
//			if (personal !=null && !"".equals(personal)){
//				personal = Base64.encodeBytes(personal.getBytes("UTF-8"));
//			}
//			System.out.println(">>MailServiceImpl 44");
		} catch (FileNotFoundException e) {
			System.out.println(">>MailServiceImpl FileNotFoundException");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(">>MailServiceImpl IOException");
			e.printStackTrace();
		}
	}
	@Logger
	Log log;

	private String host;
	private String username;
	private String password;
	private String personal;

	@Resource
	private HibernateTemplate ht;

	public String getHost() {
		if (host==null){
			host = ht.get(Config.class, ConfigConstants.Mail.MAIL_SMTP).getValue();
		}
		return host;
	}

	public String getUsername() {
		if (username==null){
			username = ht.get(Config.class, ConfigConstants.Mail.MAIL_USER_NAME).getValue();
			if (username != null && !"".equals(username)){
				try {
					username =username; //Base64.encodeBytes(username.getBytes("UTF-8"));
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return username;
	}

	public String getPassword() {
		if (password==null){
			password = ht.get(Config.class, ConfigConstants.Mail.MAIL_PASSWORD).getValue();
			if (password != null && !"".equals(password)){
				try {
					password = password;//Base64.encodeBytes(password.getBytes("UTF-8"));
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return password;
	}

	public String getPersonal() {
		if (personal==null){
			personal = ht.get(Config.class, ConfigConstants.Mail.MAIL_PERSONAL).getValue();
		}
		return personal;
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		// 设置邮件服务器
		HibernateTemplate ht = (HibernateTemplate) SpringBeanUtil
				.getBeanByName("ht");

		host = getHost();
		username = getUsername();
		password = getPassword();
		personal = getPersonal();

		properties.put("mail.smtp.host", host);
		// 验证
		properties.put("mail.smtp.auth", "true");
		return properties;
	}

	private Session getMailSession() {
		// 根据属性新建一个邮件会话
		return Session.getInstance(getProperties(), new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getUsername(), getPassword());
			}
		});

	}

	public void sendMail(String toAddress, String title, String content) throws MailSendErrorException {
		sendMail(toAddress, getPersonal(), title, content);
	}

	@SuppressWarnings("static-access")
	// FIXME:添加异常，显示邮件发送情况。另外，邮件发送，改为异步发送；如果异步，发送成功与否的消息怎么处理？
	public void sendMail(String toAddress, String personal, String title,
			String content) throws MailSendErrorException {
		final MimeMessage mailMessage;
		final Transport trans;
		Session mailSession = getMailSession();

		// 建立消息对象
		mailMessage = new MimeMessage(mailSession);
		try {
			// 发件人
			mailMessage.setFrom(new InternetAddress(getUsername(), personal));
			// 收件人
			mailMessage.setRecipient(MimeMessage.RecipientType.TO,
					new InternetAddress(toAddress));
			// 主题
			mailMessage.setSubject(title);
			// 内容
			// mailMessage.setText(content);
			mailMessage.setContent(content, "text/html;charset=utf-8");
			// 发信时间
			mailMessage.setSentDate(new Date());
			// 存储信息
			mailMessage.saveChanges();
			trans = mailSession.getTransport("smtp");

			// FIXME 需要修改为异步发送消息
			trans.send(mailMessage);

		} catch (Exception e) {
			log.info(e);
			throw new MailSendErrorException(e);
		}
	}
	public String getResult(String toAddress,String title,String content) throws Exception{
		sendMail(toAddress, getPersonal(), title, content);
		return "true";
	
	}
}
