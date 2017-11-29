package com.zw.p2p.report.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.core.annotations.ScopeType;

/**
 * @Description: TODO(用户在投资金一览表)
 * @author ch
 * @date 2016-9-21 下午6:37:05
 */
@Component
@Scope(ScopeType.VIEW)
public class UserRepayList extends EntitySQLQuery<Object> {
	private static final String lazyModelCountHql = "SELECT COUNT(lr.id)FROM loan_repay lr LEFT JOIN loan l ON lr.loan_id =l.id ";

	private static final String lazyModelHql = "SELECT l.type,l.user_id,lr.repay_day,l.name,l.money,l.deadline,give_money_time,(lr.corpus+lr.interest) AS repaymoney FROM loan_repay lr LEFT JOIN loan l ON lr.loan_id =l.id    ";

	private String username;// 借款人
	private Date repayTimeB;// 还款时间开始
	private Date repayTimeE;// 还款时间结束
	private String listJson;
	SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdfmd = new SimpleDateFormat("MM-dd");

	public UserRepayList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		setPageSize(1000);
		final String[] RESTRICTIONS = { " lr.status='repaying'", " l.user_id = #{userRepayList.username}", " lr.repay_day >= #{userRepayList.repayTimeB}", " lr.repay_day <= #{userRepayList.repayTimeE}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getRepayTimeB() {
		return repayTimeB;
	}

	public void setRepayTimeB(Date repayTimeB) {
		this.repayTimeB = repayTimeB;
	}

	public Date getRepayTimeE() {
		if (null != repayTimeE) {
			try {
				return sdft.parse(sdf.format(repayTimeE) + " 23:59:59");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return repayTimeE;
	}

	public void setRepayTimeE(Date repayTimeE) {
		this.repayTimeE = repayTimeE;
	}

	public String getListJson() {
		JSONObject jso = new JSONObject();
		jso.put("status", "success");

		List<Object> list = getLazyModelData();
		String[] xLine = new String[list.size()];
		Double[] yLine = new Double[list.size()];
		int i = 0;
		for (Object obj : list) {
			xLine[i] = sdfmd.format((Date) ((Map) obj).get("repay_day"));
			yLine[i] = (Double) ((Map) obj).get("repaymoney");
			i++;
		}
		LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();
		String oldstr = "";
		Double strSum = 0d;
		DecimalFormat df = new DecimalFormat("#0.00");
		for (int j = 0; j < xLine.length; j++) {
			String xlinestr = xLine[j];
			Double ylinestr = yLine[j];
			if (xlinestr.equals(oldstr)) {
				strSum = strSum + ylinestr;
			} else {
				oldstr = xlinestr;
				strSum = ylinestr;
			}
			map.put(xlinestr, new BigDecimal(strSum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}
		jso.put("xLine", map.keySet());
		jso.put("yLine", map.values());
		return jso.toJSONString();
	}

	public void setListJson(String listJson) {
		this.listJson = listJson;
	}
}
