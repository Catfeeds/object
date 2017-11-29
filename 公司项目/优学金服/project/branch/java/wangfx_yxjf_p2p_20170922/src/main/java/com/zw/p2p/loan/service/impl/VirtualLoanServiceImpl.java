package com.zw.p2p.loan.service.impl;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.zw.p2p.loan.model.VirtualLoan;
import com.zw.p2p.loan.service.VirtualLoanService;

@Service("virtualLoanService")
public class VirtualLoanServiceImpl implements VirtualLoanService {
	@Resource
	private HibernateTemplate ht;

	@Override
	public VirtualLoan getVirtualLoanById(String vlid) throws Exception {
		VirtualLoan user = ht.get(VirtualLoan.class, vlid);
		if (user == null) {
			throw new Exception("VirtualLoan.id:" + vlid);
		}
		return user;
	}

}
