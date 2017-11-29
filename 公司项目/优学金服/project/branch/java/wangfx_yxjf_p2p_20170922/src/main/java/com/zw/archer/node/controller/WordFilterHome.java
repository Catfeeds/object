package com.zw.archer.node.controller;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.node.NodeConstants;
import com.zw.archer.node.model.WordFilter;
import com.zw.archer.node.service.WordFilterService;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.REQUEST)
public class WordFilterHome extends EntityHome<WordFilter> {
	@Logger
	static Log log;
	@Resource
	private WordFilterService wfs;
	private final static StringManager sm = StringManager
			.getManager(NodeConstants.Package);
	
	public WordFilterHome(){
		setUpdateView(FacesUtil.redirect("/admin/node/wordFilterList"));
		setDeleteView(FacesUtil.redirect("/admin/node/wordFilterList"));
	}
	
	@Transactional(readOnly=false)
	public String save() {
		wfs.initPatterns();
		return super.save();
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteWordFilter",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		return super.delete();
	}

}
