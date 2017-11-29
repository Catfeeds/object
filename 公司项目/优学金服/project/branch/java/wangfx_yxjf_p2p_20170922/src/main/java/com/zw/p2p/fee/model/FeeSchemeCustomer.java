package com.zw.p2p.fee.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by lijin on 15/3/3.
 * 费率方案支付公司表
 */
@Entity
@Table(name = "fee_scheme_customer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class FeeSchemeCustomer implements java.io.Serializable{
    private String id;
    //投资限额
    private double investLimit;
    //限额内收费
    private double withinLimit;
    //限额外收费
    private double beyondLimit;
    //限额内收费类型(fixed/rate)
    private String withinLimitOpr;
    //限额外收费类型(fixed/rate)
    private String beyondLimitOpr;
    //充值时收费(融客临时)
    private double recharge;
    //在阈值内时提现收费(融客临时)
    private double withdrawWithinLimit;
    //超过阈值时提现收费(融客临时)
    private double withdrawBeyondLimit;
    private FeeScheme feeScheme;
    //阶梯性费率详细
    private List<FeeSchemeDetail> schemeDetails;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "feeSchemeCustomer")
    @OrderBy(value="lowerLimit")
    public List<FeeSchemeDetail> getSchemeDetails() {
        return schemeDetails;
    }

    public void setSchemeDetails(List<FeeSchemeDetail> schemeDetails) {
        this.schemeDetails = schemeDetails;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    public FeeScheme getFeeScheme() {
        return feeScheme;
    }

    public void setFeeScheme(FeeScheme feeScheme) {
        this.feeScheme = feeScheme;
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

    @Column(name = "investLimit", precision = 22, scale = 0)
    public double getInvestLimit() {
        return investLimit;
    }

    public void setInvestLimit(double investLimit) {
        this.investLimit = investLimit;
    }

    @Column(name = "withinLimit", precision = 22, scale = 0)
    public double getWithinLimit() {
        return withinLimit;
    }

    public void setWithinLimit(double withinLimit) {
        this.withinLimit = withinLimit;
    }

    @Column(name = "beyondLimit", precision = 22, scale = 0)
    public double getBeyondLimit() {
        return beyondLimit;
    }

    public void setBeyondLimit(double beyondLimit) {
        this.beyondLimit = beyondLimit;
    }

    @Column(name = "withinLimitOpr", precision = 22, scale = 0)
    public String getWithinLimitOpr() {
        return withinLimitOpr;
    }

    public void setWithinLimitOpr(String withinLimitOpr) {
        this.withinLimitOpr = withinLimitOpr;
    }

    @Column(name = "beyondLimitOpr", precision = 22, scale = 0)
    public String getBeyondLimitOpr() {
        return beyondLimitOpr;
    }

    public void setBeyondLimitOpr(String beyondLimitOpr) {
        this.beyondLimitOpr = beyondLimitOpr;
    }

    @Column(name = "recharge", precision = 22, scale = 0)
    public double getRecharge() {
        return recharge;
    }

    public void setRecharge(double recharge) {
        this.recharge = recharge;
    }

    @Column(name = "withdrawWithinLimit", precision = 22, scale = 0)
    public double getWithdrawWithinLimit() {
        return withdrawWithinLimit;
    }

    public void setWithdrawWithinLimit(double withdrawWithinLimit) {
        this.withdrawWithinLimit = withdrawWithinLimit;
    }

    @Column(name = "withdrawBeyondLimit", precision = 22, scale = 0)
    public double getWithdrawBeyondLimit() {
        return withdrawBeyondLimit;
    }

    public void setWithdrawBeyondLimit(double withdrawBeyondLimit) {
        this.withdrawBeyondLimit = withdrawBeyondLimit;
    }
}
