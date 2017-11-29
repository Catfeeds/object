package com.zw.p2p.schedule.job;

import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.repay.service.RepayService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by lijin on 15/4/23.
 */
@Component
public class InvestDelay implements Job {
    @Resource
    private RepayService repayService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        System.out.println("任务Key:" + jobName + " 正在执行，执行时间: " + dateFormat.format(Calendar.getInstance().getTime()));

        try {
            repayService.investDelay();
            
            List<Invest> list = repayService.getUnRepayInvest();
            if(list != null && list.size()>0){
            	for(Invest invest:list){
            		repayService.giveUserRateMoney(invest);
            	}
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
