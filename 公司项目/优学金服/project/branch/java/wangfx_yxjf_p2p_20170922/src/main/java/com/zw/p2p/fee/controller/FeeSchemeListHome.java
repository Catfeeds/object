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
public class FeeSchemeListHome extends EntityHome<FeeScheme> implements java.io.Serializable{
    @Resource
    private FeeSchemeService feeSchemeService;

    public String getFeeSchemeStatus(Boolean active){
        return active?"是":"否";
    }

    @Transactional(readOnly = false)
    @Override
    public String delete() {
        if (getInstance().getId().equals("1")){
            FacesUtil.addErrorMessage("默认方案不能删除!");
            return null;
        }

        if (getInstance().getActive()!=null && getInstance().getActive()){
            FacesUtil.addErrorMessage("当前方案不能删除!");
            return null;
        }

        return super.delete();
    }

    @Override
    public FeeScheme createInstance() {
        FeeScheme feeScheme = new FeeScheme();
        feeScheme.setSchemeCustomers(new ArrayList<FeeSchemeCustomer>());
        feeScheme.setSchemePays(new ArrayList<FeeSchemePay>());
        return feeScheme;
    }

    /**
     * 设置当前方案
     * @return
     */
    @Transactional(readOnly = false)
    public String setCurrentScheme() {
        FeeScheme currentFeeScheme= feeSchemeService.getCurrentFeeScheme();
        if(null != currentFeeScheme){
        	if (getInstance().getId().equals(currentFeeScheme.getId())){
                FacesUtil.addInfoMessage("当前方案设置成功!");
                return null;
            }
        	
        	currentFeeScheme.setActive(false);
            feeSchemeService.updateFeeScheme(currentFeeScheme);
        }

        getInstance().setActive(true);
        FacesUtil.addInfoMessage("当前方案设置成功!");

        return super.update();
    }

    public FeeSchemeListHome() {
        setUpdateView(FacesUtil
                .redirect("/admin/config/feeSchemeList"));

    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public String save() {
        String tmpStr= super.save();
        if (tmpStr==null){
            return null;
        }

        if (getInstance().getSchemeCustomers().size()<1) {
            FeeSchemeCustomer feeSchemeCustomer = new FeeSchemeCustomer();
            feeSchemeCustomer.setInvestLimit(0);
            feeSchemeCustomer.setWithinLimit(0);
            feeSchemeCustomer.setBeyondLimit(0);
            feeSchemeCustomer.setWithinLimitOpr("rate");
            feeSchemeCustomer.setBeyondLimitOpr("rate");
            feeSchemeCustomer.setRecharge(0);
            feeSchemeCustomer.setFeeScheme(this.getInstance());
            feeSchemeCustomer.setWithdrawWithinLimit(0);
            feeSchemeCustomer.setWithdrawBeyondLimit(0);
            getBaseService().saveOrUpdate(feeSchemeCustomer);
            //添加阶梯性费率详细
            FeeSchemeDetail schemeDetail=new FeeSchemeDetail();
            schemeDetail.setFee(0);
            schemeDetail.setFeeSchemeCustomer(feeSchemeCustomer);
            schemeDetail.setLowerLimit(0);
            schemeDetail.setUpperLimit(999999999);
            getBaseService().saveOrUpdate(schemeDetail);
        }

        return tmpStr;
    }
}
