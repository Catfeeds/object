package com.zw.p2p.rateticket.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponRuleListService;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.rateticket.RateTicketConstants;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.model.RateTicketRule;
import com.zw.p2p.rateticket.service.RateTicketRuleListService;
import com.zw.p2p.rateticket.service.RateTicketService;

@Service
public class RateTicketServiceImpl implements RateTicketService{

	@Resource
	private HibernateTemplate ht;
	@Resource
	private RateTicketRuleListService rateTicketRuleListService;
	@Resource
	private ConfigService configService;
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveRateTicketToUser(RateTicketRule rateTicketRule, User user) throws Exception {
		RateTicket rateTicket = new RateTicket();
		rateTicket.setId( UUID.randomUUID().toString().replaceAll("-", ""));
		rateTicket.setRate(rateTicketRule.getRate());
		rateTicket.setMoneyCanUse(rateTicketRule.getMoneyCanUse());
		rateTicket.setResource(rateTicketRule.getResource());
		rateTicket.setEndTime(new Date(new Date().getTime() + 1000l * 60 * 60 * 24 * rateTicketRule.getUseLine()));
		rateTicket.setUser(user);
		rateTicket.setStatus(CouponConstants.CouponStatus.ENABLE);
		rateTicket.setCreateTime(new Date());
		ht.save(rateTicket);
	}

	@Override
	public List<RateTicket> listByType(String resource, User user) {
		return ht.find("from RateTicket where resource='" + resource + "' and user.id='" + user.getId() + "'  ");
	}

	@Override
	public RateTicket getRateTicketById(String id) {
		return ht.get(RateTicket.class, id);
	}

	@Override
	public void userUserRateTicket(String ticketId) throws Exception {
		RateTicket rateTicket = ht.get(RateTicket.class, ticketId);
		ht.evict(rateTicket);
		rateTicket = ht.get(RateTicket.class, ticketId, LockMode.UPGRADE);
		if (rateTicket.getStatus().equals(CouponConstants.CouponStatus.ENABLE)&&(new Date().compareTo(rateTicket.getEndTime())<0)) {
			rateTicket.setStatus(CouponConstants.CouponStatus.USERUSE);
			rateTicket.setUsedTime(new Date());
		} else {
			throw new Exception("该红包不可用,请选择其他红包");
		}
		ht.update(rateTicket);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveRateTicketToUser(Invest invest) throws Exception {
		User investUser=invest.getUser();
		String rateticketInvestCreateTime = configService.getConfigValue("rateticketInvestCreateTime");//每种加息券的发放开始日期和结束日期可单独配置
		String rateticketInvestEndTime = configService.getConfigValue("rateticketInvestEndTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		if(now.compareTo(sdf.parse(rateticketInvestCreateTime))>0 && sdf.parse(rateticketInvestEndTime).compareTo(now)>0){
			//匹配投资规则 获得投资红包
			List<RateTicketRule> ruleListInvest=rateTicketRuleListService.listByType(RateTicketConstants.RESOURCETYPE.invest.getName());
			for (RateTicketRule rateTicketRule : ruleListInvest) {
				if(invest.getInvestMoney()>=rateTicketRule.getGetRateTicketCondition()){
					giveRateTicketToUser(rateTicketRule, investUser);
					break;
				}
			}
		}
		
		String rateticketInviteCreateTime = configService.getConfigValue("rateticketInviteCreateTime");//每种加息券的发放开始日期和结束日期可单独配置
		String rateticketInviteEndTime = configService.getConfigValue("rateticketInviteEndTime");
		if(now.compareTo(sdf.parse(rateticketInviteCreateTime))>0 && sdf.parse(rateticketInviteEndTime).compareTo(now)>0){
			//匹配邀请规则 邀请人获得邀请红包
			List<RateTicketRule> ruleListYaoqing=rateTicketRuleListService.listByType(RateTicketConstants.RESOURCETYPE.yaoqing.getName());
			for (RateTicketRule rateTicketRule : ruleListYaoqing) {
				if(invest.getInvestMoney()>=rateTicketRule.getGetRateTicketCondition()){
					String reuserid=investUser.getReferrer();
					try{
						User reuser=ht.get(User.class, reuserid);
						giveRateTicketToUser(rateTicketRule, reuser);
					}catch (Exception e) {
					}
					break;
				}
			}
		}
		
	}

	@Override
	public List<RateTicket> listCanUseRateTicketByUser(User user,double investMoney) {
		return ht.find("from RateTicket where  user.id='" + user.getId() + "' and status='"+RateTicketConstants.RateTicketStatus.ENABLE+"' and endTime > now() and "+investMoney+">=moneyCanUse");
	}

	@Override
	public List<RateTicket> listAllUserRateTicket(User user) {
		return ht.find("from RateTicket where  user.id='" + user.getId() + "' and status='"+RateTicketConstants.RateTicketStatus.ENABLE+"' and endTime > now()");
	}

	@Override
	public Long getTodayUsedRateTicket(User user) {
		StringBuffer sb=new StringBuffer();
		String startDate= DateFormatUtils.format(new Date(), "yyyy-MM-dd 00:00:00");
		String endDate= DateFormatUtils.format(new Date(), "yyyy-MM-dd 23:59:59");
		sb.append("select count(*) from RateTicket where user_id="+user.getId()+" AND used_time >='"+startDate+"' AND used_time <='"+endDate+"'");
		List list= ht.find(sb.toString());
		return (Long) list.get(0);
	}

	@Override
	public List<RateTicket> getRateTicketByStatus() {
		return ht.find("from RateTicket where  status='"+CouponConstants.CouponStatus.USING+"'");
	}

	@Override
	public void updateRateTicket(RateTicket rateTicket) {
		ht.update(rateTicket);
	}
}
