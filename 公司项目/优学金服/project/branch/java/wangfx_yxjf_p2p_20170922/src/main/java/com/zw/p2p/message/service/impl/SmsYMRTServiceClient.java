package com.zw.p2p.message.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ymrt.ResultModel;
import com.ymrt.YMRTConstants;
import com.ymrt.request.SmsBatchOnlyRequest;
import com.ymrt.request.SmsSingleRequest;
import com.ymrt.response.SmsResponse;
import com.ymrt.util.AES;
import com.ymrt.util.GZIPUtils;
import com.ymrt.util.JsonHelper;
import com.ymrt.util.http.EmayHttpClient;
import com.ymrt.util.http.EmayHttpRequestBytes;
import com.ymrt.util.http.EmayHttpResponseBytes;
import com.ymrt.util.http.EmayHttpResponseBytesPraser;
import com.ymrt.util.http.EmayHttpResultCode;
import com.zw.huifu.util.HuiFuConstants;



public class SmsYMRTServiceClient {
	
	
	/**
	 * 发送单条短信
	 * @param isGzip 是否压缩
	 */
	public static boolean sendSms(String appId,String secretKey,String host,String algorithm,String content, String customSmsId, String extendCode, String mobile,boolean isGzip,String encode) {
		SmsSingleRequest pamars = new SmsSingleRequest();
		pamars.setContent(content);
		pamars.setCustomSmsId(customSmsId);
		pamars.setExtendedCode(extendCode);
		pamars.setMobile(mobile);
		ResultModel result = request(appId,secretKey,algorithm,pamars, "http://" + host + "/inter/sendSingleSMS",isGzip,encode);
		System.out.println("result code :" + result.getCode());
		if("SUCCESS".equals(result.getCode())){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 发送批次短信
	 * @param isGzip 是否压缩
	 */
	public static boolean setBatchOnlySms(String appId,String secretKey,String host,String algorithm,String content, String extendCode, String[] mobiles,boolean isGzip,String encode) {
		SmsBatchOnlyRequest pamars = new SmsBatchOnlyRequest();
		pamars.setMobiles(mobiles);
		pamars.setExtendedCode(extendCode);
		pamars.setContent(content);
		ResultModel result = request(appId,secretKey,algorithm,pamars, "http://" + host + "/inter/sendBatchOnlySMS",isGzip,encode);
		System.out.println("result code :" + result.getCode());
		if("SUCCESS".equals(result.getCode())){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 发送验证码
	 * @param phoneNum
	 * @return
	 */
	public static String sendCM(String phoneNum){
		String cm=creatCM(6);
		String content="【优学金服】尊敬的用户，您的验证码是"+cm+"。如非本人操作请忽略";
		boolean flag=sendSms(YMRTConstants.Config.APP_ID, YMRTConstants.Config.SECRET_KEY, YMRTConstants.Config.HOST
				, YMRTConstants.Config.ALGORITHM, content, null, null, phoneNum, false, "UTF-8");
		if(flag){
			return cm;
		}
		return null;
	}
	
	/**
	 * 发送短信
	 * @param phoneNum
	 * @param content
	 * @return
	 */
	public static boolean sendSMS2(String phoneNum,String content){
		boolean flag=sendSms(YMRTConstants.Config.APP_ID, YMRTConstants.Config.SECRET_KEY, YMRTConstants.Config.HOST
				, YMRTConstants.Config.ALGORITHM, content, null, null, phoneNum, false, "UTF-8");
		return flag;
	}
	
	/**
	 * 群发短信
	 * */
	public static boolean sendGroupSMS(String content, String mobileNumber[]){
		boolean flag=setBatchOnlySms(YMRTConstants.Config.APP_ID, YMRTConstants.Config.SECRET_KEY, YMRTConstants.Config.HOST, YMRTConstants.Config.ALGORITHM,
				content, null, null, false, "UTF-8");
		return flag;
	}
	
	/**
	 * 生成验证码
	 * @param passLenth 验证码长度
	 * @return
	 */
	
	private static String creatCM(int passLenth){
		StringBuffer buffer = new StringBuffer("0123456789");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int range = buffer.length();
		for (int i = 0; i < passLenth; i++) {
			// 生成指定范围类的随机数0—字符串长度(包括0、不包括字符串长度)
			sb.append(buffer.charAt(r.nextInt(range)));
		}
		return sb.toString();
	}
	
	/**
	 * 公共请求方法
	 */
	public static ResultModel request(String appId,String secretKey,String algorithm,Object content, String url,final boolean isGzip,String encode) {
		Map<String, String> headers = new HashMap<String, String>();
		EmayHttpRequestBytes request = null;
		try {
			headers.put("appId", appId);
			headers.put("encode", encode);
			String requestJson = JsonHelper.toJsonString(content);
			System.out.println("result json: " + requestJson);
			byte[] bytes = requestJson.getBytes(encode);
			System.out.println("request data size : " + bytes.length);
			if (isGzip) {
				headers.put("gzip", "on");
				bytes = GZIPUtils.compress(bytes);
				System.out.println("request data size [com]: " + bytes.length);
			}
			byte[] parambytes = AES.encrypt(bytes, secretKey.getBytes(), algorithm);
			System.out.println("request data size [en] : " + parambytes.length);
			request = new EmayHttpRequestBytes(url, encode, "POST", headers, null, parambytes);
		} catch (Exception e) {
			System.out.println("加密异常");
			e.printStackTrace();
		}
		EmayHttpClient client = new EmayHttpClient();
		String code = null;
		String result = null;
		try {
			EmayHttpResponseBytes res = client.service(request, new EmayHttpResponseBytesPraser());
			if(res == null){
				System.out.println("请求接口异常");
				return new ResultModel(code, result);
			}
			if (res.getResultCode().equals(EmayHttpResultCode.SUCCESS)) {
				if (res.getHttpCode() == 200) {
					code = res.getHeaders().get("result");
					if (code.equals("SUCCESS")) {
						byte[] data = res.getResultBytes();
						System.out.println("response data size [en and com] : " + data.length);
						data = AES.decrypt(data, secretKey.getBytes(), algorithm);
						if (isGzip) {
							data = GZIPUtils.decompress(data);
						}
						System.out.println("response data size : " + data.length);
						result = new String(data, encode);
						System.out.println("response json: " + result);
					}
				} else {
					System.out.println("请求接口异常,请求码:" + res.getHttpCode());
				}
			} else {
				System.out.println("请求接口网络异常:" + res.getResultCode().getCode());
			}
		} catch (Exception e) {
			System.out.println("解析失败");
			e.printStackTrace();
		}
		ResultModel re = new ResultModel(code, result);
		return re;
	}
	public static void main(String[] args) {
		System.out.println(YMRTConstants.Config.ALGORITHM);
		//System.out.println(HuiFuConstants.Config.BACK_URL);
	}
}
