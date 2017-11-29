package com.zw.archer.node.controller;

import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.node.NodeConstants;
import com.zw.archer.node.model.Node;
import com.zw.archer.node.model.NodeType;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;

@Component
@Scope(ScopeType.REQUEST)
public class NodeTypeHome extends EntityHome<NodeType> {

	@Logger
	static Log log;
	private static final StringManager sm = StringManager
			.getManager(NodeConstants.Package);

	public NodeTypeHome() {
		setUpdateView(FacesUtil.redirect(NodeConstants.View.NODE_TYPE_LIST));
	}

	@Override
	@Transactional(readOnly=false)
	public String delete() {
		if (log.isInfoEnabled()) {
			log.info(sm.getString("log.info.deleteNodeType",
					FacesUtil
					.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
		}
		Set<Node> nodeSets = getInstance().getNodes();
		if (nodeSets != null && nodeSets.size() > 0) {
			FacesUtil.addWarnMessage(sm.getString("canNotDeleteNodeType"));
			if (log.isInfoEnabled()) {
				log.info(sm.getString("log.info.deleteNodeTypeUnsuccessful",
						FacesUtil
						.getExpressionValue("#{loginUserInfo.loginUserId}"), new Date(), getId()));
			}
			return null;
		} else {
			return super.delete();
		}
	}

}
