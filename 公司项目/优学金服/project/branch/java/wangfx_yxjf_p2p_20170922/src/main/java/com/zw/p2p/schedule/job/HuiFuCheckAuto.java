package com.zw.p2p.schedule.job;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.huifu.service.HuiFuCheckErrorService;
import com.zw.p2p.message.service.SmsService;

/**
 * 汇付自动对账
 * 
 * @author Administrator
 * 
 */
@Component
public class HuiFuCheckAuto implements Job {
	@Resource
	HibernateTemplate ht;
	@Resource
	HuiFuCheckErrorService huiFuCheckErrorService;
	@Resource
	private SmsService smsService;
	@Logger
	Log log;
	static int sendFlag=1;//1初始 2当前检测已发送，同一次只发一次
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		sendFlag=1;
		huiFuCheckErrorService.cleanHuiFuCheckError();
		String hql="from User where usrCustId is not null";
		List<User> listuser=ht.find(hql);
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
		for (final User user : listuser) {
			fixedThreadPool.execute(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String huiFuCheckErrorid=huiFuCheckErrorService.checkUserBalance(user);
					if(null!=huiFuCheckErrorid&&sendFlag==1){
						try {
							//smsService.sendMsg("优学金服对账异常", "13370152067");
							sendFlag=2;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			});
			
		}
	}
}
