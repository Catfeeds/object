package com.zw.p2p.schedule.service.impl;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.ObjectNotFoundException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.core.annotations.Logger;
import com.zw.p2p.schedule.ScheduleConstants;
import com.zw.p2p.schedule.job.*;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description: 项目启动以后，初始化调度
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-4-10 下午12:52:57
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-4-10 wangzhi 1.0
 */
@Component
public class InitJobs implements ApplicationListener<ContextRefreshedEvent> {

	@Resource
	StdScheduler scheduler;

	@Logger
	static Log log;

	@Resource
	ConfigService configService;

	// 开启哪些调度，能手动控制
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			
			// root application context 没有parent，他就是老大
			// 需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。 初始化所有的调度。

			// 第三方资金托管，主动查询，默认不开启
			String enableRefreshTrusteeship = "0";
			try {
				enableRefreshTrusteeship = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_REFRESH_TRUSTEESHIP);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}

			// 自动还款+检查项目逾期，默认开启
			// FIXME:自动还款应该默认是关闭的
			String enableAutoRepayment = "1";
			try {
				enableAutoRepayment = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_AUTO_REPAYMENT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			String enableRepayAlert = "0";
			try {
				enableRepayAlert = configService
						.getConfigValue(ConfigConstants.Schedule.ENABLE_REPAY_ALERT);
			} catch (ObjectNotFoundException onfe) {
				onfe.printStackTrace();
			}
			try {
				//体验金定时任务
				initTasteinvest();
				//汇付对账定时任务
				initHuiFuCheckAuto();
				//自动投标定时任务
				initAutoLoan();
				//同步汇付银行卡
				synBankCardAuto();
				//复核状态下散标，自动放款
				
				//updateCouponStatusAuto();
				
				//updateRateTicketStatusAuto();
				
				initLoanCompleteLoanMoney();
				if (enableRefreshTrusteeship.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable refresh trusteeship schdule job");
					}
					// 第三方资金托管，主动查询
					CronTrigger trigger2 = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.REFRESH_TRUSTEESHIP_OPERATION,
											ScheduleConstants.TriggerGroup.REFRESH_TRUSTEESHIP_OPERATION));
					if (trigger2 == null) {
						initRefreshTrusteeshipJob();
					} else {
						scheduler.resumeTrigger(trigger2.getKey());
					}
				}

				if (enableAutoRepayment.equals("1")) {
					if (log.isDebugEnabled()) {
						log.debug("enable auto repayment schdule job");
					}
					// 到期自动还款，改状态（还款完成、逾期之类）
					CronTrigger trigger = (CronTrigger) scheduler
							.getTrigger(TriggerKey
									.triggerKey(
											ScheduleConstants.TriggerName.AUTO_REPAYMENT,
											ScheduleConstants.TriggerGroup.AUTO_REPAYMENT));
					if (trigger == null) {
						initAutoRepaymengJob();
					} else {
						scheduler.resumeTrigger(trigger.getKey());
					}
				}

				if (enableRepayAlert.equals("1")) {
					// 还款提醒
					CronTrigger trigger3 = (CronTrigger) scheduler
							.getTrigger(TriggerKey.triggerKey(
									ScheduleConstants.TriggerName.REPAY_ALERT,
									ScheduleConstants.TriggerGroup.REPAY_ALERT));
					if (trigger3 == null) {
						JobDetail jobDetail3 = JobBuilder
								.newJob(RepayAlert.class)
								.withIdentity(
										ScheduleConstants.TriggerName.REPAY_ALERT,
										ScheduleConstants.TriggerGroup.REPAY_ALERT)
								.build();

						trigger3 = TriggerBuilder
								.newTrigger()
								.withIdentity(
										ScheduleConstants.TriggerName.REPAY_ALERT,
										ScheduleConstants.TriggerGroup.REPAY_ALERT)
								.forJob(jobDetail3)
								.withSchedule(
								// 每天上午九点
										CronScheduleBuilder
												.cronSchedule("0 0 9 * * ? *"))
								.build();

						scheduler.scheduleJob(jobDetail3, trigger3);
					} else {
						scheduler.resumeTrigger(trigger3.getKey());
					}
				}
				
				if (log.isDebugEnabled()) {
					log.debug("start loan overdue check schdule job");
				}
				// 借款逾期调度
				CronTrigger trigger4 = (CronTrigger) scheduler
						.getTrigger(TriggerKey
								.triggerKey(
										ScheduleConstants.TriggerName.LOAN_OVERDUE_CHECK,
										ScheduleConstants.TriggerGroup.LOAN_OVERDUE_CHECK));
				if (trigger4 == null) {
					initLoanOverdueCheckJob();
				} else {
					scheduler.resumeTrigger(trigger4.getKey());
				}

				CronTrigger triggerInvestDelay = (CronTrigger) scheduler
						.getTrigger(TriggerKey
								.triggerKey(
										ScheduleConstants.TriggerName.INVEST_DELAY_CHECK,
										ScheduleConstants.TriggerGroup.INVEST_DELAY_CHECK));
				if (triggerInvestDelay == null) {
					initInvestDelayJob();
				} else {
					scheduler.resumeTrigger(triggerInvestDelay.getKey());
				}

//				CronTrigger triggerInvestSendSms = (CronTrigger) scheduler
//						.getTrigger(TriggerKey
//								.triggerKey(
//										ScheduleConstants.TriggerName.INVEST_SENDSMS_CHECK,
//										ScheduleConstants.TriggerGroup.INVEST_SENDSMS_CHECK));
//				if (triggerInvestSendSms == null) {
//					initInvestSendSmsJob();
//				} else {
//					scheduler.resumeTrigger(triggerInvestSendSms.getKey());
//				}
			} catch (SchedulerException e1) {
				throw new RuntimeException(e1);
			}

		}
	}
	/**
	*@Description: TODO(体验金定时任务) 
	* @author cuihang   
	*@date 2015-12-23 上午11:40:27 
	*@throws SchedulerException
	 */
	private void initTasteinvest()throws SchedulerException{
		//String  enabletasteinvest="0";
	/*	enabletasteinvest = configService
			.getConfigValue(ConfigConstants.Schedule.ENABLE_TASTE_INVEST);
		if (enabletasteinvest.equals("1")) {*/
			if (log.isDebugEnabled()) {
				log.debug("enable refresh trusteeship schdule job");
			}
			CronTrigger trigger2 = (CronTrigger) scheduler
					.getTrigger(TriggerKey
							.triggerKey(
									ScheduleConstants.TriggerName.TASTE_INVEST,
									ScheduleConstants.TriggerGroup.TASTE_INVEST));
			if (trigger2 == null) {
				initTasteinvestJob();
			} else {
				scheduler.resumeTrigger(trigger2.getKey());
			}
	}
	/**
	 * 
	*@Description: TODO(初始恢复对账定时任务) 
	* @author cuihang   
	*@date 2017-8-2 下午4:03:07 
	*@throws SchedulerException
	 */
	private void initHuiFuCheckAuto()throws SchedulerException{
		//String  enabletasteinvest="0";
		/*	enabletasteinvest = configService
			.getConfigValue(ConfigConstants.Schedule.ENABLE_TASTE_INVEST);
		if (enabletasteinvest.equals("1")) {*/
		if (log.isDebugEnabled()) {
			log.debug("enable refresh trusteeship schdule job");
		}
		CronTrigger trigger2 = (CronTrigger) scheduler
				.getTrigger(TriggerKey
						.triggerKey(
								ScheduleConstants.TriggerName.HUIFU_CHECKERROR,
								ScheduleConstants.TriggerGroup.HUIFU_CHECKERROR));
		if (trigger2 == null) {
			initHuiFuCheckAutoJob();
		} else {
			scheduler.resumeTrigger(trigger2.getKey());
		}
	}
	/**
	*@Description: TODO(体验金) 
	* @author cuihang   
	*@date 2015-12-23 上午11:42:06 
	*@throws SchedulerException
	 */
	private void initTasteinvestJob()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(TasteInvestAuto.class)
				.withIdentity(ScheduleConstants.JobName.TASTE_INVEST,
						ScheduleConstants.JobGroup.TASTE_INVEST).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.TASTE_INVEST,
						ScheduleConstants.TriggerGroup.TASTE_INVEST)
				.forJob(jobDetail).withSchedule(
				// 没五分钟执行一次
						CronScheduleBuilder.cronSchedule("0 0/30 * * * ?"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	
	}
	/**
	*@Description: TODO(汇付对账) 
	* @author cuihang   
	*@date 2017-8-2 下午4:03:36 
	*@throws SchedulerException
	 */
	private void initHuiFuCheckAutoJob()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(HuiFuCheckAuto.class)
				.withIdentity(ScheduleConstants.JobName.HUIFU_CHECKERROR,
						ScheduleConstants.JobGroup.HUIFU_CHECKERROR).build();
		
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.HUIFU_CHECKERROR,
						ScheduleConstants.TriggerGroup.HUIFU_CHECKERROR)
						.forJob(jobDetail).withSchedule(
								// 没天早上七点
								CronScheduleBuilder.cronSchedule("0 0 7 * * ? *"))
								.build();
		
		scheduler.scheduleJob(jobDetail, trigger);
		
	}
	
	
	/**
	 *@Description: TODO(更新红包状态) 
	 * @author cuihang   
	 *@date 2017-4-17 上午10:55:02 
	 *@throws SchedulerException
	 */
	private void updateCouponStatus()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(UpdateCouponStatusAuto.class)
				.withIdentity(ScheduleConstants.JobName.UPDATE_COUPON,
						ScheduleConstants.JobGroup.UPDATE_COUPON).build();
		
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.UPDATE_COUPON,
						ScheduleConstants.TriggerGroup.UPDATE_COUPON)
				.forJob(jobDetail).withSchedule(
						// 每天半夜执行一次
						CronScheduleBuilder.cronSchedule("0 0/30 * * * ?"))
				.build();
		
		scheduler.scheduleJob(jobDetail, trigger);
		
	}
	
	
	
	/**
	 *@Description: TODO(更新红包状态) 
	 * @author cuihang   
	 *@date 2017-4-17 上午10:55:02 
	 *@throws SchedulerException
	 */
	private void updateCouponStatusAuto()throws SchedulerException{
		if (log.isDebugEnabled()) {
			log.debug("enable refresh trusteeship schdule job");
		}
		CronTrigger trigger2 = (CronTrigger) scheduler
				.getTrigger(TriggerKey
						.triggerKey(
								ScheduleConstants.TriggerName.UPDATE_COUPON,
								ScheduleConstants.TriggerGroup.UPDATE_COUPON));
		if (trigger2 == null) {
			updateCouponStatus();
		} else {
			scheduler.resumeTrigger(trigger2.getKey());
		}
	}
	
	
	/**
	*@Description: TODO(更新加息券状态) 
	* @author cuihang   
	*@date 2017-4-17 上午10:55:02 
	*@throws SchedulerException
	 */
	private void updateRateTicketStatus()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(UpdateRateTicketStatusAuto.class)
				.withIdentity(ScheduleConstants.JobName.UPDATE_RATETICKET,
						ScheduleConstants.JobGroup.UPDATE_RATETICKET).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.UPDATE_RATETICKET,
						ScheduleConstants.TriggerGroup.UPDATE_RATETICKET)
				.forJob(jobDetail).withSchedule(
				// 每天半夜执行一次
						CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	
	}
	
	
	
	/**
	*@Description: TODO(更新加息券状态) 
	* @author cuihang   
	*@date 2017-4-17 上午10:55:02 
	*@throws SchedulerException
	 */
	private void updateRateTicketStatusAuto()throws SchedulerException{
			if (log.isDebugEnabled()) {
				log.debug("enable refresh trusteeship schdule job");
			}
			CronTrigger trigger2 = (CronTrigger) scheduler
					.getTrigger(TriggerKey
							.triggerKey(
									ScheduleConstants.TriggerName.UPDATE_RATETICKET,
									ScheduleConstants.TriggerGroup.UPDATE_RATETICKET));
			if (trigger2 == null) {
				updateRateTicketStatus();
			} else {
				scheduler.resumeTrigger(trigger2.getKey());
			}
	}
	
	
	/**
	*@Description: TODO(同步用户汇付卡) 
	* @author cuihang   
	*@date 2017-4-17 上午10:55:02 
	*@throws SchedulerException
	 */
	private void synBankCardAuto()throws SchedulerException{
			if (log.isDebugEnabled()) {
				log.debug("enable refresh trusteeship schdule job");
			}
			CronTrigger trigger2 = (CronTrigger) scheduler
					.getTrigger(TriggerKey
							.triggerKey(
									ScheduleConstants.TriggerName.SYN_BANKCARD,
									ScheduleConstants.TriggerGroup.SYN_BANKCARD));
			if (trigger2 == null) {
				synBankCardAutoJob();
			} else {
				scheduler.resumeTrigger(trigger2.getKey());
			}
	}
	/**
	*@Description: TODO(同步用户汇付卡) 
	* @author cuihang   
	*@date 2017-4-17 上午10:55:02 
	*@throws SchedulerException
	 */
	private void synBankCardAutoJob()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(SynBankCardAuto.class)
				.withIdentity(ScheduleConstants.JobName.SYN_BANKCARD,
						ScheduleConstants.JobGroup.SYN_BANKCARD).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.SYN_BANKCARD,
						ScheduleConstants.TriggerGroup.SYN_BANKCARD)
				.forJob(jobDetail).withSchedule(
				// 每天半夜执行一次
						CronScheduleBuilder.cronSchedule("0 0 3 * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	
	}
	/**
	 *@Description: TODO(自动投标定时任务) 
	 * @author cuihang   
	 *@date 2016-01-11 上午11:40:27 
	 *@throws SchedulerException
	 */
	private void initAutoLoan()throws SchedulerException{
		//String  enabletasteinvest="0";
		/*	enabletasteinvest = configService
			.getConfigValue(ConfigConstants.Schedule.ENABLE_TASTE_INVEST);
		if (enabletasteinvest.equals("1")) {*/
		if (log.isDebugEnabled()) {
			log.debug("enable refresh trusteeship schdule job");
		}
		CronTrigger trigger2 = (CronTrigger) scheduler
				.getTrigger(TriggerKey
						.triggerKey(
								ScheduleConstants.TriggerName.AUTO_LOAN,
								ScheduleConstants.TriggerGroup.AUTO_LOAN));
		if (trigger2 == null) {
			initAutoLoanJob();
		} else {
			scheduler.resumeTrigger(trigger2.getKey());
		}
	}
	/**
	 *@Description: TODO(自动投标定时任务) 
	 * @author cuihang   
	 *@date 2016-01-11 上午11:40:27 
	 *@throws SchedulerException
	 */
	private void initLoanCompleteLoanMoney()throws SchedulerException{
		//String  enabletasteinvest="0";
		/*	enabletasteinvest = configService
			.getConfigValue(ConfigConstants.Schedule.ENABLE_TASTE_INVEST);
		if (enabletasteinvest.equals("1")) {*/
		if (log.isDebugEnabled()) {
			log.debug("enable refresh trusteeship schdule job");
		}
		CronTrigger trigger2 = (CronTrigger) scheduler
				.getTrigger(TriggerKey
						.triggerKey(
								ScheduleConstants.TriggerName.LoanCompleteLoanMoney,
								ScheduleConstants.TriggerGroup.LoanCompleteLoanMoney));
		if (trigger2 == null) {
			initLoanCompleteLoanMoneyJob();
		} else {
			scheduler.resumeTrigger(trigger2.getKey());
		}
	}
	/**
	 *@Description: TODO(自动投标实现) 
	 * @author cuihang   
	 *@date 2016-01-11 上午11:42:06 
	 *@throws SchedulerException
	 */
	private void initAutoLoanJob()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(LoanAuto.class)
				.withIdentity(ScheduleConstants.JobName.AUTO_LOAN,
						ScheduleConstants.JobGroup.AUTO_LOAN).build();
		
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.AUTO_LOAN,
						ScheduleConstants.TriggerGroup.AUTO_LOAN)
						.forJob(jobDetail).withSchedule(
								// 没n分钟执行一次
								CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
								.build();
		
		scheduler.scheduleJob(jobDetail, trigger);
		
	}
	/**
	 *@Description: TODO(满标自动放款) 
	 * @author cuihang   
	 *@date 2016年9月6日14:59:03 
	 *@throws SchedulerException
	 */
	private void initLoanCompleteLoanMoneyJob()throws SchedulerException{
		JobDetail jobDetail = JobBuilder
				.newJob(LoanCompleteLoanMoney.class)
				.withIdentity(ScheduleConstants.JobName.LoanCompleteLoanMoney,
						ScheduleConstants.JobGroup.LoanCompleteLoanMoney).build();
		
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.LoanCompleteLoanMoney,
						ScheduleConstants.TriggerGroup.LoanCompleteLoanMoney)
						.forJob(jobDetail).withSchedule(
								// 每n分钟执行一次
								CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
								.build();
		
		scheduler.scheduleJob(jobDetail, trigger);
		
	}
	/**
	 * 检查项目逾期
	 * @throws SchedulerException
	 */
	private void initLoanOverdueCheckJob() throws SchedulerException {
		JobDetail jobDetail = JobBuilder
				.newJob(LoanOverdueCheck.class)
				.withIdentity(ScheduleConstants.JobName.LOAN_OVERDUE_CHECK,
						ScheduleConstants.JobGroup.LOAN_OVERDUE_CHECK).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.LOAN_OVERDUE_CHECK,
						ScheduleConstants.TriggerGroup.LOAN_OVERDUE_CHECK)
				.forJob(jobDetail).withSchedule(
				// 每天1点
						CronScheduleBuilder.cronSchedule("0 0 1 * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}

	/**
	 * 自动还款
	 * @throws SchedulerException
	 */
	private void initAutoRepaymengJob() throws SchedulerException {
		// 到期自动还款
		JobDetail jobDetail = JobBuilder
				.newJob(AutoRepayment.class)
				.withIdentity(ScheduleConstants.JobName.AUTO_REPAYMENT,
						ScheduleConstants.JobGroup.AUTO_REPAYMENT).build();
		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.AUTO_REPAYMENT,
						ScheduleConstants.TriggerGroup.AUTO_REPAYMENT)
				.forJob(jobDetail).withSchedule(
				CronScheduleBuilder.cronSchedule("0 1 0 * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}

	private void initRefreshTrusteeshipJob() throws SchedulerException {
		// 资金托管主动查询
		JobDetail jobDetail2 = JobBuilder
				.newJob(RefreshTrusteeshipOperation.class)
				.withIdentity(
						ScheduleConstants.JobName.REFRESH_TRUSTEESHIP_OPERATION,
						ScheduleConstants.JobGroup.REFRESH_TRUSTEESHIP_OPERATION)
				.build();

		CronTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						ScheduleConstants.TriggerName.REFRESH_TRUSTEESHIP_OPERATION,
						ScheduleConstants.TriggerGroup.REFRESH_TRUSTEESHIP_OPERATION)
				.forJob(jobDetail2).withSchedule(
				// 每十分钟执行一次
						CronScheduleBuilder.cronSchedule("0 0/2 * * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail2, trigger2);
	}

	/**
	 * 投资用户收到的还款时间推迟
	 * 学习：http://wwwcomy.iteye.com/blog/1743854
	 * @throws SchedulerException
	 */
	private void initInvestDelayJob() throws SchedulerException {
		JobDetail jobDetail = JobBuilder
				.newJob(InvestDelay.class)
				.withIdentity(ScheduleConstants.JobName.INVEST_DELAY_CHECK,
						ScheduleConstants.JobGroup.INVEST_DELAY_CHECK).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.INVEST_DELAY_CHECK,
						ScheduleConstants.TriggerGroup.INVEST_DELAY_CHECK)
				.forJob(jobDetail).withSchedule(
						CronScheduleBuilder.cronSchedule("0 0 1 * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}

	/**
	 * 投资用户延迟短信
	 * 学习：http://wwwcomy.iteye.com/blog/1743854
	 * @throws SchedulerException
	 */
	private void initInvestSendSmsJob() throws SchedulerException {
		JobDetail jobDetail = JobBuilder
				.newJob(InvestSendSms.class)
				.withIdentity(ScheduleConstants.JobName.INVEST_SENDSMS_CHECK,
						ScheduleConstants.JobGroup.INVEST_SENDSMS_CHECK).build();

		CronTrigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(ScheduleConstants.TriggerName.INVEST_SENDSMS_CHECK,
						ScheduleConstants.TriggerGroup.INVEST_SENDSMS_CHECK)
				.forJob(jobDetail).withSchedule(
						CronScheduleBuilder.cronSchedule("0 0 7 * * ? *"))
				.build();

		scheduler.scheduleJob(jobDetail, trigger);
	}
}
