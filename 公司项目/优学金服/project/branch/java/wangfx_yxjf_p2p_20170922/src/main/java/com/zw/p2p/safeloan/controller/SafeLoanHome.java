package com.zw.p2p.safeloan.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.config.model.Config;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.node.model.Node;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.impl.UserBO;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateStyle;
import com.zw.core.util.DateUtil;
import com.zw.p2p.message.service.AutoMsgService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanStatus;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanUnit;
import com.zw.p2p.safeloan.model.SafeLoan;
import com.zw.p2p.safeloan.service.SafeLoanService;

/**
* @Description: TODO(无忧宝产品) 
* @author cuihang   
* @date 2016-1-8 下午12:23:33
 */
@Component
@Scope(ScopeType.VIEW)
public class SafeLoanHome extends EntityHome<SafeLoan> implements Serializable {
	/**
	 * 后台借款列表
	 */
	public final static String loanListUrl = "/admin/safeLoan/safeLoanList";
	
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	private SafeLoanService safeLoanService;
	@Resource
	ConfigService configService;
	@Resource
	private HibernateTemplate ht;
	@Resource
	private SmsService smsService;
	@Resource
	private UserBO userBO;
	@Resource
	private AutoMsgService autoMsgService;
	@Logger
	Log log;
	@Resource
	private DictUtil dictUtil;
	
	
	public SafeLoanHome(){
		setUpdateView(FacesUtil.redirect(loanListUrl));
		setDeleteView(FacesUtil.redirect(loanListUrl));
	}
	/**
	 * @Description: TODO(新增无忧宝产品)
	 * @author cuihang
	 * @date 2016年1月12日 16:44:10
	 * @param money
	 * @param record
	 * @return
	 */
	@Transactional(readOnly = false)
	public synchronized String save() {
		SafeLoan sl=getInstance();
		
		if(sl.getId()==null||sl.getId().length()==0){
			sl.setId(safeLoanService.createSafeLoanId());
		}else{
			if(sl.getStatus()!=SafeLoanConstants.SafeLoanStatus.CS.getIndex()){
				FacesUtil.addInfoMessage(SafeLoanConstants.SafeLoanStatus.getName(sl.getStatus())+"状态无法编辑");
				return null;
			}
		}
		if(null==sl.getName()||sl.getName().length()==0){
			FacesUtil.addErrorMessage("请填写无忧保名称");
			return null;
		}
		if(null==sl.getDeadline()||sl.getDeadline()<1){
			FacesUtil.addErrorMessage("锁定期应大于0");
			return null;
		}
		if(null==sl.getDeadhours()||sl.getDeadhours()<1){
			FacesUtil.addErrorMessage("投资期应大于0");
			return null;
		}
		if(null==sl.getRate()){
			FacesUtil.addErrorMessage("年化收益不能为空");
			return null;
		}
		if(sl.getDescription().getId()==null){
			FacesUtil.addErrorMessage("请选择产品描述");
			return null;
		}
		if(sl.getNormalPro().getId()==null){
			FacesUtil.addErrorMessage("请选择常见问题");
			return null;
		}
		if(null==sl.getLoanMoney()){
			FacesUtil.addErrorMessage("投资金额不能为空");
			return null;
		}
		if(null==sl.getMinInvestMoney()){
			FacesUtil.addErrorMessage("起投金额不能为空");
			return null;
		}
		if(null==sl.getIncMoney()){
			FacesUtil.addErrorMessage("递增金额不能为空");
			return null;
		}
		if(null==sl.getMaxInvestMoney()){
			FacesUtil.addErrorMessage("每笔投资上限不能为空");
			return null;
		}
		if(sl.getContract().getId()==null){
			FacesUtil.addErrorMessage("请选择服务协议");
			return null;
		}
		try {
			if(!safeLoanService.isPassHalf(sl.getLoanMoney())){
				FacesUtil.addErrorMessage("无忧保金额不能大于当前平台所有未满标的剩余金额总和的"+configService.getConfigValue(ConfigConstants.Schedule.NEWSAFELOANPER)+"%");
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			FacesUtil.addInfoMessage(e.getMessage());
			return null;
		}
		sl.setApproveBeginTime(new Date());
		Date approveEndTime=new Date(new Date().getTime() + sl.getDeadhours() * 60l * 60*1000 );
		sl.setApproveEndTime(approveEndTime);
		sl.setCommitTime(approveEndTime);
		Date endTime=getEndTime(sl.getDeadline(), sl.getUnit());
		sl.setEndTime(endTime);
		sl.setCreatUser(loginUserInfo.getUser());
		sl.setEnableStatus(1);//初始可用
		if(sl.getFine()==null){
			sl.setFine(0d);
		}
		sl.setMoney(0d);//初始0
		sl.setStatus(0);
		setInstance(sl);
		super.save();
		return FacesUtil.redirect(loanListUrl);
	}
	@Override
	protected SafeLoan createInstance() {
		// TODO Auto-generated method stub
		SafeLoan safeLoan = new SafeLoan();
		safeLoan.setDescription(new Node());
		safeLoan.setNormalPro(new Node());
		safeLoan.setContract(new Node());
		return safeLoan;
	}
	/**
	 * 删除无忧宝产品-只针对待审批状态的产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:52:08
	 * @param safeLoanId
	 * @return
	 */
	@Transactional(readOnly = false)
	public synchronized String delSafeLoan(String safeLoanId){
		
		try {
			String result = safeLoanService.deleteSafeLoan(safeLoanId);
			
			if("0".equals(result)){
				FacesUtil.addInfoMessage("删除成功");
			} else {
				FacesUtil.addErrorMessage("删除失败："+result);
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("删除失败："+e.getMessage());
		}
		
		return FacesUtil.redirect(loanListUrl);
	}
	
	/**
	 * 审核无忧宝产品-只针对待审批状态的产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:52:08
	 * @param safeLoanId
	 * @return
	 */
	@Transactional(readOnly = false)
	public synchronized String verify(){
		
		try {
			SafeLoan sl=getInstance();
			try {
				if(!safeLoanService.isPassHalf(sl.getLoanMoney())){
					FacesUtil.addInfoMessage("无忧保金额不能大于当前平台所有未满标的剩余金额总和的"+configService.getConfigValue(ConfigConstants.Schedule.NEWSAFELOANPER)+"%");
					return null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				FacesUtil.addInfoMessage(e.getMessage());
				return null;
			}
			String result = safeLoanService.verifySafeLoan(sl);
			
			if("0".equals(result)){
				FacesUtil.addInfoMessage("审核通过");
				SendMessage();
				
			} else {
				FacesUtil.addErrorMessage("审核失败："+result);
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("审核失败："+e.getMessage());
		}
		
		return FacesUtil.redirect(loanListUrl);
	}
	/**
	 * 初审通过后自动给所有实名认证用户发送短信
	 */
	public void SendMessage(){
		boolean bAutoSend=false;
		Config config = ht.get(Config.class, "auto_send_loan_message");
		if (config != null && (config.getValue().equals("1"))){
				bAutoSend=true;
		}
		if (!bAutoSend)
			return;
		SafeLoan loan= getInstance();
		/*DecimalFormat df=new DecimalFormat("0");
		DecimalFormat df2=new DecimalFormat("0.##");
		String msg=String.format("亲，优学金服今天发布了一个金额%s万、投资期%d、利率%s%%、筹集期 %s-%s的无忧宝，等您来投资噢。",
				df.format(loan.getLoanMoney()/10000),loan.getDeadline(), df2.format(loan.getRate()),
				DateUtil.DateToString(loan.getApproveBeginTime(),"yyyy年MM月dd日"),
				DateUtil.DateToString(loan.getApproveEndTime(),"yyyy年MM月dd日"));*/
		Set<String> userMobiles= userBO.getUserWithMobileNumber();
		for (String mobile:userMobiles){
			try {
				/*smsService.sendMsg(msg, mobile);*/
				 User user=userBO.getUserByMobileNumber(mobile);
			     Map<String,String> params = new HashMap<String, String>();
				 params.put("safeLoanName", loan.getName());
			     params.put("money", loan.getLoanMoney()/10000+"");
				 params.put("rate", loan.getRate()+"");
				 params.put("deadline", loan.getDeadline()+SafeLoanUnit.getName(loan.getUnit()));
				 //SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
				 params.put("approveBeginTime", DateUtil.DateToString(loan.getApproveBeginTime(), DateStyle.YYYY_MM_DD_CN));
				 params.put("approveEndTime", DateUtil.DateToString(loan.getApproveEndTime(), DateStyle.YYYY_MM_DD_CN));
				 autoMsgService.sendMsg(user, "safeLoan_FB", params);
				 Thread.sleep(50);
			}catch (Exception e){
				log.info("通知短信发送失败,手机号:"+ mobile);
				e.printStackTrace();
			}
		}
		       
				
	}
	/**
	 * 审核无忧宝产品-只针对待审批状态的产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:52:08
	 * @param safeLoanId
	 * @return
	 */
	@Transactional(readOnly = false)
	public synchronized String disverify(){
		
		try {
			SafeLoan sl=getInstance();
			
			String result = safeLoanService.disverifySafeLoan(sl);
			
			if("0".equals(result)){
				FacesUtil.addInfoMessage("审核不通过");
			} else {
				FacesUtil.addErrorMessage("审核失败："+result);
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("审核失败："+e.getMessage());
		}
		
		return FacesUtil.redirect(loanListUrl);
	}
	
	/**
	 * 复核无忧宝产品-只针对复核中状态的产品
	 * 
	 * @author zhenghaifeng
	 * @date 2016-1-20 上午10:52:08
	 * @param safeLoanId
	 * @return
	 */
	@Transactional(readOnly = false)
	public synchronized String recheck(){
		
		try {
			SafeLoan sl=getInstance();
			// 投资期结束时间，校验，必须大于当前时间
			Date approveEndTime = sl.getApproveEndTime();
			if(approveEndTime.compareTo(new Date()) <= 0){
				FacesUtil.addErrorMessage("复核失败：投资期结束时间必须大于当前时间");
				return FacesUtil.redirect(loanListUrl);
			}
			
			String result = safeLoanService.recheckSafeLoan(sl);
			
			if("0".equals(result)){
				FacesUtil.addInfoMessage("复核通过");
			} else {
				FacesUtil.addErrorMessage("复核失败："+result);
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage("复核失败："+e.getMessage());
		}
		
		return FacesUtil.redirect(loanListUrl);
	}
	/**
	 * 无忧宝完成百分比
	 * @return
	 */
	public double completeRate(){
		SafeLoan sl=getInstance();
		
		return new BigDecimal(sl.getMoney()).divide(new BigDecimal(sl.getLoanMoney()),4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	/**
	*@Description: TODO(获得最新的投资中的无忧宝产品信息) 
	* @author cuihang   
	*@date 2016-3-24 下午12:46:02 
	*@return
	 */
	private double compleRate=0d;
	
	public double getCompleRate() {
		return compleRate;
	}
	public void setCompleRate(double compleRate) {
		this.compleRate = compleRate;
	}
	public SafeLoan getFirstSafeLoan(){
		SafeLoan sl=safeLoanService.findSafeLoanById(configService.getConfigValue("indexreco"));
		if(null==sl||sl.getStatus()!=SafeLoanStatus.TZZ.getIndex()){
			 sl=safeLoanService.getFirstSafeLoan();
		}
		if(null!=sl){
			compleRate=new BigDecimal(sl.getMoney()).divide(new BigDecimal(sl.getLoanMoney()),4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		return sl;
	}
	/**
	*@Description: TODO(处理后的无忧宝描述) 
	* @author cuihang   
	*@date 2016-4-23 下午4:24:11 
	*@return
	 */
	public String safeloandescription(){
		String str=getInstance().getDescription().getNodeBody().getBody();
		String minInvestMoney=getInstance().getMinInvestMoney()+"";//起投金额
		String incMoney=getInstance().getIncMoney()+"";//递增金额
		String maxInvestMoney=getInstance().getMaxInvestMoney()+"";//最大投资金额
		String rate=getInstance().getRate()+"";//年化收益
		String deadline=getInstance().getDeadline()+"";//封闭期
		
		String repMinInvestMoney="#minInvestMoney#";//起投金额
		String repIncMoney="#incMoney#";//递增金额
		String repMaxInvestMoney="#maxInvestMoney#";//最大投资金额
		String repRate="#rate#";//年化收益
		String repDeadline="#deadline#";//封闭期
		str=str.replace(repMinInvestMoney, minInvestMoney);
		str=str.replace(repIncMoney, incMoney);
		str=str.replace(repMaxInvestMoney, maxInvestMoney);
		str=str.replace(repRate, rate);
		str=str.replace(repDeadline, deadline);
		return str;
	}
	private Date getEndTime(int deadline,String unit){
		if(SafeLoanUnit.MONTH.getIndex().equals(unit)){
			//按月算
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH,deadline);
			return c.getTime();
		}else if(SafeLoanUnit.DAY.getIndex().equals(unit)){
			return new Date(new Date().getTime()+deadline*1000L*60*60*24);
		}
		else{
			return null;
		}
		
	}
}
