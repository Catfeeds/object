package com.zw.archer.banner.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.banner.model.Banner;
import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.node.NodeConstants;
import com.zw.archer.node.model.Node;
import com.zw.archer.node.model.NodeType;
import com.zw.archer.product.ProductConstants;
import com.zw.archer.product.controller.ProductList;
import com.zw.archer.product.model.Product;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;

/**
 * banner查询
 * 
 */
@Component
@Scope(ScopeType.VIEW)
public class BannerList extends EntityQuery<Banner>  implements Serializable{
	private static final long serialVersionUID = -1350682013319140386L;

	public BannerList() {
		final String[] RESTRICTIONS = { "id like #{bannerList.example.id}",
				"description like #{bannerList.example.description}" };
		ArrayList<String> a = new ArrayList(Arrays.asList(RESTRICTIONS));
		setRestrictionExpressionStrings(a);
	}

	public Banner getBannerById(String bannerId) {
		return getHt().get(Banner.class, bannerId);
	}

	public String getFirstPicPath(String bannerId) {
		Banner banner = getBannerById(bannerId);
		if (banner != null) {
			List<BannerPicture> pics = banner.getPictures();
			if (pics != null && pics.size() > 0) {
				return pics.get(0).getPicture();
			}
		}
		return "";
	}
}
