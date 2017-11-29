package com.zw.p2p.fee.service.impl;

import com.zw.p2p.fee.model.FeeSchemePay;
import com.zw.p2p.fee.service.FeeSchemePayService;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

/**
 * Created by lijin on 15/3/2.
 */
@Service("feeSchemePayService")
public class FeeSchemePayServiceImpl implements FeeSchemePayService {
    @Resource
    private HibernateTemplate ht;

    @Override
    public FeeSchemePay getFeeSchemePayById(String id) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FeeSchemePay.class);
        criteria.add(Restrictions.eq("id", id));

        List<FeeSchemePay> contentList=ht.findByCriteria(criteria);

        if (contentList.size()>0)
            return contentList.get(0);
        else
            return null;
    }
}
