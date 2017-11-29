package com.zw.archer.invite;

import com.zw.archer.invite.model.invite;

/**
 * 邀请关系业务接口
 * @author majie
 * @date 2016-1-14 15:07:24
 */
public interface InviteService {
	
	/**
	 * 变更手机号
	 * @param oldMobile 旧手机号
	 * @param newMobile 新手机号
	 */
	public void updateMobile(String oldMobile,String newMobile);
	
	public void save(invite in);
}
