package com.zw.archer.user.service.impl;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.model.UserBill;
import com.zw.archer.user.service.UserBillService;
import com.zw.p2p.loan.exception.InsufficientBalance;

@Service(value = "userBillService")
public class UserBillServiceImpl implements UserBillService {

	@Resource
	private HibernateTemplate ht;

	@Resource
	private UserBO userBO;

	@Resource
	private UserBillBO userBillBO;

	@Override
	public void paybackIntoBalance(String userId, double money, String operatorInfo, String operatorDetail) {
		userBillBO.payback(userId, money, operatorInfo, operatorDetail);
	}

	/**
	 * 冻结金额
	 * userId 用户id
	 * money 冻结金额
	 * operatorInfo 资金转移的操作类型
	 * operatorDetail  资金转移的详述
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void freezeMoney(String userId, double money, String operatorInfo,
			String operatorDetail,String loanId,String otherMoney) throws InsufficientBalance {
		// FIXME:验证用户不存在。其他方法同样需要验证。
		userBillBO.freezeMoney(userId, money, operatorInfo, operatorDetail,loanId,otherMoney);
	}

	@Override
	public double getBalance(String userId) {
		return userBillBO.getBalance(userId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String loanId)
			throws InsufficientBalance {
		userBillBO.transferOutFromFrozen(userId, money, operatorInfo,
				operatorDetail,loanId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void unfreezeMoney(String userId, double money, String operatorInfo,
			String operatorDetail,String loanId) throws InsufficientBalance {
		userBillBO.unfreezeMoney(userId, money, operatorInfo, operatorDetail,loanId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromBalance(String userId, double money,
			String operatorInfo, String operatorDetail)
			throws InsufficientBalance {
		userBillBO.transferOutFromBalance(userId, money, operatorInfo, operatorDetail);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail) {
		userBillBO.transferIntoBalance(userId, money, operatorInfo, operatorDetail);
	}
	@Override
	public void transferSafeLoanIntoBalance(String userId, double money, String operatorInfo, String operatorDetail, String safeloanid) {
		// TODO Auto-generated method stub
		userBillBO.transferSafeLoanIntoBalance(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	/*@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void addSafeLoanRecord(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.addSafeLoanRecord(userId, money, operatorInfo, operatorDetail,safeloanid);
	}*/
	@Override
	public void freezeSafeLoanMoney(String userId, double money, String operatorInfo, String operatorDetail, String safeloanid) {
		// TODO Auto-generated method stub
		userBillBO.freezeSafeLoanMoney(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void unfreezeSafeLoanMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.unfreezeSafeLoanMoney(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.transferOutFromsafeloanFrozen(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromsafeloanFrozenToFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.transferOutFromsafeloanFrozenToFrozen(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromFrozenTosafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.transferOutFromFrozenTosafeloanFrozen(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferInsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		userBillBO.transferInsafeloanFrozen(userId, money, operatorInfo, operatorDetail,safeloanid);
	}
	@Override
	public double getFrozenMoney(String userId) {
		return userBillBO.getFrozenMoney(userId);
	}
	@Override
	public double getSafeLoanFrozenMoney(String userId) {
		return userBillBO.getSafeLoanFrozenMoney(userId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void save(UserBill ub) {
		ht.saveOrUpdate(ub);
	}

	


}
