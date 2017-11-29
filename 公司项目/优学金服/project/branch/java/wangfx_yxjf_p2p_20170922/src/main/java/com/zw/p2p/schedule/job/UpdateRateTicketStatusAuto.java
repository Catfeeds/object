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
import com.zw.p2p.rateticket.RateTicketConstants;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.service.RateTicketService;

/**
 * 自动同步用户银行卡
 * 
 * @author Administrator
 * 
 */
@Component
public class UpdateRateTicketStatusAuto implements Job {
	@Resource
	RateTicketService rateTicketService;
	@Logger
	Log log;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<RateTicket> rateTickets= rateTicketService.getRateTicketByStatus();
		for (RateTicket rateTicket : rateTickets) {
			rateTicket.setStatus(RateTicketConstants.RateTicketStatus.ENABLE);
			rateTicketService.updateRateTicket(rateTicket);
		}
	}
}
