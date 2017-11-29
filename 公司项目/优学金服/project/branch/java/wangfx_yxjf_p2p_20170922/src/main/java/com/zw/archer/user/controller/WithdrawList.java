package com.zw.archer.user.controller;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.primefaces.component.datatable.DataTable;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.notice.model.NoticePool;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.huifu.service.HuiFuTradeService;
import com.zw.p2p.loan.model.WithdrawCash;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.user.service.WithdrawCashService;

/**
 * 提现查询
 *
 */
@Component
@Scope(ScopeType.VIEW)
public class WithdrawList extends EntityQuery<WithdrawCash> implements java.io.Serializable{
	
	private static final long serialVersionUID = 9057256750216810237L;
	
	@Logger static Log log ;
	@Resource
	LoginUserInfo loginUserInfo;
	@Resource
	WithdrawCashService wcs;
	@Resource
	NoticePool noticePool;
	@Resource
	private HuiFuTradeService huiFuTradeService;
	
	private Date startTime ;
	private Date endTime ;
	private String userName;
	private String payname;
	private String realName;
	private String status;
	private boolean bSelectAll;
	@Resource
	private AutoMsgService autoMsgService;
	private List<WithdrawCash> selectedData;

	public WithdrawList(){
		final String[] RESTRICTIONS =
				{"id like #{withdrawList.example.id}",
//				"user.username like #{withdrawList.example.user.username}",
				"user.username like #{withdrawList.userName}",
				"user.realname like #{withdrawList.realName}",
				"time >= #{withdrawList.startTime}",
				"time <= #{withdrawList.endTime}",
				"pay.name = #{withdrawList.payname}",
				"status like #{withdrawList.status}"};
		selectedData=new ArrayList<WithdrawCash>();
//		this.setPageSize(2);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

	}

	public String getPayname() {
		return payname;
	}

	public void setPayname(String payname) {
		this.payname = payname;
	}

	public List<WithdrawCash> getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(List<WithdrawCash> selectedData) {
		this.selectedData = selectedData;
	}

	public boolean isbSelectAll() {
		return bSelectAll;
	}

	public void setbSelectAll(boolean bSelectAll) {
		this.bSelectAll = bSelectAll;
	}

	public String getStatus() {
		if (status==null)
			return "nothing";
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = "%"+realName+"%";
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		//模糊查询
		this.userName = "%"+userName+"%";
	}
	
	@Override
	protected void initExample() {
		super.initExample();
		getExample().setUser(new User());
	}
	
	public Object getSumMoney(){
		final String hql = parseHql("Select sum(money),sum(fee) from WithdrawCash");
		@SuppressWarnings("unchecked")
		List<Object> resultList = getHt().execute(new HibernateCallback<List<Object>>() {

			public List<Object> doInHibernate(Session session)
			
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
				// 从第0行开始
				query.setFirstResult(0);
				query.setMaxResults(5);
				for (int i = 0; i < getParameterValues().length; i++) {
					query.setParameter(i, getParameterValues()[i]);
				}
				return query.list();
			}

		});
		
		if(resultList != null && resultList.get(0) != null){
			return resultList.get(0);
		}
		return 0D;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void selectItems(DataTable dataTable) throws Exception{
		List<WithdrawCash> cashList= this.getLazyModelData();
		List<WithdrawCash> withdrawCashs= (List<WithdrawCash>)this.getLazyModel().getWrappedData();//实际当前页的数据
		//bSelectAll和实际的选中相反
		if (bSelectAll)
			selectedData.clear();
		else {
			for (WithdrawCash cash : cashList) {
				//datatable 中的页变了以后，currentPage不会跟着改变，所以 如果用 this.setPageSize(2) 和 this.getLazyModelData() 以后只能取到第一页的数据，只好用下面的方法了
				for (WithdrawCash cash1:withdrawCashs) {
					if (cash.getId().equals(cash1.getId())) {
						cash.setSelected(true);
						selectedData.add(cash);
						break;
					}
				}
			}
		}
	}

	/**
	 * 发送提现通知
	 * nodeId: 成功:withdraw_success,失败:withdraw_fail
	 */
	public void sendWithdrawMsg(WithdrawCash cash) {
		String nodeId="withdraw_success";
		User user = cash.getUser();
		Map<String,String> params = new HashMap<String, String>();
		params.put("username",user.getUsername());
		try {
			autoMsgService.sendMsg(user,nodeId,params);
		}catch (Exception e){
			log.error("提现信息发送失败");
		}
	}

	public void clearSelected(){
		this.selectedData.clear();
	}

	public void updateSelected(WithdrawCash cash){
		if (cash.getSelected()){
			selectedData.add(cash);
		}else
			selectedData.remove(cash);
	}

	public void recheckAll(){
		DecimalFormat df = new DecimalFormat("0.00");
		for (WithdrawCash cash:selectedData){
//			if (cash.getSelected()){
				String money = df.format(cash.getApplyMoney());
				cash.setRecheckUser((new User(loginUserInfo.getLoginUserId())));
				huiFuTradeService.tradeCashAudit(cash.getUser().getUsrCustId(), money,"S",cash.getHuiFuOrderId());
				//wcs.passWithdrawCashRecheck(cash);
				//noticePool.add(new Notice("提现："+cash.getId()+"复审通过"));
				//sendWithdrawMsg(cash);
//			}
		}
		FacesUtil.addInfoMessage("操作完毕");
	}
}
