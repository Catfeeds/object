	package com.zw.p2p.report.controller;

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.archer.invite.model.invite;
import com.zw.core.annotations.ScopeType;

/**
 * @Description: TODO(用户在投资金一览表)
 * @author ch
 * @date 2016-9-21 下午6:37:05
 */
@Component
@Scope(ScopeType.VIEW)
public class UserInvestList extends EntitySQLQuery<invite> {
	private static final String lazyModelCountHql = "SELECT count(i.id)FROM invest i LEFT JOIN USER u ON i.user_id =u.id  ";

	private static final String lazyModelHql = "SELECT SUM(i.invest_money) AS sunmoney,u.username,u.realname,u.mobile_number,u.referrer_id,i.invest_money,(SELECT COUNT(id) FROM invest WHERE  STATUS ='repaying' AND user_id=u.id) AS invnum FROM invest i LEFT JOIN USER u ON i.user_id =u.id where 1=1 ";

	private String username;
	private String referrerid;

	public UserInvestList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		setPageSize(1000);
		final String[] RESTRICTIONS = {
				" i.status = 'repaying'",
				" u.referrer_id = #{userInvestList.referrerid}",
				" u.username = #{userInvestList.username}",
				" 1=1 group by u.username"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getReferrerid() {
		return referrerid;
	}

	public void setReferrerid(String referrerid) {
		this.referrerid = referrerid;
	}

}
