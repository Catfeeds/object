package com.zw.huifu.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.bean.model.HuiFuCheckError;
import com.zw.huifu.service.HuiFuCheckErrorService;
import com.zw.huifu.service.HuiFuMoneyService;

@Service("huiFuCheckErrorService")
public class HuiFuCheckErrorServiceImpl implements HuiFuCheckErrorService {
	@Resource
	UserBillService userBillService;
	@Resource
	HuiFuMoneyService huiFuMoneyService;

	@Resource
	HibernateTemplate ht;
	@Override
	public String checkUserBalance(User user) {
		// TODO Auto-generated method stub
	
		if(null!=user.getUsrCustId()){
			//第三方平台开会才可以对账
			Double balance=userBillService.getBalance(user.getId());
			Double frozenMoney=userBillService.getFrozenMoney(user.getId());
			JSONObject json=huiFuMoneyService.userBalance(user.getUsrCustId());
			Double AvlBal=json.getDouble("AvlBal");//可用余额账户可以支取的余额
			Double FrzBal=json.getDouble("FrzBal");////冻结余额
			if(balance.compareTo(AvlBal)!=0||frozenMoney.compareTo(FrzBal)!=0){
				//余额对不上或冻结对不上
				HuiFuCheckError huiFuCheckError=new HuiFuCheckError();
				huiFuCheckError.setBalance(balance);
				huiFuCheckError.setFrozenMoney(frozenMoney);
				huiFuCheckError.setHfbalance(AvlBal);
				huiFuCheckError.setHffrozenMoney(FrzBal);
				huiFuCheckError.setCreateTime(new Date());
				huiFuCheckError.setId(IdGenerator.randomUUID());
				huiFuCheckError.setUser(user);
			return	saveHuiFuCheckError(huiFuCheckError);
			}
		}
		return null;
	}
	public String saveHuiFuCheckError(HuiFuCheckError huiFuCheckError){
		return (String) ht.save(huiFuCheckError);
	}
	public void cleanHuiFuCheckError(){
		String hql=" from HuiFuCheckError";
		List<HuiFuCheckError> listError=ht.find(hql);
		ht.deleteAll(listError);
	}
	
}
