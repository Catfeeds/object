package com.zw.p2p.cashticket.controller;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.RegistCash;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.core.annotations.ScopeType;
import com.zw.core.bean.ZwJson;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.huifu.service.HuiFuTransferService;
import com.zw.p2p.cashticket.model.CashTicket;
import com.zw.p2p.cashticket.service.CashTicketService;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.risk.service.SystemBillService;

@Component
@Scope(ScopeType.VIEW)
public class FreeCashList extends EntityQuery<RegistCash>{
	
	@Resource
	private HibernateTemplate ht;
	@Resource
	private HuiFuTransferService huiFuTransferService;
	@Resource
	private CashTicketService cashTicketService;
	@Resource
	private SystemBillService systemBillService;
	@Resource
	private UserBillBO userBillBO;
	

	private static final String lazyModelCountHql = "select count(distinct registCash) from RegistCash registCash";
	private static final String lazyModelHql = "select distinct registCash from RegistCash registCash";
	private String userid;
	public FreeCashList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = {
				"registCash.isused = #{freeCashList.example.isused}",
				"registCash.pk_user = #{freeCashList.userid}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	@Transactional(readOnly = false , rollbackFor = Exception.class)
	public void useCashTicket(String id) {
		CashTicket cashTicket = ht.get(CashTicket.class, id);
		ZwJson json = huiFuTransferService.transferMoney(cashTicket.getUser().getUsrCustId(), cashTicket.getMoney());
		if(json.isSuccess()){
			try {
				cashTicketService.useCashTicket(cashTicket);
				userBillBO.transferIntoBalance(cashTicket.getUser().getId(), cashTicket.getMoney(),
						"cash_money","用户使用现金券"+cashTicket.getMoney()+"元",
						null);
				systemBillService.transferOut(cashTicket.getMoney(), "cash_money","用户使用现金券"+cashTicket.getMoney()+"元", null, null, null, null, null, null, null, null, cashTicket.getUser());
			} catch (InsufficientBalance e) {
				e.printStackTrace();
			}
			FacesUtil.addInfoMessage(json.getMsg());
			return;
		}else{
			FacesUtil.addErrorMessage(json.getMsg());
            return;
		}
	}
}
