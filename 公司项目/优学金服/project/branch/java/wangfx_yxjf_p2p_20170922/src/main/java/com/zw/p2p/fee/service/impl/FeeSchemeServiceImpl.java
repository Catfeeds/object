package com.zw.p2p.fee.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.core.util.SpringBeanUtil;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemeContent;
import com.zw.p2p.fee.model.FeeSchemeCustomer;
import com.zw.p2p.fee.model.FeeSchemeDetail;
import com.zw.p2p.fee.model.FeeSchemePay;
import com.zw.p2p.fee.service.FeeSchemeService;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.safeloan.model.SafeLoanRecord;
import com.zw.p2p.safeloan.service.SafeLoanRecordService;
import com.zw.p2p.user.service.RechargeService;

/**
 * Created by lijin on 15/3/2.
 */
@Service("feeSchemeService")
public class FeeSchemeServiceImpl implements FeeSchemeService {
    @Resource
    private HibernateTemplate ht;
    @Resource
    FeeSchemeContentServiceImpl feeSchemeContentService;
    @Resource
	private ConfigService configService;
    @Resource
	private RechargeService rechargeService;
    @Resource
	private InvestService investService;
    @Resource
	private SafeLoanRecordService safeLoanRecordService;
    @Override
    public FeeScheme getFeeSchemeById(String id) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FeeScheme.class);
        criteria.add(Restrictions.eq("id", id));

        List<FeeScheme> feeSchemeList=ht.findByCriteria(criteria);

        if (feeSchemeList.size()>0)
            return feeSchemeList.get(0);
        else
            return null;
    }

    @Override
    public double getRechargeFee(FeeSchemePay schemePay, double amount) {
        double retFee=0;

        for (FeeSchemeContent content:schemePay.getSchemeContents()){
            FeeSchemeContent tmpContent= feeSchemeContentService.getFeeSchemeContentById(content.getId());
            if (tmpContent.getDirection().equals("充值")){
                for (FeeSchemeDetail detail:tmpContent.getSchemeDetails()){
                    if (amount> detail.getLowerLimit() && amount<= detail.getUpperLimit()){
                        if (content.getOperateMode().equals("rate")){
                            retFee= amount * detail.getFee() /100;
                        }else
                            retFee= detail.getFee();
                    }
                }
                break;
            }
        }

        return retFee;
    }

    @Override
    public double getCustomerWithdrawFee(double amount,String userid) {
    	double fee=0;
    	  FeeSchemeCustomer schemeCustomer= getCurrentFeeScheme().getSchemeCustomers().get(0);
		   if (amount<=schemeCustomer.getInvestLimit()){
			   fee= schemeCustomer.getWithdrawWithinLimit();
		   }else{
			   fee= schemeCustomer.getWithdrawBeyondLimit();
		   }
		boolean investor=false;
		boolean loaner=false;
		  User user;
		try {
			UserService userService = (UserService) SpringBeanUtil.getBeanByName("userService");
			user = userService.getUserById(userid);
			List<Role> listRole=user.getRoles();
			for (Role roles : listRole) {
				if(roles.getId().equals("INVESTOR")){
					investor=true;
				}
				if(roles.getId().equals("LOANER")){
					loaner=true;
				}
			}
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
        	if(!investor||loaner){
        		//不是投资者不适用下面扣费标准
        		return fee;
        	}
    	
    	Invest invest=investService.getLastInvestByUser(userid);
		//散投的最近一次投资时间
		Date InvestTime=null;
		if(null!=invest){
			 InvestTime = invest.getTime();	
		}
		//无忧宝最近一次投资时间
		SafeLoanRecord slr=safeLoanRecordService.getLastSafeLoanRecordByUser(userid);
		Date SafeLoanRecordTime=null;
		if(null!=slr){
			SafeLoanRecordTime = slr.getBeforeInvestTime();
		}
		//最近一次充值时间
		Recharge rec=rechargeService.getLastRechargeByUser(userid);
		double minFee=fee;
		if(null!=rec){
			Date RechargeTime = rec.getTime();
			if(compareTo(RechargeTime,SafeLoanRecordTime)&&compareTo(RechargeTime,InvestTime)){
				//未进行任何投资
				String rate=configService.getConfigValue("noinviterate");
				Double rates =  Double.valueOf(rate);             //Integer.parseInt(rate);
				 fee = new BigDecimal(amount).multiply(new BigDecimal(rates)).divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP).doubleValue() ;
				if(fee<minFee){
					fee=minFee;
				}
			}
		}
    	return fee;
    }
	/**
	 *比较 param1 param2 param1>param2 return true;(当前时间与投资时间)
	 * @param param1
	 * @param param2
	 * @return
	 */
	private boolean compareTo(Date param1,Date param2){
		if(null==param2){
			return true;
		}else{
			return(param1.compareTo(param2)>0);
		}
	}
    @Override
    public double getWithdrawFee(FeeSchemePay schemePay, double amount) {
        double retFee=0;

        for (FeeSchemeContent content:schemePay.getSchemeContents()){
            FeeSchemeContent tmpContent= feeSchemeContentService.getFeeSchemeContentById(content.getId());
            if (tmpContent.getDirection().equals("提现")){
                for (FeeSchemeDetail detail:tmpContent.getSchemeDetails()){
                    if (amount> detail.getLowerLimit() && amount<= detail.getUpperLimit()){
                        if (content.getOperateMode().equals("rate")){
                            retFee= amount * detail.getFee() /100;
                        }else {
//                            retFee = detail.getFee();
                            //根据单笔限额计算倍数(因为提现时会按单笔限额拆分)
                            retFee= detail.getFee() * Math.ceil(amount / tmpContent.getWithdrawLimit());
                        }
                    }
                }
                break;
            }
        }

        return retFee;
    }

    @Override
    public FeeScheme getCurrentFeeScheme() {
        DetachedCriteria criteria = DetachedCriteria.forClass(FeeScheme.class);
        criteria.add(Restrictions.eq("active", true));

        List<FeeScheme> feeSchemeList=ht.findByCriteria(criteria);

        if (feeSchemeList.size()>0)
            return feeSchemeList.get(0);
        else
            return null;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor=Exception.class)
    public void updateFeeScheme(FeeScheme scheme) {
        ht.saveOrUpdate(scheme);
    }
}
