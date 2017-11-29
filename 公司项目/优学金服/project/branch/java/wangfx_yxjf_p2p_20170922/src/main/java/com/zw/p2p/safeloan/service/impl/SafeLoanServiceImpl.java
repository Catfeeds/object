package com.zw.p2p.safeloan.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.model.SafeLoan;
import com.zw.p2p.safeloan.service.SafeLoanService;

@Service("safeLoanService")
public class SafeLoanServiceImpl implements SafeLoanService {
	@Resource
	HibernateTemplate ht;
	@Resource
	ConfigService configService;
	@Resource
	LoanCalculator loanCalculator;
	@Override
	public String createSafeLoanId() {
		String sign="L";
		String gid = sign+DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select sl from SafeLoan sl where sl.id = (select max(slM.id) from SafeLoan slM where slM.id like ?)";
		List<SafeLoan> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			SafeLoan safeLoan = contractList.get(0);
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
	public String deleteSafeLoan(String id) {
		String result = "";   // 0：表示成功
		String hql = "select sl from SafeLoan sl where sl.id = ?)";
		List<SafeLoan> contractList = ht.find(hql, id);
		if (1 <= contractList.size()) {
			SafeLoan safeLoan = contractList.get(0);
			if(SafeLoanConstants.SafeLoanStatus.CS.getIndex() == safeLoan.getStatus()){
				ht.lock(safeLoan, LockMode.UPGRADE);
				ht.delete(safeLoan);
				result = "0";
			} else {
				result = "只能删除待审核状态的产品";
			}
		} else {
			result = "该产品不存在";
		}
		return result;
	}
	
	@Override
	public String verifySafeLoan(SafeLoan sl) {
		String result = "";   // 0：表示成功
		if (null != sl) {
			if(SafeLoanConstants.SafeLoanStatus.CS.getIndex() == sl.getStatus()){
				// 【状态】改为投资中
				sl.setStatus(SafeLoanConstants.SafeLoanStatus.TZZ.getIndex());
				// 【投资期开始时间】改为当前时间
				sl.setApproveBeginTime(new Date());
				// 【投资期结束时间】改为当前时间往后顺延锁定期限
				sl.setApproveEndTime(new Date(sl.getApproveBeginTime().getTime()+sl.getDeadhours() * 60 * 60*1000));
				ht.merge(sl);
				result = "0";
			} else {
				result = "只能审核待审核状态的产品";
			}
		} else {
			result = "该产品不存在";
		}
		return result;
	}
	
	@Override
	public String disverifySafeLoan(SafeLoan sl) {
		String result = "";   // 0：表示成功
		if (null != sl) {
			if(SafeLoanConstants.SafeLoanStatus.CS.getIndex() == sl.getStatus()){
				sl.setStatus(SafeLoanConstants.SafeLoanStatus.BTG.getIndex());
				ht.merge(sl);
				result = "0";
			} else {
				result = "只能审核待审核状态的产品";
			}
		} else {
			result = "该产品不存在";
		}
		return result;
	}
	
	@Override
	public String recheckSafeLoan(SafeLoan sl) {
		String result = "";   // 0：表示成功
		if (null != sl) {
			if(SafeLoanConstants.SafeLoanStatus.FHZ.getIndex() == sl.getStatus()){
				// 【状态】改为投资中
				sl.setStatus(SafeLoanConstants.SafeLoanStatus.TZZ.getIndex());
				ht.merge(sl);
				result = "0";
			} else {
				result = "只能复核复核中状态的产品";
			}
		} else {
			result = "该产品不存在";
		}
		return result;
	}

	@Override
	public String getTUSER() {
		// TODO Auto-generated method stub
		return configService.getConfigValue("TUSER");
	}

	@Override
	public boolean isPassHalf(Double money) throws Exception{
		// TODO Auto-generated method stub
		String nowTimeStr=DateUtil.DateToString(new Date(), "yyyy-MM-dd hh:mm:ss");
		String getloanlist = "FROM Loan WHERE STATUS IN ('raising') AND expect_time >'"+nowTimeStr+"'";
		List<Loan> loanList = ht.find(getloanlist);
		
		double minSumLoan=0d;
		for (Loan loan : loanList) {
			minSumLoan=ArithUtil.add(minSumLoan,loanCalculator.calculateMoneyNeedRaised(loan.getId()));
		}
		String per=configService.getConfigValue(ConfigConstants.Schedule.NEWSAFELOANPER);
		double moenyline=ArithUtil.div(ArithUtil.mul(minSumLoan, Double.parseDouble(per)), 100d);
		if(money>moenyline){
			return false;
		}
		return true;
	}

	@Override
	public SafeLoan getFirstSafeLoan() {
		// TODO Auto-generated method stub
		String getsloanlist = "FROM SafeLoan WHERE  status in("+SafeLoanConstants.SafeLoanStatus.TZZ.getIndex()+","+SafeLoanConstants.SafeLoanStatus.YMB.getIndex()+") order by status asc, approveBeginTime desc";
		List<SafeLoan> sloanList = ht.find(getsloanlist);
		if(null!=sloanList&&sloanList.size()>0){
			return sloanList.get(0);
		}
		return null;
	}

	@Override
	public SafeLoan findSafeLoanById(String id) {
		// TODO Auto-generated method stub
		return ht.get(SafeLoan.class, id);
	}
}
