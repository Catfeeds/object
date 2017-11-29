package com.zw.huifu.util;

import java.io.Serializable;
import java.security.MessageDigest;

import chinapnr.SecureLink;

public class SignUtils implements Serializable {
    private static final long  serialVersionUID = 3640874934537168392L;

    /**
     * RSA方式加签
     * 
     * @param custId
     * @param forEncryptionStr
     * @param charset
     * @return
     * @throws Exception 
     */
    public static String encryptByRSA(String forEncryptionStr) throws Exception {
        SecureLink sl = new SecureLink();
        int result = sl.SignMsg(HuiFuConstants.Config.RECV_MER_ID, HuiFuConstants.Config.MER_PRI_KEY_PATH, forEncryptionStr);
        if (result < 0) {
            // 打印日志 
            throw new Exception();
        }
        return sl.getChkValue();
    }

    public static boolean verifyByRSA(String forEncryptionStr, String chkValue)throws Exception {
        try {
            int verifySignResult = new SecureLink().VeriSignMsg(HuiFuConstants.Config.MER_PUB_KEY_PATH, forEncryptionStr, chkValue);
            return verifySignResult == 0;
        } catch (Exception e) {
            // 打印日志
            throw new Exception();
        }
    }
    /***
	 * MD5加码 生成32位md5码
	 */
	public static String MD5Encode(String inStr){
		MessageDigest md5 = null;
		try{
			md5 = MessageDigest.getInstance("MD5");
		}catch (Exception e){
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++){
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}
}
