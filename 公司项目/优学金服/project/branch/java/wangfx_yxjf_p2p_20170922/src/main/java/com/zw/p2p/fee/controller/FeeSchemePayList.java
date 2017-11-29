package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemePay;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemePayList extends EntityQuery<FeeSchemePay> implements java.io.Serializable{
    public FeeSchemePayList() {
        final String[] RESTRICTIONS = { "feeSchemePay.id like #{feeSchemePayList.example.id}",
                "feeSchemePay.name like #{feeSchemePayList.example.name}",
                "feeSchemePay.feeScheme.id like #{feeSchemePayList.example.feeScheme.id}",
        };
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    }


    @Override
    protected void initExample() {
        FeeSchemePay feeSchemePay = new FeeSchemePay();
        feeSchemePay.setFeeScheme(new FeeScheme());
        setExample(feeSchemePay);
    }

}
