package com.zw.huifu.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.core.bean.ZwJson;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.ArithUtil;
import com.zw.core.util.DateUtil;
import com.zw.core.util.IdGenerator;
import com.zw.huifu.service.HuiFuHttpUtilService;
import com.zw.huifu.service.HuiFuLoanService;
import com.zw.huifu.service.HuiFuMoneyService;
import com.zw.huifu.util.HuiFuConstants;
import com.zw.huifu.util.HuiFuHttpUtil;
import com.zw.huifu.util.OrderNoService;
import com.zw.p2p.coupons.model.Coupons;
import com.zw.p2p.invest.InvestConstants;
import com.zw.p2p.invest.exception.InvestException;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.LoanConstants.RepayStatus;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.loan.model.LoanType;
import com.zw.p2p.repay.RepayConstants.RepayType;
import com.zw.p2p.repay.RepayConstants.RepayUnit;
import com.zw.p2p.repay.model.LoanRepay;

@Service("HuiFuLoanService")
public class HuiFuLoanServiceImpl implements HuiFuLoanService {
	@Resource
	HibernateTemplate ht;
	@Logger
	static Log log;
	@Resource
	HuiFuMoneyService huiFuMoneyService;
	@Resource
	ConfigService configService;
	@Resource
	private TransferService transferService;
	@Resource
	private UserBillService ubs;
	@Resource
	private HuiFuHttpUtilService huiFuHttpUtilService;
	
	@Override
	public JSONObject AddBidInfo( Loan loan) {
		// TODO Auto-generated method stub
		JSONObject jso=new JSONObject();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		String Version="20";//固定为 20，如版本升级，能向前兼容
		String CmdId="AddBidInfo";// 消息类型 每一种消息类型代表一种交易， 此处为 AddBidInfo
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户客户号 商户的唯一标识
		String ProId=loan.getId();//项目 ID 标的的唯一标识，为英文和数字组合
		String BidName=loan.getName();//标的名称 
		String BidType="02";//标的类型 01：信用   02：抵押 03：债权转让  99：其他
		Double BorrTotAmt=loan.getLoanMoney();		//发标金额 单位为元，精确到分，例如 1000.01
		Double YearRate=loan.getRatePercent();//发标年化利率 百分比，保留 2 位小数，例如 24.55
		Double RetInterest=0d;//应还款总利息  单位为元，精确到分，例如 1000.01 （商户可传根据发标金额和利率计算的值）
		String LastRetDate="";//最后还款日期 格式 yyyymmdd
		String BidStartDate=sdf.format(loan.getVerifyTime());//计划投标开始日期  格式 yyyyMMddHHmmss
		String BidEndDate=sdf.format(loan.getExpectTime());//计划投标截止日期 格式 yyyyMMddHHmmss
		LoanType loanType=loan.getType();
		int deadline=loan.getDeadline();//还款周期
		String repayTimeUnit=loanType.getRepayTimeUnit();//还款单位月或天
		String LoanPeriod="";//借款期限LoanPeriod 例如：XX 天、XX 月、XX 年
		String RetType="";//还款方式 01：一次还本付息    02：等额本金    03：等额本息   04：按期付息到期还本   99：其他
		if (loanType.getRepayType().equals(RepayType.RFCL)) {
			RetType="04";
		} else if (loanType.getRepayType().equals(RepayType.CPM)) {
			RetType="03";
		} else if (loanType.getRepayType().equals(RepayType.RLIO)) {
			RetType="01";
		} else {
			log.error("不支持该还款类型::"+loanType.getRepayType());
			jso.put("msg", "不支持该还款类型::"+loanType.getRepayType());
			return jso;
		}
		String RetDate="";//应还款日期 格式 yyyymmdd
		Double rate =loan.getRate();//还款利率
		if(repayTimeUnit.equals(RepayUnit.MONTH)){
			//月
			LoanPeriod=deadline+"月";
			LastRetDate=sdfymd.format(DateUtil.addMonth(loan.getVerifyTime(), deadline));
			RetDate=sdfymd.format(DateUtil.addMonth(loan.getVerifyTime(), deadline));
			RetInterest=ArithUtil.mul(BorrTotAmt, loan.getDeadline()/12d*rate,2);
		}else if(repayTimeUnit.equals(RepayUnit.DAY)){
			//日
			LoanPeriod=deadline+"天";
			LastRetDate=sdfymd.format(DateUtil.addDay(loan.getVerifyTime(), deadline));
			RetDate=sdfymd.format(DateUtil.addDay(loan.getVerifyTime(), deadline));
			RetInterest=ArithUtil.mul(BorrTotAmt, loan.getDeadline()/365*rate,2);
		}
		if(RetInterest==0d){
			RetInterest=0.01d;
		}
		//String GuarantType="";//可选 本息保障 01：保本保息 02：保本不保息 03：不保本不保息
		String BidProdType="99";//标的产品类型 01：房贷类 02：车贷类 03：收益权转让类 04：信用贷款类 05：股票配资类 06：银行承兑汇票 07：商业承兑汇票 08：消费贷款类 09：供应链类 99：其他
		Double cardinalNumber=loan.getCardinalNumber();//递增金额
		Double minInvestMoney=loan.getMinInvestMoney();//起投金额
		String LimitMinBidAmt=(int)ArithUtil.div(minInvestMoney,cardinalNumber)+"";//可选 限定最低投标份数 整数 
		Double LimitBidSum=cardinalNumber;//可选 限定每份投标金额 单位为元，精确到分，例如 1000.01
		Double LimitMaxBidSum=loan.getMaxInvestMoney();//可选 限定最多投标金额单位为元，精确到分，例如 1000.01
		Double LimitMinBidSum=minInvestMoney;//可选 单位为元，精确到分，例如 1000.01
		String BorrType="01";//单借款人类型 01：个人 02：企业
		String BorrCustId=loan.getUser().getUsrCustId();//借款人 ID 借款人的唯一标识
		String BorrName=loan.getUser().getRealname();//借款人名称 文本，借款人真实姓名或者借款企业名称
		String BorrCertType="00";//借款人证件类型
		String BorrCertId=loan.getUser().getIdCard();//借款人证件号码 
		String BorrMobiPhone=loan.getUser().getMobileNumber();//借款人手机号
		String BorrPurpose=loan.getLoanPurpose();//借款用途 文本
		String CharSet="UTF-8";//加签验签的时候，商户需告知汇付其系统编码，汇
							//付在验签的时候进行相应的编码转换验签。
							//因字段中有中文，应为：GBK 或者 UTF-8
							//如果是空，默认 UTF-8
		//String RetUrl=request.getScheme() + "://" + request.getServerName() +request.getContextPath()+"/admin/loan/loanList.htm?loanStatus=raising";//实名后跳转的地址
		String BgRetUrl=HuiFuConstants.Config.BACK_URL;//商户后台应答地址--通过后台异步通知，商户网站都应在应答接收页面 输出 RECV_ORD_ID 字样的字符串，表明商户已经 收到该笔交易结果
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		DecimalFormat    df   = new DecimalFormat("######0.00");   
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("ProId", ProId);
		params.put("BidName", BidName);//不加签名
		params.put("BidType", BidType);
		params.put("BorrTotAmt", df.format(BorrTotAmt));
		params.put("YearRate", df.format(YearRate));
		params.put("RetInterest", df.format(RetInterest));
		params.put("LastRetDate", LastRetDate);
		params.put("BidStartDate", BidStartDate);
		params.put("BidEndDate", BidEndDate);
		params.put("LoanPeriod", LoanPeriod);//不加签名
		params.put("RetType", RetType);
		params.put("RetDate", RetDate);
		//params.put("GuarantType", GuarantType);
		params.put("BidProdType", BidProdType);
		params.put("LimitMinBidAmt", LimitMinBidAmt);
		params.put("LimitBidSum", df.format(LimitBidSum));
		params.put("LimitMaxBidSum", df.format(LimitMaxBidSum));
		params.put("LimitMinBidSum", df.format(LimitMinBidSum));
		params.put("BorrType", BorrType);
		params.put("BorrCustId", BorrCustId);
		params.put("BorrName", BorrName);//不加签名
		params.put("BorrCertType", BorrCertType);
		params.put("BorrCertId", BorrCertId);
		params.put("BorrMobiPhone", BorrMobiPhone);
		params.put("BorrPurpose", BorrPurpose);//不加签名
		params.put("CharSet", CharSet);
		//params.put("RetUrl", RetUrl);
		params.put("BgRetUrl", BgRetUrl);
		params.put("MerPriv",IdGenerator.randomUUID());
		try {
		String delStr=	huiFuHttpUtilService.doPost(params);
		 jso=JSONObject.parseObject(delStr);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jso;
	}

	@Override
	public String InitiativeTender(Invest invest,Loan loan) throws InvestException {
		// TODO Auto-generated method stub
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		DecimalFormat    df   = new DecimalFormat("######0.00");  
		String Version="20";
		String CmdId="InitiativeTender";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户客户号 商户的唯一标识
		String OrdId=OrderNoService.getOrderNo();;//由商户的系统生成，必须保证唯一，请使用纯数字
		String OrdDate=sdfymd.format(new Date());//订单日期  
		Double TransAmt=invest.getMoney();//交易金额 
		String UsrCustId=invest.getUser().getUsrCustId();//由汇付生成，用户的唯一性标
		Double MaxTenderRate=new Double(configService.getConfigValue("huifu_maxTenderRate"));//最大投资 手续费率
		String BorrowerDetails="[{\"BorrowerCustId\":\""+loan.getUser().getUsrCustId()+"\",\"BorrowerAmt\":\""+df.format(TransAmt)+"\",\"BorrowerRate\":\""+df.format(1d)+"\",\"ProId\":\""+loan.getId()+"\"}]";//借款人信息支持传送多个借款人信息，使用 json 格式传送，
		//[{"BorrowerCustId":"6000010000000014"，
		//	"BorrowerAmt": "20.01"，
		//	"BorrowerRate":"0.18"，
		//	"ProId":"0000000000000001" }}]
		String IsFreeze="Y";//是否冻结  Y--冻结 N--不冻结
		String FreezeOrdId=OrderNoService.getOrderNo();
		invest.setOrdDate(OrdDate);
		invest.setFreezeOrdId(FreezeOrdId);
		//ht.save(invest);
		String RetUrl=request.getScheme() + "://" + request.getServerName()+HuiFuHttpUtil.getPort(request)+HuiFuHttpUtil.getPort(request) +request.getContextPath()+"/invest/hasInvest/"+OrdId;//实名后跳转的地址
		String BgRetUrl=HuiFuConstants.Config.BACK_URL;//商户后台应答地址--通过后台异步通知，商户网站都应在应答接收页面 输出 RECV_ORD_ID 字样的字符串，表明商户已经 收到该笔交易结果
		
		String MerPriv=loan.getId();
		if(invest.getRateTicket() != null){
			MerPriv = MerPriv + ",r"+invest.getRateTicket().getId();
		}
		if(invest.getCoupons()!=null){
			MerPriv=MerPriv + ",c"+invest.getCoupons().getId();
		}
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("OrdId", OrdId);
		params.put("OrdDate", OrdDate);
		params.put("TransAmt", df.format(TransAmt));
		params.put("UsrCustId", UsrCustId);
		params.put("MaxTenderRate", df.format(MaxTenderRate));
		params.put("BorrowerDetails",BorrowerDetails);
		params.put("IsFreeze", IsFreeze);
		params.put("FreezeOrdId", FreezeOrdId);
		params.put("RetUrl", RetUrl);
		params.put("BgRetUrl", BgRetUrl);
		params.put("MerPriv", MerPriv);
		if(invest.getCoupons()!=null){//用户使用红包投资
			Double couponsMoney = invest.getCoupons().getMoney();//红包代金券金额
			String SubAccId=HuiFuConstants.Config.DIV_ACCT_ID;//子账户账户号  （用户使用 红包代金券 商户扣款账户）
			String ReqExt="{\"Vocher\":{\"AcctId\":\""+SubAccId+"\",\"VocherAmt\":\""+df.format(couponsMoney)+"\"}}";//代金券 用于扩展请求参数，使用 json 格式传送，
			params.put("ReqExt", ReqExt);
		}
		String result="";
		try {
			result = huiFuHttpUtilService.doFormPost(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 根据借款id放款
	 * @author majie
	 * @param loan_id
	 * @return
	 * @date 2016年8月20日 下午7:35:17
	 */
	public ZwJson loanMoneyByLoanId(String loanId){
		ZwJson json = new ZwJson();
		Loan loan = ht.get(Loan.class, loanId);
		ht.evict(loan);
		loan = ht.get(Loan.class, loanId, LockMode.UPGRADE);
		List<Invest> invests = ht.find("from Invest invest where invest.loan.id="+loanId);
		String usrCustId=ht.get(User.class, loan.getUser().getId()).getUsrCustId();
		for (Invest invest : invests) {
			if (invest.getStatus().equals(InvestConstants.InvestStatus.BID_SUCCESS)) {
				ZwJson jsonTemp = loanMoney(invest.getId(),invest.getOrdId(),invest.getMoney(), invest.getUser().getUsrCustId(),usrCustId ,
						loan.getId(), invest.getOrdDate(),invest.getFreezeTrxId(),invests.size(),invest.getCoupons());
				if(!jsonTemp.isSuccess()){
					json.setSuccess(false);
					json.setMsg(jsonTemp.getMsg());
					return json;
				}
			}
		}
		json.setSuccess(true);
		json.setMsg("放款成功");
		return json;
	}

	/**
	 * 放款
	 * @author majie
	 * @param investId 投标id
	 * @param investOrdId 投标订单号
	 * @param transAmt 金额
	 * @param outCustId 出账客户号
	 * @param inCustId 入账客户号
	 * @param unFreezeOrdId 解冻订单号
	 * @param proId  标的id
	 * @param subOrdId 投标订单流水是 SubOrdId
	 * @param subOrdDate  投标订单日期
	 * @param freezeTrxId 冻结唯一标示
	 * @param investNum 投资记录数
	 * @return
	 * @date 2016年8月18日 上午11:47:02
	 */
	 private ZwJson loanMoney(String investId,String investOrdId,Double transAmt,String outCustId,String inCustId,String proId
			,String subOrdDate,String freezeTrxId,int investNum,Coupons coupons){
		ZwJson json = new ZwJson();
		String ordId = OrderNoService.getOrderNo();
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", "20");
		params.put("CmdId", "Loans");
		params.put("MerCustId", HuiFuConstants.Config.MERCUSTID);
		params.put("OrdId",ordId);
		params.put("OrdDate", OrderNoService.getNowDate());
		params.put("OutCustId", outCustId);
		params.put("TransAmt", new DecimalFormat("#.00").format(transAmt));
		params.put("Fee", "0.00");
		params.put("SubOrdId", investOrdId);
		params.put("SubOrdDate", subOrdDate);
		params.put("InCustId", inCustId);
		params.put("IsDefault", "N");
		params.put("IsUnFreeze", "Y");
		params.put("UnFreezeOrdId",ordId);
		params.put("FreezeTrxId",freezeTrxId);
		params.put("BgRetUrl",HuiFuConstants.Config.BACK_URL);
		params.put("MerPriv", investId+","+investNum);//私有域，"传投资id,投资记录数"
		params.put("ReqExt", "{\"ProId\":\""+proId+"\"}");
		params.put("ProId", proId);
		if(coupons!=null){//用户使用红包投资
			Double couponsMoney = coupons.getMoney();//红包代金券金额
			String ReqExt="{\"LoansVocherAmt\":\""+new DecimalFormat("#.00").format(couponsMoney)+"\",\"ProId\":\""+proId+"\"}";//代金券 用于扩展请求参数，使用 json 格式传送，
			params.put("ReqExt", ReqExt);
		}
		try {
			String result = huiFuHttpUtilService.doPost(params);
			JSONObject jsonObj = JSONObject.parseObject(result);
			String respCode = (String) jsonObj.get("RespCode");
			String respDesc = (String) jsonObj.get("RespDesc");
			if("000".equals(respCode)){
				json.setSuccess(true);
			}else{
				json.setSuccess(false);
			}
			json.setMsg(respDesc);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.setSuccess(false);
			json.setMsg("调用放款接口异常");
			return json;
		}
	}

	@Override
	public JSONArray QueryBidInfoDetail(String proId, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		String Version="10";
		String CmdId="QueryBidInfoDetail";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;
		String ProId=proId;
		String PageNum=pageNum+"";
		String PageSize=pageSize+"";
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("ProId", ProId);
		params.put("PageNum", PageNum);
		params.put("PageNum", PageNum);
		params.put("PageSize", PageSize);
		
		JSONArray result=new JSONArray();
		String resultStr;
		try {
			resultStr = huiFuHttpUtilService.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		JSONObject jsonObj = JSONObject.parseObject(resultStr);
		String respCode = (String) jsonObj.get("RespCode");
		if("000".equals(respCode)){
			return 	(JSONArray) JSONArray.parse(jsonObj.getString("BidLogDtoList"));
		}
		return result;
	}

	@Override
	public String creditAssign(String transferApplyId,User user) throws Exception{
		// TODO Auto-generated method stub
		TransferApply transferApply=transferService.getTransferApplyById(transferApplyId);
		double balance=ubs.getBalance(user.getId());
		double taworth=ArithUtil.sub(transferApply.getWorth(),ArithUtil.sub(transferApply.getCorpus(), transferApply.getTransferOutPrice()));//债权价值（本金+利息）-折让金=承接金额
		if(balance<taworth){
			throw new Exception("余额不足,请充值后购买");
		}
		List<LoanRepay> list=transferApply.getInvest().getLoan().getLoanRepays();
		double hasRepay=0d;//已还
		for (LoanRepay loanRepay : list) {
			if(RepayStatus.COMPLETE.equals(loanRepay.getStatus())){
				hasRepay=ArithUtil.add(hasRepay, loanRepay.getCorpus());
			}
		}
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		SimpleDateFormat sdfymd=new SimpleDateFormat("yyyyMMdd");
		DecimalFormat    df   = new DecimalFormat("######0.00");  
	//	TransferApply ta = ht.get(TransferApply.class, transferApplyId);
		double remainCorpus =transferApply.getInvest().getRepayRoadmap().getUnPaidCorpus();//未还本金 transferService.calculateRemainCorpus(transferApplyId);
		String Version="30";
		String CmdId="CreditAssign";
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户客户号 商户的唯一标识
		String SellCustId=transferApply.getInvest().getUser().getUsrCustId();//转让人客户号
		Double CreditAmt=remainCorpus;//转让金额
		Double CreditDealAmt=taworth;//承接金额
		String BidDetails="{\"BidDetails\":[{\"BidOrdId\":\""+transferApply.getInvest().getOrdId()+"\",\"BidOrdDate\":\""+sdfymd.format(transferApply.getInvest().getTime())+"\",\"BidCreditAmt\":\""+ df.format(CreditAmt)+"\",\"BorrowerDetails\":[{\"BorrowerCustId\":\""+transferApply.getInvest().getLoan().getUser().getUsrCustId()+"\",\"BorrowerCreditAmt\":\""+df.format(CreditAmt)+"\",\"PrinAmt\":\""+df.format(hasRepay)+"\",\"ProId\":\""+transferApply.getInvest().getLoan().getId()+"\"}]}]}";//债权转让明细
		double fee = transferApply.getFee();
		double Fee=ArithUtil.round(fee ,2);//扣款手续费
		String DivDetails="[{\"DivAcctId\":\""+HuiFuConstants.Config.DIV_ACCT_ID+"\",\"DivAmt\":\""+df.format(Fee)+"\"}]";
		String BuyCustId=user.getUsrCustId();//承接人客户号  
		String OrdId=OrderNoService.getOrderNo();//订单号  由商户的系统生成，必须保证唯一，请使用纯数字
		String OrdDate=sdfymd.format(new Date());//订单日期格式为 YYYYMMDD，例如：20130307
		String RetUrl=request.getScheme() + "://" + request.getServerName()+HuiFuHttpUtil.getPort(request) +request.getContextPath()+"/user/center";//实名后跳转的地址
		String BgRetUrl=HuiFuConstants.Config.BACK_URL;
		String ReqExt=transferApplyId;
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("SellCustId", SellCustId);
		params.put("CreditAmt", df.format(CreditAmt));
		params.put("CreditDealAmt", df.format(CreditDealAmt));
		params.put("BidDetails", BidDetails);
		params.put("Fee", df.format(Fee));
		params.put("DivDetails", DivDetails);
		params.put("BuyCustId", BuyCustId);
		params.put("OrdId", OrdId);
		params.put("OrdDate", OrdDate);
		params.put("RetUrl", RetUrl);
		params.put("BgRetUrl", BgRetUrl);
		params.put("MerPriv", ReqExt);
		try {
			String html=HuiFuHttpUtil.doPost(params);
			return html;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
		
	}

	@Override
	public JSONObject QueryBidInfo(String proId) {
		// TODO Auto-generated method stub
		JSONObject jso=new JSONObject();
		String Version="10";
		String CmdId="QueryBidInfo";
		String ProId=proId;
		String MerCustId=HuiFuConstants.Config.MERCUSTID;//商户客户号 商户的唯一标识
		LinkedHashMap<String, String> params=new LinkedHashMap<String, String>();
		params.put("Version", Version);
		params.put("CmdId", CmdId);
		params.put("MerCustId", MerCustId);
		params.put("ProId", ProId);
		String delStr="";
		try {
			delStr = HuiFuHttpUtil.doPost(params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 jso=JSONObject.parseObject(delStr);
		 return jso;
	}
	
}
