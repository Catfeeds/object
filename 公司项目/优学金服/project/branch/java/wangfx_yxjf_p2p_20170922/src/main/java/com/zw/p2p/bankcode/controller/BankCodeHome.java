package com.zw.p2p.bankcode.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.PingYinUtil;
import com.zw.p2p.bankcode.model.BankCode;

@Component
@Scope(ScopeType.VIEW)
public class BankCodeHome extends EntityHome<BankCode> {
	@Resource
	private HibernateTemplate ht;
	@Override
	@Transactional(readOnly = false, rollbackFor=Exception.class)
	public String save() {
		//把名称的首字声母作为分类依据
		String firstPingYinCharacter= PingYinUtil.toPinYin(this.getInstance().getName());
		if (firstPingYinCharacter.length()>0)
			firstPingYinCharacter= firstPingYinCharacter.substring(0,1);
		else
			firstPingYinCharacter="";

		this.getInstance().setCategory(firstPingYinCharacter);
		return super.save();
	}

	public BankCodeHome() {
		setUpdateView(FacesUtil.redirect("/admin/bankCode/bankCodeList"));
	}
	/**
	*@Description: TODO(根据bankNo 获得通联银行代码) 
	* @author cuihang   
	*@date 2016-6-28 上午10:49:58 
	*@return
	 */
	Map<String,BankCode> mapbanl=null;
	public String getTlbankcode(String bankNo){
		if(null==mapbanl){
			mapbanl=new HashMap<String,BankCode>();
			String hql="from BankCode ";
			List<BankCode> list=ht.find(hql);
			for (BankCode bankCode : list) {
				mapbanl.put(bankCode.getPayCode(), bankCode);
			}
		}
		if(mapbanl.containsKey(bankNo)){
			return mapbanl.get(bankNo).getTlbankcode();
		}
		
		
		return "";
	}
}
