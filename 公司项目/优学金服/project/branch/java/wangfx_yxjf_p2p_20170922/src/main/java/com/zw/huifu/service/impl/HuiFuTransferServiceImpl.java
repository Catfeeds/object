package com.zw.huifu.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zw.core.bean.ZwJson;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuTransferService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;

@Service("huiFuTransferService")
public class HuiFuTransferServiceImpl implements HuiFuTransferService {
	@Resource
	private HuiFuHttpUtilService huiFuHttpUtilService;

	/**
	 * 给用户转账    (谨慎使用，一旦调用钱就立刻转账)
	 * @author majie
	 * @param inCustId 入账客户号
	 * @param transAmt 交易金额 
	 * @return
	 * @date 2016年8月29日 上午11:34:30
	 */
	@Override
	public ZwJson transferMoney(String inCustId, Double transAmt) {
		ZwJson result = new ZwJson();
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("Version", "10");// 2.0 接口中此字段的值为 20，如版本升级，能向前兼容
		params.put("CmdId", "Transfer");// 每一种消息类型代表一种交易，此处为  Transfer
		params.put("OrdId", OrderNoService.getOrderNo());// 由商户的系统生成，必须保证唯一，请使用纯数字
		params.put("OutCustId", HuiFuConstants.Config.MERCUSTID);//出账客户号(商户号)
		params.put("OutAcctId", HuiFuConstants.Config.DIV_ACCT_ID);//出账子账户
		params.put("TransAmt", new BigDecimal(transAmt).setScale(2,4).toString());// 泛指交易金额，如充值、支付、取现、冻结和解冻金额（金额格式必须是###.00）比如 2.00，2.0
		params.put("InCustId",inCustId);//入账客户号
		params.put("BgRetUrl", HuiFuConstants.Config.BACK_URL);// 通过后台异步通知，商户网站都应在应答接收页面输出 RECV_ORD_ID 字样的字符串， 表明商户已经收到该笔交易结果
		try {
			String jsonStr = huiFuHttpUtilService.doPost(params);
			JSONObject jsonObj = JSONObject.parseObject(jsonStr);
			if("000".equals(jsonObj.get("RespCode"))){
				result.setSuccess(true);
			}else{
				result.setSuccess(false);
			}
			result.setMsg(jsonObj.get("RespDesc").toString());
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setMsg("调用汇付转账接口异常");
		}
		return result;
	}
	public static void main(String[] args) {
		System.out.println(new BigDecimal(0.56d).setScale(2,4).toString());
	}
}
