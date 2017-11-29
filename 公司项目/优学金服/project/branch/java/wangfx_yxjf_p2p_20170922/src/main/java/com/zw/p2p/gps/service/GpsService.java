package com.zw.p2p.gps.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * GPS接口
 * @author majie
 * @date 2016年9月20日 下午4:52:47
 */
public interface GpsService {

	/**
	 * 根据imei号码查询经度、纬度
	 * @author majie
	 * @return（经度,纬度）
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @date 2016年9月20日 下午4:52:02
	 */
	public String getRemoteCoordinate(String imei);
}
