package com.zw.p2p.bankcode.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.bankcode.model.BankCode;
import com.zw.p2p.bankcode.service.BankCodeService;
@Service("bankCodeService")
public class BankCodeServiceImpl implements BankCodeService {
	@Resource
	private HibernateTemplate ht;
	@Override
	public List<BankCode> findAllBankCode() {
		// TODO Auto-generated method stub
		
		return ht.find("from BankCode ");
	}

}
