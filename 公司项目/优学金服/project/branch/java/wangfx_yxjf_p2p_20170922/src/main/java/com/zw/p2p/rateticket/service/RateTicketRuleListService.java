package com.zw.p2p.rateticket.service;

import java.util.List;

import com.zw.p2p.rateticket.model.RateTicketRule;


public interface RateTicketRuleListService {
	/**
	 * @Description: TODO(列出某规则的红包)
	 * @author cuihang
	 * @date 2017-3-21 上午11:35:12
	 * @param resource
	 * @return
	 */
	List<RateTicketRule> listByType(String resource);
}
