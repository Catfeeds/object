package com.zw.p2p.user.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.p2p.fee.model.FeeSchemePay;
import com.zw.p2p.fee.service.impl.FeeSchemePayServiceImpl;
import com.zw.p2p.fee.service.impl.FeeSchemeServiceImpl;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.risk.service.SystemBillService;

@Service
public class RechargeBO {
	
	@Resource
	HibernateTemplate ht;

	@Resource
	private UserBillService ibs;

	@Resource
	private SystemBillService sbs;

	@Resource
	FeeSchemeServiceImpl feeSchemeService;

	@Resource
	FeeSchemePayServiceImpl feeSchemePayService;

	@Logger
	Log log;

	/**
	 * 充值支付成功
	 * @param recharge
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void rechargeSuccess(Recharge recharge) {
		recharge.setStatus(UserConstants.RechargeStatus.SUCCESS);
		recharge.setSuccessTime(new Date());
		ht.merge(recharge);
		// 往InvestorBill中插入值并计算余额
		ibs.transferIntoBalance(recharge.getUser().getId(),
				recharge.getActualMoney(), OperatorInfo.RECHARGE_SUCCESS,
				"充值编号：" + recharge.getId());
		sbs.transferInto(
				recharge.getFee(),
				OperatorInfo.RECHARGE_SUCCESS,
				"充值手续费, 用户ID：" + recharge.getUser().getId() + "充值ID"
						+ recharge.getId(),null,null,null,recharge,null,null,null,null);

		//更新系统收益账户,by lijin,2015-03-10
		if (recharge.getRechargeWay()==null || recharge.getRechargeWay().equals("admin")){
			return;
		}

		FeeSchemePay schemePay=null;
		if (recharge.getRechargeWay().equals("huifu"))
			schemePay= feeSchemePayService.getFeeSchemePayById("huifu_default");
		else if (recharge.getRechargeWay().equals("rongbao"))
			schemePay= feeSchemePayService.getFeeSchemePayById("reapal_default");
		else if (recharge.getRechargeWay().equals("fengfu")){
			schemePay= feeSchemePayService.getFeeSchemePayById("sumapay_default");
		}

		if (schemePay==null)
			return;

		try {
			sbs.transferOut(feeSchemeService.getRechargeFee(schemePay, recharge.getActualMoney()), OperatorInfo.RECHARGE_PAYMENT,
					"客户充值, 系统充值扣款支出。充值ID:" + recharge.getId(),null,null,null,recharge,null,null,null,null,null);
		}catch (InsufficientBalance e){
			e.printStackTrace();
			log.error("系统充值扣款支出失败，充值ID："+recharge.getId());
		}

	}
}
