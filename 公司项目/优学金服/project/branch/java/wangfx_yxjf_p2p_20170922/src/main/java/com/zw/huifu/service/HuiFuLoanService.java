package com.zw.huifu.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;
import com.zw.core.bean.ZwJson;
import com.zw.p2p.invest.exception.InvestException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.loan.model.Loan;


/**
 * 标的接口
 * @author ch
 * @date 2016年8月17日10:48:08
 */
public interface HuiFuLoanService {
	/**
	 * 标的信息录入接口
	 * AddBidInfo
	 */
	public JSONObject AddBidInfo(Loan loan);
	/**
	 * 主动投标
	 * @return
	 */
	public String InitiativeTender(Invest invest,Loan loan)throws InvestException;
	/**
	 * 分页查询汇付系统中标的投资信息
	 * @param proId
	 * @param PageNum
	 * @param PageSize
	 * @return
	 */
	public JSONArray QueryBidInfoDetail(String proId,int PageNum,int PageSize);

	/**
	 * 放款
	 * @author majie
	 * @param transAmt 金额
	 * @param outCustId 出账客户号
	 * @param inCustId 入账客户号
	 * @param unFreezeOrdId 解冻订单号
	 * @param proId  标的id
	 * @param subOrdId 投标订单流水是 SubOrdId
	 * @param subOrdDate  投标订单日期
	 * @param freezeTrxId 冻结唯一标示
	 * @return
	 * @date 2016年8月18日 上午11:47:02
	 */
//	public ZwJson loanMoney(String investId,Double transAmt,String outCustId,String inCustId,String unFreezeOrdId,String proId
//			,String subOrdId,String subOrdDate,String freezeTrxId);
	
	/**
	 * 根据借款id放款
	 * @author majie
	 * @param loan_id
	 * @return
	 * @date 2016年8月20日 下午7:35:17
	 */
	public ZwJson loanMoneyByLoanId(String loan_id);
	/**
	 * 投资债权转让
	 * @return
	 */
	public String creditAssign(String transferApplyId,User user) throws Exception;
	/**
	 * 
	*@Description: TODO(标的审核状态查询接口) 
	* @author cuihang   
	*@date 2016-9-7 下午7:21:52 
	*@return
	 */
	public JSONObject QueryBidInfo(String ProId);
}

