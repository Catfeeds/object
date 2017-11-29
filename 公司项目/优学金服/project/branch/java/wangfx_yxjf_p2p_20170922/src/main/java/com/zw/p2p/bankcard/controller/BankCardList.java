package com.zw.p2p.bankcard.controller;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.bankcard.BankCardConstants.BankCardStatus;
import com.zw.p2p.bankcard.model.BankCard;

/**
 * 银行卡查询
 */
@Component
@Scope(ScopeType.VIEW)
public class BankCardList extends EntityQuery<BankCard> {
	private static final long serialVersionUID = -1350682013319140386L;
	
	public BankCardList() {
		final String[] RESTRICTIONS = { 
				"bankCard.user.id like #{bankCardList.example.user.id}",
				"bankCard.status like #{bankCardList.example.status}"
				};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	@Override
	protected void initExample() {
		BankCard bc = new BankCard();
		bc.setUser(new User());
		setExample(bc);
	}

	private List<BankCard> bankCardListbyLoginUser;
	
	@Resource
	private LoginUserInfo loginUserInfo;

	public List<BankCard> getBankCardListbyLoginUser() {
		if (bankCardListbyLoginUser == null) {
			User loginUser = getHt().get(User.class, loginUserInfo.getLoginUserId());
			if (loginUser == null) {
				FacesUtil.addErrorMessage("用户未登录");
				return null;
			}
			bankCardListbyLoginUser = getHt().find(
					"from BankCard where user.id = ? and status=?", new String[]{loginUser.getId(),BankCardStatus.BINDING});
		}
		return bankCardListbyLoginUser;
	}
	
}
