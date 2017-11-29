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
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.rateticket.service.RateTicketService;

/**
 * 自动同步用户银行卡
 * 
 * @author Administrator
 * 
 */
@Component
public class UpdateCouponStatusAuto implements Job {
	@Resource
	CouponSService couponService;
	@Logger
	Log log;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<Coupons> coupons=couponService.getCouPonsByStatus();
		for (Coupons coupons2 : coupons) {
			coupons2.setStatus(CouponConstants.CouponStatus.ENABLE);
			couponService.updateCoupon(coupons2);
		}
	}
}
