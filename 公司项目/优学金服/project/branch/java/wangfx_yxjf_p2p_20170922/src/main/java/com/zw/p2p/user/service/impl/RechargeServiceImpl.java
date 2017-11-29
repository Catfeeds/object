package com.zw.p2p.user.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import weibo4j.org.json.JSONArray;

import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.UserConstants.RechargeStatus;
import com.zw.archer.user.model.RechargeBankCard;
import com.zw.archer.user.model.RechargeBankCardImpl;
import com.zw.archer.user.model.User;
import com.zw.archer.user.model.UserBill;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.NumberUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.area.model.RechargeBank;
import com.zw.p2p.bankcode.model.BankCode;
import com.zw.p2p.bankcode.service.BankCodeService;
import com.zw.p2p.fee.model.FeeSchemeCustomer;
import com.zw.p2p.fee.service.FeeSchemeService;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Recharge;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.user.service.RechargeService;

/**
 * Company: p2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description:
 * 
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-1-27 上午10:28:55
 * 
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-27 wangzhi 1.0
 */
@Service("rechargeService")
public class RechargeServiceImpl implements RechargeService {

	@Resource
	HibernateTemplate ht;

	@Resource
	RechargeBO rechargeBO;
	
	@Resource
	private UserBillService userBillService;
	
	@Resource
	private BankCodeService BankCodeService;
	
	@Resource
	private SystemBillService sbs;

	@Resource
	private AutoMsgService autoMsgService;

	private List<RechargeBank> bankList;
	@Resource
	private FeeSchemeService feeSchemeService;

	@Logger
	Log log;
	
	@PostConstruct
	public void init(){
		bankList=new ArrayList<RechargeBank>();

		int i=0;
		bankList.add(new RechargeBank(i,"CMB","CMB.png","招商银行","95555","http://www.cmbchina.com/"));
		bankList.add(new RechargeBank(++i,"ICBC","ICBC.png","中国工商银行","95588","http://www.icbc.com.cn/"));
		bankList.add(new RechargeBank(++i,"CCB","CCB.png","中国建设银行","95533","http://www.ccb.com/"));
		bankList.add(new RechargeBank(++i,"BOC","BOC.png","中国银行","95566","http://www.boc.cn/"));
		bankList.add(new RechargeBank(++i,"ABC","ABC.png","中国农业银行","95599","http://www.95599.cn/"));
		bankList.add(new RechargeBank(++i,"BOCM","BANKCOMM.png","交通银行","95559","http://www.bankcomm.com/"));
		bankList.add(new RechargeBank(++i,"SPDB","SPDB.png","浦发银行","95528","http://www.spdb.com.cn/"));
		bankList.add(new RechargeBank(++i,"CGB","GDB.png","广发银行","95508","http://www.cgbchina.com.cn/"));
		bankList.add(new RechargeBank(++i,"CITIC","CITIC.png","中信银行","95558","http://bank.ecitic.com/"));
		bankList.add(new RechargeBank(++i,"CEB","CEB.png","中国光大银行","95595","http://www.cebbank.com/"));
		bankList.add(new RechargeBank(++i,"CIB","CIB.png","兴业银行","95561","http://www.cib.com.cn/"));
		bankList.add(new RechargeBank(++i,"CMBC","CMBC.png","中国民生银行","95568","http://www.cmbc.com.cn/"));
		bankList.add(new RechargeBank(++i,"HXB","HXB.png","华夏银行","95577","http://www.hxb.com.cn/"));
		bankList.add(new RechargeBank(++i,"SDB","PINGAN.png","平安银行","95511","http://www.pingan.com/"));
	}

	@Override
	public double calculateFee(double amount) {
		FeeSchemeCustomer schemeCustomer= feeSchemeService.getCurrentFeeScheme().getSchemeCustomers().get(0);
		return schemeCustomer.getRecharge();
//
//		return feeConfigBO.getFee(FeePoint.RECHARGE, FeeType.FACTORAGE, null,
//				null, amount);
	}

	/**
	 * 充值成功
	 * @param rechargeId 充值编号
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean rechargePaySuccess(String rechargeId) {
		Recharge recharge = ht
				.get(Recharge.class, rechargeId);
		ht.evict(recharge);
		recharge = ht
				.get(Recharge.class, rechargeId, LockMode.UPGRADE);
		if (recharge != null
				&& recharge.getStatus().equals(
						UserConstants.RechargeStatus.WAIT_PAY)) {
			rechargeBO.rechargeSuccess(recharge);
			sendRechargeMsg(recharge);
			return true;
		}
		
		return false;
	}

	/**
	 * 充值成功发送信息
	 * @param recharge  充值对象
	 */
	public void sendRechargeMsg(Recharge recharge){
		Map<String,String>  params = new HashMap<String, String>();
		User user = ht.get(User.class,recharge.getUser().getId());
		params.put("time",DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("username",user.getId());
		params.put("money", NumberUtil.insertComma(recharge.getActualMoney()+"", 2));
		try {

			autoMsgService.sendMsg(user,"recharge_success",params);
		}catch (Exception e){
			log.error("充值成功信息发送错误");
		}
	}

	/**
	 * 生成充值id
	 * 
	 * @return
	 */
	private String generateId() {
//		String gid = DateUtil.DateToString(new Date(), DateStyle.YYYYMMDD);
//		String hql = "select recharge from Recharge recharge where recharge.id = (select max(rechargeM.id) from Recharge rechargeM where rechargeM.id like ?)";
//		List<Recharge> contractList = ht.find(hql, gid + "%");
//		Integer itemp = 0;
//		if (contractList.size() == 1) {
//			Recharge recharge = contractList.get(0);
//			ht.lock(recharge, LockMode.UPGRADE);
//			String temp = recharge.getId();
//			temp = temp.substring(temp.length() - 6);
//			itemp = Integer.valueOf(temp);
//		}
//		itemp++;
//		gid += String.format("%08d", itemp);
//		return gid;
		
		return OrderNoService.getOrderNo()+ new Random().nextInt(100);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String createOfflineRechargeOrder(Recharge recharge) {
		// 往recharge中插入值。
		recharge.setId(generateId());
		recharge.setFee(calculateFee(recharge.getActualMoney()));
		// 用rechargeWay进行判断，判断是要跳转到银行卡还是支付平台
		// recharge.setRechargeWay("借记卡");
		recharge.setIsRechargedByAdmin(false);
		recharge.setTime(new Date());
		recharge.setStatus(UserConstants.RechargeStatus.WAIT_PAY);
		ht.save(recharge);
		
		return "success";
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public String createRechargeOrder(Recharge recharge) {
		// 往recharge中插入值。
		recharge.setId(generateId());
		recharge.setFee(calculateFee(recharge.getActualMoney()));
		// 用rechargeWay进行判断，判断是要跳转到银行卡还是支付平台
		// recharge.setRechargeWay("借记卡");

		recharge.setIsRechargedByAdmin(false);
		recharge.setTime(new Date());
		recharge.setStatus(UserConstants.RechargeStatus.WAIT_PAY);
		ht.save(recharge);
		return FacesUtil.getHttpServletRequest().getContextPath()
				+ "/to_recharge/" + recharge.getId();
	}

	@Override
	public String getBankNameByNo(final String bankNo){
		List<RechargeBankCard> banks = getBankCardsList();
		for(RechargeBankCard bank : banks){
			if(StringUtils.equals(bank.getNo(), bankNo)){
				return bank.getBankName();
			}
		}
		return "Not found Bank";
	}
	
	/*private static Properties props = new Properties(); 
	static{
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("banks.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	@Override
	public RechargeBank getRechargeBankByIdx(int idx) {
		return bankList.get(idx);
	}

	public String getBanksJSON(){
		JSONArray jsonArray= new JSONArray();
		for (int i=0;i<bankList.size();i++){
			JSONObject object=JSONObject.fromObject(bankList.get(i));
			jsonArray.put(object);
		}
		return jsonArray.toString();
	}

	@Override
	public List<RechargeBank> getRechargeBankList(int flag) {
		List<RechargeBank> banks=new ArrayList<RechargeBank>();

		switch (flag){
			case 0:
				for (int i=0;i<4;i++){
					banks.add(bankList.get(i));
				}
				break;
			case 1:
				for (int i=4;i<8;i++){
					banks.add(bankList.get(i));
				}
				break;
			case 2:
				for (int i=8;i<12;i++){
					banks.add(bankList.get(i));
				}
				break;
			case 3:
				for (int i=12;i<bankList.size();i++){
					banks.add(bankList.get(i));
				}
				break;
		}
		return banks;
	}

	@Override
	public List<Integer> getFlags() {
		List<Integer> flags=new ArrayList<Integer>();
		flags.add(0);
		flags.add(1);
		flags.add(2);
		flags.add(3);

		return flags;
	}

	@Override
	public List<RechargeBankCard> getBankCardsList() {
		List<RechargeBankCard> bcs = new ArrayList<RechargeBankCard>();
		/*Iterator<Entry<Object, Object>> it = props.entrySet().iterator();  
		while(it.hasNext()){
			Entry<Object, Object> entry = it.next();  
            String key = (String) entry.getKey();  
            String value = (String) entry.getValue();  
			bcs.add(new RechargeBankCardImpl(key, value));
		}*/
		//2016年4月5日18:41:48 cuihang
		List<BankCode> bclist=BankCodeService.findAllBankCode();
		for (BankCode bankCode : bclist) {
			bcs.add(new RechargeBankCardImpl(bankCode.getPayCode(), bankCode.getName()));
		}
		// bcs.add(new RechargeBankCardImpl("TESTBANK", "测试银行"));
//		bcs.add(new RechargeBankCardImpl("ABC", "农业银行"));
		// bcs.add(new RechargeBankCardImpl("GNXS", "广州市农信社"));
		// bcs.add(new RechargeBankCardImpl("BCCB", "北京银行"));
		// bcs.add(new RechargeBankCardImpl("GZCB", "广州市商业银行"));
		// bcs.add(new RechargeBankCardImpl("BJRCB", "北京农商行"));
		// bcs.add(new RechargeBankCardImpl("HCCB", "杭州银行"));
//		bcs.add(new RechargeBankCardImpl("BOC", "中国银行"));
		// bcs.add(new RechargeBankCardImpl("HKBCHINA", "汉口银行"));
		// bcs.add(new RechargeBankCardImpl("BOS", "上海银行"));
		// bcs.add(new RechargeBankCardImpl("HSBANK", "徽商银行"));
		// bcs.add(new RechargeBankCardImpl("CBHB", "渤海银行"));
		// bcs.add(new RechargeBankCardImpl("HXB", "华夏银行"));
//		bcs.add(new RechargeBankCardImpl("CCB", "建设银行"));
//		bcs.add(new RechargeBankCardImpl("ICBC", "工商银行"));
		// bcs.add(new RechargeBankCardImpl("CCQTGB", "重庆三峡银行"));
		// bcs.add(new RechargeBankCardImpl("NBCB", "宁波银行"));
		// bcs.add(new RechargeBankCardImpl("CEB", "光大银行"));
		// bcs.add(new RechargeBankCardImpl("NJCB", "南京银行"));
		// bcs.add(new RechargeBankCardImpl("CIB", "兴业银行"));
		// bcs.add(new RechargeBankCardImpl("PSBC", "中国邮储银行"));
		// bcs.add(new RechargeBankCardImpl("CITIC", "中信银行"));
		// bcs.add(new RechargeBankCardImpl("SHRCB", "上海农村商业银行"));
//		bcs.add(new RechargeBankCardImpl("CMB", "招商银行"));
		// bcs.add(new RechargeBankCardImpl("SNXS", "深圳农村商业银行"));
		// bcs.add(new RechargeBankCardImpl("CMBC", "民生银行"));
		// bcs.add(new RechargeBankCardImpl("SPDB", "浦东发展银行"));
		// bcs.add(new RechargeBankCardImpl("COMM", "交通银行"));
		// bcs.add(new RechargeBankCardImpl("SXJS", "晋城市商业银行"));
		// bcs.add(new RechargeBankCardImpl("CSCB", "长沙银行"));
		// bcs.add(new RechargeBankCardImpl("SZPAB", "平安银行"));
		// bcs.add(new RechargeBankCardImpl("CZB", "浙商银行"));
		// bcs.add(new RechargeBankCardImpl("UPOP", "银联在线支付"));
		// bcs.add(new RechargeBankCardImpl("CZCB", "浙江稠州商业银行"));
		// bcs.add(new RechargeBankCardImpl("WZCB", "温州市商业银行"));
		// bcs.add(new RechargeBankCardImpl("GDB", "广东发展银行"));
		return bcs;
	}

	public RechargeServiceImpl() {
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void paybackByAdmin(UserBill userBill) {
		//充值记录
		Recharge r = new Recharge();
		r.setActualMoney(userBill.getMoney());
		r.setFee(0D);
		r.setId(generateId());
		r.setIsRechargedByAdmin(true);
		r.setRechargeWay("admin");
		r.setStatus(RechargeStatus.SUCCESS);

		r.setSuccessTime(new Date());
		r.setTime(new Date());
		r.setUser(userBill.getUser());
		ht.save(r);


		userBillService.paybackIntoBalance(userBill.getUser().getId(),
				userBill.getMoney(), OperatorInfo.ADMIN_OPERATION,
				userBill.getDetail());

		try {
			sbs.transferOut(userBill.getMoney(), UserBillConstants.Type.PAYBACK,
					userBill.getDetail(),null,null,null,r,null,null,null,null,null);
		}catch (InsufficientBalance e){
			e.printStackTrace();
			log.error("系统充值扣款支出失败，充值ID："+r.getId());
		}

		sendRechargeMsg(r);
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void rechargeByAdmin(UserBill userBill) {
		Recharge r = new Recharge();
		r.setActualMoney(userBill.getMoney());
		r.setFee(0D);
		r.setId(generateId());
		r.setIsRechargedByAdmin(true);
		r.setRechargeWay("admin");
		r.setStatus(RechargeStatus.SUCCESS);

		r.setSuccessTime(new Date());
		r.setTime(new Date());
		r.setUser(userBill.getUser());
		ht.save(r);
		userBillService.transferIntoBalance(userBill.getUser().getId(),
				userBill.getMoney(), OperatorInfo.ADMIN_OPERATION,
				userBill.getDetail());
		sendRechargeMsg(r);
	}

	/**
	 * 管理员充值
	 * @param rechargeId 充值编号
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public void rechargeByAdmin(String rechargeId) {
		rechargePaySuccess(rechargeId);
		Recharge r = ht.get(Recharge.class, rechargeId);
		r.setIsRechargedByAdmin(true);
		ht.update(r);
	}
	
	
	/**
	 * 
	 * 创建Pos充值订单，返回主键
	 * @author majie
	 * @date 2016年5月9日 上午11:12:14
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Recharge createHuiFuRechargeOrder(Recharge recharge,String orderNo){
		recharge.setId(generateId());
		recharge.setFee(calculateFee(recharge.getActualMoney()));
		recharge.setIsRechargedByAdmin(false);
		recharge.setTime(new Date());
		recharge.setStatus(UserConstants.RechargeStatus.WAIT_PAY);
		recharge.setOrdId(orderNo);
		ht.save(recharge);
		return recharge;
	}
	
	/**
	 * 
	 * 创建Pos充值订单，返回主键
	 * @author majie
	 * @date 2016年5月9日 上午11:12:14
	 */
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void createPosRechargeOrder(Recharge recharge){
//		String id = new Date().getTime()+""+(int)(Math.random()*1000);
//		recharge.setId(id);
		recharge.setRechargeWay("tonglian_pos");
		recharge.setFee(calculateFee(recharge.getActualMoney()));
		recharge.setIsRechargedByAdmin(false);
		recharge.setTime(new Date());
		recharge.setStatus(UserConstants.RechargeStatus.WAIT_PAY);
		ht.save(recharge);
		//发送pos订单短信通知
		try {
			User user = ht.get(User.class,recharge.getUser().getId());
			Map<String,String> params = new HashMap<String, String>();
			params.put("id", recharge.getId());
			params.put("actualMoney", recharge.getActualMoney()+"");
			params.put("username",user.getUsername());
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			params.put("time", sdf.format(new Date()));
			autoMsgService.sendMsg(user, "tonglian_pos_order", params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Recharge getLastRechargeByUser(String userid) {
		String  hql = "from Recharge recharge where recharge.user.id='"+userid+"' order by recharge.time desc";
		List<Recharge> rlist = ht.find(hql);
		if (null!=rlist&&rlist.size() > 0) {
			Recharge reCharge = rlist.get(0);
			return reCharge;
		}
		return null;
	}



}
