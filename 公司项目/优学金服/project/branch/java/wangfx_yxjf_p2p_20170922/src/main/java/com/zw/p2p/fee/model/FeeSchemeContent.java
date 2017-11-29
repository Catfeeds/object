package com.zw.p2p.fee.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijin on 15/3/4.
 * 方案内容
 */
@Entity
@Table(name = "fee_scheme_content")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class FeeSchemeContent implements java.io.Serializable{
    private String id;
    //方向(充值/提现)
    private String direction;
    //收费类型(fixed/rate)
    private String operateMode;
    //单笔提现限额
    private double withdrawLimit;
    //每日提现限额
    private double withdrawDailyLimit;
    private FeeSchemePay feeSchemePay;
    //阶梯性费率详细
    private List<FeeSchemeDetail> schemeDetails;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "feeSchemeContent")
    @OrderBy(value="lowerLimit")
    public List<FeeSchemeDetail> getSchemeDetails() {
        return schemeDetails;
    }

    public void setSchemeDetails(List<FeeSchemeDetail> schemeDetails) {
        this.schemeDetails = schemeDetails;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_pay_id")
    public FeeSchemePay getFeeSchemePay() {
        return feeSchemePay;
    }

    public void setFeeSchemePay(FeeSchemePay feeSchemePay) {
        this.feeSchemePay = feeSchemePay;
    }

    @GenericGenerator(name = "generator", strategy = "uuid.hex")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "id", unique = true, nullable = false, length = 32)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "direction", length = 4)
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Column(name = "operateMode", length = 5)
    public String getOperateMode() {
        return operateMode;
    }

    public void setOperateMode(String operateMode) {
        this.operateMode = operateMode;
    }

    @Column(name = "withdrawLimit", precision = 22, scale = 0)
    public double getWithdrawLimit() {
        return withdrawLimit;
    }

    public void setWithdrawLimit(double withdrawLimit) {
        this.withdrawLimit = withdrawLimit;
    }

    @Column(name = "withdrawDailyLimit", precision = 22, scale = 0)
    public double getWithdrawDailyLimit() {
        return withdrawDailyLimit;
    }

    public void setWithdrawDailyLimit(double withdrawDailyLimit) {
        this.withdrawDailyLimit = withdrawDailyLimit;
    }
}
