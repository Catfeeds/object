package com.zw.p2p.fee.controller;

import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemeContent;
import com.zw.p2p.fee.model.FeeSchemeDetail;
import com.zw.p2p.fee.model.FeeSchemePay;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemePayListHome extends EntityHome<FeeSchemePay> implements java.io.Serializable{
    private String scheme_id;

    public FeeSchemePayListHome() {
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
    public FeeSchemePay createInstance() {
        FeeSchemePay feeSchemePay = new FeeSchemePay();
        feeSchemePay.setSchemeContents(new ArrayList<FeeSchemeContent>());
        FeeScheme feeScheme= getBaseService().get(FeeScheme.class, FacesUtil.getParameter("scheme_id"));
        feeSchemePay.setFeeScheme(feeScheme);
        return feeSchemePay;
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void deletePay(FeeSchemePay schemePay) {
        if (schemePay != null) {
            try{
                getBaseService().delete(schemePay);
            } catch(Exception e){
                e.printStackTrace();
                FacesUtil.addInfoMessage("无法删除。");
            }
        }
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public String save() {
        String tmpStr= super.save();
        if (tmpStr==null){
            return null;
        }

        if (getInstance().getSchemeContents().size()<2) {
            FeeSchemeContent feeSchemeContent = new FeeSchemeContent();
            feeSchemeContent.setFeeSchemePay(getInstance());
            feeSchemeContent.setDirection("提现");
            feeSchemeContent.setOperateMode("rate");
            feeSchemeContent.setWithdrawDailyLimit(999999999);
            feeSchemeContent.setWithdrawLimit(999999999);
            getBaseService().saveOrUpdate(feeSchemeContent);
            //添加阶梯性费率详细
            FeeSchemeDetail schemeDetail=new FeeSchemeDetail();
            schemeDetail.setFee(0);
            schemeDetail.setFeeSchemeContent(feeSchemeContent);
            schemeDetail.setLowerLimit(0);
            schemeDetail.setUpperLimit(999999999);
            getBaseService().saveOrUpdate(schemeDetail);

            feeSchemeContent = new FeeSchemeContent();
            feeSchemeContent.setFeeSchemePay(getInstance());
            feeSchemeContent.setDirection("充值");
            feeSchemeContent.setOperateMode("rate");
            feeSchemeContent.setWithdrawDailyLimit(999999999);
            feeSchemeContent.setWithdrawLimit(999999999);
            getBaseService().saveOrUpdate(feeSchemeContent);
            //添加阶梯性费率详细
            schemeDetail=new FeeSchemeDetail();
            schemeDetail.setFee(0);
            schemeDetail.setFeeSchemeContent(feeSchemeContent);
            schemeDetail.setLowerLimit(0);
            schemeDetail.setUpperLimit(999999999);
            getBaseService().saveOrUpdate(schemeDetail);
        }

        return tmpStr;
    }
}
