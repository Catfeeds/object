package com.zw.p2p.schedule.job;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.core.annotations.Logger;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;

/**
 * 无忧宝产品相关定时任务
 * 
 * @author Administrator
 * 
 */
@Component
public class LoanAuto implements Job {
	@Resource
	ConfigService configService;
	@Resource
	SafeLoanTaskService safeLoanTaskService;
	@Logger
	Log log;

	@Override
	public synchronized void execute(JobExecutionContext context)
			throws JobExecutionException {
		String enabletasteinvest = "0";
		enabletasteinvest = configService
				.getConfigValue(ConfigConstants.Schedule.AUTO_LOAN);
		// 更新产品审核期内未投满为复核状态
		
		if (enabletasteinvest.equals("1")) {
			//更新产品审核期内未投满为复核状态 ,/将投资记录结算期进行付息并更新为已结清状态
			safeLoanTaskService.updateSafeLoanStatus();
			try {
				// 先投之前剩下的转让债券
				safeLoanTaskService.autoTurnSafeLoan();

				// 自动投标债权 复利投资不匹配无忧宝和债权
				safeLoanTaskService.autoSafeLoan();
				//第二中计息方式无忧宝
				safeLoanTaskService.autoSafeLoanTwo();
			} catch (Exception e) {
				e.printStackTrace();
				throw new JobExecutionException(e.getMessage());
			}
			try {
				// 对结算期的产品投资记录中计息中的进行债权转让并结息，计息成功改成已结清，所有记录都更新为已结清时更新产品状态为结束
				safeLoanTaskService.updateExpireSafeLoanIncome();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e);
			}
			safeLoanTaskService.updateSafeLoanStatus();
			safeLoanTaskService.retunAutoTimeout();			
		}

	}

}
