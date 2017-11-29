package com.zw.p2p.repay.controller;

import javax.annotation.Resource;

import org.hibernate.LockMode;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.repay.exception.AdvancedRepayException;
import com.zw.p2p.repay.exception.NormalRepayException;
import com.zw.p2p.repay.exception.OverdueRepayException;
import com.zw.p2p.repay.model.LoanRepay;
import com.zw.p2p.repay.model.Repay;
import com.zw.p2p.repay.service.RepayService;

import java.util.List;

/**
 * 还款
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.VIEW)
public class RepayHome extends EntityHome<Repay> {

	@Resource
	RepayService repayService;

	@Resource
	UserBillBO userBillBO;

	@Resource
	HibernateTemplate ht;

	/**
	 * 正常还款
	 * 
	 * @return
	 */
	public void normalRepay(String repayId) {
		try {
			LoanRepay repay = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
			List<LoanRepay>  lrs = ht.find("from LoanRepay lr  where lr.loan.id = ? ",repay.getLoan().getId());
			for (LoanRepay lr : lrs){
				if ((repay.getPeriod() > lr.getPeriod() && lr.getStatus().equals("complete")) || repay.getPeriod() == 1){
					repayService.normalRepay(repayId);
					FacesUtil.addInfoMessage("还款成功！");
					break;
				}else {
					FacesUtil.addErrorMessage("请还清逾期或者坏账！");
					break;
				}
			}
		} catch (InsufficientBalance e) {
			// 余额不足
			FacesUtil.addErrorMessage("您的账户余额不足，无法完成还款，请充值。");
		} catch (NormalRepayException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	/**
	 * 提前还款
	 */
	public void advanceRepay(String loanId) {
		try {
			repayService.advanceRepay(loanId);
			FacesUtil.addInfoMessage("提前还款成功！");
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足！");
		} catch (AdvancedRepayException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

	/**
	 * 逾期还款
	 */
	public void overdueRepay(String repayId) {
		try {
			LoanRepay repay = ht.get(LoanRepay.class, repayId, LockMode.UPGRADE);
			List<LoanRepay>  lrs = ht.find("from LoanRepay lr  where lr.loan.id = ? ",repay.getLoan().getId());
			for (LoanRepay lr : lrs){
				if ((repay.getPeriod() > lr.getPeriod() && lr.getStatus().equals("complete")) || repay.getPeriod() == 1){
					repayService.overdueRepay(repayId);
					FacesUtil.addInfoMessage("逾期还款成功！");
					break;
				}else {
					FacesUtil.addErrorMessage("请还清逾期或者坏账！");
					break;
				}
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足！");
		} catch (OverdueRepayException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
	}

}
