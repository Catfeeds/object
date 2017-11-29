package com.zw.p2p.fee.service;

import com.zw.p2p.fee.model.FeeSchemePay;

/**
 * Created by lijin on 15/3/2.
 */
public interface FeeSchemePayService {
    /**
     * 根据ID获取
     */
    public abstract FeeSchemePay getFeeSchemePayById(String id);
}
