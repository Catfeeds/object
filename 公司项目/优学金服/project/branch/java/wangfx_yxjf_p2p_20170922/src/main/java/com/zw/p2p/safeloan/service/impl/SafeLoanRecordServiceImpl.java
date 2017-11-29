package com.zw.p2p.safeloan.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.safeloan.model.SafeLoanRecord;
import com.zw.p2p.safeloan.service.SafeLoanRecordService;
@Service("safeLoanRecordService")
public class SafeLoanRecordServiceImpl implements SafeLoanRecordService {
	@Resource
	HibernateTemplate ht;
	
	@Override
	public int recordNumBySafeLoanId(String safeLoanId) {
		// TODO Auto-generated method stub
		String hql="from SafeLoanRecord where safeloanid.id='"+safeLoanId+"'";
		List list=ht.find(hql);
		if(null!=list)
			return list.size();
		return 0;
	}
	@Override
	public String createSafeLoanRecordId() {
		String sign="R";
		String gid = sign+DateUtil.DateToString(new Date(), DateStyle.YYYYMMDDHHmmss);
		String sql = "select slr from SafeLoanRecord slr where slr.salrid = (select max(slrm.salrid) from SafeLoanRecord slrm where slrm.salrid like ?)";
		Integer itemp = 0;
		List<SafeLoanRecord> temp = ht.find(sql, gid + "%");
		if (temp.size() == 1) {
			SafeLoanRecord safeLoanRe = temp.get(0);
			ht.lock(safeLoanRe, LockMode.UPGRADE);
			String tempid = safeLoanRe.getSalrid();
			tempid = tempid.substring(tempid.length() - 3);
			itemp = Integer.valueOf(tempid);
		}
		itemp++;
		gid += String.format("%03d", itemp);
		return gid;
	}
	@Override
	public double getrealIncome(double money, Date starttime, Date endTime, double rate) {
if(null==starttime){
	starttime=new Date();
}
		//String nowDate=DateUtil.DateToString(new Date(), "yyyy-MM-dd HH:mm:ss");
		int days=(int)((endTime.getTime() - starttime.getTime())/86400000);
		if(days<1){
			return 0;
		}
		return 	new BigDecimal(money).multiply(new BigDecimal(rate).divide(new BigDecimal(100))).setScale(5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(days).setScale(5, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(365), BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	
	
	}
	@Override
	public double getgetFrozenMoney(String userid) {
		// TODO Auto-generated method stub
		String cfree = "select sum(slul.loanMoney)FROM Loan l ,SafeLoan_User_Loan slul WHERE slul.userid='"+userid+"' and   l.status IN ('raising','recheck') and l.id=slul.loanid ";
		List<Object> dfree = ht.find(cfree);
		return  (null==dfree.get(0))?0d:(Double)dfree.get(0);
	}
	@Override
	public double getInvestMoney(String userid) {
		// TODO Auto-generated method stub
		String cfree = "select sum(slr.money)FROM SafeLoanRecord slr where slr.userid.id='"+userid+"'";
		List<Object> dfree = ht.find(cfree);
		return (null==dfree.get(0))?0d:(Double)dfree.get(0);
	}
	@Override
	public SafeLoanRecord getLastSafeLoanRecordByUser(String userid) {
		String hql = "from SafeLoanRecord safloanrecord where safloanrecord.userid.id='"+userid+"' order by safloanrecord.beforeInvestTime desc";
		List<SafeLoanRecord> Slist = ht.find(hql);
				if(null!= Slist&& Slist.size()> 0){
					SafeLoanRecord safeLoan = new SafeLoanRecord();
					safeLoan = Slist.get(0);
					return safeLoan;
					}
				return null;
		}
	}
