package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.fee.model.*;
import com.zw.p2p.fee.service.FeeSchemeService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemeCustomerListHome extends EntityHome<FeeSchemeCustomer> implements java.io.Serializable{
    private String scheme_id;
    @Resource
    FeeSchemeCustomerList feeSchemeCustomerList;
    @Resource
    FeeSchemeService feeSchemeService;

    public FeeSchemeCustomerListHome() {
//        setUpdateView(FacesUtil
//                .redirect("/admin/config/feeSchemePayList").concat("scheme_id=").concat(FacesUtil.getParameter("scheme_id")));
        //在这里设置的话，delete的时候会因为没有scheme_id而报错
//        setDeleteView(FacesUtil
//                .redirect("/admin/config/feeSchemePayList").concat("scheme_id=").concat(FacesUtil.getParameter("scheme_id")));
    }

    public String getScheme_id() {
        return FacesUtil.getParameter("scheme_id");
//        return scheme_id;
    }

    public void setScheme_id(String scheme_id) {
        this.scheme_id = scheme_id;
    }

    @Override
    public String getUpdateView() {
//        return FacesUtil
//                .redirect("/admin/config/feeSchemePayList").concat("scheme_id=").concat(FacesUtil.getParameter("scheme_id"));
        return FacesUtil
                .redirect("/admin/config/feeSchemePayList").concat("scheme_id=").concat(getInstance().getFeeScheme().getId());
    }

    @Override
    public String getDeleteView() {
        return FacesUtil
                .redirect("/admin/config/feeSchemePayList").concat("scheme_id=").concat(getInstance().getFeeScheme().getId());
    }

    @Override
    public FeeSchemeCustomer createInstance() {
        FeeSchemeCustomer feeSchemeCustomer = new FeeSchemeCustomer();
        feeSchemeCustomer.setSchemeDetails(new ArrayList<FeeSchemeDetail>());
        FeeScheme feeScheme= getBaseService().get(FeeScheme.class, FacesUtil.getParameter("scheme_id"));
        feeSchemeCustomer.setFeeScheme(feeScheme);
        return feeSchemeCustomer;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public String save() {
        return super.save();
    }

    public void initFromSchemeId(){
        scheme_id= getScheme_id();
        if (scheme_id!=null) {
            FeeScheme feeScheme = feeSchemeService.getFeeSchemeById(scheme_id);
            if (feeScheme.getSchemeCustomers().size()>0){
                setInstance(feeScheme.getSchemeCustomers().get(0));
            }
        }
        System.out.println();
    }
}
