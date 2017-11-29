package com.zw.huifu.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;

/**
 * 用户服务接口
 * 
 * @author majie
 * @date 2016年8月13日 下午4:02:47
 */
public interface HuiFuUserService {
	/**
	 * 登录
	 * 
	 * @param usrCustId
	 * @return
	 */
	public String uerLogin(String usrCustId);
	/**
	 * 修改汇付信息
	 * 
	 * @param usrCustId
	 * @return
	 */
	public String  acctModify(String usrCustId);

	/**
	 * @author 获得汇付注册html
	 * @param user
	 * @return
	 */
	public String userRegister(User user);

	/**
	 * UsrCustId绑定银行卡
	 * 
	 * @return
	 */
	public String bindCard(String UsrCustId);

	/**
	 * usrCustId CardId删除银行卡
	 * 
	 * @return
	 */
	public JSONObject delCard(String usrCustId, String CardId);

	/**
	 * 列出usrCustId银行卡
	 * 
	 * @param usrCustId
	 * @return
	 */
	public JSONArray QueryCardInfo(String usrCustId);
	/**
	 * 根据身份证号查询汇付用户信息
	 * @param CertId
	 * @return
	 */
	public JSONObject QueryUsrInfo(String certId);
	
}
