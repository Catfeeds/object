package com.zw.p2p.bankcard.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.BankCardConstants.BankCardStatus;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.user.service.RechargeService;
import com.zw.p2p.user.service.WithdrawCashService;

@Component
@Scope(ScopeType.VIEW)
public class ChangeBankCardHome extends EntityHome<BankCard> implements java.io.Serializable{

	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;

	@Resource
	private LoginUserInfo loginUserInfo;
	
	@Resource
	private RechargeService rechargeService ;
	@Resource
	private UserBillService userBillService ;
	@Resource
	private BankCardService bankCardService ;
	@Resource
	private WithdrawCashService withdrawCashService ;
	private int showStatus;
	private double userbalance;
	private double countWithdraw;
	

	public double getCountWithdraw() {
		return countWithdraw;
	}

	public void setCountWithdraw(double countWithdraw) {
		this.countWithdraw = countWithdraw;
	}

	public double getUserbalance() {
		return userbalance;
	}

	public void setUserbalance(double userbalance) {
		this.userbalance = userbalance;
	}

	public int getShowStatus() {
		return showStatus;
	}

	public void setShowStatus(int showStatus) {
		this.showStatus = showStatus;
	}

	/*@Override
	@Transactional(readOnly = false)
	public String save() {
		User loginUser = getBaseService().get(User.class,
				loginUserInfo.getLoginUserId());
		if (loginUser == null) {
			FacesUtil.addErrorMessage("用户未登录");
			return null;
		}
		if (StringUtils.isEmpty(this.getInstance().getId())) {
			getInstance().setId(IdGenerator.randomUUID());
			getInstance().setUser(loginUser);
			getInstance().setStatus(BankCardConstants.BankCardStatus.BINDING);
			getInstance().setBank(rechargeService.getBankNameByNo(getInstance().getBankNo()));
		} else {
			this.setId(getInstance().getId());
		}

		if (getInstance().getName()==null || getInstance().getName().trim().equals(""))
			getInstance().setName("分行");

		if (getInstance().getBranch()==null || getInstance().getBranch().trim().equals(""))
			getInstance().setBranch("支行");

		getInstance().setTime(new Date());
		super.save(false);
		this.setInstance(null);
		FacesUtil.addInfoMessage("保存银行卡成功！");
		if(StringUtils.isNotEmpty(super.getSaveView())){
			return super.getSaveView();
		}
		return "pretty:bankCardList";
	}*/

	@Override
	@Transactional(readOnly = false)
	public String delete() {
		// 银行卡标记为删除状态
		this.getInstance().setStatus(BankCardStatus.DELETED);
		getBaseService().update(this.getInstance());
		return "pretty:bankCardList";
	}

	@Transactional(readOnly = false)
	public String delete(String bankCardId) {
		BankCard bc = getBaseService().get(BankCard.class, bankCardId);
		if (bc == null) {
			FacesUtil.addErrorMessage("未找到编号为" + bankCardId + "的银行卡！");
		} else {
			// 银行卡标记为删除状态
			this.setInstance(bc);
			this.getInstance().setStatus(BankCardStatus.DELETED);
			getBaseService().update(this.getInstance());
			this.setInstance(null);
		}
		return "pretty:bankCardList";
	}

	/**
	 * 删除银行卡，资金托管
	 * 
	 * @return
	 */
	public String deleteTrusteeship() {
		throw new RuntimeException("you must override this method!");
	}

	public void initProvince(){
		if (getInstance()==null)
			return;

		if (getInstance().getBankProvince()==null){
			getInstance().setBankProvince("北京市");
		}
	}
	
	public int showStatus(){
		String loginUserId=loginUserInfo.getLoginUserId();
		//获得余额
		double balance=userBillService.getBalance(loginUserId);
		userbalance=balance;
		countWithdraw=withdrawCashService.countWithdraw(loginUserId);
		return bankCardService.userShowBankCardStatus(loginUserId);
	}
	public void setShowStatus(){
		showStatus=showStatus();
	}
	public int getStatus(){
		setShowStatus();
		return showStatus;
	}
}
