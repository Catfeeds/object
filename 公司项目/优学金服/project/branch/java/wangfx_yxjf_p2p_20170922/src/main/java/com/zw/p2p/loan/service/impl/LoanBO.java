package com.zw.p2p.loan.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.loan.model.Loan;


/**  
 * Company:     p2p <br/> 
 * Copyright:   Copyright (c)2013 <br/>
 * Description:  
 * @author:     wangzhi  
 * @version:    1.0
 * Create at:   2014-1-21 下午3:59:31  
 *  
 * Modification History: <br/>
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014-1-21      wangzhi      1.0          
 */
@Service("loanBO")
public class LoanBO {
	
//	@Resource
//	HibernateTemplate ht;
	
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyMMddHHmmss");
	
	/**
	 * 生成16位主键
	 * @author majie
	 * @return
	 * @date 2016年8月29日 下午3:31:48
	 */
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String generateId() {
		String id = SDF.format(new Date());
		id +=RandomStringUtils.randomNumeric(4);
    	return id;
		/*String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select loan from Loan loan where loan.id = (select max(loanM.id) from Loan loanM where loanM.id like ?)";
		List<Loan> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			Loan loan = contractList.get(0);
			ht.lock(loan, LockMode.UPGRADE);
			String temp = loan.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%05d", itemp);
		return gid;*/
	}
	
}
