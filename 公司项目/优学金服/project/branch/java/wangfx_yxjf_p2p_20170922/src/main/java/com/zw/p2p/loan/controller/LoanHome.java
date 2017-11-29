package com.zw.p2p.loan.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdScheduler;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.notice.model.Notice;
import com.zw.archer.notice.model.NoticePool;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.exception.UserNotFoundException;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserService;
import com.zw.archer.user.service.impl.UserBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.bean.ZwJson;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.core.util.ImageUploadUtil;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.p2p.gps.service.GpsService;
import com.zw.p2p.loan.LoanConstants;
import com.zw.p2p.loan.exception.ExistWaitAffirmInvests;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.loan.exception.InvalidExpectTimeException;
import com.zw.p2p.loan.exception.LoanException;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.service.LoanService;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.message.service.impl.MessageBO;
import com.zw.p2p.safeloan.service.SafeLoanService;
import com.zw.p2p.safeloan.service.SafeLoanTaskService;
import com.zw.p2p.schedule.ScheduleConstants;
import com.zw.p2p.schedule.job.AutoInvestAfterLoanPassed;

/**
 * Filename: LoanHome.java Description: Copyright: Copyright (c)2013 Company:
 * jdp2p
 * c
 * @author: yinjunlu
 * @version: 1.0 Create at: 2014-1-11 下午4:28:49
 * 
 *           Modification History: Date Author Version Description
 *           ------------------------------------------------------------------
 *           2014-1-11 yinjunlu 1.0 1.0 Version
 */
@Component
@Scope(ScopeType.VIEW)
public class LoanHome extends EntityHome<Loan> implements Serializable {

	private boolean ispass = false;
	@Resource
	private LoanService loanService;
	@Resource
	private LoginUserInfo loginUser;
	@Resource
	private UserService userService;
	@Resource
	private SafeLoanService safeLoanService;
	@Resource
	NoticePool noticePool;
	@Resource
	ConfigService configService;

	@Resource
	private SmsService smsService;

	@Resource
	private HibernateTemplate ht;

	@Resource
	private MessageBO messageBO;

	@Resource
	private AutoMsgService autoMsgService;
	
	@Logger
	Log log;

	@Resource
	StdScheduler scheduler;

	@Resource
	private UserBO userBO;

	@Resource
	private DictUtil dictUtil;
	@Resource
	SafeLoanTaskService safeLoanTaskService;
	
	@Resource
	private HuiFuLoanService huiFuLoanService;
	
	@Resource
	private GpsService gpsService;
	/**
	 * GPS定位的经度
	 */
	private Double gpsCoordinate_x;
	
	/**
	 * GPS定位的纬度
	 */
	private Double gpsCoordinate_y;
	
	
	private Double loanMoney;
	
	public void setLoanMoney(Double loanMoney) {
		getInstance().setLoanMoney(loanMoney);
		if(getInstance().getMaxInvestMoney()==null){
			getInstance().setMaxInvestMoney(loanMoney);
		}
	}
	
	public Double getLoanMoney() {
		return getInstance().getLoanMoney();
	}


	/**
	 * 后台借款列表
	 */
	public final static String loanListUrl = "/admin/loan/loanList";

	public void initVerify(Loan loan) {
		this.setInstance(loan);
		if (LoanConstants.LoanVerifyStatus.PASSED.equals(this.getInstance()
				.getVerified())) {
			ispass = true;
		}
	}

	public boolean getIspass() {
		return ispass;
	}

	public void setIspass(boolean ispass) {
		this.ispass = ispass;
	}

	/**
	 * 借款审核
	 */
	public String verify() {
	String massss=this.getInstance().getVerifyMessage();
		if (ispass) {
			if (this.getInstance().getExpectTime() == null) {
				FacesUtil.addErrorMessage("请填写预计执行时间。");
				ispass = false;
				return null;
			}
			try {
				this.getInstance().setVerifyUser(
						new User(loginUser.getLoginUserId()));
				loanService.passApply(this.getInstance());
				//addAutoInvestJob(getInstance());
				//autoSendLoanMessage();
			} catch (InvalidExpectTimeException e) {
				FacesUtil.addErrorMessage("招标到期时间必须在当前时间之后");
				ispass = false;
				return null;
			} catch (InsufficientBalance e) {
				FacesUtil.addErrorMessage("余额不足，无法支付借款保证金。");
				ispass = false;
				return null;
			}
			FacesUtil.addInfoMessage("通过借款申请");
			noticePool.add(new Notice("借款："+getInstance().getId()+"通过申请"));
		} else {
		  String mess=this.getInstance().getVerifyMessage();
			loanService.refuseApply(this.getInstance().getId(), this
					.getInstance().getVerifyMessage(), loginUser
					.getLoginUserId());
			FacesUtil.addInfoMessage("拒绝借款申请");
			noticePool.add(new Notice("借款："+getInstance().getId()+"申请被拒绝"));
			sendVerifyMsg(this.getInstance().getUser());
		}
		return FacesUtil.redirect(loanListUrl);
	}

	/**
	 * 流标通知
	 * @param user
	 */
	public void sendVerifyMsg(User user){
		loanService.sendVerifyMsg(user);
		/*Map<String,String> params = new HashMap<String, String>();
		params.put("username",user.getUsername());
		try {

			autoMsgService.sendMsg(user,"verify",params);
		}catch (Exception e){
			log.error("流标通知发送失败");
		}*/
	}
//    returnBtn
	/**
	 * 申请借款
	 */
	public String save() {
		try {
			//获取当前登录用户
			User user = userService.getUserById(loginUser.getLoginUserId());
			//保存申请人
			getInstance().setUser(user);
			loanService.applyLoan(getInstance());
			FacesUtil.addInfoMessage("发布借款成功，请填写个人基本信息。");
			return "pretty:loanerPersonInfo";
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage(e.getMessage());
			e.printStackTrace();
		} catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录！");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 延期
	 * 
	 * @return
	 */
	public String delay() {
		try {
			loanService.delayExpectTime(getInstance().getId(), getInstance()
					.getExpectTime());
		} catch (InvalidExpectTimeException e) {
			FacesUtil.addErrorMessage("招标到期时间必须在当前时间之后");
			return null;
		}
		FacesUtil.addInfoMessage("项目延期成功，募集期延至"
				+ DateFormatUtils.format(getInstance().getExpectTime(),
						"yyyy年MM月dd日 HH:mm:ss"));
		noticePool.add(new Notice("借款："+getInstance().getId()+"延期"));
		return FacesUtil.redirect(loanListUrl);
	}

	/**
	 * 复核
	 */
	public String recheck(User user) {
		try {
			loanService.recheckLoan(this.getInstance().getId());
			
		}catch (Exception e) {
			FacesUtil.addInfoMessage(e.getMessage());
			return FacesUtil.redirect(loanListUrl);
		}

		FacesUtil.addInfoMessage("复核成功");
		noticePool.add(new Notice("借款：" + getInstance().getId() + "复核通过"));
		sendApprovalMsg(user);
		return FacesUtil.redirect(loanListUrl);
	}

	/**
	 * 发送审核信息
	 * @param user
	 */
	public void sendApprovalMsg(User user){
		Map<String,String> params = new HashMap<String, String>();
		params.put("username",user.getUsername());
		try{
			autoMsgService.sendMsg(user,"borrowing_approval",params);
		}catch (Exception e){
			log.error("审核信息发送失败");
		}
	}
	/**
	 * 放款
	 */

	public String financialLoan() {
		ZwJson json = huiFuLoanService.loanMoneyByLoanId(this.getInstance().getId());
		if(json.isSuccess()){
			FacesUtil.addInfoMessage("放款成功");
			noticePool.add(new Notice("借款：" + getInstance().getId() + "放款成功"));
			return FacesUtil.redirect(loanListUrl);
		}else{
			FacesUtil.addInfoMessage("放款失败");
			return null;
		}
		
	}

	

	/**
	 * 流标
	 * 
	 * @return
	 */
	public String failByManager() {
			
			try {
				boolean res = loanService.fail(this.getInstance().getId(),
						loginUser.getLoginUserId());
				if(res){
					sendVerifyMsg(this.getInstance().getUser());
					FacesUtil.addInfoMessage("流标成功");
					noticePool.add(new Notice("借款："+getInstance().getId()+"流标"));
					sendFailMsg();
				}else{
					FacesUtil.addInfoMessage("流标失败,请再次尝试或联系管理员");
				}
			} catch (ExistWaitAffirmInvests e) {
				FacesUtil.addInfoMessage("流标失败，存在等待第三方资金托管确认的投资。");
				return null;
			}
			return FacesUtil.redirect(loanListUrl);
	}
	
	
	/**
	 * 初审时流标
	 * @author majie
	 * @date 2016年9月2日 下午6:09:24
	 */
	public String failLoanOfVerify(){
		try {
			loanService.failReturn(this.getInstance().getId(),loginUser.getLoginUserId());
			return failByManagerReturn();
		} catch (ExistWaitAffirmInvests e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 流标
	 * 
	 * @return
	 */
	public String failByManagerReturn() {
		//sendVerifyMsg(this.getInstance().getUser());
		FacesUtil.addInfoMessage("流标成功");
		noticePool.add(new Notice("借款："+getInstance().getId()+"流标"));
		//sendFailMsg();
		return FacesUtil.redirect(loanListUrl);
	}

	/**
	 * 发送流标信息
	 */
	public void sendFailMsg(){
		loanService.sendFailMsg(this.getInstance());
		/*Map<String,String> params = new HashMap<String, String>();
		params.put("dealNumber","'" + this.getInstance().getName() + "'");
		params.put("status", "失败");
		params.put("person", "您");
		params.put("time", DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
		List<Invest> invests = ht.find("from Invest  where  loan_id = ?",this.getInstance().getId());
		for (Invest invest : invests){
			params.put("username",invest.getUser().getUsername());
			params.put("money",NumberUtil.insertComma(invest.getMoney() + "",2));
			try {
				if (safeLoanTaskService.investInclude(invest)){
					continue;
				}else{
					autoMsgService.sendMsg(invest.getUser(),"invest_success",params);
				}
			}catch (Exception e){
				log.error("流标信息发送错误");
			}
		}*/
	}

	/**
	 * 更新借款，只能更新不影响流程的字段
	 */
	public String update() {
		loanService.update(getInstance());
		FacesUtil.addInfoMessage("项目修改成功！");
		return FacesUtil.redirect(loanListUrl);
	}

	/**
	 * 管理员手动添加借款。
	 * 
	 * @return
	 */
	public String createAdminLoan() {
		Loan loan = this.getInstance();
		if (!userService.hasRole(loan.getUser().getId(), "INVESTOR")) {
			FacesUtil.addErrorMessage("用户：" + loan.getUser().getId()
					+ "未实名认证，不能发起借款！");
			return null;
		}

		try {
			loanService.createLoanByAdmin(loan);
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足，无法支付借款保证金。");
			return null;
		}catch (InvalidExpectTimeException e) {
			FacesUtil.addErrorMessage("招标到期时间必须在当前时间之后");
			return null;
		}catch (LoanException e){
			FacesUtil.addErrorMessage(e.getMessage());
			return null;
		}

		FacesUtil.addInfoMessage("发布借款成功");
		return FacesUtil.redirect("/admin/loan/loanList");
	}
	/**
	 * 用户申请添加。
	 * 
	 * @return
	 */
	public String createLoanByLoaner() {
		Loan loan = this.getInstance();
		if(loan.getGuaranteeInfoPics()==null||loan.getGuaranteeInfoPics().size()==0){
			FacesUtil.addErrorMessage("抵押物资为必填项");
			return "pretty:applyLoan";
		}
		if (loan.getLoanInfoPics()==null||loan.getLoanInfoPics().size()==0) {
			FacesUtil.addErrorMessage("项目材料为必填项");
			return "pretty:applyLoan";
		}
		User user=loginUser.getUser();
		List loanlist=loanService.getUserVerify(user);
		if(null!=loanlist){
			int loansize=loanlist.size();
			int maxNum=1;
			try{
				String applyloantoloanMaxNum=configService.getConfigValue("applyloantoloanMaxNum");
				maxNum=Integer.parseInt(applyloantoloanMaxNum);
			}catch (Exception e) {
				// TODO: handle exception
			}
			if(loansize>maxNum){
				FacesUtil.addInfoMessage("您已有"+loansize+"个申请正在审核中,请耐心等待管理员审核");
				return "pretty:userCenter";
			}
			
		}
		loan.setUser(user);
		/*if(null==loan.getGuaranteeInfoPics()){
			FacesUtil.addErrorMessage("请上传抵押物资照片。");
			return null;
		}
		if(null==loan.getLoanInfoPics()){
			FacesUtil.addErrorMessage("请上传项目材料照片。");
			return null;
		}*/
		if (!userService.hasRole(loan.getUser().getId(), "INVESTOR")) {
			FacesUtil.addErrorMessage("用户：" + loan.getUser().getId()
					+ "未实名认证，不能发起借款！");
			return null;
		}

		try {
			loan.setFeeOnRepay(0d);
			loan.setDescription("<p>"+loan.getDescription()+"</p>");
			loan.setBusinessType("个人借款");
			loan.setExpectTime(new Date());
			loanService.createLoanByAdmin(loan);
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足，无法支付借款保证金。");
			return null;
		}catch (InvalidExpectTimeException e) {
			FacesUtil.addErrorMessage("招标到期时间必须在当前时间之后");
			return null;
		}catch (LoanException e){
			FacesUtil.addErrorMessage(e.getMessage());
			return null;
		}

		FacesUtil.addInfoMessage("发布借款成功,请耐心等待管理员审核");
		return "pretty:userCenter";
	}
	/**
	 * 上传抵押物资图片
	 *
	 * @return
	 */
	@Transactional(readOnly = false)
	public void uploadGuaranteeInfoPics(FileUploadEvent event) {
		UploadedFile uploadFile = event.getFile();
		InputStream is = null;
			try {
				 List<BannerPicture>  listBanne=this.getInstance().getGuaranteeInfoPics();
				 if(null==listBanne){
					 listBanne=new ArrayList<BannerPicture>();
					 this.getInstance().setGuaranteeInfoPics(listBanne);
				 }
				is = uploadFile.getInputstream();
				BannerPicture pp = new BannerPicture();
				pp.setId(IdGenerator.randomUUID());
				String  fileName= uploadFile.getFileName();
				String fileName1=fileName.substring(0,fileName.length()-4);
				pp.setFileName(fileName1);
				pp.setPicture(ImageUploadUtil.upload(is, uploadFile.getFileName()));
				pp.setSeqNum(listBanne.size() + 1);
				listBanne.add(pp);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			FacesUtil.addInfoMessage("上传成功！");
	}
	public void moveGuaranteeInfoPics(BannerPicture bp) {
		List<BannerPicture>  listBanne=this.getInstance().getGuaranteeInfoPics();

		if (bp != null) {
			try{				
				listBanne.remove(bp);
			} catch(Exception e){
			}
		}
	
	}
	/**
	 * 上传项目材料图片
	 *
	 * @return
	 */
	@Transactional(readOnly = false)
	public void uploadLoanInfoPics(FileUploadEvent event) {
		UploadedFile uploadFile = event.getFile();
		InputStream is = null;
		try {
			List<BannerPicture>  listBanne=this.getInstance().getLoanInfoPics();
			if(null==listBanne){
				listBanne=new ArrayList<BannerPicture>();
				this.getInstance().setLoanInfoPics(listBanne);
			}
			is = uploadFile.getInputstream();
			BannerPicture pp = new BannerPicture();
			pp.setId(IdGenerator.randomUUID());
			String  fileName= uploadFile.getFileName();
			String fileName1=fileName.substring(0,fileName.length()-4);
			pp.setFileName(fileName1);
			pp.setPicture(ImageUploadUtil.upload(is, uploadFile.getFileName()));
			pp.setSeqNum(listBanne.size() + 1);
			listBanne.add(pp);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		FacesUtil.addInfoMessage("上传成功！");
	}
	public void moveLoanInfoPics(BannerPicture bp) {
		List<BannerPicture>  listBanne=this.getInstance().getLoanInfoPics();
		if (bp != null) {
			try{				
				listBanne.remove(bp);
			} catch(Exception e){
			}
		}
	
	}
	/**
	 * 判断一个loan是否有某个属性
	 * 
	 * @param loanId
	 * @param attrId
	 *            属性id
	 * @return
	 * @author liuchun
	 */
	public boolean hasAttr(String loanId, String attrId) {
		String hql = "select loan from Loan loan left join loan.loanAttrs attr where loan.id=? and attr.id = ?";
		return getBaseService().find(hql, new String[] { loanId, attrId })
				.size() > 0;
	}
	

	/**
	 * 调度，自动投标--wangxiao 2014-4-28
	 * 
	 * @param loan
	 */
	private void addAutoInvestJob(Loan loan) {
		JobDetail jobDetail2 = JobBuilder
				.newJob(AutoInvestAfterLoanPassed.class)
				.withIdentity(
						loan.getId(),
						ScheduleConstants.JobGroup.AUTO_INVEST_AFTER_LOAN_PASSED)
				.build();
		jobDetail2.getJobDataMap().put(AutoInvestAfterLoanPassed.LOAN_ID,
				loan.getId());
		// FIXME:需判断DELAY_TIME是否大于0
		Date startDate = DateUtil
				.addMinute(new Date(), Integer.parseInt(configService
						.getConfigValue(ConfigConstants.AutoInvest.DELAY_TIME)));
		SimpleTrigger trigger2 = TriggerBuilder
				.newTrigger()
				.withIdentity(
						loan.getId(),
						ScheduleConstants.TriggerGroup.AUTO_INVEST_AFTER_LOAN_PASSED)
				.forJob(jobDetail2)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule())
				.startAt(startDate).build();
		// ///////////////////////////////////////////////////
		try {
			scheduler.scheduleJob(jobDetail2, trigger2);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		if (log.isDebugEnabled())
			log.debug("添加[自动投标]调度成功，项目编号["
					+ loan.getId()
					+ "]，时间："
					+ DateUtil.DateToString(startDate,
							DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
	}

	/**
	 * 初审通过后，自动给所有人发送短信
	 */
	public void autoSendLoanMessage(){
		Loan loan= getInstance();
		loanService.autoSendLoanMessage(loan);
		}
	public int isTuser(){
		try {
			String loginid=loginUser.getLoginUserId();
			if(loginid==null){
				return 0;
			}
			User	user = userService.getUserById(loginid);
			if(user.getId().equals(safeLoanService.getTUSER())){
				return 1;
			}
		}catch (UserNotFoundException e) {
			FacesUtil.addErrorMessage("用户未登录！");
			e.printStackTrace();
		}
		return 0;
	}
	
	
	public Double getGpsCoordinate_x() {
		if(gpsCoordinate_x == null){
			String coordinate = getRemoteCoordinate();
			String[] xy = coordinate.split(",");
			gpsCoordinate_x = Double.valueOf(xy[0]);
			gpsCoordinate_y = Double.valueOf(xy[1]);
		}
		return gpsCoordinate_x;
	}

	public void setGpsCoordinate_x(Double gpsCoordinate_x) {
		this.gpsCoordinate_x = gpsCoordinate_x;
	}

	public Double getGpsCoordinate_y() {
		if(gpsCoordinate_y == null){
			String coordinate = getRemoteCoordinate();
			String[] xy = coordinate.split(",");
			gpsCoordinate_x = Double.valueOf(xy[0]);
			gpsCoordinate_y = Double.valueOf(xy[1]);
		}
		return gpsCoordinate_y;
	}

	public void setGpsCoordinate_y(Double gpsCoordinate_y) {
		this.gpsCoordinate_y = gpsCoordinate_y;
	}
	
	/**
	 * 通过webservice调用GPS平台获取经度纬度
	 * @author majie
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @date 2016年9月20日 上午10:37:38
	 */
	private String getRemoteCoordinate(){
		String coordinate = "0,0";
		String imei = getInstance().getGpsCode();
		if(imei == null || imei.trim().length() !=15)
			return coordinate;
		String result = gpsService.getRemoteCoordinate(imei);
		if(result != null)
			return result;
		return coordinate;
	}
}
