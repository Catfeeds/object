package com.zw.huifu.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * 订单号工具
 * @author majie
 * @date 2016年8月15日 下午4:54:21
 */
public class OrderNoService {

	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	
	public static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyyMMdd");
	
	/**
	 * 生成订单编号
	 * @author majie
	 * @return
	 * @date 2016年8月15日 下午4:54:38
	 */
    public static synchronized String getOrderNo() {  
    	String orderNo = SDF.format(new Date());
    	orderNo +=RandomStringUtils.randomNumeric(6);
        return orderNo;  
    }
    
    /**
	 * 生成8位当前日期(格式为 YYYYMMDD，例如 ：20130307)
	 * @author majie
	 * @return
	 * @date 2016年8月15日 下午4:54:38
	 */
    public static String getNowDate(){
        return SDF_DATE.format(new Date());  
    }
    
    public static void main(String[] args) {
		System.out.println(getNowDate());
		System.out.println(SDF.format(new Date()));
	}
}