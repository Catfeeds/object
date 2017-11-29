package com.zw.huifu.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zw.huifu.bean.model.HuiFuLog;
import com.zw.huifu.service.HuiFuLogService;

@Service("huiFuLogService")
public class HuiFuLogServiceImpl implements HuiFuLogService {
	
	@Resource
	private HibernateTemplate ht;

	@Override
	@Transactional(readOnly = false,propagation = Propagation.NOT_SUPPORTED)
	public void SaveHuiFuLog(HuiFuLog huiFuLog) {
		// TODO Auto-generated method stub
		ht.save(huiFuLog);
	}

	@Override
	public HuiFuLog findHuiFuLogByMerPriv(String MerPriv) {
		List<HuiFuLog> huiFuLog= ht.find("from HuiFuLog where MerPriv =?",MerPriv);
		return huiFuLog.get(0);
	}

	@Override
	public void updateHuiFuLog(HuiFuLog huiFuLog) {
		ht.update(huiFuLog);
	}

	@Override
	public HuiFuLog findHuiFuLogByOrderId(String orderId) {
		List<HuiFuLog> huiFuLog= ht.find("from HuiFuLog where orderId =?",orderId);
		return huiFuLog.get(0);
	}

}
