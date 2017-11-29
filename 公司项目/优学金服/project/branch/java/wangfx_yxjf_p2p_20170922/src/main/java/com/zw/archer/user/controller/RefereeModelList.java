package com.zw.archer.user.controller;

import java.util.Arrays;
import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.archer.user.model.RefereeModel;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.invest.InvestConstants.InvestStatus;

/**
 * 
 * @author hch 推荐人模型控制器
 */
@Component
@Scope(ScopeType.VIEW)
public class RefereeModelList extends EntitySQLQuery<RefereeModel> {
	/*private static final String lazyModelCount = "select "
			+ "count(user.referrer) "
			+ "from Invest invest inner join invest.user user "
			+ "where user.referrer != null and " + "invest.status in ('"
			+ InvestStatus.BID_SUCCESS + "','" + InvestStatus.REPAYING + "','"
			+ InvestStatus.OVERDUE + "','" + InvestStatus.COMPLETE + "','"
			+ InvestStatus.BAD_DEBT + "')";

	// 查询数据如果为空会报错，需要修改
	private static final String lazyModel = "select "
			+ "new com.zw.archer.user.model.RefereeModel(user.referrer,sum(invest.money),min(invest.time),max(invest.time)) "
			+ "from Invest invest inner join invest.user user "
			+ "where user.referrer != null and " + "invest.status in ('"
			+ InvestStatus.BID_SUCCESS + "','" + InvestStatus.REPAYING + "','"
			+ InvestStatus.OVERDUE + "','" + InvestStatus.COMPLETE + "','"
			+ InvestStatus.BAD_DEBT + "')";*/
	
	private static final String lazyModelCount = "select "
			+ "count(user.referrer) "
			+ "from user user inner join User user1 ON user.referrer=user1.id "
			+ "left join invest invest ON user.id=invest.user_id "
			+ "where (invest.status in ("
					+ "'bid_success','repaying','overdue','complete','bad_debt')"
					+ " or invest.status is null) ";
	
	// 查询数据如果为空会报错，需要修改
	private static final String lazyModel = "select "
			+ "user1.realname referee,sum(invest.money) sumMoney ,min(invest.time) startTime,max(invest.time) endTime,user1.id refereePhone,user.realname referrals,user.id referralsPhone,invest.status "
			+ "from user user inner join User user1 ON user.referrer=user1.id "
			+ "left join invest invest ON user.id=invest.user_id "
			+ "where (invest.status in ("
					+ "'bid_success','repaying','overdue','complete','bad_debt')"
					+ " or invest.status is null) ";
		
	// 查询条件 start
	private String referee;// 推荐人
	private Date searchCommitMinTime, searchCommitMaxTime;

	// 查询条件 end

	public RefereeModelList() {
		setCountHql(lazyModelCount);
		setHql(lazyModel);
		final String[] RESTRICTIONS = {
				"invest.time >= #{refereeModelList.searchCommitMinTime}",
				"invest.time <= #{refereeModelList.searchCommitMaxTime}",
				"user.referrer=#{refereeModelList.referee}",
				"1=1 group by user.id" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	public String getReferee() {
		return referee;
	}

	public void setReferee(String referee) {
		this.referee = referee;
	}

	public Date getSearchCommitMinTime() {
		return searchCommitMinTime;
	}

	public void setSearchCommitMinTime(Date searchCommitMinTime) {
		this.searchCommitMinTime = searchCommitMinTime;
	}

	public Date getSearchCommitMaxTime() {
		return searchCommitMaxTime;
	}

	public void setSearchCommitMaxTime(Date searchCommitMaxTime) {
		this.searchCommitMaxTime = searchCommitMaxTime;
	}
}
