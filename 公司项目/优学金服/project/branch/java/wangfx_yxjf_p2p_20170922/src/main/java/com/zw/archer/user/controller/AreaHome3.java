package com.zw.archer.user.controller;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class AreaHome3 extends AreaHome implements Serializable {
	private static final long serialVersionUID = -9028139079103290282L;
}
