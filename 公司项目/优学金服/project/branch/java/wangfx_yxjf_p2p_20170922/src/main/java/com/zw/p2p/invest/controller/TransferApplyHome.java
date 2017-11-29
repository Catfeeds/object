package com.zw.p2p.invest.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.invest.exception.ExceedInvestTransferMoney;
import com.zw.p2p.invest.exception.InvestTransferException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.repay.model.InvestRepay;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filename: InvestHome.java Description: Copyright: Copyright (c)2013 Company:
 * p2p
 *
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:26:10
 * <p/>
 * Modification History: Date Author Version Description
 * ------------------------------------------------------------------
 * 2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class TransferApplyHome extends EntityHome<TransferApply> implements Serializable {

	@Resource
	private TransferService transferService;

	@Resource
	private LoginUserInfo loginUserInfo;

	@Resource
	private ConfigService configService;

	@Resource
	private HibernateTemplate ht;

	@Resource
	private AutoMsgService autoMsgService;

	/**
	 * 申请债权转让
	 *
	 * @param investId      被转让的还款Id
	 * @param money         转让的本金
	 * @param transferMoney 债权转让价格
	 * @param transferRate  债权转让的利率
	 * @param deadline      债权转让到期时间
	 */
	public String applyInvestTransfer() {
		TransferApply ta = this.getInstance();

		try {
			transferService.canTransfer(ta.getInvest().getId());
			ta.setCorpus(ta.getInvest().getInvestMoney());
			ta.setFee(transferService.calculateFee(ta.getInvest().getRepayRoadmap().getUnPaidCorpus()));
			ta.setTransferOutPrice(ta.getInvest().getRepayRoadmap().getUnPaidCorpus() - ta.getPremium());
			transferService.applyInvestTransfer(ta);
			FacesUtil.addInfoMessage("债权转让申请成功！");
			return "pretty:user-transfer-transfering";
		} catch (ExceedInvestTransferMoney e) {
			FacesUtil.addErrorMessage(e.getMessage());
		} catch (InvestTransferException e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		return null;

	}


	/**
	 * 取消债权
	 *
	 * @param investTransferId 债权Id
	 */
	public void cancel(String investTransferId) {
		try {
			double percent = transferService.calculateInvestTransferCompletedRate(investTransferId);
			if (percent == 0) {
				transferService.cancel(investTransferId);
				FacesUtil.addInfoMessage("取消债权成功");
			} else {
				FacesUtil.addErrorMessage("已经有部分债权转出，不能够取消债权");
			}
		} catch (NoMatchingObjectsException e) {
			e.printStackTrace();
		}
	}

	public boolean canTransfer(String investId) {
		try {
			return transferService.canTransfer(investId);
		} catch (InvestTransferException e) {
			//TODO:此处可以log.debug返回false的原因，便于查找为嘛无法进行债权转让
			return false;
		}
	}

	/**
	 * 可购买本金
	 * @param investId
	 * @param ta
	 * @return
	 */
	public double getCanCorpus(String investId, TransferApply ta) {
		Invest invest = ht.get(Invest.class, investId);
		double money = invest.getMoney();
		double corpus = ta.getCorpus();
		double outPrice = ta.getTransferOutPrice();
		return money / corpus * outPrice;
	}

	/**
	 * 实际转让本金
	 *
	 * @param investId
	 * @return
	 */
	public double getTransferCorpus(String investId, double all) {
		double money = 0D;
		String hql = "from InvestRepay ir where ir.invest.id = ?";
		List<InvestRepay> irs = ht.find(hql, investId);
		for (InvestRepay ir : irs) {
			money += ir.getCorpus();
		}
		if (all < money) {
			return 0;
		} else {
			return all - money;
		}
	}

	/**
	 * 实际投资本金
	 *
	 * @param investId
	 * @return
	 */
	public double getInvestCorpus(String investId) {
		double money = 0D;
		String hql = "from InvestRepay ir where ir.invest.id = ?";
		List<InvestRepay> irs = ht.find(hql, investId);
		for (InvestRepay ir : irs) {
			money += ir.getCorpus();
		}
		return money;
	}

	public void initDialog(Invest invest) {
		getInstance().setPremium(0);
		getInstance().setInvest(invest);
	}
}
