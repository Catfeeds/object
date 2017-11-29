package com.zw.p2p.user.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.UserConstants.WithdrawStatus;
import com.zw.archer.user.model.UserBill;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemePay;
import com.zw.p2p.fee.service.impl.FeeSchemeServiceImpl;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.risk.FeeConfigConstants.FeePoint;
import com.zw.p2p.risk.FeeConfigConstants.FeeType;
import com.zw.p2p.risk.model.FeeConfig;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.risk.service.impl.FeeConfigBO;
import com.zw.p2p.user.service.WithdrawCashService;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-24 下午3:25:48
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-24 wangzhi 1.0
 */
@Service(value = "withdrawCashService")
public class WithdrawCashServiceImpl implements WithdrawCashService {

	@Logger
	private static Log log;

	@Resource
	HibernateTemplate ht;

	@Resource
	private FeeConfigBO feeConfigBO;

	@Resource
	UserBillBO userBillBO;

	@Resource
	SystemBillService sbs;

	@Resource
	FeeSchemeServiceImpl feeSchemeService;

	@Override
	public double calculateFee(double amount,String userid) {
//		return feeConfigBO.getFee(FeePoint.WITHDRAW, FeeType.FACTORAGE, null,
//				null, amount);
		return feeSchemeService.getCustomerWithdrawFee(amount,userid);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void passWithdrawCashApply(WithdrawCash withdrawCash) {
		// 更新提现审核状态，到等待复核状态
		WithdrawCash wdc = ht.get(WithdrawCash.class, withdrawCash.getId());
		ht.evict(wdc);
		wdc = ht.get(WithdrawCash.class, wdc.getId(), LockMode.UPGRADE);
		if (wdc.getStatus().equals(WithdrawStatus.WAIT_VERIFY)) {
			wdc.setVerifyTime(new Date());
			wdc.setStatus(UserConstants.WithdrawStatus.RECHECK);
			wdc.setVerifyMessage(withdrawCash.getVerifyMessage());
			wdc.setVerifyUser(withdrawCash.getVerifyUser());
			wdc.setPay(withdrawCash.getPay());
			ht.merge(wdc);

			if (log.isInfoEnabled())
				log.info("提现审核初审通过，提现编号：" + wdc.getId() + "，审核人："
						+ withdrawCash.getVerifyUser().getId() + "，审核时间:"
						+ wdc.getVerifyTime());
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void passWithdrawCashRecheck(WithdrawCash withdrawCash) {
		// 从冻结金额中取，系统账户也要记录
		WithdrawCash wdc = ht.get(WithdrawCash.class, withdrawCash.getId());
		ht.evict(wdc);
		wdc = ht.get(WithdrawCash.class, wdc.getId(), LockMode.UPGRADE);
		if (wdc.getStatus().equals(WithdrawStatus.RECHECK) || wdc.getStatus().equals(WithdrawStatus.WAIT_VERIFY)) {
			wdc.setRecheckTime(new Date());
			wdc.setStatus(UserConstants.WithdrawStatus.SUCCESS);
			wdc.setRecheckMessage(withdrawCash.getRecheckMessage());
			wdc.setRecheckUser(withdrawCash.getRecheckUser());
			wdc.setPay(withdrawCash.getPay());
			ht.merge(wdc);
			try {
				userBillBO.transferOutFromFrozen(wdc.getUser().getId(),
						wdc.getMoney(), OperatorInfo.WITHDRAW_SUCCESS,
						"提现申请通过，扣除提现金额",null );
				/*userBillBO.transferOutFromFrozen(wdc.getUser().getId(),
						wdc.getFee(), OperatorInfo.WITHDRAW_SUCCESS,
						"提现申请通过，扣除手续费",null );*/
//				sbs.transferInto(wdc.getFee(), OperatorInfo.WITHDRAW_SUCCESS,
//						"提现申请通过, 扣除手续费",null,wdc,null,null,null,null,null,null);
				sbs.transferOut(feeSchemeService.getWithdrawFee(wdc.getPay(),wdc.getMoney()), OperatorInfo.WITHDRAW_PAYMENT,
						"客户提现申请通过, 系统提现扣款支出" ,null,wdc,null,null,null,null,null,null,null);
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			}

			if (log.isInfoEnabled())
				log.info("提现审核复核通过，提现编号：" + wdc.getId());
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void refuseWithdrawCashApply(WithdrawCash withdrawCash) {
		// 解冻申请时候冻结的金额
		WithdrawCash wdc = ht.get(WithdrawCash.class, withdrawCash.getId());
		ht.evict(wdc);
		wdc = ht.get(WithdrawCash.class, wdc.getId(), LockMode.UPGRADE);
		if (wdc.getStatus().equals(WithdrawStatus.RECHECK)
				|| wdc.getStatus().equals(WithdrawStatus.WAIT_VERIFY)) {
			if(wdc.getStatus().equals(WithdrawStatus.RECHECK)){
				wdc.setRecheckMessage(withdrawCash.getRecheckMessage());
				wdc.setRecheckUser(withdrawCash.getRecheckUser());
				wdc.setRecheckTime(new Date());
			}else{
				wdc.setVerifyMessage(withdrawCash.getVerifyMessage());
				wdc.setVerifyUser(withdrawCash.getVerifyUser());
				wdc.setVerifyTime(new Date());
			}
			wdc.setStatus(UserConstants.WithdrawStatus.VERIFY_FAIL);
			ht.merge(wdc);
			try {
				userBillBO.unfreezeMoney(wdc.getUser().getId(), wdc.getMoney(),
						OperatorInfo.REFUSE_APPLY_WITHDRAW,
						"提现申请被拒绝，解冻提现金额",null);
				userBillBO.unfreezeMoney(wdc.getUser().getId(), wdc.getFee(),
						OperatorInfo.REFUSE_APPLY_WITHDRAW, "提现申请被拒绝，解冻手续费", null);

				sbs.transferOut(feeSchemeService.getWithdrawFee(wdc.getPay(),wdc.getMoney()), OperatorInfo.WITHDRAW_FAIL,
						"客户提现申请被支付平台拒绝, 系统提现扣款支出" ,null,wdc,null,null,null,null,null,null,null);
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			}
			if (log.isInfoEnabled())
				log.info("提现审核未通过，提现编号：" + wdc.getId());
		}
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void failWithdrawCashApply(String  withdrawCashid,String RespDesc) {
		// 解冻申请时候冻结的金额
		WithdrawCash wdc = ht.get(WithdrawCash.class, withdrawCashid);
		ht.evict(wdc);
		wdc = ht.get(WithdrawCash.class, wdc.getId(), LockMode.UPGRADE);
		if (wdc.getStatus().equals(WithdrawStatus.RECHECK)
				|| wdc.getStatus().equals(WithdrawStatus.WAIT_VERIFY)) {
			if(wdc.getStatus().equals(WithdrawStatus.RECHECK)){
				wdc.setRecheckTime(new Date());
			}else{
				wdc.setVerifyTime(new Date());
			}
			wdc.setStatus(UserConstants.WithdrawStatus.VERIFY_FAIL);
			ht.merge(wdc);
			try {
				userBillBO.unfreezeMoney(wdc.getUser().getId(), wdc.getMoney(),
						OperatorInfo.REFUSE_APPLY_WITHDRAW,
						RespDesc+"，解冻提现金额",null);
				userBillBO.unfreezeMoney(wdc.getUser().getId(), wdc.getFee(),
						OperatorInfo.REFUSE_APPLY_WITHDRAW, RespDesc+"，解冻手续费", null);
				
			} catch (InsufficientBalance e) {
				throw new RuntimeException(e);
			}
			if (log.isInfoEnabled())
				log.info("提现"+RespDesc+"，提现编号：" + wdc.getId());
		}
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void applyWithdrawCash(WithdrawCash withdraw)
			throws InsufficientBalance {
		// FIXME:缺验证
//		withdraw.setFee(calculateFee(withdraw.getMoney(),withdraw.getUser().getId()));
		withdraw.setFee(withdraw.getHuiFuFee());
		withdraw.setCashFine(0D);

		withdraw.setId(generateId());
		withdraw.setTime(new Date());

		//设置一个默认的支付公司，否则会出错
		FeeSchemePay schemePay= new FeeSchemePay();
		if (feeSchemeService.getCurrentFeeScheme().getSchemePays().size()>0){
			schemePay= feeSchemeService.getCurrentFeeScheme().getSchemePays().get(0);
		}
		withdraw.setPay(schemePay);

		userBillBO.freezeMoney(withdraw.getUser().getId(), withdraw.getMoney(),
				OperatorInfo.APPLY_WITHDRAW,
				"申请提现，冻结提现金额",null,null);
		/*userBillBO.freezeMoney(withdraw.getUser().getId(), withdraw.getFee(),
				OperatorInfo.APPLY_WITHDRAW,"申请提现，冻结提现手续费",null,null);*/
		sbs.transferOut(withdraw.getFee(), "applyWithdraw", "自动扣除", null, withdraw, null, null, null, null, null, null, null);
		// 等待审核
		withdraw.setStatus(UserConstants.WithdrawStatus.WAIT_VERIFY);
		ht.save(withdraw);
	}

	/**
	 * 生成id
	 * 
	 * @return
	 */
	private String generateId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select withdraw from WithdrawCash withdraw where withdraw.id = (select max(withdrawM.id) from WithdrawCash withdrawM where withdrawM.id like ?)";
		List<WithdrawCash> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			WithdrawCash withdrawCash = contractList.get(0);
			ht.lock(withdrawCash, LockMode.UPGRADE);
			String temp = withdrawCash.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%09d", itemp);
		return gid;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void withdrawByAdmin(UserBill ub) throws InsufficientBalance {
		WithdrawCash wc = new WithdrawCash();
		wc.setCashFine(0D);
		wc.setFee(0D);
		wc.setId(generateId());
		wc.setIsWithdrawByAdmin(true);
		wc.setMoney(ub.getMoney());
		wc.setStatus(WithdrawStatus.SUCCESS);
		wc.setTime(new Date());
		wc.setUser(ub.getUser());
		ht.save(wc);
		userBillBO.transferOutFromBalance(ub.getUser().getId(), ub.getMoney(),
				OperatorInfo.ADMIN_OPERATION, ub.getDetail());
	}

	@Override
	public List<WithdrawCash> getApproving(String userid) {
		// TODO Auto-generated method stub
		String hql = "select withdraw from WithdrawCash withdraw where withdraw.status in('wait_verify','recheck') and withdraw.user.id='"+userid+"'";
		List<WithdrawCash> contractList = ht.find(hql);
		return contractList;
	}

	@Override
	public double countWithdraw(String userid) {
		// TODO Auto-generated method stub
		Double result=0d;
		List<WithdrawCash> list=getApproving(userid);
		for (WithdrawCash withdrawCash : list) {
			result=ArithUtil.add(result,withdrawCash.getMoney());
		}
		return result;
	}

	@Override
	public String getIdByHuiFuOrderId(String huiFuOrderId) {
		List<WithdrawCash> list = ht.find("from WithdrawCash c where c.huiFuOrderId='"+huiFuOrderId+"'");
		if(list != null && list.size()>0){
			return list.get(0).getId();
		}
		return null;
	}

}
