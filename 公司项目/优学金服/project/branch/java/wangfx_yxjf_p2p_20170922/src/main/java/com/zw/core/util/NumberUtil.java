package com.zw.core.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 15-3-23
 * Time: 下午3:43
 * To change this template use File | Settings | File Templates.
 */
public class NumberUtil {
	/**
	 * 金额格式化
	 * @param s 金额
	 * @param len 小数位数
	 * @return 格式后的金额
	 */
	public static String insertComma(String s, int len) {
		if (s == null || s.length() < 1) {
			return "";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(s);
		if (len == 0) {
			formater = new DecimalFormat("###,###");

		} else {
			StringBuffer buff = new StringBuffer();
			buff.append("###,###.");
			for (int i = 0; i < len; i++) {
				buff.append("#");
			}
			formater = new DecimalFormat(buff.toString());
		}
		return formater.format(num);
	}
	/**
	 * 金额去掉“,”
	 * @param s 金额
	 * @return 去掉“,”后的金额
	 */
	public static String delComma(String s) {
		String formatString = "";
		if (s != null && s.length() >= 1) {
			formatString = s.replaceAll(",", "");
		}

		return formatString;
	}
}
