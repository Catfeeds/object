package com.zw.p2p.risk.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.privilege.model.Privilege;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.risk.SystemBillConstants;
import com.zw.p2p.risk.model.SystemBill;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.safeloan.model.SafeLoanRecord;

@Service(value = "systemBillService")
@SuppressWarnings("unchecked")
public class SystemBillServiceImpl implements SystemBillService {

	@Resource
	private HibernateTemplate ht;
	@Logger
	Log log;

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void adjustFee(double money, String reason, String detail) {
		try {
			if (money>0) {
				transferInto(money, UserBillConstants.Type.ADJUST_FEE,
						detail, null, null, null, null, null,null,null,null);
			}else {
				transferOut(Math.abs(money), UserBillConstants.Type.ADJUST_FEE,
						detail, null, null, null, null, null,null,null,null,null);
			}
		}catch (InsufficientBalance e){
			e.printStackTrace();
			log.error("手续费调整失败");
		}

	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	private SystemBill getLastestBill() {
		DetachedCriteria criteria = DetachedCriteria.forClass(SystemBill.class);
		criteria.setLockMode(LockMode.UPGRADE);
		criteria.addOrder(Order.desc("seqNum"));
		List<SystemBill> ibs = ht.findByCriteria(criteria, 0, 1);
		if (ibs.size() > 0) {
			SystemBill sb = ibs.get(0);
			if (sb.getBalance() == null) {
				double in = getSumByType(SystemBillConstants.Type.IN);
				double out = getSumByType(SystemBillConstants.Type.OUT);
				sb.setBalance(ArithUtil.sub(in, out));
				ht.update(sb);
			}
			return sb;
		}
		return null;
	}

	@Override
	public double getBalance() {
		// double in = getSumByType(SystemBillConstants.Type.IN);
		// double out = getSumByType(SystemBillConstants.Type.OUT);
		// return ArithUtil.sub(in, out);
		SystemBill sb = getLastestBill();
		return sb == null ? 0D : sb.getBalance();
	}

	/**
	 *
	 * @param money 金额
	 * @param reason 操作类型
	 * @param detail 操作详情
	 *
	 * @param loanRepay
	 * @param withdrawCash
	 * @param investRepay
	 * @param recharge
	 * @param invest
	 * @param coupons
	 * @throws InsufficientBalance
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOut(double money, String reason, String detail, LoanRepay loanRepay  ,WithdrawCash withdrawCash ,
							InvestRepay investRepay , Recharge recharge ,Invest invest,SafeLoanRecord slr,Coupons coupons,Privilege privilege,User user)
			throws InsufficientBalance {
		if(money == 0 )
			return;
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		SystemBill ibLastest = getLastestBill();
		SystemBill ib = new SystemBill();

		if (recharge!=null){
			if (recharge.getRechargeWay().equals("huifu")) {
				ib.setMoneyResource("huifu_default");
			}else if (recharge.getRechargeWay().equals("rongbao")) {
				ib.setMoneyResource("reapal_default");
			}else if (recharge.getRechargeWay().equals("fengfu")){
				ib.setMoneyResource("sumapay_default");
			}
		}
		if (withdrawCash != null){
			ib.setMoneyResource(withdrawCash.getPay().getId());
		}else {
			ib.setMoneyResource("system_fee");
		}

//		double balance = getBalance();
		//在系统账户金额不足的情况下也可以扣款,by lijin,2015-03-13
//		if (balance < money) {
//			throw new InsufficientBalance("transfer out money:" + money
//					+ ", balance:" + balance);
//		} else {
		ib.setId(IdGenerator.randomUUID());
		ib.setMoney(money);
		ib.setTime(new Date());
		ib.setDetail(detail);
		ib.setReason(reason);
		ib.setType(SystemBillConstants.Type.OUT);
		ib.setWithdrawCash(withdrawCash);
		ib.setLoanRepay(loanRepay);
		ib.setInvestRepay(investRepay);
		ib.setInvest(invest);
		ib.setSafeLoanRecord(slr);
		ib.setCoupons(coupons);
		ib.setUser(user);
		if (invest != null){
			ib.setTransferApply(invest.getTransferApply());
		}
		ib.setRecharge(recharge);
		if (ibLastest == null) {
				// 第一条数据
				ib.setSeqNum(1L);
				// 余额=0
				ib.setBalance(ArithUtil.sub(0, money));
			} else {
				// 余额=上一条余额-取出的钱
				ib.setBalance(ArithUtil.sub(ibLastest.getBalance(), money));
				ib.setSeqNum(ibLastest.getSeqNum() + 1);
			}
			ht.save(ib);
//		}

	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferInto(double money, String reason, String detail,LoanRepay loanRepay  ,WithdrawCash withdrawCash , InvestRepay investRepay ,Recharge recharge ,Invest invest,SafeLoanRecord slr,Privilege privilege,User user) {
		if(money == 0 )
			return;
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		SystemBill ibLastest = getLastestBill();
		SystemBill lb = new SystemBill();
		if (withdrawCash != null){
			lb.setMoneyResource(withdrawCash.getPay().getId());
			lb.setWithdrawCash(withdrawCash);
		}else {
			lb.setMoneyResource("system_fee");
		}
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(detail);
		lb.setType(SystemBillConstants.Type.IN);
		lb.setReason(reason);
		lb.setLoanRepay(loanRepay);
		lb.setInvestRepay(investRepay);
		lb.setSafeLoanRecord(slr);
		lb.setInvest(invest);
		lb.setUser(user);
		if (invest != null){
			lb.setTransferApply(invest.getTransferApply());
		}
		lb.setRecharge(recharge);

		if (ibLastest == null) {
			// 第一条数据
			lb.setSeqNum(1L);
			// 余额=money
			lb.setBalance(money);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+money
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
		}
		ht.save(lb);
	}
	
	

	private double getSumByType(String type) {
		String hql = "select sum(sb.money) from SystemBill sb where sb.type =?";
		Double sum = (Double) ht.find(hql, type).get(0);
		if (sum == null) {
			return 0;
		}
		return sum.doubleValue();
	}

}
