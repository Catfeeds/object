package com.zw.p2p.coupons.controller;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.CouponConstants.RESOURCETYPE;
import com.zw.p2p.coupons.model.CouponRule;
import com.zw.p2p.coupons.service.CouponRuleListService;

@Component
@Scope(ScopeType.VIEW)
public class CouponRuleHome extends EntityHome<CouponRule> implements java.io.Serializable{
	@Resource
	CouponRuleListService couponRuleListService;
	/**
	 * 
	 */
	private static final long serialVersionUID = -743762611543652168L;

	public CouponRuleHome() {
		setUpdateView(FacesUtil.redirect("/admin/coupon/couponRuleList"));
	}
	
	@Override
	@Transactional(readOnly = false)
	public String save() {
		
		if(StringUtils.isEmpty(getInstance().getId())){
			//新增
			getInstance().setId(IdGenerator.randomUUID());
		}
		
		return super.save();
		
	}
	
	public RESOURCETYPE[] ruleType(){
		RESOURCETYPE[] array=RESOURCETYPE.values();
		RESOURCETYPE[] result=new RESOURCETYPE[array.length-1];
		int tempIndex=0;
	
		for (RESOURCETYPE resourcetype : array) {
			if(!resourcetype.getName().equals(RESOURCETYPE.admindo.getName())){
				result[tempIndex]=resourcetype;
				tempIndex++;
			}
			
		}
		
		
		return 	result;
	}
}
