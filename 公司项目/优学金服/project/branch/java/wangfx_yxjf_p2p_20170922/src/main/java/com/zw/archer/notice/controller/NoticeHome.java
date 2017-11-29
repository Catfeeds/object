package com.zw.archer.notice.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.notice.model.NoticePool;
import com.zw.core.annotations.ScopeType;

/**
 * 后台管理员通知
 * 
 * @author Administrator
 * 
 */
@Component
@Scope(ScopeType.VIEW)
public class NoticeHome implements java.io.Serializable{

	@Resource
	NoticePool noticePool;

	public NoticePool getNoticePool() {
		return noticePool;
	}
	
}
