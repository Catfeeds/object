package com.zw.p2p.bankcard.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.service.HuiFuUserService;
import com.zw.p2p.bankcard.BankCardConstants;
import com.zw.p2p.bankcard.BankCardConstants.BankCardPayType;
import com.zw.p2p.bankcard.BankCardConstants.BankCardStatus;
import com.zw.p2p.bankcard.BankCardConstants.BankCardUnbindApproveStatus;
import com.zw.p2p.bankcard.model.BankCard;
import com.zw.p2p.bankcard.service.BankCardService;
import com.zw.p2p.bankcard.service.BankCardUnbindApproveService;
import com.zw.p2p.user.service.WithdrawCashService;
@Service("bankCardService")
public class BankCardServiceImp implements BankCardService {
	@Resource
	private HibernateTemplate ht;
	@Resource
	private WithdrawCashService withdrawCashService ;
	@Resource
	private BankCardService bankCardService ;
	@Resource
	private UserBillService userBillService ;
	@Resource
	private HuiFuUserService huiFuUserService ;
	@Resource
	private BankCardUnbindApproveService bankCardUnbindApproveService;
	@Override
	public List<BankCard> getBankCardsByUserId(String userId) {
		 List<BankCard> bankCardListbyLoginUser=null;;
		if (bankCardListbyLoginUser == null) {
			User loginUser = ht.get(User.class, userId);
			if (loginUser == null) {
				FacesUtil.addErrorMessage("用户未登录");
				return null;
			}
			bankCardListbyLoginUser=getBankCardsByUserIdNoLogin(userId);
		}
		return bankCardListbyLoginUser;
	}
	@Override
	public List<BankCard> getBankCardsByUserIdNoLogin(String userId) {
		 List<BankCard> bankCardListbyLoginUser= ht.find(
					"from BankCard where user.id = ? and status=?", new String[]{userId,BankCardStatus.BINDING});
		return bankCardListbyLoginUser;
	}
	@Override
	public int userShowBankCardStatus(String userId) {
		int result=1;
		String loginUserId=userId;
		//获得余额
		double balance=userBillService.getBalance(loginUserId);
		//获得最后一条解绑申请的状态
		int bindApprovenums=bankCardUnbindApproveService.countApproveByUser(loginUserId, BankCardUnbindApproveStatus.CS.getIndex());//0审核中 1审核同意 2审核驳回bankCardUnbindApproveService
		//当前有效银行卡数量
		int cardNum= bankCardService.getBankCardsByUserId(loginUserId).size();
		if(cardNum==0){
			result=7;//无卡提示去新增卡
		}else if(withdrawCashService.getApproving(loginUserId).size()>0){
			result=2;
		}else if(balance>0&&bindApprovenums==0){
			result=1;
		}else if(balance==0&&withdrawCashService.getApproving(loginUserId).size()==0){
			result=3;
		}else if(bindApprovenums!=0){
			result=4;
		}
		
		return result;
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void bindBankCard(BankCard bankCard) {
		// TODO Auto-generated method stub
		ht.save(bankCard);
	}
	@Override
	public void delCardByCardId(String cardNo) {
		// TODO Auto-generated method stub
		String queryString="from BankCard where  cardNo=?";
		List<BankCard> list=ht.find(queryString, cardNo);
		if(null!=list&&list.size()>0){
			BankCard bk=list.get(0);
			bk.setStatus(BankCardStatus.DELETED);
			ht.update(bk);
		}
	}
	
	/**
	 * 批量删除银行
	 * @param cardNo
	 */
	public void delCard(List<BankCard> bankCards){
		if(bankCards != null && bankCards.size()>0){
			for(BankCard bankCard : bankCards){
				bankCard.setStatus(BankCardStatus.DELETED);
				ht.update(bankCard);
			}
		}
	}
	
	@Override
	public List<BankCard> getHuiFuFastBankCardsByUserId(String userId) {
		 List<BankCard> bankCardListbyLoginUser=null;;
		if (bankCardListbyLoginUser == null) {
			User loginUser = ht.get(User.class, userId);
			if (loginUser == null) {
				FacesUtil.addErrorMessage("用户未登录");
				return null;
			}
			bankCardListbyLoginUser = ht.find(
					"from BankCard where user.id = ? and status=? and paytype=?", new String[]{loginUser.getId(),BankCardStatus.BINDING,BankCardPayType.QUICKPAY});
		}
		return bankCardListbyLoginUser;
	}
	@Override
	public void synBankCadrByUser(User user) {
		// TODO Auto-generated method stub
		if(null!=user&&null!=user.getUsrCustId()){
			//先清空用户卡，
			Map<String,BankCard > locBankCard=new HashMap<String,BankCard >();
			 List<BankCard> listcard=getBankCardsByUserIdNoLogin(user.getId());
			 for (BankCard bankCard : listcard) {
				 locBankCard.put(bankCard.getCardNo(), bankCard);
			}
			 //同步插入汇付用户卡
			JSONArray jsarry= huiFuUserService.QueryCardInfo(user.getUsrCustId());
			Map<String,JSONObject > huifuBankCard=new HashMap<String,JSONObject >();
			int length=jsarry.size();
			for (int i=0;i<length;i++) {
				JSONObject jsonObject=jsarry.getJSONObject(i);
				if(!locBankCard.containsKey(jsonObject.getString("CardId"))){
					//本地没有汇付的卡则新增上  缺的增加
					addBankCard(jsonObject, user);
				}
				huifuBankCard.put(jsonObject.getString("CardId"), jsonObject);
			}
			 for (BankCard bankCard : listcard) {
				if( !huifuBankCard.containsKey(bankCard.getCardNo())){
					delCardByCardId(bankCard.getCardNo());
				} 
			}
		}
	
	}
	private void addBankCard(JSONObject jsonObject,User user){
		BankCard bankCard=new BankCard();
		bankCard.setBankNo(jsonObject.getString("BankId"));
		bankCard.setId(IdGenerator.randomUUID());
		bankCard.setStatus(BankCardConstants.BankCardStatus.BINDING);
		bankCard.setCardNo(jsonObject.getString("CardId"));
		bankCard.setUser(user);
		bankCard.setTime(new Date());
		if("Y".equals(jsonObject.getString("ExpressFlag"))){
		//	快捷
			bankCard.setPayType(BankCardConstants.BankCardPayType.QUICKPAY);
		}else{
			//取现卡
			bankCard.setPayType(BankCardConstants.BankCardPayType.QUXIAN);
		}
		bankCardService.bindBankCard(bankCard);
	}
	
	
	
	
}
