package com.zw.p2p.fee.service.impl;

import com.zw.p2p.fee.model.*;
import com.zw.p2p.fee.service.FeeSchemeContentService;
import com.zw.p2p.fee.service.FeeSchemeService;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

/**
 * Created by lijin on 15/3/2.
 */
@Service("feeSchemeContentService")
public class FeeSchemeContentServiceImpl implements FeeSchemeContentService {
    @Resource
    private HibernateTemplate ht;

    @Override
    public FeeSchemeContent getFeeSchemeContentById(String id) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FeeSchemeContent.class);
        criteria.add(Restrictions.eq("id", id));

        List<FeeSchemeContent> contentList=ht.findByCriteria(criteria);

        if (contentList.size()>0)
            return contentList.get(0);
        else
            return null;
    }

    @Override
    public List<FeeSchemeDetail> getFeeSchemeDetailsByContentId(String id) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FeeSchemeDetail.class);
        criteria.add(Restrictions.eq("scheme_content_id", id));

        return ht.findByCriteria(criteria);
    }
}
