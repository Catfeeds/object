package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityHome;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemeContent;
import com.zw.p2p.fee.model.FeeSchemePay;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemeContentListHome extends EntityHome<FeeSchemeContent> implements java.io.Serializable{
    List<FeeSchemeContent> schemeContents;
    private String scheme_pay_id;
    private String scheme_id;

    public FeeSchemeContentListHome() {
//        setUpdateView(FacesUtil
//                .redirect("/admin/config/feeSchemeContentList")
//                    .concat("scheme_pay_id=").concat(FacesUtil.getParameter("scheme_pay_id"))
//                        .concat("&scheme_id=").concat(FacesUtil.getParameter("scheme_id"))
//        );
    }

    @Override
    public String getUpdateView() {
        return FacesUtil
                .redirect("/admin/config/feeSchemeContentList")
                    .concat("scheme_pay_id=").concat(getInstance().getFeeSchemePay().getId())
                        .concat("&scheme_id=").concat(getInstance().getFeeSchemePay().getFeeScheme().getId())
        ;
    }

    public List<FeeSchemeContent> getSchemeContents() {
        return schemeContents;
    }

    public void setSchemeContents(List<FeeSchemeContent> schemeContents) {
        this.schemeContents = schemeContents;
    }

    public String getScheme_id() {
//        return scheme_id;
        return FacesUtil.getParameter("scheme_id");
    }

    public void setScheme_id(String scheme_id) {
        this.scheme_id = scheme_id;
    }

    public String getScheme_pay_id() {
        return scheme_pay_id;
    }

    public void setScheme_pay_id(String scheme_pay_id) {
        this.scheme_pay_id = scheme_pay_id;
    }

    public FeeSchemeContent createInstance() {
        FeeSchemeContent feeSchemeContent = new FeeSchemeContent();
        FeeSchemePay feeSchemePay= getBaseService().get(FeeSchemePay.class, FacesUtil.getParameter("scheme_pay_id"));
        FeeScheme feeScheme= getBaseService().get(FeeScheme.class, FacesUtil.getParameter("scheme_id"));
        feeSchemePay.setFeeScheme(feeScheme);
        feeSchemeContent.setFeeSchemePay(feeSchemePay);
        return feeSchemeContent;
    }

    public void initSchemeContents(List<FeeSchemeContent> value) {
        if (this.schemeContents == null) {
            this.schemeContents = new ArrayList<FeeSchemeContent>();
        }
        if(value != null){
            this.schemeContents.addAll(value);
        }
        else{
            this.schemeContents = new ArrayList<FeeSchemeContent>();
        }
    }

    @Transactional(readOnly = false)
    public void saveSchemeContent(FeeSchemeContent schemeContent){
        if (schemeContent != null) {
            try{
                getBaseService().saveOrUpdate(schemeContent);
                FacesUtil.addInfoMessage("保存成功");
            } catch(Exception e){
                FacesUtil.addErrorMessage("保存失败！");
            }
        }
    }

    public String getOprString(FeeSchemeContent content){
        if (content==null)
            return "";

        if (content.getOperateMode().equals("rate"))
            return "%";
        else
            return "元";
    }

    public String getDirectionStr(FeeSchemeContent content){
        if (content==null)
            return "";

        if (content.getDirection().equals("充值"))
            return "充值 (客户充值时，系统需要支付给支付公司的费用）";
        else
            return "提现 (财务人员给客户打款时，系统需要支付给支付公司的费用）";
    }
}
