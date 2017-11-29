package com.zw.archer.message.controller;

import java.util.Arrays;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.message.model.InBox;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class InBoxList extends EntityQuery<InBox> {
	private String status;
	public InBoxList() {
		setHql("select ib from InBox ib");
		setCountHql("select count(ib) from InBox ib");
		final String[] RESTRICTIONS = {
			"ib.recevier.id = #{inBoxList.example.recevier.id}",
			"ib.status like #{inBoxList.example.status}"
		};
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	protected void initExample() {
		InBox inBox = new InBox();
		inBox.setRecevier(new User());
		setExample(inBox);
	}
}
