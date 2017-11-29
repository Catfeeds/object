package com.zw.archer.items.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.items.SelectItemConstant;
import com.zw.archer.items.model.SelectItemGroup;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.core.util.PublicUtils;

@Component
@Scope(ScopeType.REQUEST)
public class SelectItemGroupHome extends EntityHome<SelectItemGroup> {

	@Resource
	HibernateTemplate ht;
	@Logger
	static Log log;
	public SelectItemGroupHome(){
		setUpdateView(FacesUtil.redirect(SelectItemConstant.View.SELECTITEMGROUP_LIST));
	}
	
    @Override
    @Transactional(readOnly = false)
    public String save() {
    	if(PublicUtils.isEmpty(getId())){
    		if(PublicUtils.isEmpty(getInstance().getId())){
    			getInstance().setId(IdGenerator.randomUUID());
    		}
    		if(isExistName()){
    			log.error("name has exist! please input again");
    			FacesUtil.addErrorMessage(SelectItemConstant.ErrorMsg.NAMEHASEXIST);
    			return null;
    		}
    	}
    	return super.save();
    }
    
    @SuppressWarnings("unchecked")
	public boolean isExistName(){
    	List<SelectItemGroup> itemgroup = ht.findByNamedQuery("SelectItemGroup.findSelectItemGroupByname",
				getInstance().getName());
		if (itemgroup != null && itemgroup.size() > 1) {
			return true;
		}else if(itemgroup != null && itemgroup.size() == 1){
			String group = itemgroup.get(0).getId();
			if(!group.equals(getInstance().getId())){
				return true;
			}
		}
		return false;
    }
}
