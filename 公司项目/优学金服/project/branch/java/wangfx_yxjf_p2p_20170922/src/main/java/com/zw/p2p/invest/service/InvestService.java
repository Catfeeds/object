package com.zw.p2p.invest.service;

import java.util.List;

import com.zw.archer.user.model.User;
import com.zw.p2p.invest.exception.ExceedMaxAcceptableRate;
import com.zw.p2p.invest.exception.ExceedMoneyNeedRaised;
import com.zw.p2p.invest.exception.IllegalLoanStatusException;
import com.zw.p2p.invest.exception.InvestException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.repay.model.InvestRepay;

/**
 * Filename: InvestService.java <br/>
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:投资service
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-4 下午3:36:30
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-4 wangzhi 1.0
 */
public interface InvestService {

	/**
	 * 生成id，当前日期+当前投资六位顺序序号+借款编号+投资所在借款中六位顺序序号
	 * 
	 * @param loanId
	 *            投资的借款的id
	 * @return 生成的借款id
	 */
	public String generateId(String loanId);

	/**
	 * 新建投资
	 * 
	 * @param invest 新建投资对象
	 * @throws InsufficientBalance 余额不足
	 * @throws ExceedMoneyNeedRaised 投资金额大于尚未募集的金额
	 * @throws ExceedMaxAcceptableRate 竞标借款利率大于借款者可接受的最高利率
	 * @throws ExceedDeadlineException 优惠券过期
	 * @throws UnreachedMoneyLimitException 优惠券未达到使用条件
	 * @throws IllegalLoanStatusException 借款不是可投资状态
	 *            
	 */
	public String create(Invest invest) throws InsufficientBalance, ExceedMoneyNeedRaised, ExceedMaxAcceptableRate, IllegalLoanStatusException,InvestException,Exception;
	
	/**
	 * 通过用户id，查询该用户的所有投资
	 * 
	 * @param userId
	 *            用户ID
	 * @return 该用户的所有投资
	 */
	//	public List<Invest> getInvestsByUserId(String userId);
	public Invest getInvstById(String id);
	/**
	 * 根据汇付id获得invest
	 * @param ordId
	 * @return
	 */
	public Invest getInvstByOrdId(String ordId);
	
	/**
	 * 查询当前id用户最近一次散投的投资记录
	 */
	 public Invest getLastInvestByUser(String userId);
	 
	 /**
	  * 更新用户的InvestRepay
	  * */
	 public void UpdateInvestRepay(InvestRepay investRepay);
	 
	 /**
	  * 查找InvestRepay
	  * */
	 public List<InvestRepay> findInvestRepay(String loanId,Integer period);
	 /**
	  *@Description: TODO(用一句话描述该文件做什么) 
	  * @author cuihang   
	  *@date 2016-10-31 下午3:36:26 
	  *@param loanType
	  *@return
	   */
	  public List<Invest> getInvestsByLoanType(User user,String loantypeb);
}
