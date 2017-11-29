package com.zw.p2p.bankcard.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.invite.InviteService;
import com.zw.archer.invite.model.invite;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.util.CommonUtil;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.service.HuiFuUserService;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.BankCardConstants.BankCardStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveResouse;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApprovetype;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.model.BankCardUnbindApprove;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.bankcard.service.BankCardUnbindApproveService;
import com.zw.p2p.message.service.SmsService;
import com.zw.p2p.user.service.RechargeService;

@Component
@Scope(ScopeType.VIEW)
public class BankCardHome extends EntityHome<BankCard> implements java.io.Serializable {

	@Logger
	static Log log;
	@Resource
	HibernateTemplate ht;

	@Resource
	private LoginUserInfo loginUserInfo;
	@Resource
	private BankCardUnbindApproveService bankCardUnbindApproveService;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private BankCardService bankCardService;
	@Resource
	private SmsService smsService;
	@Resource
	private InviteService inviteService;
	@Resource
	private HuiFuUserService huiFuUserService;
	private String sendyzm = "";// 发送出去的验证码
	private String yanzhengma;
	private String addtype;
	
	public String getAddtype() {
		return addtype;
	}

	public void setAddtype(String addtype) {
		this.addtype = addtype;
	}

	public String getYanzhengma() {
		return yanzhengma;
	}

	public void setYanzhengma(String yanzhengma) {
		this.yanzhengma = yanzhengma;
	}

	@Override
	@Transactional(readOnly = false)
	public String save() {
		String loginUserId = loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class, loginUserId);
		if (loginUser == null) {
			FacesUtil.addErrorMessage("用户未登录");
			return null;
		}
		if(null!=getAddtype()&&getAddtype().endsWith("change")){
			if (bankCardService.userShowBankCardStatus(loginUserId) != 3) {
				FacesUtil.addErrorMessage("当前状态无法修改银行卡");
				return "pretty:mybankcardchange";
			}
		}
		if ((null!=getAddtype()&&getAddtype().endsWith("change"))&&(null == yanzhengma || yanzhengma.length() == 0 || !yanzhengma.equals(sendyzm))) {
			FacesUtil.addErrorMessage("请输入正确验证码");
			return null;
		}
		if (StringUtils.isEmpty(this.getInstance().getId())) {
			getInstance().setId(IdGenerator.randomUUID());
			getInstance().setUser(loginUser);
			getInstance().setStatus(BankCardConstants.BankCardStatus.BINDING);
			getInstance().setBank(rechargeService.getBankNameByNo(getInstance().getBankNo()));
		} else {
			this.setId(getInstance().getId());
		}

		if (getInstance().getName() == null || getInstance().getName().trim().equals(""))
			getInstance().setName("分行");

		if (getInstance().getBranch() == null || getInstance().getBranch().trim().equals(""))
			getInstance().setBranch("支行");

		getInstance().setTime(new Date());
		// 将旧卡置为删除状态
		// 获得已绑定的卡
		List<BankCard> list = bankCardService.getBankCardsByUserId(loginUserId);
		for (BankCard bankCard : list) {
			bankCard.setStatus(BankCardStatus.DELETED);
			getBaseService().update(bankCard);
		}

		super.save(false);
		this.setInstance(null);
		if(null!=getAddtype()&&getAddtype().endsWith("change")){
			BankCardUnbindApprove bcuba=new BankCardUnbindApprove();
			bcuba.setId(IdGenerator.randomUUID());
			bcuba.setUserid(loginUser);
			bcuba.setCommitTime(new Date());
			bcuba.setStatus(BankCardUnbindApproveStatus.TG.getIndex());
			bcuba.setResouse(BankCardUnbindApproveResouse.PC.getIndex());
			bcuba.setApprovetype(BankCardUnbindApprovetype.ZD.getIndex());
			bankCardUnbindApproveService.creatApprove(bcuba);
		}
	
		
		FacesUtil.addInfoMessage("保存银行卡成功！");
		if (StringUtils.isNotEmpty(super.getSaveView())) {
			return super.getSaveView();
		}
		
		//被邀请人绑定银行卡后修改进行状态给予邀请人奖励
		String hql="from invite i where i.touserid=?";
		List<invite> invite=getBaseService().find(hql,loginUserId);
		if(null==invite||invite.size()<=0){
			return "pretty:bankCardList";
		}
		invite in=invite.get(0);
		in.setBandcardstatus(1);
		inviteService.save(in);
		return "pretty:bankCardList";
	}

	@Override
	@Transactional(readOnly = false)
	public String delete() {
		// 银行卡标记为删除状态
		if (bankCardService.getBankCardsByUserId(loginUserInfo.getLoginUserId()).size() == 1) {
			FacesUtil.addErrorMessage("请至少保留一张银行卡");
			return null;
		}
		this.getInstance().setStatus(BankCardStatus.DELETED);
		getBaseService().update(this.getInstance());
		return "pretty:bankCardList";
	}

	@Transactional(readOnly = false)
	public String delete(String bankCardId) {
		BankCard bc = getBaseService().get(BankCard.class, bankCardId);
		if (bc == null) {
			FacesUtil.addErrorMessage("未找到编号为" + bankCardId + "的银行卡！");
		} else {
			// 银行卡标记为删除状态
		/*	if (bankCardService.getBankCardsByUserId(loginUserInfo.getLoginUserId()).size() == 1) {
				FacesUtil.addErrorMessage("请至少保留一张银行卡");
				return null;
			}*/
			User user=loginUserInfo.getUser();
			JSONObject jso=	huiFuUserService.delCard(user.getUsrCustId(), bc.getCardNo());
				if("000".equals(jso.get("RespCode"))){
					//删除成功
					// 银行卡标记为删除状态
					this.setInstance(bc);
					this.getInstance().setStatus(BankCardStatus.DELETED);
					getBaseService().update(this.getInstance());
					this.setInstance(null);
				}else if("544".equals(jso.get("RespCode"))){
					this.setInstance(bc);
					this.getInstance().setStatus(BankCardStatus.DELETED);
					getBaseService().update(this.getInstance());
					this.setInstance(null);
				}else{
					FacesUtil.addErrorMessage((String) jso.get("RespDesc"));
					return null;
				}
		}
		return "pretty:bankCardList";
	}

	/**
	 * 删除银行卡，资金托管
	 * 
	 * @return
	 */
	public String deleteTrusteeship() {
		throw new RuntimeException("you must override this method!");
	}

	public void initProvince() {
		if (getInstance() == null)
			return;

		if (getInstance().getBankProvince() == null) {
			getInstance().setBankProvince("北京市");
		}
	}

	/**
	 * @Description: TODO(发送验证码)
	 * @author cuihang
	 * @date 2016-3-7 下午2:25:24
	 * @return
	 * @throws Exception
	 */
	public String sendyzms() throws Exception {

		String loginUserId = loginUserInfo.getLoginUserId();
		User loginUser = getBaseService().get(User.class, loginUserId);
		sendyzm = CommonUtil.getRandomString(6);
		smsService.sendCM("尊敬的用户，您的验证码为："+sendyzm+"如非本人操作请忽略。", loginUser.getMobileNumber());
		FacesUtil.addInfoMessage("发送成功,请注意查收！");
		return null;
	}
	//获得汇付用户绑卡页面
		public void turnBindCard(){
			try {
				User user=loginUserInfo.getUser();
				String html = huiFuUserService.bindCard(user.getUsrCustId());
				FacesUtil.getHttpServletResponse().setContentType("text/html;charset=UTF-8");
				FacesUtil.getHttpServletResponse().getWriter().print(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/**
		 * 
		 * 是否可以去绑定取现卡
		 * 可以返回true 不可以返回false
		 * @return
		 */
		public boolean canBindQXCard(){
			String loginUserId = loginUserInfo.getLoginUserId();
			 List<BankCard> list=	bankCardService.getHuiFuFastBankCardsByUserId(loginUserId);
			 if(null!=list&&list.size()>0){
				 return false;
			 }

			return true;
		}
}
