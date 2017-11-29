package com.zw.archer.system.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.system.SystemConstants;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.SpringBeanUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.REQUEST)
public class CacheManager {
	
	static StringManager sm = StringManager.getManager(SystemConstants.Package);
	@Logger
	static Log log ;
	
	@Resource
	HibernateTemplate ht ;
	
	public void clearCache(){
		SessionFactory sf = ht.getSessionFactory();
		
		Map<String, ClassMetadata> roleMap = sf.getAllCollectionMetadata();
		for (String roleName : roleMap.keySet()) {
			sf.evictCollection(roleName);
		}

		Map<String, ClassMetadata> entityMap = sf.getAllClassMetadata();
		for (String entityName : entityMap.keySet()) {
			sf.evictEntity(entityName);
		}

		sf.evictQueries();

		final String message = sm.getString("log.clearCacheSuccess");
		if(log.isDebugEnabled()){
			log.debug(message);
		}
		FacesUtil.addInfoMessage(message);
	}
}
