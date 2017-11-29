package com.ymrt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YMRTConstants {
	private static Properties props;
	static {
		props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("com/ymrt/ymrt.properties"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("找不到ymrt.properties文件", e);
		} catch (IOException e) {
			throw new RuntimeException("读取ymrt.properties文件出错", e);
		}
	}
	
	public static  class Config {
		/** APPID **/
	    public static final String APP_ID  = props.getProperty("APPID");

	    /** SECRET_KEY **/
	    public static final String SECRET_KEY = props.getProperty("SECRET_KEY");
	    
	    /** HOST 接口地址**/
	    public static final String HOST=props.getProperty("HOST");
	    
	    /**ALGORITHM 加密算法**/
	    public static final String ALGORITHM=props.getProperty("ALGORITHM");
	}
}
