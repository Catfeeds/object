package com.zw.p2p.safeloan.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.safeloan.model.MoneyDetailRecord;
import com.zw.p2p.safeloan.service.MoneyDetailRecordService;
@Service("moneyDetailRecordService")
public class MoneyDetailRecordServiceImpl implements MoneyDetailRecordService {
	@Resource
	HibernateTemplate ht;
	private String createId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select mdr from MoneyDetailRecord mdr where mdr.id = (select max(mdrM.id) from MoneyDetailRecord mdrM where mdrM.id like ?)";
		List<MoneyDetailRecord> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			MoneyDetailRecord safeLoan = contractList.get(0);
			ht.lock(safeLoan, LockMode.UPGRADE);
			String temp = safeLoan.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%06d", itemp);
		return gid;
	}
	@Override
	public void addRecord(String loanid, String safeloanid, String safeloanrecordid, int type, double inmoney, double outmoney) {
		// TODO Auto-generated method stub
		MoneyDetailRecord mdr=new MoneyDetailRecord();
		mdr.setId(createId());
		mdr.setCommitTime(new Date());
		if(inmoney>0){
			mdr.setInMoney(inmoney);
		}
		if(outmoney>0){
			mdr.setOutMoney(outmoney);
		}
		if(null!=loanid){
			mdr.setLoanid(loanid);
			Loan loan=ht.get(Loan.class, loanid);
			mdr.setLoanName(loan.getName());
		}
		mdr.setSafeLoanId(safeloanid);
		mdr.setSafeLoanRecordId(safeloanrecordid);
		mdr.setType(type);
		ht.save(mdr);
	}

}
