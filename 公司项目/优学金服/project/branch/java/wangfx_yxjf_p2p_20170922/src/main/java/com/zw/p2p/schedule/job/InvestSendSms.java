package com.zw.p2p.schedule.job;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.p2p.repay.service.RepayService;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lijin on 4/5/23.
 */
@Component
public class InvestSendSms implements Job {
    @Resource
    private RepayService repayService;
    @Resource
	SafeLoanTaskService safeLoanTaskService;
    @Resource
	ConfigService configService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        System.out.println("任务Key:" + jobName + " 正在执行，执行时间: " + dateFormat.format(Calendar.getInstance().getTime()));

        String enabletasteinvest = "0";
		enabletasteinvest = configService
				.getConfigValue(ConfigConstants.Schedule.AUTO_LOAN);
		if (enabletasteinvest.equals("1")) {
			try {
				safeLoanTaskService.sendSafeLoanSms();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
