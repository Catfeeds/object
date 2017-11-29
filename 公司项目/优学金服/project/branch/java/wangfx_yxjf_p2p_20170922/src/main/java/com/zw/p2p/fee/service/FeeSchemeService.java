package com.zw.p2p.fee.service;

import com.zw.p2p.fee.model.FeeScheme;
import com.zw.p2p.fee.model.FeeSchemePay;

/**
 * Created by lijin on 15/3/2.
 */
public interface FeeSchemeService {
    /**
     * 获取当前方案
     */
    public abstract FeeScheme getCurrentFeeScheme();

    /**
     * 根据ID获取方案
     */
    public abstract FeeScheme getFeeSchemeById(String id);

    /**
     * 更新方案
     */
    public abstract void updateFeeScheme(FeeScheme scheme);

    /**
     * 计算提现扣款支出
     */
    public abstract double getWithdrawFee(FeeSchemePay schemePay,double amount);

    /**
     * 计算充值扣款支出
     */
    public abstract double getRechargeFee(FeeSchemePay schemePay,double amount);

    /**
     * 计算客户提现手续费
     */
    public abstract double getCustomerWithdrawFee(double amount,String userid);


}
