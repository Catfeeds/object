package com.zw.p2p.bankcard.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.user.model.User;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveStatus;
import com.zw.p2p.bankcard.model.BankCardUnbindApprove;
import com.zw.p2p.bankcard.service.BankCardUnbindApproveService;
@Service("bankCardUnbindApproveService")
public class BankCardUnbindApproveServiceImp implements BankCardUnbindApproveService {
	@Resource
	private HibernateTemplate ht;
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void creatApprove(BankCardUnbindApprove bcuba) {
		// TODO Auto-generated method stub
			ht.save(bcuba);
	}
	@Override
	public int countApproveByUser(String userid, int status) {
		// TODO Auto-generated method stub
		
		String sql="select bau from BankCardUnbindApprove  bau where bau.userid='"+userid+"' and bau.status="+status;
		List<BankCardUnbindApprove> list=ht.find(sql);
		return list.size();
	}
	@Override
	public String ApproveAgree(BankCardUnbindApprove bcua) {
		// TODO Auto-generated method stub
		if(bcua.getStatus()!=BankCardUnbindApproveStatus.CS.getIndex()){
			return "只能审核待审核的申请";
		}
		bcua.setStatus(BankCardUnbindApproveStatus.TG.getIndex());
		bcua.setApproveTime(new Date());
		ht.merge(bcua);
		return "";
	}
	@Override
	public String ApproveResuse(BankCardUnbindApprove bcua) {
		// TODO Auto-generated method stub
		if(bcua.getStatus()!=BankCardUnbindApproveStatus.CS.getIndex()){
			return "只能审核待审核的申请";
		}
		bcua.setApproveTime(new Date());
		bcua.setStatus(BankCardUnbindApproveStatus.BH.getIndex());
		ht.merge(bcua);
		return "";
	}
	
	
}
