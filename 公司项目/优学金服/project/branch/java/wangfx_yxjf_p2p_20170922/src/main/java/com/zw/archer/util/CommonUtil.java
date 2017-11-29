package com.zw.archer.util;

import java.util.Random;

public class CommonUtil {
	public static String getRandomString(int length) { // length表示生成字符串的长度
		String base = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		int baslength=base.length();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(baslength);
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
