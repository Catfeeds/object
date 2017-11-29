package com.zw.p2p.schedule.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.archer.user.service.UserService;
import com.zw.core.annotations.Logger;
import com.zw.core.util.DateUtil;
import com.zw.huifu.service.HuiFuTransferService;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.VirtualLoanRecord;
import com.zw.p2p.loan.service.VirtualLoanRecordService;
import com.zw.p2p.risk.service.SystemBillService;

/**
 * 自动计算到期的体验标产生的利息放入到用户余额中
 * 
 * @author Administrator
 * 
 */
@Component
public class TasteInvestAuto implements Job {
	@Resource
	VirtualLoanRecordService virtualLoanRecordService;
	@Resource
	ConfigService configService;
	@Resource
	UserService userService;
	@Resource
	UserBillService userBillService;
	@Resource
	private SystemBillService sbs;
	@Resource
	private HuiFuTransferService huiFuTransferService;
	@Logger
	Log log;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String enabletasteinvest = "0";
		enabletasteinvest = configService.getConfigValue(ConfigConstants.Schedule.ENABLE_TASTE_INVEST);
		if (enabletasteinvest.equals("1")) {
				Map<String, String> param = new HashMap<String, String>();
				param.put("endTime", DateUtil.DateToString(new Date(), "yyyy-MM-dd"));
				List<VirtualLoanRecord> list = virtualLoanRecordService.getVLRListByVlId(ConfigConstants.VirtualLoan.INVEST, param);
				for (VirtualLoanRecord virtualLoanRecord : list) {
					String userid = virtualLoanRecord.getUserid();
					User user=null;
					try {
						user = userService.getUserById(userid);
					} catch (UserNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// 为这个人发放该记录利息
					
					try {
						huiFuTransferService.transferMoney(user.getUsrCustId(), virtualLoanRecord.getIncome());
						sbs.transferOut(virtualLoanRecord.getIncome(), OperatorInfo.TASTEINVETSIC,"用户"+userid+"体验标利息", null, null, null, null, null,null,null,null,user);
						userBillService.transferIntoBalance(userid, virtualLoanRecord.getIncome(), "体验标利息", "自动转入：体验标收益");
						virtualLoanRecord.setEnableStatus(2);
						virtualLoanRecordService.updateVlr(virtualLoanRecord);
					} catch (InsufficientBalance e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new JobExecutionException(e.getMessage());
					}
					
				}

		}

	}
}
