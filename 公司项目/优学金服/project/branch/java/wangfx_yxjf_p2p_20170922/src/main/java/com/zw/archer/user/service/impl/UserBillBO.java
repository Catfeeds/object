package com.zw.archer.user.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.model.User;
import com.zw.archer.user.model.UserBill;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.model.LoanRepay;

@Service(value = "userBillBO")
public class UserBillBO {

	@Resource
	private HibernateTemplate ht;

	public UserBill getLastestBill(String userId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(UserBill.class);
		criteria.addOrder(Order.desc("seqNum"));
		criteria.setLockMode(LockMode.UPGRADE);
		criteria.add(Restrictions.eq("user.id", userId));
		List<UserBill> ibs = ht.findByCriteria(criteria, 0, 1);
		if (ibs.size() > 0) {
			UserBill ub = ibs.get(0);
			if (ub.getBalance() == null || ub.getFrozenMoney() == null) {
				if (ub.getBalance() == null) {
					double freeze = getSumByType(userId,
							UserBillConstants.Type.FREEZE);//冻结
					double transferIntoBalance = getSumByType(userId,
							UserBillConstants.Type.TI_BALANCE);//转入到余额 tansfer into balance
					double transferOutFromBalance = getSumByType(userId,
							UserBillConstants.Type.TO_BALANCE);//从冻结金额中转出 transfer out frome frozen money
					double unfreeze = getSumByType(userId,
							UserBillConstants.Type.UNFREEZE);//解冻
					ub.setBalance(ArithUtil.add(ArithUtil.sub(ArithUtil.sub(
							transferIntoBalance, transferOutFromBalance),
							freeze), unfreeze));
				}
				if (ub.getFrozenMoney() == null) {
					double freeze = getSumByType(userId,
							UserBillConstants.Type.FREEZE);//冻结
					double transferOutFromFrozen = getSumByType(userId,
							UserBillConstants.Type.TO_FROZEN);//从冻结金额中转出 transfer out frome frozen money
					double unfreeze = getSumByType(userId,
							UserBillConstants.Type.UNFREEZE);//解冻
					ub.setFrozenMoney(ArithUtil.sub(
							ArithUtil.sub(freeze, unfreeze),
							transferOutFromFrozen));
				}
				ht.update(ub);
			}
			return ub;
		}
		return null;
	}

	/**
	 * 冻结金额
	 * 
	 * @param userId 用户的id
	 * @param money 金额
	 * @param operatorInfo  资金转移的操作类型
	 * @param operatorDetail  资金转移的详述
	 * @throws InsufficientBalance 抛出异常 余额不足以支付保证金
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void freezeMoney(String userId, double money, String operatorInfo,
			String operatorDetail,String loanId,String otherMoney) throws InsufficientBalance {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		double balance = getBalance(userId);
		if (balance < money) {
			throw new InsufficientBalance("freeze money:" + money
					+ ", balance:" + balance);
		} else {
			ib.setId(IdGenerator.randomUUID());
			ib.setMoney(money);
			ib.setTime(new Date());
			ib.setDetail(operatorDetail);
			ib.setType(UserBillConstants.Type.FREEZE);
			ib.setTypeInfo(operatorInfo);
			ib.setUser(new User(userId));
			ib.setLoanId(loanId);
			if (ibLastest == null) {
				ib.setSeqNum(1L);
				// 余额=0
				ib.setBalance(0D);
				// 最新冻结金额=0
				ib.setFrozenMoney(0D);
				ib.setSafeLoanfrozenMoney(0d);
			} else {
				ib.setSeqNum(ibLastest.getSeqNum() + 1);
				// 余额=上一条余额-将要被冻结的金额
				ib.setBalance(ArithUtil.sub(ibLastest.getBalance(), money));
				// 最新冻结金额=上一条冻结+将要冻结
				ib.setFrozenMoney(ArithUtil.add(ibLastest.getFrozenMoney(), money));
				ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
			}
			if(otherMoney!=null){
				if(otherMoney.startsWith("c")){
					ib.setCouponsMoney(otherMoney.substring(1));
				}else if(otherMoney.startsWith("r")){
					ib.setRateRate(otherMoney.substring(1));
				}
			}
			ht.save(ib);
		}
	}

	/**
	 * 获取余额
	 * 
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public double getBalance(String userId) {
		UserBill ub = getLastestBill(userId);
		return ub == null ? 0D : ub.getBalance();
		// double freeze = getSumByType(userId, UserBillConstants.Type.FREEZE);
		// double transferIntoBalance = getSumByType(userId,
		// UserBillConstants.Type.TI_BALANCE);
		// double transferOutFromBalance = getSumByType(userId,
		// UserBillConstants.Type.TO_BALANCE);
		// double transferOutFromFrozen =
		// getSumByType(UserBillConstants.Type.TO_FROZEN);
		// double unfreeze = getSumByType(userId,
		// UserBillConstants.Type.UNFREEZE);
		// return ArithUtil.add(ArithUtil.sub(
		// ArithUtil.sub(transferIntoBalance, transferOutFromBalance),
		// freeze), unfreeze);
	}

	/**
	 * 获取冻结金额
	 * 
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public double getFrozenMoney(String userId) {
		UserBill ub = getLastestBill(userId);
		return ub == null ? 0D : ub.getFrozenMoney();
		// double freeze = getSumByType(userId, UserBillConstants.Type.FREEZE);
		// double transferOutFromFrozen = getSumByType(userId,
		// UserBillConstants.Type.TO_FROZEN);
		// double unfreeze = getSumByType(userId,
		// UserBillConstants.Type.UNFREEZE);
		// return ArithUtil.sub(ArithUtil.sub(freeze, unfreeze),
		// transferOutFromFrozen);
	}
	/**
	 * 获取无忧宝冻结金额
	 * 
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public double getSafeLoanFrozenMoney(String userId) {
		UserBill ub = getLastestBill(userId);
		return ub == null ? 0D : ub.getSafeLoanfrozenMoney();
		// double freeze = getSumByType(userId, UserBillConstants.Type.FREEZE);
		// double transferOutFromFrozen = getSumByType(userId,
		// UserBillConstants.Type.TO_FROZEN);
		// double unfreeze = getSumByType(userId,
		// UserBillConstants.Type.UNFREEZE);
		// return ArithUtil.sub(ArithUtil.sub(freeze, unfreeze),
		// transferOutFromFrozen);
	}

	/**
	 * 从冻结金额中转出
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 * @throws InsufficientBalance
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String loanId)
			throws InsufficientBalance {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		double frozen = getFrozenMoney(userId);
		if (frozen < money) {
			throw new InsufficientBalance("transfer from frozen money:" + money
					+ ", frozen money:" + frozen);
		}
		ib.setId(IdGenerator.randomUUID());
		ib.setMoney(money);
		ib.setTime(new Date());
		ib.setDetail(operatorDetail);
		ib.setType(UserBillConstants.Type.TO_FROZEN);
		ib.setTypeInfo(operatorInfo);
		ib.setUser(new User(userId));
		ib.setLoanId(loanId);
		if (ibLastest == null) {
			ib.setSeqNum(1L);
			// 余额=0
			ib.setBalance(0D);
			// 最新冻结金额=0
			ib.setFrozenMoney(0D);
			ib.setSafeLoanfrozenMoney(0d);
		} else {
			ib.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额
			ib.setBalance(ibLastest.getBalance());
			// 最新冻结金额=上一条冻结-取出的
			ib.setFrozenMoney(ArithUtil.sub(ibLastest.getFrozenMoney(), money));
			ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(ib);
	}

	/**
	 * 解冻金额
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 * @throws InsufficientBalance
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void unfreezeMoney(String userId, double money, String operatorInfo,
			String operatorDetail,String loanId) throws InsufficientBalance {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		double frozen = getFrozenMoney(userId);
		if (frozen < money) {
			throw new InsufficientBalance("unfreeze money:" + money
					+ ", frozen money:" + frozen);
		} else {
			ib.setId(IdGenerator.randomUUID());
			ib.setMoney(money);
			ib.setTime(new Date());
			ib.setDetail(operatorDetail);
			ib.setType(UserBillConstants.Type.UNFREEZE);
			ib.setTypeInfo(operatorInfo);
			ib.setUser(new User(userId));
			ib.setLoanId(loanId);
			if (ibLastest == null) {
				ib.setSeqNum(1L);
				// 余额=0
				ib.setBalance(0D);
				// 最新冻结金额=0
				ib.setFrozenMoney(0D);
				ib.setSafeLoanfrozenMoney(0d);
			} else {
				ib.setSeqNum(ibLastest.getSeqNum() + 1);
				// 余额=上一条余额+解冻的金额
				ib.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
				// 最新冻结金额=上一条冻结-解冻的金额
				ib.setFrozenMoney(ArithUtil.sub(ibLastest.getFrozenMoney(), money));
				ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
			}
			ht.save(ib);
		}
	}

	/**
	 * 从余额转出
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 * @throws InsufficientBalance
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromBalance(String userId, double money,
			String operatorInfo, String operatorDetail)
			throws InsufficientBalance {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		double balance = getBalance(userId);
		if (balance < money) {
			throw new InsufficientBalance("transfer out money:" + money
					+ ",balance:" + balance);
		} else {
			ib.setId(IdGenerator.randomUUID());
			ib.setMoney(money);
			ib.setTime(new Date());
			ib.setDetail(operatorDetail);
			ib.setType(UserBillConstants.Type.TO_BALANCE);
			ib.setTypeInfo(operatorInfo);
			ib.setUser(new User(userId));
			if (ibLastest == null) {
				ib.setSeqNum(1L);
				// 余额=0
				ib.setBalance(0D);
				// 最新冻结金额=0
				ib.setFrozenMoney(0D);
				ib.setSafeLoanfrozenMoney(0d);
			} else {
				ib.setSeqNum(ibLastest.getSeqNum() + 1);
				// 余额=上一条余额-money
				ib.setBalance(ArithUtil.sub(ibLastest.getBalance(), money));
				// 最新冻结金额=上一条冻结
				ib.setFrozenMoney(ibLastest.getFrozenMoney());
				ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
			}
			ht.save(ib);
		}
	}

	/**
	 * 退还手续费
	 *
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void payback(String userId, double money,
									String operatorInfo, String operatorDetail) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.PAYBACK);
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));

		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=money
			lb.setBalance(money);
			// 最新冻结金额=上一条冻结-取出的
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0d);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+money
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
			// 最新冻结金额=上一条冻结
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			lb.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(lb);
	}

	/**
	 * 增加无忧宝记录
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 *//*
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void addSafeLoanRecord(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.SAFELOAN_RECORD);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		lb.setSeqNum(ibLastest.getSeqNum() + 1);
		// 余额不变
		lb.setBalance(ibLastest.getBalance());
		lb.setFrozenMoney(ibLastest.getFrozenMoney());
		lb.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}*/
	/**
	 * 转入到余额
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.TI_BALANCE);
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));

		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=money
			lb.setBalance(money);
			// 最新冻结金额=上一条冻结-取出的
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0d);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+money
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
			// 最新冻结金额=上一条冻结
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			lb.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(lb);
	}
	/**
	 * 无忧宝收益到余额
	 * 
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferSafeLoanIntoBalance(String userId, double money,
			String operatorInfo, String operatorDetail, String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.TI_BALANCE);
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=money
			lb.setBalance(money);
			// 最新冻结金额=上一条冻结-取出的
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0d);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+money
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
			// 最新冻结金额=上一条冻结
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			lb.setSafeLoanfrozenMoney(ArithUtil.sub(ibLastest.getSafeLoanfrozenMoney(),money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 * 转入到红包
	 *
	 * @param user
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferIntoCoupon(User user, double money,
									String operatorInfo, String operatorDetail) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(user.getId());
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.TI_COUPON);
		lb.setTypeInfo(operatorInfo);
		lb.setUser(user);

		if (ibLastest == null) {
			lb.setSeqNum(1L);
			lb.setBalance(0d);
			lb.setFrozenMoney(0d);
			lb.setSafeLoanfrozenMoney(0d);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			lb.setBalance(ibLastest.getBalance());
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			lb.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(lb);
	}
	/**
	 * 从红包转出
	 *
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 * @throws InsufficientBalance
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromCoupon(String userId, double money,
									   String operatorInfo, String operatorDetail,String loanId)
			throws InsufficientBalance {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		ib.setId(IdGenerator.randomUUID());
		ib.setMoney(money);
		ib.setTime(new Date());
		ib.setDetail(operatorDetail);
		ib.setType(UserBillConstants.Type.TO_COUPON);
		ib.setTypeInfo(operatorInfo);
		ib.setLoanId(loanId);
		ib.setUser(new User(userId));
		if (ibLastest == null) {
			ib.setSeqNum(1L);
			ib.setBalance(0d);
			ib.setFrozenMoney(0d);
			ib.setSafeLoanfrozenMoney(0d);
		} else {
			ib.setSeqNum(ibLastest.getSeqNum() + 1);
			ib.setBalance(ibLastest.getBalance());
			ib.setFrozenMoney(ibLastest.getFrozenMoney());
			ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(ib);
	}

	/**
	 * 转入到余额
	 *
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferIntoBalance(String userId, double money,
									String operatorInfo, String operatorDetail,String loanId) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.TI_BALANCE);
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		lb.setLoanId(loanId);

		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=money
			lb.setBalance(money);
			// 最新冻结金额=上一条冻结-取出的
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0d);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+money
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
			// 最新冻结金额=上一条冻结
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			lb.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
		}
		ht.save(lb);
	}
	/**
	 * 从余额转出
	 *
	 * @param userId
	 * @param money
	 * @param operatorInfo
	 * @param operatorDetail
	 * @throws InsufficientBalance
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromBalance(String userId, double money,
									   String operatorInfo, String operatorDetail,String loanId)
			throws InsufficientBalance {
		if(money == 0)
			return;
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill ib = new UserBill();
		double balance = getBalance(userId);
		if (balance < money) {
			throw new InsufficientBalance("transfer out money:" + money
					+ ",balance:" + balance);
		} else {
			ib.setId(IdGenerator.randomUUID());
			ib.setMoney(money);
			ib.setTime(new Date());
			ib.setDetail(operatorDetail);
			ib.setType(UserBillConstants.Type.TO_BALANCE);
			ib.setTypeInfo(operatorInfo);
			ib.setUser(new User(userId));
			ib.setLoanId(loanId);
			if (ibLastest == null) {
				ib.setSeqNum(1L);
				// 余额=0
				ib.setBalance(0D);
				// 最新冻结金额=0
				ib.setFrozenMoney(0D);
				ib.setSafeLoanfrozenMoney(0d);
			} else {
				ib.setSeqNum(ibLastest.getSeqNum() + 1);
				// 余额=上一条余额-money
				ib.setBalance(ArithUtil.sub(ibLastest.getBalance(), money));
				// 最新冻结金额=上一条冻结
				ib.setFrozenMoney(ibLastest.getFrozenMoney());
				ib.setSafeLoanfrozenMoney(ibLastest.getSafeLoanfrozenMoney());
			}
			ht.save(ib);
		}
	}

	private double getSumByType(String userId, String type) {
		String hql = "select sum(ub.money) from UserBill ub where ub.user.id =? and ub.type=?";
		Double sum = (Double) ht.find(hql, new String[] { userId, type })
				.get(0);
		if (sum == null) {
			return 0;
		}
		return ArithUtil.round(sum.doubleValue(), 2);
	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String setTypeInfo(UserBill userBill){
		if (userBill.getDetail().contains("债权转让成功"))
			userBill.setTypeInfo("transfered");
		ht.update(userBill);
		return null;
	}

	public String checkDetail(String detail){
		if (detail.contains("债权转让成功"))
			return "债权转让成功";
		if (detail.contains("提现申请通过，扣除手续费"))
			return  "提现成功手续费";
		if (detail.contains("申请提现，冻结提现手续费"))
			return "申请提现手续费";
		if (detail.contains("提现申请被拒绝，解冻手续费"))
			return "提现申请未通过手续费";
		return null;
	}
	/**
	*@Description: TODO(冻结无忧保冻结金额，从余额转入冻结
	* @author cuihang   
	*@date 2016-3-26 下午6:45:05 
	*@param userId
	*@param money
	*@param operatorInfo
	*@param operatorDetail
	*@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void freezeSafeLoanMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.SAFELOANFREEZE);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额-将要被冻结的金额
			lb.setBalance(ArithUtil.sub(ibLastest.getBalance(), money));
			// 最新无忧宝冻结金额=上一条冻结+将要冻结
			lb.setSafeLoanfrozenMoney(ArithUtil.add(ibLastest.getSafeLoanfrozenMoney(), money));
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 *@Description: TODO(解冻无忧宝冻结金额转入余额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午6:45:05 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void unfreezeSafeLoanMoney(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.UNSAFELOANFREEZE);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额=上一条余额+解冻的金额
			lb.setBalance(ArithUtil.add(ibLastest.getBalance(), money));
			
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			// 最新冻结金额=上一条冻结-解冻的金额
			lb.setSafeLoanfrozenMoney(ArithUtil.sub(ibLastest.getSafeLoanfrozenMoney(), money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 *@Description: TODO(从无忧宝冻结金额中转出) 
	 * @author cuihang   
	 *@date 2016-3-26 下午6:45:05 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.OUTSAFELOANFREEZE);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额不变
			lb.setBalance(ibLastest.getBalance());
			
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			// 最新冻结金额=上一条冻结转出的金额
			lb.setSafeLoanfrozenMoney(ArithUtil.sub(ibLastest.getSafeLoanfrozenMoney(), money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 *@Description: TODO(从无忧宝冻结金额中转到冻结金额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午6:45:05 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromsafeloanFrozenToFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.SAFELOANFREEZETOFREEZE);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额不变
			lb.setBalance(ibLastest.getBalance());
			
			lb.setFrozenMoney(ArithUtil.add(ibLastest.getFrozenMoney(),money));
			// 最新冻结金额=上一条冻结转出的金额
			lb.setSafeLoanfrozenMoney(ArithUtil.sub(ibLastest.getSafeLoanfrozenMoney(), money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 *@Description: TODO(从冻结金额中转到无忧宝冻结金额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午6:45:05 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferOutFromFrozenTosafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.FREEZETOSAFELOANF);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额不变
			lb.setBalance(ibLastest.getBalance());
			
			lb.setFrozenMoney(ArithUtil.sub(ibLastest.getFrozenMoney(),money));
			// 最新冻结金额=上一条冻结转出的金额
			lb.setSafeLoanfrozenMoney(ArithUtil.add(ibLastest.getSafeLoanfrozenMoney(), money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	/**
	 *@Description: TODO(转入无忧宝冻结金额) 
	 * @author cuihang   
	 *@date 2016-3-26 下午6:45:05 
	 *@param userId
	 *@param money
	 *@param operatorInfo
	 *@param operatorDetail
	 *@param safeloanid
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void transferInsafeloanFrozen(String userId, double money,
			String operatorInfo, String operatorDetail,String safeloanid) {
		if (money < 0) {
			throw new RuntimeException("money cannot be less than zero!");
		}
		UserBill ibLastest = getLastestBill(userId);
		UserBill lb = new UserBill();
		lb.setId(IdGenerator.randomUUID());
		lb.setMoney(money);
		lb.setTime(new Date());
		lb.setDetail(operatorDetail);
		lb.setType(UserBillConstants.Type.INSAFELOANFREEZE);//UserBillConstants.Type.TI_BALANCE
		lb.setTypeInfo(operatorInfo);
		lb.setUser(new User(userId));
		if (ibLastest == null) {
			lb.setSeqNum(1L);
			// 余额=0
			lb.setBalance(0D);
			// 最新冻结金额=0
			lb.setFrozenMoney(0D);
			lb.setSafeLoanfrozenMoney(0D);
		} else {
			lb.setSeqNum(ibLastest.getSeqNum() + 1);
			// 余额不变
			lb.setBalance(ibLastest.getBalance());
			
			lb.setFrozenMoney(ibLastest.getFrozenMoney());
			// 最新冻结金额=上一条冻结转出的金额
			lb.setSafeLoanfrozenMoney(ArithUtil.add(ibLastest.getSafeLoanfrozenMoney(), money));
		}
		lb.setLoanId(safeloanid);
		ht.save(lb);
	}
	
	/**
	 * 校验是否余额不足
	 * @author majie
	 * @param userId
	 * @param money
	 * @throws InsufficientBalance
	 * @date 2016年12月8日 上午9:31:38
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void checkBalance(LoanRepay repay)throws InsufficientBalance {
		if(repay == null)
			return;
		String userId = repay.getLoan().getUser().getId();
		//本金+ 利息+罚息（逾期利息+网站逾期罚息）+手续费（给系统的）
		double money = ArithUtil.add(repay.getCorpus(),repay.getInterest(),repay.getDefaultInterest(),repay.getFee());
		double balance = getBalance(userId);
		if (balance < money) {
			throw new InsufficientBalance("transfer out money:" + money
					+ ",balance:" + balance);
		}
	}
}
