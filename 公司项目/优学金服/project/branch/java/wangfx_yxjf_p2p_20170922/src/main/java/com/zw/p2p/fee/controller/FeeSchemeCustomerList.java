package com.zw.p2p.fee.controller;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemeCustomer;
import com.zw.p2p.fee.model.FeeSchemePay;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Created by lijin on 15/2/8.
 */
@Component
@Scope(ScopeType.VIEW)
public class FeeSchemeCustomerList extends EntityQuery<FeeSchemeCustomer> implements java.io.Serializable{
    public FeeSchemeCustomerList() {
        final String[] RESTRICTIONS = { "feeSchemeCustomer.id like #{feeSchemeCustomerList.example.id}",
                "feeSchemeCustomer.feeScheme.id like #{feeSchemeCustomerList.example.feeScheme.id}",
        };
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    }


    @Override
    protected void initExample() {
        FeeSchemeCustomer feeSchemeCustomer=new FeeSchemeCustomer();
        feeSchemeCustomer.setFeeScheme(new FeeScheme());
        setExample(feeSchemeCustomer);
    }

}
