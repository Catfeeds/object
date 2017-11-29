package com.zw.p2p.schedule.job;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.core.annotations.Logger;
import com.zw.core.util.SpringBeanUtil;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.loan.service.LoanService;

/**
 * 复核状态下散标，自动放款
 * 
 * @author Administrator
 * 
 */
@Component
public class LoanCompleteLoanMoney implements Job {
	@Resource
	ConfigService configService;
	@Resource
	LoanCalculator loanCalculator;
	@Resource
	HuiFuLoanService huiFuLoanService;
	@Resource
	 LoanService loanService;
	@Logger
	Log log;

	@Override
	public synchronized void execute(JobExecutionContext context) throws JobExecutionException {
		// 1:满标放款。2:满标复核
		//loanService = (LoanService) SpringBeanUtil.getBeanByName("loanService");
		String loanCompleteLoanMoney = "2";
		loanCompleteLoanMoney = configService.getConfigValue(ConfigConstants.Schedule.LOANCOMPLETELOANMONEY);
		if ("1".equals(loanCompleteLoanMoney)) {
			List<Loan> list = loanService.getRecheckLoanList();
			for (Loan loan : list) {
				String loanId = loan.getId();
				try {
					if (loanCalculator.calculateMoneyNeedRaised(loanId) == 0) {
						//投资完成则放款
						huiFuLoanService.loanMoneyByLoanId(loanId);			
					}
				} catch (Exception e) {
					// TODO: handle exception
					log.error(loanId + "自动放款出错");
				}
			}
		}

	}

}
