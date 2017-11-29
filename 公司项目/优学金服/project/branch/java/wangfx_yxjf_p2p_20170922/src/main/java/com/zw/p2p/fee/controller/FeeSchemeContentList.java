package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemeContent;
import com.zw.p2p.fee.model.FeeSchemePay;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemeContentList extends EntityQuery<FeeSchemeContent> implements java.io.Serializable{
    public FeeSchemeContentList() {
        final String[] RESTRICTIONS = { "feeSchemeContent.id like #{feeSchemeContentList.example.id}",
                "feeSchemeContent.direction like #{feeSchemeContentList.example.direction}",
                "feeSchemeContent.feeSchemePay.id like #{feeSchemeContentList.example.feeSchemePay.id}",
        };
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    }

    @Override
    protected void initExample() {
        FeeSchemeContent feeSchemeContent = new FeeSchemeContent();
        feeSchemeContent.setFeeSchemePay(new FeeSchemePay());
        setExample(feeSchemeContent);
    }

}
