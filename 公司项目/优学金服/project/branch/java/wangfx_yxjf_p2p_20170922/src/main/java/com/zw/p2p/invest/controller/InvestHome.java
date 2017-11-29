package com.zw.p2p.invest.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.controller.HuiFuHome;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.coupons.service.CouponSService;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.exception.ExceedInvestTransferMoney;
import com.zw.p2p.invest.exception.ExceedMaxAcceptableRate;
import com.zw.p2p.invest.exception.ExceedMoneyNeedRaised;
import com.zw.p2p.invest.exception.IllegalLoanStatusException;
import com.zw.p2p.invest.exception.InvestException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.InvestService;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanCalculator;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.rateticket.model.RateTicket;
import com.zw.p2p.rateticket.service.RateTicketService;
import com.zw.p2p.repay.model.InvestRepay;
import com.zw.p2p.repay.service.RepayService;

/**
 * Filename: InvestHome.java Description: Copyright: Copyright (c)2013 Company:
 * jdp2p
 * 
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:26:10
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class InvestHome extends EntityHome<Invest> implements Serializable{

	@Resource
	private InvestService investService;

	@Resource
	private LoginUserInfo loginUserInfo;

	@Resource
	private RepayService repayService;
	
	@Resource
	private TransferService transferService;

	@Resource
	private AutoMsgService autoMsgService;

	@Resource
	private HibernateTemplate ht;
	@Resource
	private HuiFuLoanService huiFuLoanService;
	@Resource
	private CouponSService couponSService;
	@Resource
	private RateTicketService rateTicketService;
	@Resource
	private LoanCalculator loanCalculator;

	private String pwd;
	
	private boolean flag=true;
	
	private boolean chek=true;
	@Logger
	Log log;
	
	public void setMoney(Double money) {
		getInstance().setMoney(money);
	}
	
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	@Logger
	private Long couponCount;
	
	
	public Long getCouponCount() {
		return couponCount;
	}

	public void setCouponCount(Long couponCount) {
		this.couponCount = couponCount;
	}

	@Override
	protected Invest createInstance() {
		Invest invest = new Invest();
		TransferApply ta = new TransferApply();
		Loan loan = new Loan();
		loan.setUser(new User());
		invest.setLoan(loan);
		invest.setTransferApply(ta);
		return invest;
	}
	
	/**
	 * 列出所有加息券
	 * @return
	 */
	public List<RateTicket> userChooseRateTicket(){
		if(loginUserInfo.getUser()!=null){
			return rateTicketService.listAllUserRateTicket(loginUserInfo.getUser());
		}
		return null;
	}
	
	/**
	 * 列出所有红包
	 * @return
	 */					 
	public List<Coupons> userChooseCoupons(){
		if(loginUserInfo.getUser()!=null){
			return couponSService.listAllUserCoupons(loginUserInfo.getUser());
		}
		return null;
	}
	
	/**购买债权转让*/
	public void transfer(String transferApplyId){
		try {
			/*if (loginUserInfo.getLoginUserId().equals(getInstance().getTransferApply().getInvest().getUser().getId())) {
				FacesUtil.addErrorMessage("您不能购买自己的债权");
				return "pretty:invest_transfer_list";
			}*/
			//String cashPwd = HashCrypt.getDigestHash(pwd);
			//User user = ht.get(User.class,loginUserInfo.getLoginUserId());
			//if (pwd != null && cashPwd.equals(user.getCashPassword())){
				//transferService.transfer(getInstance().getTransferApply().getId(), loginUserInfo.getLoginUserId(), getInstance().getInvestMoney());
				User user=	loginUserInfo.getUser();
				
				double remainCorpus = transferService.calculateRemainCorpus(transferApplyId);
				// 出价必须大于0，小于可购买的金额
				if (remainCorpus<=0) {
					throw new ExceedInvestTransferMoney("购买本金必须大于0");
				}
				
				String html="";
				try {
					html = huiFuLoanService.creditAssign(transferApplyId,user);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					throw new ExceedInvestTransferMoney(e1.getMessage());
				}
				
				FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
				try {
					FacesUtil.getHttpServletResponse().getWriter().print(html);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			/*}else {
				FacesUtil.addErrorMessage("交易密码输入错误");
				return "pretty:invest_transfer_list";
			}*/
			FacesUtil.addInfoMessage("购买成功！");
			/*TransferApply ta = ht.get(TransferApply.class,getInstance().getTransferApply().getId());
			if ("transfered".equals(ta.getStatus())){
				sendTranMsg();
			}*/
			//return "pretty:user-transfer-purchased";
		} catch (ExceedInvestTransferMoney e) {
			FacesUtil.addErrorMessage(e.getMessage());
		}
		//return "pretty:invest_transfer_list";
	}

	/**
	 * 债权转让通知
	 */
	public void sendTranMsg(){
		User user = ht.get(User.class,getInstance().getTransferApply().getInvest().getUser().getId());
		Map<String,String> params = new HashMap<String, String>();
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		params.put("username",user.getId());
		params.put("dealNumber","'" + getInstance().getTransferApply().getInvest().getLoan().getName() + "'");
		try{
			autoMsgService.sendMsg(user,"assignment_debt",params);
		}catch (Exception e){
			log.error("债权转让信息通知发送错误");
		}
	}
	public static Map<String,String> hasloan=new HashMap<String, String>();
	/**
	 * 投资
	 */
	public void saveIn() {
		//String cashPwd = HashCrypt.getDigestHash(pwd);
		User user = ht.get(User.class,loginUserInfo.getLoginUserId());
		try {
			Loan loan = getBaseService().get(Loan.class,
					getInstance().getLoan().getId());
			if (loan.getUser().getId()
					.equals(loginUserInfo.getLoginUserId())) {
				FacesUtil.addInfoMessage("你不能投自己的项目！");
				return ;
			} else {
				if("xsdb326c5b664870b698739e93f2d6xs".equals(loan.getLoantypeb())){
					List<Invest> list=investService.getInvestsByLoanType(user, "xsdb326c5b664870b698739e93f2d6xs");
					if(null!=list&&list.size()>0){
						FacesUtil.addInfoMessage("每个用户只能投一次新手标");
						return ;
					}
				}
				
				this.getInstance().setUser(user);
				this.getInstance().setIsAutoInvest(false);
				//if (cashPwd != null && cashPwd.equals(getInstance().getUser().getCashPassword())){
					
			String html = investService.create(getInstance());
			FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
			FacesUtil.getHttpServletResponse().getWriter().print(html);
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("账户余额不足，请充值！");
			return ;
		} catch (ExceedMoneyNeedRaised e) {
			FacesUtil.addErrorMessage("投资金额不能大于尚未募集的金额！");
			return ;
		} catch (ExceedMaxAcceptableRate e) {
			FacesUtil.addErrorMessage("竞标利率不能大于借款者可接受的最高利率！");
			return ;
		} catch (IllegalLoanStatusException e) {
			FacesUtil.addErrorMessage("当前借款不可投资");
			return ;
		}catch (InvestException e){
			FacesUtil.addErrorMessage(e.getMessage());
			return ;
		} catch (Exception e) {
			e.printStackTrace();
		}

//		FacesUtil.addInfoMessage("投资成功！");
//		hasloan.put(user.getId(), user.getId());
//		if (FacesUtil.isMobileRequest()) {
//			return ;
//		}
		
		/*return "pretty:showfinaDialog()";*/
	}
	/**是否投资后显示活动
	 * @return
	 */
	public String getShowAct(){
	/*	String userid=loginUserInfo.getLoginUserId();
		if(hasloan.containsKey(userid)){
			hasloan.remove(userid);
			return "1";
		}*/
		return "0";
	}
	
	/**
	 * 今天已使用红包数量
	 * */
	public void getCouponsCount(){
		User user = ht.get(User.class,loginUserInfo.getLoginUserId());
		setCouponCount(couponSService.getTodayUsedCoupon(user));
	}
	/**
	 * 投资成功发送信息
	 */
	 public void sendInvestMsg(){
		 User user = ht.get(User.class,loginUserInfo.getLoginUserId());
		 Map<String,String>  params  = new HashMap<String, String>();
		 Loan loan = getBaseService().get(Loan.class,
				 getInstance().getLoan().getId());
		 params.put("username",user.getUsername());
		 params.put("money",getInstance().getMoney()+"");
		 params.put("dealNumber",loan.getName());
		 params.put("status","成交");
		 params.put("person","借款人");
		 try{
			 autoMsgService.sendMsg(user,"invest_success",params);
		 }catch (Exception e){
		 	log.error("投资成功信息发送失败");
		 }
	 }
	
	/**
	 * 获取某个用户总的投资额
	 * 
	 * @return
	 */
	public double getSumInvest(String userId) {
		Object o = getBaseService().find(
				"select sum(im.money) from Invest im where im.user.id=?",
				userId).get(0);
		if (o == null) {
			return 0;
		}
		return (Double) o;
	}
	
	/**
	  * 获取某个用户获得的总的投资额(投资成功的)
	  * 取状态投资已完成和债权转让成功
	  * yinxunzhi
	  * @return
	  */
	 public double getSumInvestMoney(String userId) {
	  Object o = getBaseService()
	    .find("select sum(im.money) from Invest im where im.user.id=? and im.status in (?,?)",
	      new String[] { userId,
	        InvestConstants.InvestStatus.COMPLETE}).get(0);
	  if (o == null) {
	   return 0;
	  }
	  return (Double) o;
	 }


	/**
	 * 某个用户投资的数量
	 * 
	 * @return
	 */
	public long getInvestAmount(String userId) {
		String hql = "select count(*) from Invest invest where invest.user.id=?";
		return (Long) getBaseService().iterate(hql, userId).next();
	}

	/**
	 * 某个项目的收到的总金额
	 * @param investId
	 * @return
	 */
	public double getAllAmount(String investId){
		double amount = 0D;
		List<InvestRepay> investRepays = ht.find("from InvestRepay ir where ir.invest.id = ? ",investId);
		for (InvestRepay ir : investRepays){
			if (!"advance".equals(ir.getRepayWay())){
				amount += ir.getCorpus() + ir.getInterest() + ir.getDefaultInterest() - ir.getFee();
			}else {
				amount += ir.getCorpus() + ir.getDefaultInterest();
			}
		}
		return amount;
	}

	/**
	 * 某个项目的收到的总收益
	 * @param investId
	 * @return
	 */
	public double getGainAmount(String investId){
		double amount = 0D;
		List<InvestRepay> investRepays = ht.find("from InvestRepay ir where ir.invest.id = ? ",investId);
		for (InvestRepay ir : investRepays){
			if (!"advance".equals(ir.getRepayWay())){
				amount += ir.getInterest() + ir.getDefaultInterest() - ir.getFee();
			}else {
				amount += ir.getDefaultInterest();
			}
		}
		return amount;
	}

	/**
	 * 获得剩余天数
	 * @param investId
	 * @return
	 */
	public int getDays(String investId){
		Invest invest = ht.get(Invest.class,investId);
		int days = invest.getLoan().getDeadline() - DateUtil.getIntervalDays(new Date(),invest.getLoan().getGiveMoneyTime());
		return days > 0 ? days : 0;
	}
	//格式化姓名
		public String formatname(String name){
			if(name==null||name.equals("")){
				return "";
			}
			String fristname=name.substring(0,2);
			String lastname=name.substring(name.length()-2,name.length());
			String tophone=fristname+"***"+lastname;
			
			//String tophone=name.replaceAll("(\\d{1})\\d{2,}(\\d{1})","$1***$2");
			return tophone;
		}
		
	public void userChooseConpons(Coupons coupons,String raise){
		if(getInstance().getRateTicket()!=null){
			getInstance().setRateTicket(null);
			if(Double.valueOf(coupons.getMoney())>Double.valueOf(raise)){
				FacesUtil.addErrorMessage("该红包金额大于您的投资金额，若您使用此红包将无法获得与红包金额相等的收益");
			}
			getInstance().setCoupons(coupons);
			flag=false;
			return;
		}
		if(!flag){
			flag=true;
		}
		if(Double.valueOf(coupons.getMoney())>Double.valueOf(raise)){
			FacesUtil.addErrorMessage("该红包金额大于您的投资金额，若您使用此红包将无法获得与红包金额相等的收益");
		}
		getInstance().setCoupons(coupons);
		
		/*if(couponSService.getTodayUsedCoupon(coupons.getUser())>=3){
			FacesUtil.addErrorMessage("今天已使用三个红包");
			getInstance().setCoupons(null);
			chek=false;
			return;
		}*/
	}
	
	public void userChooseRateTicket(RateTicket rateTicket){
		if(getInstance().getCoupons()!=null){
			getInstance().setCoupons(null);
			getInstance().setRateTicket(rateTicket);
			flag=false;
			return;
		}
		if(!flag){
			flag=true;
		}
		getInstance().setRateTicket(rateTicket);
		/*if(rateTicketService.getTodayUsedRateTicket(rateTicket.getUser())>=3){
			FacesUtil.addErrorMessage("今天已使用三个加息券");
			getInstance().setRateTicket(null);
			chek=false;
			return;
		}*/
	}
	public void cancelChoose(){
		getInstance().setCoupons(null);
		getInstance().setRateTicket(null);
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public boolean isChek() {
		return chek;
	}

	public void setChek(boolean chek) {
		this.chek = chek;
	}
	/**
	*@Description: TODO(是否投资成功) 
	* @author cuihang   
	*@date 2017-8-30 下午2:38:06 
	*@return
	 */
	public boolean hasInvest() {
		Invest invest=getInstance();
		String ordId=invest.getOrdId();
		invest=investService.getInvstByOrdId(ordId);
		if(invest!=null){
			return true;
		}
		return false;
	}	
	/**
	 *@Description: TODO(是否投资超额) 
	 * @author cuihang   
	 *@date 2017-8-30 下午2:38:06 
	 *@return
	 */
	public boolean hasInvestOut() {
		Invest invest=getInstance();
		String ordId=invest.getOrdId();
		if(HuiFuHome.hasInvestOut.containsKey(ordId)){
			//HuiFuHome.hasInvestOut.remove(ordId);
			return true;
		}
		return false;
	}	
	public void hasInvestJso() {
		boolean result=hasInvest();
		JSONObject json = new JSONObject();
		json.put("success", result);
		try {
			FacesUtil.getHttpServletResponse().getWriter().print(encodeUrl(json.toJSONString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	/**
	 *@Description: TODO(是否投资超额) 
	 * @author cuihang   
	 *@date 2017-8-30 下午2:38:06 
	 *@return
	 */
	public void hasInvestOutJso() {
		boolean result=hasInvestOut();
		JSONObject json = new JSONObject();
		json.put("success", result);
		try {
			FacesUtil.getHttpServletResponse().getWriter().print(encodeUrl(json.toJSONString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	private String encodeUrl(String str){
		try {
			return URLEncoder.encode(str,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
