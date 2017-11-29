package com.zw.p2p.fee.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by lijin on 15/3/4.
 * 方案内容
 */
@Entity
@Table(name = "fee_scheme_detail")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class FeeSchemeDetail implements java.io.Serializable{
    private String id;
    //下限
    private double lowerLimit;
    //上限
    private double upperLimit;
    //收费标准
    private double fee;
    private FeeSchemeContent feeSchemeContent;
    private FeeSchemeCustomer feeSchemeCustomer;

    @Column(name = "fee", precision = 22, scale = 0)
    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;

    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_customer_id")
    public FeeSchemeCustomer getFeeSchemeCustomer() {
        return feeSchemeCustomer;
    }

    public void setFeeSchemeCustomer(FeeSchemeCustomer feeSchemeCustomer) {
        this.feeSchemeCustomer = feeSchemeCustomer;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_content_id")
    public FeeSchemeContent getFeeSchemeContent() {
        return feeSchemeContent;
    }

    public void setFeeSchemeContent(FeeSchemeContent feeSchemeContent) {
        this.feeSchemeContent = feeSchemeContent;
    }

    @Column(name = "lowerLimit", precision = 22, scale = 0)
    public double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    @Column(name = "upperLimit", precision = 22, scale = 0)
    public double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
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
}
