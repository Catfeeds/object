package com.zw.p2p.baidumap.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.baidumap.service.BaiDuMapService;

@Component
@Scope(ScopeType.VIEW)
public class BaiDuMapHome extends EntityHome {
	@Resource
	private BaiDuMapService baiDuMapService;
	public JSONObject getRealAddress() {
		JSONObject jso = new JSONObject();
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		String ipsStr=baiDuMapService.getIpAddress(request);
		if(null!=ipsStr){
			
			String iparay[]=ipsStr.split(",");
			if(null!=iparay){
				String realip=iparay[0];
				jso=baiDuMapService.getLocationByIp(realip, baiDuMapService.getQterm(request));
			}
		}
	
		return jso;
	}
	
}
