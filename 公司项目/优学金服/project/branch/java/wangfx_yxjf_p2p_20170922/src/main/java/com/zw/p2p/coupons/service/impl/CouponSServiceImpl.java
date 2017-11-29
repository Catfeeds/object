package com.zw.p2p.coupons.service.impl;

import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponRuleListService;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.invest.model.Invest;

import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CouponSServiceImpl implements CouponSService{

	@Resource
	private HibernateTemplate ht;
	@Resource
	private CouponRuleListService couponRuleListService;
	@Resource
	private ConfigService configService;

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveCouponToUser(CouponRule couponRule, User user) throws Exception {
		Coupons coupons = new Coupons();
		coupons.setId( UUID.randomUUID().toString().replaceAll("-", ""));
		coupons.setMoney(couponRule.getMoney());
		coupons.setMoneyCanUse(couponRule.getMoneyCanUse());
		coupons.setResource(couponRule.getResource());
		coupons.setEndTime(new Date(new Date().getTime() + 1000l * 60 * 60 * 24 * couponRule.getUseLine()));
		coupons.setUser(user);
		coupons.setStatus(CouponConstants.CouponStatus.ENABLE);
		coupons.setCreateTime(new Date());
		ht.save(coupons);
	}

	@Override
	public List<Coupons> listByType(String resource, User user) {
		return ht.find("from Coupons where resource='" + resource + "' and user.id='" + user.getId() + "'  ");
	}

	@Override
	public Coupons getCouponsById(String id) {
		return ht.get(Coupons.class, id);
	}

	@Override
	public void userUserCoupon(String couponsid) throws Exception {
		Coupons coupons = ht.get(Coupons.class, couponsid);
		ht.evict(coupons);
		coupons = ht.get(Coupons.class, couponsid, LockMode.UPGRADE);
		if (coupons.getStatus().equals(CouponConstants.CouponStatus.ENABLE)&&(new Date().compareTo(coupons.getEndTime())<0)) {
			coupons.setStatus(CouponConstants.CouponStatus.USERUSE);
			coupons.setUsedTime(new Date());
		} else {
			throw new Exception("该红包不可用,请选择其他红包");
		}
		ht.update(coupons);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void giveCouponToUser(Invest invest) throws Exception {
		User investUser=invest.getUser();
		String investCreateTime = configService.getConfigValue("investCreateTime");//每种红包的发放开始日期和结束日期可单独配置
		String investEndTime = configService.getConfigValue("investEndTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		if(now.compareTo(sdf.parse(investCreateTime))>0 && sdf.parse(investEndTime).compareTo(now)>0){
			//匹配投资规则 获得投资红包
			List<CouponRule> ruleListInvest=couponRuleListService.listByType(CouponConstants.RESOURCETYPE.invest.getName());
			for (CouponRule couponRule : ruleListInvest) {
				if(invest.getInvestMoney()>=couponRule.getGetCouponCondition()){
					giveCouponToUser(couponRule, investUser);
					break;
				}
			}
		}
		
		String inviteCreateTime = configService.getConfigValue("inviteCreateTime");//每种红包的发放开始日期和结束日期可单独配置
		String inviteEndTime = configService.getConfigValue("inviteEndTime");
		if(now.compareTo(sdf.parse(inviteCreateTime))>0 && sdf.parse(inviteEndTime).compareTo(now)>0){
			//匹配邀请规则 邀请人获得邀请红包
			List<CouponRule> ruleListYaoqing=couponRuleListService.listByType(CouponConstants.RESOURCETYPE.yaoqing.getName());
			for (CouponRule couponRule : ruleListYaoqing) {
				if(invest.getInvestMoney()>=couponRule.getGetCouponCondition()){
					String reuserid=investUser.getReferrer();
					try{
						User reuser=ht.get(User.class, reuserid);
						giveCouponToUser(couponRule, reuser);
					}catch (Exception e) {
						// TODO: handle exception
					}
					break;
				}
			}
		}
		
	}

	@Override
	public List<Coupons> listCanUseCouponsByUser(User user,double investMoney) {
		return ht.find("from Coupons where  user.id='" + user.getId() + "' and status='"+CouponConstants.CouponStatus.ENABLE+"' and endTime > now() and "+investMoney+">=moneyCanUse");
	}

	@Override
	public List<Coupons> listAllUserCoupons(User user) {
		return ht.find("from Coupons where  user.id='" + user.getId() + "' and status='"+CouponConstants.CouponStatus.ENABLE+"' and endTime > now()");
	}

	@Override
	public Long getTodayUsedCoupon(User user) {
		StringBuffer sb=new StringBuffer();
		String startDate= DateFormatUtils.format(new Date(), "yyyy-MM-dd 00:00:00");
		String endDate= DateFormatUtils.format(new Date(), "yyyy-MM-dd 23:59:59");
		sb.append("select count(*) from Coupons where user_id="+user.getId()+" AND used_time >='"+startDate+"' AND used_time <='"+endDate+"'");
		List list= ht.find(sb.toString());
		return (Long) list.get(0);
	}

	@Override
	public List<Coupons> getCouPonsByStatus() {
		// TODO Auto-generated method stub
		return ht.find("from Coupons where  status='"+CouponConstants.CouponStatus.USING+"'");
	}

	@Override
	public void updateCoupon(Coupons coupons) {
		ht.update(coupons);
	}
}
