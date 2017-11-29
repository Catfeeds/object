package com.zw.archer.invite.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntitySQLQuery;
import com.zw.archer.invite.model.invite;
import com.zw.archer.menu.MenuConstants;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.VIEW)
public class InviteList extends EntitySQLQuery<invite> {
	
	@Logger
	private static Log log;
	
	private static StringManager sm = StringManager
			.getManager(MenuConstants.Package);
	@Resource
	HibernateTemplate ht;
	
	@Resource
	private LoginUserInfo loginUserInfo;
	
	private static final String lazyModelCountHql = "select count(aaa.id) from ((select invitef.*,userf.realname tuijian,tope.photo,tope.beituijian from invite AS invitef left join user AS userf on userf.mobile_number = invitef.from_phone left join (select inviteto.*,userto.realname beituijian,userto.photo photo from invite AS inviteto left join user AS userto on userto.mobile_number = inviteto.to_phone) AS tope on invitef.id=tope.id ORDER BY tope.id)) aaa";
	
	private static final String lazyModelHql ="select * from(select invitef.*,userf.realname tuijian,tope.beituijian,tope.photo from invite AS invitef left join user AS userf on userf.mobile_number = invitef.from_phone left join (select inviteto.*,userto.realname beituijian,userto.photo photo from invite AS inviteto left join user AS userto on userto.mobile_number = inviteto.to_phone) AS tope on invitef.id=tope.id ORDER BY tope.id) aaa";
	
	private String tuijian;
	private String beituijian;
	private String fromuserid;
	private String touserid;
	
	public InviteList(){
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		setPageSize(1000);
		final String[] RESTRICTIONS = {
				" aaa.tuijian like #{inviteList.tuijian}",
				" aaa.beituijian like #{inviteList.beituijian}",
				" aaa.fromuserid = #{inviteList.fromuserid}",
				" aaa.touserid = #{inviteList.touserid}"
		};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	public String getTuijian() {
		return tuijian;
	}

	public void setTuijian(String tuijian) {
		if (!StringUtils.isEmpty(tuijian))
			this.tuijian = "%"+tuijian+"%";
		else
			this.tuijian=null;
	}

	public String getBeituijian() {
		return beituijian;
	}

	public void setBeituijian(String beituijian) {
		if (!StringUtils.isEmpty(beituijian))
			this.beituijian = "%"+beituijian+"%";
		else
			this.beituijian=null;
	}

	public String getFromuserid() {
		return fromuserid;
	}

	public void setFromuserid(String fromuserid) {
		this.fromuserid = fromuserid;
	}

	public String getTouserid() {
		return touserid;
	}

	public void setTouserid(String touserid) {
		this.touserid = touserid;
	}
	
}
