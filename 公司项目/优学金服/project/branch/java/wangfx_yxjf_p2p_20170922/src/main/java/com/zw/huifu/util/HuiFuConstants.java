package com.zw.huifu.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 配置文件读取类
 * @author majie
 * @date 2016年8月13日 下午4:28:10
 */
public class HuiFuConstants {
	
	private static Properties props;
	static {
		props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("huifu.properties"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("找不到huifu.properties文件", e);
		} catch (IOException e) {
			throw new RuntimeException("读取huifu.properties文件出错", e);
		}
	}

	public static  class Config {
		/** MD5签名类型 **/
	    public static final String SIGN_TYPE_MD5  = props.getProperty("SIGN_TYPE_MD5");

	    /** RSA签名类型 **/
	    public static final String SIGN_TYPE_RSA = props.getProperty("SIGN_TYPE_RSA");

	    /** 商户客户号 **/
	    public static final String RECV_MER_ID   = props.getProperty("RECV_MER_ID");
	    
	    /** 分账账户号 **/
	    public static final String DIV_ACCT_ID = props.getProperty("DIV_ACCT_ID");
	    
	    /** 商户的唯一性标识 **/
	    public static final String MERCUSTID   = props.getProperty("MERCUSTID");

	    /** 商户公钥文件地址 **/
	    public static final String MER_PUB_KEY_PATH = HuiFuConstants.class.getResource(props.getProperty("MER_PUB_KEY_NAME")).getPath();

	    /** 商户私钥文件地址 **/
	    public static final String MER_PRI_KEY_PATH = HuiFuConstants.class.getResource(props.getProperty("MER_PRI_KEY_NAME")).getPath();
	    
	    /** 请求地址 **/
	    public static final String HUIFU_HTTP_URL= props.getProperty("HTTP_URL");
	    
	    /** 异步回调地址  **/
	    public static final String BACK_URL= props.getProperty("BACK_URL");
	    
	    /** 同步返回页面地址  **/
	    public static final String RETURN_URL= props.getProperty("RETURN_URL");
	    /**汇付用户名前缀 **/
	    public static final String USER_BEF= props.getProperty("USER_BEF");
	}
	
	/**
	 * 记录标的投资人给借款人放款的订单号集合
	 * <loanId,Set<订单号>>
	 */
	public static Map<String,Set<String>> LOAN_PAY_NUM = new HashMap<String,Set<String>>();
	
	/**
	 * 获取放款投资记录次数
	 * @author majie
	 * @param loanId
	 * @param ordId
	 * @return
	 * @date 2016年8月20日 下午9:11:15
	 */
	public static int getLoanNum(String loanId,String ordId){
		Set<String> set = LOAN_PAY_NUM.get(loanId);
		if(set == null){
			Set<String> tempSet = new HashSet<String>();
			tempSet.add(ordId);
			LOAN_PAY_NUM.put(loanId, tempSet);
		}else{
			LOAN_PAY_NUM.get(loanId).add(ordId);
		}
		return LOAN_PAY_NUM.get(loanId).size();
	}
	
}
