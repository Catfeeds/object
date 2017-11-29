package com.zw.archer.user.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.message.MessageConstants;
import com.zw.archer.message.model.InBox;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.core.util.StringManager;

import org.apache.commons.logging.Log;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Scope(ScopeType.VIEW)
public class LoanerList extends EntityQuery<User> {

	private static final String COUNT_HQL = "select count(distinct user) from User user left join user.roles role";
	private static final String HQL = "select distinct user from User user left join user.roles role";

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Logger
	private static Log log;

	private List<User> hasLoanRoleUsers;

	private Date registerTimeStart;
	private Date registerTimeEnd;

	public LoanerList() {
		setCountHql(COUNT_HQL);
		setHql(HQL);
		final String[] RESTRICTIONS = { "user.id like #{loanerList.example.id}",
				"user.username like #{loanerList.example.username}",
				"user.mobileNumber like #{loanerList.example.mobileNumber}",
				"user.status = #{loanerList.example.status}",
//                "user.loanerStatus = '1'",
				"user.email like #{loanerList.example.email}",
				"user.referrer like #{loanerList.example.referrer}",
				"user.registerTime >= #{loanerList.registerTimeStart}",
				"user.registerTime <= #{loanerList.registerTimeEnd}",
//				"user in elements(role.users) and role.id = #{loanerList.example.roles[0].id}" };
                "user in elements(role.users) and role.id = 'LOANER'" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	private List<User> selectedUsers;

	private String title;
	private String message;

	@Override
	protected void initExample() {
		User user = new User();
		List<Role> roles = new ArrayList<Role>();
		roles.add(new Role());
		user.setRoles(roles);
		setExample(user);
	}

	@Override
	public User getLazyModelRowData(String rowKey) {
		List<User> users = (List<User>) getLazyModel().getWrappedData();
		for (User user : users) {
			if (user.getId().equals(rowKey)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public Object getLazyModelRowKey(User user) {
		return user.getId();
	}

	/**
	 * 给被选中的用户发站内信。
	 */
	@Transactional(readOnly = false)
	public void sendMessageToSelectedUsers() {
		if (getSelectedUsers().size() == 0) {
			FacesUtil.addErrorMessage("请选择用户！");
		} else {
			for (User user : getSelectedUsers()) {
				InBox inbox = new InBox();
				inbox.setId(IdGenerator.randomUUID());
				inbox.setRecevier(user);
				inbox.setSender(new User("admin"));
				inbox.setReceiveTime(new Date());
				inbox.setContent(message);
				inbox.setStatus(MessageConstants.InBoxConstants.NOREAD);
				inbox.setTitle(title);
				getHt().save(inbox);
			}
			RequestContext.getCurrentInstance().addCallbackParam("sendSuccess",
					true);
			FacesUtil.addInfoMessage("发送成功！");
			getSelectedUsers().clear();
			this.title = null;
			this.message = null;
		}
	}

	/**
	 * 获取新注册的用户，按照注册时间倒序排列
	 * 
	 * @param count
	 *            新注册用户个数
	 * @return
	 */
	public List<User> getNewUsers(int count) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.addOrder(Order.desc("registerTime"));
		getHt().setCacheQueries(true);
		return getHt().findByCriteria(criteria, 0, count);
	}

	/**
	 * 获取有借款权限的用户
	 */
	@SuppressWarnings({ "unchecked" })
	public List<User> allHasLoanRoleUser() {
		List<User> users = getHt().find("from User");
		List<User> hasLoanRoleUser = new ArrayList<User>();
		for (User user : users) {
			for (Role role : user.getRoles()) {
				if (role.getId().equals("LOANER")
						|| role.getId().equals("ADMINISTRATOR")) {
					hasLoanRoleUser.add(user);
				}
			}
		}
		return hasLoanRoleUser;
	}

	/**
	 * 获取网站有效注册人数
	 * 
	 * @return
	 */
	// FIXME :放到用户统计类中
	public long getValidUserNumber() {
		return (Long) getHt().find(
				"select count(user) from User user where user.status='1'").get(
				0);
	}

	public List<User> getHasLoanRoleUsers() {
		if (this.hasLoanRoleUsers == null) {
			this.hasLoanRoleUsers = allHasLoanRoleUser();
		}
		return this.hasLoanRoleUsers;
	}

	/**
	 * @author wanghm 根据用户名模糊查询符合条件的用户，最多返回50条记录
	 */
	@SuppressWarnings("unchecked")
	public List<User> queryUsersByUserName(String username) {
		if (log.isDebugEnabled())
			log.debug("模糊查询用户名，传入的值为：" + username);
		User example = new User();
		example.setUsername("%" + username + "%");
		DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
		criteria.add(Restrictions.like("username", "%" + username + "%"));
		return getHt().findByCriteria(criteria, 0, 50);
	}

	public void setHasLoanRoleUsers(List<User> hasLoanRoleUsers) {
		this.hasLoanRoleUsers = hasLoanRoleUsers;
	}

	public List<User> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<User> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getRegisterTimeStart() {
		return registerTimeStart;
	}

	public void setRegisterTimeStart(Date registerTimeStart) {
		this.registerTimeStart = registerTimeStart;
	}

	public Date getRegisterTimeEnd() {
		return registerTimeEnd;
	}

	public void setRegisterTimeEnd(Date registerTimeEnd) {
		this.registerTimeEnd = registerTimeEnd;
	}

}
