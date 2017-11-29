package com.zw.p2p.fee.service;

import com.zw.p2p.fee.model.FeeSchemeContent;
import com.zw.p2p.fee.model.FeeSchemeDetail;

import java.util.List;

/**
 * Created by lijin on 15/3/2.
 */
public interface FeeSchemeContentService {
    /**
     * 根据ID获取
     */
    public abstract FeeSchemeContent getFeeSchemeContentById(String id);

    public abstract List<FeeSchemeDetail> getFeeSchemeDetailsByContentId(String id);
}
