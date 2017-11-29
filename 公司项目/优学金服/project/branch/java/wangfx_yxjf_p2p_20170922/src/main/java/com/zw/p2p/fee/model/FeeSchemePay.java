package com.zw.p2p.fee.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;

/**
 * Created by lijin on 15/3/3.
 * 费率方案支付公司表
 */
@Entity
@Table(name = "fee_scheme_pay")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class FeeSchemePay implements java.io.Serializable{
    private String id;
    //支付公司名称
    private String name;
    //描述/备注
    private String description;
    private FeeScheme feeScheme;
    //方案内容
    private List<FeeSchemeContent> schemeContents;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "feeSchemePay")
    @OrderBy(value="direction")
    public List<FeeSchemeContent> getSchemeContents() {
        return schemeContents;
    }

    public void setSchemeContents(List<FeeSchemeContent> schemeContents) {
        this.schemeContents = schemeContents;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    public FeeScheme getFeeScheme() {
        return feeScheme;
    }

    public void setFeeScheme(FeeScheme feeScheme) {
        this.feeScheme = feeScheme;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "name", length = 32)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description", length = 200)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
