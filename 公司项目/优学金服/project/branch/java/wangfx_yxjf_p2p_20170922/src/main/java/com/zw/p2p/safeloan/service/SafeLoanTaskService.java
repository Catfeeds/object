package com.zw.p2p.safeloan.service;

import com.zw.p2p.invest.model.Invest;

public interface SafeLoanTaskService {

	/**
	 * @Description: TODO(1更新产品审核期内未投满为复核状态,2无忧宝产品期满，更新状态为结算期,3所有投资记录已结清更新状态为结束)
	 * @author cuihang
	 * @date 2016-1-12 下午6:30:26
	 */
	public void updateSafeLoanStatus();
	/**
	 * @Description: TODO(接收特定用户待转让债权)
	 * @author cuihang
	 * @date 2016-1-13 下午1:49:12
	 */
	public void autoTurnSafeLoan() throws Exception;
	/**
	 * @Description: TODO(自动投资)
	 * @author cuihang
	 * @date 2016-1-13 下午1:49:12
	 */
	public void autoSafeLoan() throws Exception;
	/**
	*@Description: TODO(无忧宝第二种计息方式，不进行复利投资，匹配债权时间) 
	* @author cuihang   
	*@date 2016-6-14 下午2:07:15 
	*@throws Exception
	 */
	public void autoSafeLoanTwo() throws Exception;

	/**
	 * @Description: TODO(//对结算期的产品投资记录中计息中的进行债权转让并结息，计息成功改成已结清，所有记录都更新为已结清时更新产品状态为结束)
	 * @author cuihang
	 * @throws Exception
	 * @date 2016-1-14 下午1:49:12
	 */
	public void updateExpireSafeLoanIncome() throws Exception;
	/**
	 * @Description: TODO(判断该投资记录是否是是无忧宝自动投标，如果是进行计息,更新投资记录可投金额，修改safeloanuserloan状态为收益)
	 * @author cuihang
	 * @date 2016-1-15 下午1:49:12
	 */
	public boolean updateWuyoubaoLoanRecord(Invest invest,double interest,int status);
	/**
	 * @Description: TODO(判断该投资记录是否是是无忧宝自动投标)
	 * @author cuihang
	 * @date 2016年1月18日 15:11:52
	 */
	public boolean investInclude(Invest invest);
	
	/**
	 * 
	*@Description: TODO(自动投资超时时返还当前可投的本息) 
	* @author cuihang   
	*@date 2016-2-17 上午9:35:22 
	*@return
	 */
	public boolean retunAutoTimeout();
	/**
	*@Description: TODO(发送无忧宝结息短信) 
	* @author cuihang   
	*@date 2016-6-22 下午2:12:53
	 */
	public void sendSafeLoanSms()throws Exception;
}
