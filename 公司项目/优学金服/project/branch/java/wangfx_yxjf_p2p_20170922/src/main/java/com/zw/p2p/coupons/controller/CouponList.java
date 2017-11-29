package com.zw.p2p.coupons.controller;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponListService;

@Component
@Scope(ScopeType.VIEW)
public class CouponList extends EntityQuery<Coupons> {
	@Resource
	CouponListService couponListService;
	
	private static final String lazyModelCountHql = "select count(distinct coupons) from Coupons coupons";
	private static final String lazyModelHql = "select distinct coupons from Coupons coupons";
	private String userid;
	public CouponList() {
		setCountHql(lazyModelCountHql);
		setHql(lazyModelHql);
		final String[] RESTRICTIONS = { "coupons.status = #{couponList.example.status}",
				"coupons.user.id = #{couponList.userid}",
				"coupons.resource = #{couponList.example.resource}"};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}

	@Override
	protected void initExample() {
		Coupons uc = new Coupons();
		uc.setUser(new User());
		setExample(uc);
	}

	/**
	 * @return 根据指定用户获取可用红包
	 */
	public Double getUnusedCoupons(String id) {
	return 	couponListService.getUnusedCoupons(id);
		
	}

	/**
	 * @return 根据指定用户获取已使用红包
	 */
	public Double getUsedCoupons(String id) {
		return 	couponListService.getUsedCoupons(id);
		
	}

}
