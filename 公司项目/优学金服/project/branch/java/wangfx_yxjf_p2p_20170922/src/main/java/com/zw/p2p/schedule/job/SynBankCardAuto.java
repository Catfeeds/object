package com.zw.p2p.schedule.job;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.core.annotations.Logger;
import com.zw.p2p.bankcard.service.BankCardService;

/**
 * 自动同步用户银行卡
 * 
 * @author Administrator
 * 
 */
@Component
public class SynBankCardAuto implements Job {
	@Resource
	UserService userService;
	@Resource
	 BankCardService  bankCardService;
	@Logger
	Log log;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<User> list = userService.synBankCardUser();
		for (User user : list) {
			bankCardService.synBankCadrByUser(user);
		}
	}
}
