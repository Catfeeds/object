package com.zw.p2p.bankcard.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.collection.AbstractPersistentCollection;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.banner.model.BannerPicture;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.util.CommonUtil;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.p2p.bankcard.BankCardConstants.BankCardStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveResouse;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApprovetype;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.model.BankCardUnbindApprove;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.bankcard.service.BankCardUnbindApproveService;
import com.zw.p2p.message.service.SmsService;

@Component
@Scope(ScopeType.VIEW)
public class BankCardUnbindApproveHome extends EntityHome<BankCardUnbindApprove> implements java.io.Serializable {
	public final static String ncuaListUrl = "/admin/bankcard/bankCardUnbindApproveList";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Resource
	private HibernateTemplate ht;
	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	private BankCardService bankCardService ;
	@Resource
	private BankCardUnbindApproveService bankCardUnbindApproveService;
	@Resource
	private ConfigService configService;
	@Resource
	private SmsService smsService;
	private List<BannerPicture> idCardsImg;// 身份证照片
	private List<BannerPicture> bankProve;// 银行证明
	private List<BannerPicture> statementText;// 手写解绑声明
	private String yanzhengma;
	private String sendyzm="";//发送出去的验证码 

	public String getYanzhengma() {
	return yanzhengma;
	}

	public void setYanzhengma(String yanzhengma) {
		this.yanzhengma = yanzhengma;
	}

	public List<BannerPicture> getIdCardsImg() {
		return idCardsImg;
	}

	public void setIdCardsImg(List<BannerPicture> idCardsImg) {
		this.idCardsImg = idCardsImg;
	}

	public List<BannerPicture> getBankProve() {
		return bankProve;
	}

	public void setBankProve(List<BannerPicture> bankProve) {
		this.bankProve = bankProve;
	}

	public List<BannerPicture> getStatementText() {
		return statementText;
	}

	public void setStatementText(List<BannerPicture> statementText) {
		this.statementText = statementText;
	}

	@Override
	@Transactional(readOnly = false)
	public String save() {
		String loginUserId=loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class,
				loginUserId);
		if (loginUser == null) {
			FacesUtil.addErrorMessage("用户未登录");
			return null;
		}
		if(bankCardUnbindApproveService.countApproveByUser(loginUserId, BankCardUnbindApproveStatus.CS.getIndex())>0){
			FacesUtil.addErrorMessage("有正在审核中的申请,无法重复提交");
			return null;
		}
		if(null==yanzhengma||yanzhengma.length()==0||!yanzhengma.equals(sendyzm)){
			FacesUtil.addErrorMessage("请输入正确验证码");
			return null;
		}
		
		BankCardUnbindApprove bcuba = getInstance();
		bcuba.setId(IdGenerator.randomUUID());
		setPics(bcuba);
		if(bcuba.getIdCardsImgStr().length()==0){
			FacesUtil.addErrorMessage("请上传手持身份证的照片");
			return null;
		}
		if(bcuba.getBankProveStr().length()==0){
			FacesUtil.addErrorMessage("请上传银行开具的证明");
			return null;
		}
		if(bcuba.getStatementTextStr().length()==0){
			FacesUtil.addErrorMessage("请上传手写并签字的解绑声明");
			return null;
		}
		
		bcuba.setUserid(loginUser);
		bcuba.setCommitTime(new Date());
		bcuba.setStatus(BankCardUnbindApproveStatus.CS.getIndex());
		bcuba.setResouse(BankCardUnbindApproveResouse.PC.getIndex());
		bcuba.setApprovetype(BankCardUnbindApprovetype.SD.getIndex());
		bankCardUnbindApproveService.creatApprove(bcuba);
		this.setInstance(null);
		FacesUtil.addInfoMessage("申请提交成功！");
		
		
		return "pretty:mybankcardchange";
	}

	private void setPics(BankCardUnbindApprove bca) {
		List<BannerPicture> listid = getIdCardsImg();
		if (listid != null && !(listid instanceof AbstractPersistentCollection)) {

			bca.setIdCardsImgStr(getpicsStr(listid));
		}
		List<BannerPicture> listbp = getBankProve();
		if (listbp != null && !(listbp instanceof AbstractPersistentCollection)) {
			bca.setBankProveStr(getpicsStr(listbp));
		}
		List<BannerPicture> listst = getStatementText();
		if (listst != null && !(listst instanceof AbstractPersistentCollection)) {
			bca.setStatementTextStr(getpicsStr(listst));
		}
	}

	private String getpicsStr(List<BannerPicture> listid) {
		StringBuffer result = new StringBuffer();
		int length = listid.size();
		int num = 0;
		for (BannerPicture lip : listid) {
			num++;
			if (num != length) {
				result.append(lip.getPicture()).append(",");
			} else {
				result.append(lip.getPicture());
			}

		}

		return result.toString();
	}
	public String[] idcardsimg(){
		return getInstance().getIdCardsImgStr().split(",");
	}
	public String[] bankProvesimg(){
		return getInstance().getBankProveStr().split(",");
	}
	public String[] statementTextsimg(){
		return getInstance().getStatementTextStr().split(",");
	}
	/**
	*@Description: TODO(审核通过) 
	* @author cuihang   
	*@date 2016-3-7 上午11:12:07
	 */
	@Transactional(readOnly = false)
	public String agree(){
		String loginUserId=loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class,
				loginUserId);
		BankCardUnbindApprove bcuaApprove=getInstance();
		
		bcuaApprove.setApproveUser(loginUser);
		bcuaApprove.setApprovetype(BankCardUnbindApprovetype.SD.getIndex());
		String result=bankCardUnbindApproveService.ApproveAgree(bcuaApprove);
		if(result.length()>0){
			FacesUtil.addErrorMessage(result);
			return null;
		}
		 List<BankCard> list=	bankCardService.getBankCardsByUserId(bcuaApprove.getUserid().getId());
			for (BankCard bankCard : list) {
				bankCard.setStatus(BankCardStatus.DELETED);
				getBaseService().update(bankCard);
			}
			try {
				smsService.sendCM("您在优学金服提交的解绑银行卡申请已被通过，为了您的资金安全，请尽快登录优学金服绑定新的银行卡！", bcuaApprove.getUserid().getMobileNumber());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		FacesUtil.addInfoMessage("审核成功！");
		return FacesUtil.redirect(ncuaListUrl);
	}
	/**
	 * 
	*@Description: TODO(拒绝) 
	* @author cuihang   
	*@date 2016-3-7 上午11:12:25
	 */
	@Transactional(readOnly = false)
	public String refuse(){
		String loginUserId=loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class,
				loginUserId);
		BankCardUnbindApprove bcuaApprove=getInstance();
		if(bcuaApprove.getApproveDes() == null || bcuaApprove.getApproveDes().trim().length()==0){
			FacesUtil.addErrorMessage("请填写拒绝原因");
			return null;
		}
		bcuaApprove.setApproveUser(loginUser);
		bcuaApprove.setApprovetype(BankCardUnbindApprovetype.SD.getIndex());
		String result=bankCardUnbindApproveService.ApproveResuse(bcuaApprove);
		if(result.length()>0){
			FacesUtil.addErrorMessage(result);
			return null;
		}
		try {
			smsService.sendCM("您在优学金服提交的解绑银行卡申请已被拒绝，拒绝原因为"+bcuaApprove.getApproveDes()+" ，您可以登录优学金服重新提交申请！", bcuaApprove.getUserid().getMobileNumber());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FacesUtil.addInfoMessage("审核成功！");
		return FacesUtil.redirect(ncuaListUrl);
	}
	/**
	*@Description: TODO(发送验证码) 
	* @author cuihang   
	*@date 2016-3-7 下午2:25:24 
	*@return
	*@throws Exception
	 */
	public String sendyzms() throws Exception{
		
		String loginUserId=loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class,
				loginUserId);
		if(bankCardUnbindApproveService.countApproveByUser(loginUserId, BankCardUnbindApproveStatus.CS.getIndex())>0){
			FacesUtil.addErrorMessage("有正在审核中的申请,无法重复提交");
			return null;
		}
		sendyzm=CommonUtil.getRandomString(6);
		smsService.sendCM("尊敬的用户，您的验证码为："+sendyzm+"如非本人操作请忽略。", loginUser.getMobileNumber());
		FacesUtil.addInfoMessage("发送成功,请注意查收！");
		return null;
	}
	public String getImgUrl(int resouse){
		if(resouse==BankCardUnbindApproveResouse.PC.getIndex()){
			return  FacesUtil.getContextPath();
		}
		if(resouse==BankCardUnbindApproveResouse.APP.getIndex()){
			return configService.getConfigValue("serverurlAPP");
		}
		if(resouse==BankCardUnbindApproveResouse.WX.getIndex()){
			return  configService.getConfigValue("serverurlWECHAT");
		}
		return "";
	}
}
