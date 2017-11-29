package com.zw.archer.user.service.impl;

import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.model.Role;
import com.zw.archer.user.model.User;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.coupons.model.Coupons;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LockMode;
import org.hibernate.ObjectNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.awt.geom.Arc2D;
import java.util.*;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-13 下午3:08:49
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-13 wangzhi 1.0
 */
@Service("userBO")
public class UserBO {
	@Resource
	ConfigService configService;
	@Resource
	HibernateTemplate ht;

	public void save(User user) {
		validateField(user);
		user.setId(user.getUsername());
		ht.save(user);
	}

	/**
	 * 检查某些域值输入格式和是否已存在，例如：username、email、mobile
	 * 
	 * @param user
	 */
	private void validateField(User user) {
		if (StringUtils.isEmpty(user.getUsername())) {
			throw new IllegalArgumentException(
					"user.username can not be empty!");
		}
		if (!user.getUsername().matches("^[0-9A-Za-z]{4,32}$")) {
			throw new IllegalArgumentException(
					"user.username cannot be a mobile number and must contains by number or word!");
		}
		User userExist = getUserByUsername(user.getUsername());
		ht.evict(userExist);
		if (userExist != null && !userExist.getId().equals(user.getId())) {
			throw new DuplicateKeyException("user.username '"
					+ user.getUsername() + "' already exists!");
		}
		if (StringUtils.isNotEmpty(user.getEmail())) {
			User emailUser = getUserByEmail(user.getEmail());
			ht.evict(emailUser);
			if (emailUser != null
					&& !emailUser.getId().equals(user.getId())) {
				throw new DuplicateKeyException("user.email '"
						+ user.getEmail() + "' already exists!");
			}
		}
		if (StringUtils.isNotEmpty(user.getMobileNumber())) {
			User mobileUser = getUserByMobileNumber(user.getMobileNumber());
			ht.evict(mobileUser);
			if (mobileUser != null
					&& !mobileUser.getId().equals(user.getId())) {
				throw new DuplicateKeyException("user.mobileNumber '"
						+ user.getMobileNumber() + "' already exists!");
			}
		}
	}

	public void update(User user) {
		validateField(user);
		ht.update(user);
	}
	public void updateForCashPwd(User user) {
//		validateField(user);
		ht.update(user);
	}

	public User getUserByUsername(String username) {
		List<User> users = ht.find("from User user where user.username=?",
				username);
		if (users.size() == 0) {
			return null;
		} else if (users.size() > 1) {
			throw new DuplicateKeyException("duplicate user.username:"
					+ username);
		}
		return users.get(0);
	}

	public User getUserByEmail(String email) {
		List<User> users = ht.find("from User user where user.email=?", email);
		if (users.size() == 0) {
			return null;
		} else if (users.size() > 1) {
			throw new DuplicateKeyException("duplicate user.email:" + email);
		}
		return users.get(0);
	}

	/**
	 * 对特定账户的用户发送红包
	 * @param user
	 */
	/*public void sendCoupons(User user){
		if(null!=user){

			Double couponMoney=0d;
			try {
				String couponMoneyStr = configService
						.getConfigValue(ConfigConstants.InvestTransfer.COUPON_MONEY);
				couponMoney= Double.valueOf(couponMoneyStr);
			} catch (ObjectNotFoundException onfe) {
				couponMoney = 50.0;
			}
			//红包参数金额不为0时添加红包
			if(0d!=couponMoney){
				Coupons coupons=new Coupons();
				coupons.setId(generateId());
				coupons.setGenerateTime(new Date());
				coupons.setExpired(DateUtil.addMonth(new Date(),1));
				coupons.setMoney(couponMoney);
				coupons.setResource("注册红包");
				coupons.setStatus("un_used");
				coupons.setUser(user);
				coupons.setUseRule("投资即可使用，回款后全额提现");
				ht.save(coupons);
			}
		}
	}
	/**
	 * 根据手机号，获得某个用户
	 * @param mobileNumber
	 * @return
	 */

	public User getUserByMobileNumber(String mobileNumber) {
		List<User> users = ht.find("from User user where user.mobileNumber=?",
				mobileNumber);
		if (users.size() == 0) {
			return null;
		} else if (users.size() > 1) {
			throw new DuplicateKeyException("duplicate user.mobileNumber:"
					+ mobileNumber);
		}
		return users.get(0);
	}

	public void addRole(User user, Role role) {
		for (Role roleT : user.getRoles()) {
			if (roleT.getId().equals(role.getId())) {
				return;
			}
		}
		user.getRoles().add(role);
		ht.update(user);
	}

	public void removeRole(User user, Role role) {
		List<Role> roles = user.getRoles();
		for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
			Role role2 = (Role) iterator.next();
			if (role2.getId().equals(role.getId())) {
				iterator.remove();
			}
		}
		ht.update(user);
	}

	public void enableUser(User user) {
		user.setStatus(UserConstants.UserStatus.ENABLE);
		ht.update(user);
	}
    public Role getRoleByRoleId(String roleId) {
        List<Role> roles = ht.find("from Role role where role.id=?",
                roleId);
        if (roles.size() == 0) {
            return null;
        } else if (roles.size() > 1) {
            throw new DuplicateKeyException("duplicate role.id:"
                    + roleId);
        }
        return roles.get(0);
    }

	/**
	 * 生成优惠券id
	 *
	 * @return
	 */
	private String generateId() {
		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
		String hql = "select coupons from Coupons coupons where coupons.id = (select max(couponsM.id) from Coupons couponsM where couponsM.id like ?)";
		List<Coupons> contractList = ht.find(hql, gid + "%");
		Integer itemp = 0;
		if (contractList.size() == 1) {
			Coupons coupons = contractList.get(0);
			ht.lock(coupons, LockMode.UPGRADE);
			String temp = coupons.getId();
			temp = temp.substring(temp.length() - 6);
			itemp = Integer.valueOf(temp);
		}
		itemp++;
		gid += String.format("%08d", itemp);
		return gid;
	}

	/**
	 * 获取所有有手机号的用户的手机号
	 * @return
	 */

	public Set<String> getUserWithMobileNumber() {
		List<User> users = ht.find("from User user where user.mobileNumber is not null and idCard is not null");
		Set<String> mobileSet= new HashSet<String>();
		for (User user:users){
			if (StringUtils.isNotEmpty(user.getMobileNumber())){
				mobileSet.add(user.getMobileNumber());
			}
		}
		return mobileSet;
	}

	/**
	 * 根据HuiFu用户id获取本平台用户Id
	 * @param curId
	 * @return
	 * @Auth Songli Li
	 * @Date 2016年8月16日 下午6:49:10
	 */
	public User getUserIdByHuiFuCurId(String curId) {
		String sql = "from User user where user.usrCustId="+curId;
		List<User> users = ht.find(sql);
		if(users != null && users.size()>0){
			return users.get(0);
		}
		return null;
	}

}
