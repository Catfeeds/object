package com.zw.archer.banner.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.banner.service.BannerService;
import com.zw.archer.node.model.NodeBodyHistory;
import com.zw.archer.product.model.Product;
import com.zw.archer.product.model.ProductPicture;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;

@Service
@Transactional
public class BannerServiceImpl implements BannerService {

	@Resource
	HibernateTemplate ht;
	@Logger
	static Log log;

	public void deleteBannerPicture(BannerPicture bannerPicture) {
		File file = new File(FacesUtil.getAppRealPath()
				+ bannerPicture.getPicture());
		file.delete();
		BannerPicture pp = ht.get(BannerPicture.class, bannerPicture.getId());
		if (pp != null) {
			ht.delete(pp);
		}
	}
}
