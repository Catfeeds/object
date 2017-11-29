package com.zw.p2p.safeloan.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.pdf.BaseFont;
import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.ConfigConstants;
import com.zw.archer.node.model.Node;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.HashCrypt;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.common.SafeLoanConstants.SafeLoanUnit;
import com.zw.p2p.safeloan.model.SafeLoan;
import com.zw.p2p.safeloan.model.SafeLoanRecord;
import com.zw.p2p.safeloan.service.SafeLoanRecordService;

/**
 * @Description: TODO(无忧宝产品)
 * @author cuihang
 * @date 2016-1-8 下午12:23:33
 */
@Component
@Scope(ScopeType.VIEW)
public class SafeLoanRecordHome extends EntityHome<SafeLoanRecord> implements
		Serializable {

	@Resource
	private HibernateTemplate ht;
	@Resource
	private SafeLoanRecordService safeLoanRecordService;
	@Resource
	private UserBillService userBillService;
	private String safeLoanReId;
	private String userid;
	private String pwd;
	private boolean xieyi1;
	private boolean xieyi2;
	
	public String getSafeLoanReId() {
		return safeLoanReId;
	}

	public void setSafeLoanReId(String safeLoanReId) {
		this.safeLoanReId = safeLoanReId;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Resource
	private LoginUserInfo loginUserInfo;

	public SafeLoanRecordHome() {
		setUpdateView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
		setDeleteView(FacesUtil.redirect(ConfigConstants.View.CONFIG_LIST));
	}

	public boolean isXieyi1() {
		return xieyi1;
	}

	public void setXieyi1(boolean xieyi1) {
		this.xieyi1 = xieyi1;
	}

	public boolean isXieyi2() {
		return xieyi2;
	}

	public void setXieyi2(boolean xieyi2) {
		this.xieyi2 = xieyi2;
	}
	public static Map<String,String> hasloan=new HashMap<String, String>();
	/**
	 * @Description: TODO(新增投资记录)
	 * @author cuihang
	 * @date 2016年1月22日 16:30:00
	 * @param money
	 * @param record
	 * @return
	 */
	@Transactional(readOnly = false)
	public String save() {
		if(!xieyi1){
			FacesUtil.addErrorMessage("请先同意协议");
			return null;
		}
		String cashPwd = HashCrypt.getDigestHash(pwd);
		User user = ht.get(User.class, loginUserInfo.getLoginUserId());
		SafeLoan safeloan = getBaseService().get(SafeLoan.class,
				getInstance().getSafeloanid().getId());
		try {
		
			if (safeloan.getStatus() != SafeLoanConstants.SafeLoanStatus.TZZ.getIndex()) {
				FacesUtil.addErrorMessage("此产品无法投资");
				return "pretty:safeLoanList";
			}
			this.getInstance().setUserid(user);
			
			//if (cashPwd != null && cashPwd.equals(getInstance().getUserid().getCashPassword())) {
				String checkmoneySt = checkMony(getInstance().getMoney(), safeloan);
				if (checkmoneySt.length() == 0) {
					if (getInstance().getMoney() >userBillService.getBalance(user.getId())) {
						FacesUtil.addErrorMessage("您的账户余额不足");
					} else {
						SafeLoanRecord slr=getInstance();
						slr.setSalrid(safeLoanRecordService.createSafeLoanRecordId());
						DecimalFormat df = new DecimalFormat(".00");
						slr.setEnableMoney(slr.getMoney());
						slr.setIncome(0d);
						slr.setBeforeInvestTime(new Date());
						Date endTime=getEndTime(safeloan.getDeadline(),safeloan.getUnit());
						slr.setExpectincome(safeLoanRecordService.getrealIncome(slr.getMoney(), new Date(), endTime, safeloan.getRate()));
						slr.setSalTime(new Date());
						slr.setEndTime( endTime);
						slr.setEnableStatus(SafeLoanConstants.EnableStatus.Y.getIndex());
						slr.setRecentlyIcome(0d);
						slr.setReturnIncome(0d);
						slr.setReturnMoney(0d);
						slr.setUserid(user);
						slr.setFirstSms(0);
						slr.setLastSms(0);
						slr.setSafeloanid(safeloan);
						slr.setStatus(SafeLoanConstants.SafeLoanRecordStatus.FBQ.getIndex());
						userBillService.freezeSafeLoanMoney(user.getId(), slr.getMoney(), OperatorInfo.INVEST_SAFE_LOAN, "投资无忧宝"+safeloan.getName(),safeloan.getId());
						ht.save(slr);
						BigDecimal hasmoney = new BigDecimal(safeloan.getMoney());
						hasmoney = hasmoney.add(new BigDecimal(df.format(slr.getMoney())));
						safeloan.setMoney(hasmoney.doubleValue());
						if (hasmoney.compareTo(new BigDecimal(safeloan.getLoanMoney())) == 0) {
							safeloan.setStatus(SafeLoanConstants.SafeLoanStatus.YMB.getIndex());
						}else if(hasmoney.compareTo(new BigDecimal(safeloan.getLoanMoney()))> 0){
							throw new Exception("投资失败,投资金额大于可投金额");
						}
						ht.update(safeloan);
						FacesUtil.addInfoMessage("无忧宝投标成功");
						hasloan.put(user.getId(), user.getId());
					}
				}else{
					FacesUtil.addErrorMessage(checkmoneySt);
				}
				
			/*} else {
				FacesUtil.addErrorMessage("交易密码错误");
				return "pretty:safeLoanDetail";
			}*/

		} catch (Exception e) {
			FacesUtil.addErrorMessage(e.getMessage());
			return "pretty:safeLoanList";
		}

		if (FacesUtil.isMobileRequest()) {
			return "pretty:safeLoanInvest";
		}
		return "pretty:safeLoanList";
	}
	/**是否投资后显示活动
	 * @return
	 */
	public String getShowAct(){
		String userid=loginUserInfo.getLoginUserId();
		if(hasloan.containsKey(userid)){
			hasloan.remove(userid);
			return "1";
		}
		return "0";
	}
	/**
	 * @Description: TODO(检查投资额不小于最小投资额等规则)
	 * @author cuihang
	 * @date 2016-1-12 下午2:37:01
	 * @param money
	 * @param record
	 * @return
	 */
	private String checkMony(Double money, SafeLoan safeLoan){
		String result = "";
		if (null != money) {
			BigDecimal moneybd = new BigDecimal(money);

			BigDecimal minInvestMoney = new BigDecimal(safeLoan.getMinInvestMoney());// 最小投资金额
			BigDecimal maxInvestMoney = new BigDecimal(safeLoan.getMaxInvestMoney());// 最大投资金额
			BigDecimal loanMoney = new BigDecimal(safeLoan.getLoanMoney());// 预计借款金额
			BigDecimal moneysj = new BigDecimal(safeLoan.getMoney());// 实际借到的金额
			BigDecimal incMoney = new BigDecimal(safeLoan.getIncMoney());// 递增金额
			BigDecimal bs = moneybd.subtract(minInvestMoney).divide(incMoney);
			if (bs.compareTo(new BigDecimal(bs.intValue())) != 0) {
				// 未被整出，
				result = "投资递增金额为" + incMoney.intValue();
			}
			if (moneybd.compareTo(minInvestMoney) < 0) {
				result = "投资金额不可小于最小投资金额";
			}
			if (moneybd.compareTo(maxInvestMoney) > 0) {
				result = "投资金额不可大于最大投资金额";
			}

			if (moneybd.add(moneysj).compareTo(loanMoney) > 0) {
				result = "投资金额不可大于可投金额";
			}

		} else {
			result = "请输入投资金额";
		}
		return result;
	}
	/**
	 * 得到投资记录状态
	 * @param safeLoanId
	 * @return
	 */
	public int getRecordNumBySafeLoanId(String safeLoanId){
		return safeLoanRecordService.recordNumBySafeLoanId(safeLoanId);
	}
/**
 * 下载无忧宝协议
 * @param safeLoanid
 */
	public void safeLoanContractPdfDownload(String slrid) {
	String body=safeLoanContractContent(slrid);
	body=body.replace("&nbsp;", "&#160;");
		StringReader contentReader = new StringReader(body);
		InputSource source = new InputSource(contentReader);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		ServletOutputStream sos = null;
		try {
			documentBuilder = factory.newDocumentBuilder();
			org.w3c.dom.Document xhtmlContent = documentBuilder.parse(source);
			ITextRenderer renderer = new ITextRenderer();
			ITextFontResolver fontResolver = renderer.getFontResolver();
			fontResolver.addFont(FacesUtil.getRealPath("SIMSUN.TTC"),
					BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
			renderer.setDocument(xhtmlContent, "");
			renderer.layout();
			FacesUtil.getHttpServletResponse().setHeader(
					"Content-Disposition",
					"attachment; filename=\""
							+ new String((getInstance().getSalrid() + ".pdf").getBytes("utf-8"),
									"iso8859-1") + "\"");
			FacesUtil.getHttpServletResponse().setContentType(
					"application/pdf;charset=UTF-8");
			sos = FacesUtil.getHttpServletResponse().getOutputStream();
			renderer.createPDF(sos);
			FacesUtil.getCurrentInstance().responseComplete();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (com.lowagie.text.DocumentException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(sos);
		}
	}
	/**
	*@Description: TODO(无忧宝协议内容) 
	* @author cuihang   
	*@date 2016-4-22 下午6:03:27 
	*@param slrid
	*@return
	 */
	public String safeLoanContractContent(String slrid){
		String repSLSlrid="#slrid#";//协议编号
		String repUserName="#username#";//甲方
		String repUserId="#userid#";//身份证号
		String repUserMobile="#usermobile#";//电话
		String repSLName="#slname#";//产品名称
		String repSLRate="#slrate#";//收益率
		String repSLMoney="#slmoney#";//本金数额
		String repSLDealLine="#sldealline#";//封闭期
		String repSLCommitTime="#slbegintime#";//封闭期开始时间
		String repSLEndTime="#slendtime#";//封闭期结束时间
		String reqSLApproveBeginTime="#slapprovebegintime#";//投资开始时间
		String repSLApproveEndTime="#slapproveendtime#";//投资结束时间
		
		SafeLoanRecord saferl=	ht.get(SafeLoanRecord.class, slrid);
		SafeLoan safel=ht.get(SafeLoan.class, saferl.getSafeloanid().getId());
		User user=saferl.getUserid();
		Node node=safel.getContract();
		String contractContent=node.getNodeBody().getBody();
		//String contractContent= getInstance().getSafeloanid().getContract().getNodeBody().getBody();
		String body = contractContent.replaceAll("&nbsp;", "&#160;");
		body=body.replaceAll(repSLSlrid, slrid);
		body=body.replaceAll(repUserName, user.getRealname());
		body=body.replaceAll(repUserId, user.getIdCard());
		body=body.replaceAll(repUserMobile, null==user.getMobileNumber()?"":user.getMobileNumber());
		body=body.replaceAll(repSLName, safel.getName());
		body=body.replaceAll(repSLRate, safel.getRate()+"");
		body=body.replaceAll(repSLMoney, saferl.getMoney()+"");
		body=body.replaceAll(repSLDealLine, safel.getDeadline()+"");
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		body=body.replaceAll(repSLCommitTime, sdf.format(saferl.getSalTime()));
		body=body.replaceAll(repSLEndTime,  sdf.format(saferl.getEndTime()));
		body=body.replaceAll(reqSLApproveBeginTime,  sdf.format(safel.getApproveBeginTime()));
		body=body.replaceAll(repSLApproveEndTime,  sdf.format(safel.getApproveEndTime()));
		body = "<html><head></head><body style=\"font-family:'SimSun';\">"
				+ body + "</body></html>";
		return body;
	}
	
	public double getInvestMoney(String userId){
		return safeLoanRecordService.getInvestMoney(userId);
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
	public int days(int deadline,String unit){
		  long between_days=(getEndTime(deadline,unit).getTime()-new Date().getTime())/(1000*3600*24); 
		return Integer.parseInt(String.valueOf(between_days));
	}
}
 