package com.zw.p2p.loan.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.RegistCash;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.loan.model.VirtualLoanRecord;
import com.zw.p2p.loan.service.VirtualLoanRecordService;

@Service("virtualLoanRecordService")
public class VirtualLoanRecordServiceImpl implements VirtualLoanRecordService {
	@Resource
	private HibernateTemplate ht;
	@Resource
	private ConfigService configService;

	@Override
	public List<VirtualLoanRecord> getVLRListByVlId(String virtualloanid, Map<String, String> param) {
		// TODO Auto-generated method stub
		String sql = "select vlr from VirtualLoanRecord vlr where enableStatus=1 and vlr.virtualloanid='"+virtualloanid+"' "+getWhereStrByParam(param);
		return ht.find(sql);
	}
	private String getWhereStrByParam(Map<String, String> param){
		StringBuffer stb=new StringBuffer("");
		if(null!=param){
			for (String key : param.keySet()) {
				String sing="=";
				String value=param.get(key);
				if(null!=value){
					if(key.equals("endTime")){
						stb.append(" and DATE(endTime)").append(sing).append("'").append(value).append("' ");
					}
				}
				
			}
		}
		
		return stb.toString();
	}
	@Override
	public void updateVlr(VirtualLoanRecord vlr) {
		// TODO Auto-generated method stub
		ht.update(vlr);
	}
	@Override
	public RegistCash getRegistCashByUser(String userid) {
		// TODO Auto-generated method stub
		String hql="from RegistCash where pk_user=?";
		List list=ht.find(hql, userid);
		if(null!=list&&list.size()>0){
			return (RegistCash) list.get(0);
		}
		return null;
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String saveIn(String userid) {
		RegistCash registCash=	getRegistCashByUser(userid);
		if((1==registCash.getIsused())&&(registCash.getEndtime().compareTo(new Date())>0)){
			VirtualLoanRecord virtualloanrecord = new VirtualLoanRecord();
			virtualloanrecord.setVlrid(OrderNoService.getOrderNo());
			int deadline = Integer.parseInt(configService.getConfigValue("registcash_outtime"));
			int endpayeimt = Integer.parseInt(configService.getConfigValue("registcash_endrepaytime"));
			Double rate = Double.parseDouble(configService.getConfigValue("regestcash_rate"));
			BigDecimal income = new BigDecimal(registCash.getCash()).multiply(new BigDecimal(rate)).multiply(new BigDecimal(endpayeimt)).divide(new BigDecimal(365), 2, BigDecimal.ROUND_HALF_UP);
			virtualloanrecord.setMoney(registCash.getCash());
			virtualloanrecord.setVlrTime(new Date());
			virtualloanrecord.setEndTime( new Date(new Date().getTime() + deadline * 24 * 60 * 60 * 1000));
			virtualloanrecord.setGetEndPayTime(new Date(new Date().getTime() + endpayeimt * 24 * 60 * 60 * 1000));
			virtualloanrecord.setRatebak(rate);
			virtualloanrecord.setIncome(income.doubleValue());
			virtualloanrecord.setEnableStatus(1);
			virtualloanrecord.setUserid(userid);
			virtualloanrecord.setVirtualloanid("tiyanbiaoid");
			ht.save(virtualloanrecord);
			registCash.setIsused(2);
			ht.update(registCash);
			return virtualloanrecord.getVlrid();
		}else{
			return null;
		}
		
		
	}
	@Override
	public VirtualLoanRecord getVirtualLoanRecordByUser(String userid) {
		// TODO Auto-generated method stub
		String hql="from VirtualLoanRecord where userid=?";
		List list=ht.find(hql, userid);
		if(null!=list&&list.size()>0){
			return (VirtualLoanRecord) list.get(0);
		}
		return null;
	}

}
