package com.zw.p2p.invest.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

/**
 * 债权转让统计
 *
 * @author Administrator
 *
 */
@Component
public class TransferStatistics {

	@Resource
	HibernateTemplate ht;

	/**
	 * 根据类型计算本金总额
	 *
	 * @param type
	 * @return
	 */
	public double getAllMoneyByType(String type) {
		String hql = "select sum(ta.corpus) from TransferApply ta "
				+ "where ta.status =?";
		List<Object> oos = ht.find(hql, type);
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 根据类型计算数量
	 *
	 * @param type
	 * @return
	 */
	public long getCountByType(String type) {
		String hql = "select count(ta) from TransferApply ta "
				+ "where ta.status =?";
		List<Object> oos = ht.find(hql, type);
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Long) o;
	}

	/**
	 * 计算已转出的本金
	 *
	 * @return
	 */
	public double getAllMoneyByOut() {
		String hql = "from TransferApply ta where ta.status = ?";
		List<TransferApply> tas = ht.find(hql,"transfered");
		double money = 0D;
		for (TransferApply ta : tas){
			money += ta.getTransferOutPrice();
		}
		return money;
	}

	/**
	 * 计算转出的数量
	 *
	 * @return
	 */
	public long getCountOut() {
		String hql = "from TransferApply ta where ta.status = ?";
		List<TransferApply> tas = ht.find(hql,"transfered");
		return  tas.size();
	}


	/**
	 * 根据类型和转出用户计算本金总额
	 *
	 * @return
	 */
	public double getAllMoneyByTypeUser(String type, String userId) {
		String hql = "select sum(ta.corpus) from TransferApply ta "
				+ "where ta.status =? and ta.invest.user.id =?";
		List<Object> oos = ht.find(hql, new String[] { type, userId });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}

	/**
	 * 根据类型和转出用户计算数量
	 *
	 * @return
	 */
	public long getCountByTypeUser(String type, String userId) {
		String hql = "select count(ta) from TransferApply ta "
				+ "where ta.status =? and ta.invest.user.id =?";
		List<Object> oos = ht.find(hql, new String[] { type, userId });
		Object o = oos.get(0);
		if (o == null) {
			return 0;
		}
		return (Long) o;
	}

	/**
	 * 计算某用户已转出的本金
	 *
	 * @return
	 */
	public double getAllMoneyByUserOut(String userId) {
		List<TransferApply> tas = getTransferApply(userId);
		double money = 0D;
		for (TransferApply ta : tas){
			money += ta.getCorpus();
		}
		return money;
	}

	/**
	 * 计算某用户已转出债权的盈亏
	 *
	 * @return
	 */
	public double getAllGainByUserOut(String userId) {
		List<TransferApply> tas = getTransferApply(userId);
		double gain = 0D;
		for (TransferApply ta : tas){
			gain += 0-ta.getPremium();
		}
		return gain;
	}

	/**
	 * 计算某用户转出的数量
	 *
	 * @return
	 */
	public long getCountByUserOut(String userId) {
		List<TransferApply> tas = getTransferApply(userId);
		return tas.size();
	}

	/**
	 * 计算某用户转入本金
	 *
	 * @return
	 */
	public double getAllMoneyByUserIn(String userId) {
		double money = 0D;
		List<Invest> invests = getTransferedInvest(userId);
		for (Invest invest :invests){
			money += invest.getInvestMoney();
		}
		return money;
	}

	/**
	 * 计算某用户转入债权的盈亏
	 *
	 * @return
	 */
	public double getAllGainByUserIn(String userId) {
		List<Invest> invests = getTransferedInvest(userId);
		double gain = 0D;
		for (Invest invest : invests){
			gain += invest.getTransferApply().getPremium();
		}
		return gain;
	}

	/**
	 * 计算某用户转入的数量
	 *
	 * @return
	 */
	public long getCountByUserIn(String userId) {
		List<Invest> invests = getTransferedInvest(userId);
		return invests.size();
	}

	/**
	 * 查询债权转入的数据
	 * @param userId 用户Id
	 * @return
	 */
	public List<Invest> getTransferedInvest(String userId){
		String hql = "from Invest invest where invest.user.id = ? and invest.transferApply is not null";
		List<Invest> invests = ht.find(hql,userId);
		return invests;
	}

	/**
	 * 查询债权转出的数据
	 * @param userId 用户Id
	 * @return
	 */
	public List<TransferApply> getTransferApply(String userId){
		String hql =  "from TransferApply ta where ta.invest.user.id = ? and ta.status = ?";
		List<TransferApply> tas = ht.find(hql,userId,"transfered");
		return tas;
	}

	/**
	 * 查询可转出债权的数据
	 * @param userId 用户Id
	 * @return
	 */
	public List<Invest> getCanTransfer(String userId){
		String hql = "from Invest invest where invest.user.id = ? and invest.transferApply is null and invest.status = 'repaying'";
		List<Invest> invests = ht.find(hql,userId);
		return invests;
	}

	/**
	 * 查询正在转出的债权
	 * @return
	 */
	public List<TransferApply>  getTransferingApply(String userId){
		String hql = "from TransferApply ta where ta.status = ? and ta.invest.status = ? and ta.invest.user.id = ?";
		List<TransferApply> tas = ht.find(hql,"transfering","repaying",userId);
		return tas;
	}

	/**
	 * 计算转让中的债权数量
	 * @param userId
	 * @return
	 */
	public int getTransferingNumber(String userId){
		List<TransferApply> tas = getTransferingApply(userId);
		return tas.size();
	}

	/**
	 * 计算转让中的本金
	 * @param
	 * @return
	 */
	public double getTransferingCorpus(String userId){
		List<TransferApply> tas = getTransferingApply(userId);
		double corpus = 0D;
		for (TransferApply ta : tas){
			corpus += ta.getInvest().getRepayRoadmap().getUnPaidCorpus();
		}
		return corpus;
	}

	/**
	 * 查询所有的转让数据
	 * @return
	 */
	public List<TransferApply> getAllTransfer(){
		String hql = "from TransferApply ta where ta.status != ?";
		List<TransferApply> tas = ht.find(hql,"cancel");
		return tas;
	}

}
