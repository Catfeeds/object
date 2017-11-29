package com.zw.p2p.area.controller;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.area.model.Province;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ScopeType.VIEW)
public class ProvinceHome extends EntityHome<Province> implements java.io.Serializable{
}
