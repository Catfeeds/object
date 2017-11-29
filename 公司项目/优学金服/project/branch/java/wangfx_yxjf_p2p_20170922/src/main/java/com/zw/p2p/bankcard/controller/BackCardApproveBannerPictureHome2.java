package com.zw.p2p.bankcard.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.banner.controller.BannerPictureHome;
import com.zw.archer.banner.model.BannerPicture;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.VIEW)
public class BackCardApproveBannerPictureHome2 extends BannerPictureHome{

	private List<BannerPicture> bannerPictures;

	public List<BannerPicture> getBannerPictures() {
		return bannerPictures;
	}

	public void setBannerPictures(List<BannerPicture> bannerPictures) {
		this.bannerPictures = bannerPictures;
	}
	@Override
	public void initBannerPictures(List<BannerPicture> value) {

		if (this.bannerPictures == null) {
			this.bannerPictures = new ArrayList<BannerPicture>();
		}
		this.bannerPictures = sortBySeqNum(bannerPictures);

	}

	private List<BannerPicture> sortBySeqNum(List<BannerPicture> pics) {
		Collections.sort(pics, new Comparator<BannerPicture>() {
			public int compare(BannerPicture o1, BannerPicture o2) {
				return o1.getSeqNum() - o2.getSeqNum();
			}
		});
		return pics;
	}
}
