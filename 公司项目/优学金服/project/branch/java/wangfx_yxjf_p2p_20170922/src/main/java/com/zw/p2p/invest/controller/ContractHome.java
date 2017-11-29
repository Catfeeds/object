package com.zw.p2p.invest.controller;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.lowagie.text.pdf.BaseFont;
import com.zw.archer.node.model.Node;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.*;
import com.zw.p2p.invest.InvestConstants.InvestStatus;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.RepayConstants;
import com.zw.p2p.risk.model.FeeConfig;
import com.zw.p2p.risk.service.impl.FeeConfigBO;

@Component
@Scope(ScopeType.VIEW)
public class ContractHome {
    @Resource
    private LoginUserInfo userInfo;
	@Resource
	private HibernateTemplate ht;

	@Resource
	private DictUtil dictUtil;
	@Resource
	private FeeConfigBO feeConfigBO;

	private String contractContent;

	public void contractPdfDownload(String fileName) {
		String body = contractContent.replace("&nbsp;", "&#160;");
		body = "<html><head></head><body style=\"font-family:'SimSun';\">"
				+ body + "</body></html>";
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
							+ new String((fileName + ".pdf").getBytes("utf-8"),
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
	 * 替换 借款合同正文中的信息
	 * 
	 * @param loanId
	 * @return
	 */
	public String dealLoanContractContent(String loanId) {
		// FIXME： #{investTransferList}", "债权转让列表
        Loan loan =null;

        loan=ht.load(Loan.class,loanId);
        List<Loan> loanList=ht.find("from Loan where id= '"+loanId +"'");
        if(null!=loanList&&loanList.size()>0){
            loan=loanList.get(0);
        }
		// #{investList} 投资列表
		Element table = Jsoup
				.parseBodyFragment("<table border='1' style='margin: 0px auto; border-collapse: collapse; border: 1px solid rgb(0, 0, 0); width: 70%; '>" +
                        "<tbody><tr class='firstRow'><td style='text-align:center;'>用户名</td><td style='text-align:center;'>真实姓名</td><td style='text-align:center;'>投标来源</td><td style='text-align:center;'>借出金额</td><td style='text-align:center;'>借款期限</td><td style='text-align:center;'>应收利息</td></tr></tbody></table>");
		Element tbody = table.getElementsByTag("tbody").first();
        String userId=userInfo.getLoginUserId();
        List<Invest> is1 = ht.find(
                "from Invest i where i.loan.id=? and i.status!=? and  i.user.id =? ",
                new String[] { loan.getId(), InvestStatus.CANCEL, userId});
        for (Invest invest : is1) {
            tbody.append("<tr><td style='text-align:center;'>"
                    + invest.getUser().getUsername()
                    + "</td><td style='text-align:center;'>"+
                    invest.getUser().getRealname()
                    + "</td><td style='text-align:center;'>"
                    + (invest.getIsSafeLoanInvest()?"计划投标":invest.getIsAutoInvest() ? "自动投标" : "手动投标")
                    + "</td><td style='text-align:center;'>"
                    + NumberUtil.insertComma(String.valueOf(invest.getRepayRoadmap().getRepayCorpus()),2)
                    + "</td><td style='text-align:center;'>"
                    + loan.getDeadline()
                    * loan.getType().getRepayTimePeriod()
                    + "("
                    + dictUtil.getValue("repay_unit", loan.getType()
                    .getRepayTimeUnit()) + ")"
                    + "</td><td style='text-align:center;'>"
                    +NumberUtil.insertComma(String.valueOf(invest.getRepayRoadmap().getRepayInterest()),2)

                    + "</td></tr>");
        }
		List<Invest> is = ht.find(
				"from Invest i where i.loan.id=? and i.status!=? and i.user.id!=? ",
				new String[] { loan.getId(), InvestStatus.CANCEL,userId });
		for (Invest invest : is) {

            tbody.append("<tr><td style='text-align:center;'>"
                    + StringManager.getReplaceString(invest.getUser().getUsername(), 2)
                    + "</td><td style='text-align:center;'>"
                    +StringManager.getReplaceAfterString(invest.getUser().getRealname(),1)
                    + "</td><td style='text-align:center;'>"
					+ (invest.getIsSafeLoanInvest()?"计划投标":invest.getIsAutoInvest() ? "自动投标" : "手动投标")
					+ "</td><td style='text-align:center;'>"
					+NumberUtil.insertComma(String.valueOf(invest.getRepayRoadmap().getRepayCorpus()),2)
					+ "</td><td style='text-align:center;'>"
					+ loan.getDeadline()
					* loan.getType().getRepayTimePeriod()
					+ "("
					+ dictUtil.getValue("repay_unit", loan.getType()
							.getRepayTimeUnit()) + ")"
					+ "</td><td style='text-align:center;'>"
							+NumberUtil.insertComma(String.valueOf(invest.getRepayRoadmap().getRepayInterest()),2)

					+ "</td></tr>");
		}
		tbody.append("<tr><td style='text-align:center;'>总计</td><td></td><td></td><td style='text-align:center;'>"
				+NumberUtil.insertComma(String.valueOf(loan.getRepayRoadmap().getRepayCorpus()),2)
				+ "</td><td></td><td style='text-align:center;'>"
				+ NumberUtil.insertComma(String.valueOf(loan.getRepayRoadmap().getRepayInterest()),2) + "</td></tr>");

		Node node = ht.get(Node.class, loan.getContractType());
		if (contractContent == null) {
			/**caijinmin 201412231623 判断还款单元是天还是月，计算借款到期日  add begin**/
			Date endDate = null;
			if (RepayConstants.RepayUnit.DAY.equals(loan.getType()
					.getRepayTimeUnit())) {
				endDate = DateUtil.addDay(loan.getInterestBeginTime(),
						loan.getDeadline());
			} else if (RepayConstants.RepayUnit.MONTH.equals(loan.getType()
					.getRepayTimeUnit())) {
				endDate = DateUtil.addMonth(loan.getInterestBeginTime(),
						loan.getDeadline());
			}
			String repayDay1="";
			if(loan.getRepayRoadmap().getRepayPeriod()>1){
                repayDay1="每月"+String.valueOf(DateUtil.getDay(loan.getLoanRepays().get(0).getRepayDay()))+"日";
			}else{
			    repayDay1=Strings.nullToEmpty(DateUtil.DateToString(endDate,
                        DateStyle.YYYY_MM_DD_CN));
			}
			;
			/**caijinmin 201412231623 add 判断还款单元是天还是月，计算借款到期日 end**/
			contractContent = node.getNodeBody().getBody();
			contractContent = contractContent
					.replace("#{loanId}", Strings.nullToEmpty(loan.getId()))
					.replace("#{actualMoney}", NumberUtil.insertComma(String.valueOf(loan.getMoney()), 2))
					.replace("#{investList}", table.outerHtml())
					.replace("#{interest}",loan.getRepayRoadmap().getRepayInterest().toString())
					.replace("#{loanerRealname}",Strings.nullToEmpty(loan.getContractName()))
					.replace("#{loanerIdCard}", Strings.nullToEmpty(loan.getContractIdCard()))
					.replace("#{loanerUsername}", loan.getUser().getUsername())
					.replace("#{guaranteeCompanyName}",Strings.nullToEmpty(loan.getGuaranteeCompanyName()))
					.replace("#{giveMoneyDate}",Strings.nullToEmpty(DateUtil.DateToString(loan.getGiveMoneyTime(), DateStyle.YYYY_MM_DD_CN)))
					.replace("#{rate}", loan.getRatePercent().toString())
					.replace("#{deadline}",String.valueOf(loan.getRepayRoadmap().getRepayPeriod()))
					.replace("#{interestBeginTime}", Strings.nullToEmpty(DateUtil.DateToString(loan.getInterestBeginTime(), DateStyle.YYYY_MM_DD_CN)))
							// FIXME:需要根据借款类型，来计算借款到期日
					.replace("#{interestEndTime}", Strings.nullToEmpty(DateUtil.DateToString(endDate, DateStyle.YYYY_MM_DD_CN)))
					.replace("#{repayAllMoney}", NumberUtil.insertComma(String.valueOf(loan.getRepayRoadmap().getRepayMoney()), 2))
					// FIXME:需要根据借款类型，来显示还款日
					.replace("#{repayDay}",repayDay1)
					.replace("#{advanceInvestorFee}", feeConfigBO.getFee("advance_repay_investor") * 100 + "%")
					.replace("#{overdueSystemFee}",feeConfigBO.getFee("overdue_repay_system") * 100 + "%")
					.replace("#{overdueInvestorFee}", feeConfigBO.getFee("overdue_repay_investor") * 100 + "%")
					.replace("#{investTransferList}", "债权转让列表");
//					String str=contractContent.replace("#{repayDay}",repayDay1);
//					System.out.println(str);
//					System.out.println(contractContent);
		}
		return contractContent;
	}
	
	
	
	/**
	 * 替换债权借款合同正文中的信息
	 * 
	 * @param transferId
	 * @return
	 */
	@SuppressWarnings({ "unused", "static-access" })
	public String dealLoanContractTransferContent(String transferId,String role) {
		//获取债权信息
		TransferApply transferApply = ht.get(TransferApply.class, transferId);
		//获取投资列表
		Invest invest = ht.get(Invest.class, transferApply.getInvest().getId());
		Loan loan = ht.get(Loan.class, invest.getLoan().getId());
		//
		List<Invest> is = ht.find("from Invest i where i.transferApply.id=?",transferId );

		/*if(is.size()>1||null==is){
			return "合同信息有误!编号:"+transferId;
		}*/
		Element table = Jsoup
				.parseBodyFragment("<table border='1' style='margin: 0px auto; border-collapse: collapse; border: 1px solid rgb(0, 0, 0); width: 70%; '>" +
						"<tbody><tr class='firstRow'><td style='text-align:center;'>用户名</td><td style='text-align:center;'>真实姓名</td><td style='text-align:center;'>身份证号</td><td style='text-align:center;'>转入本金</td><td style='text-align:center;'>应收利息</td></tr></tbody></table>");
		Element tbody = table.getElementsByTag("tbody").first();
		String userId=userInfo.getLoginUserId();
		double allInvestMoney = 0D;
		double allInterest = 0D;
		if ("invest".equals(role)){
			List<Invest> is1 = ht.find(
					"from Invest i where i.loan.id=? and i.status!=? and  i.user.id =? and i.transferApply.id is not null ",
					new String[] { loan.getId(), InvestStatus.CANCEL, userId});
			for (Invest i : is1) {
				tbody.append("<tr><td style='text-align:center;'>"
						+ i.getUser().getUsername()
						+ "</td><td style='text-align:center;'>"
						+i.getUser().getRealname()
						+ "</td><td style='text-align:center;'>"
						+i.getUser().getIdCard()
						+ "</td><td style='text-align:center;'>"
						+ i.getInvestMoney()
						+ "</td><td style='text-align:center;'>"
						+ NumberUtil.insertComma(String.valueOf(i.getRepayRoadmap().getRepayInterest()), 2)
						+ "</td></tr>");
				allInvestMoney += i.getInvestMoney();
				allInterest += i.getRepayRoadmap().getRepayInterest();
			}
			List<Invest> is2 = ht.find(
					"from Invest i where i.loan.id=? and i.status!=? and i.user.id!=? and i.transferApply.id is not null ",
					new String[] { loan.getId(), InvestStatus.CANCEL,userId });
			for (Invest i : is2) {

				tbody.append("<tr><td style='text-align:center;'>"
						+ StringManager.getReplaceString(i.getUser().getUsername(), 2)
						+ "</td><td style='text-align:center;'>"
						+StringManager.getReplaceAfterString(i.getUser().getRealname(),1)
						+ "</td><td style='text-align:center;'>"
						+StringManager.getReplaceString(i.getUser().getIdCard(),1,1)
						+ "</td><td style='text-align:center;'>"
						+ i.getInvestMoney()
						+ "</td><td style='text-align:center;'>"
						+NumberUtil.insertComma(String.valueOf(i.getRepayRoadmap().getRepayInterest()), 2)

						+ "</td></tr>");
				allInvestMoney += i.getInvestMoney();
				allInterest += i.getRepayRoadmap().getRepayInterest();
			}
		}else if ("loan".equals(role)){
			List<Invest> invests = ht.find("from Invest i where i.loan.id=? and i.status!=? and i.transferApply.id is not null",new String[] { loan.getId(), InvestStatus.CANCEL});
			for (Invest i : invests){
				tbody.append("<tr><td style='text-align:center;'>"
						+ i.getUser().getUsername()
						+ "</td><td style='text-align:center;'>"
						+i.getUser().getRealname()
						+ "</td><td style='text-align:center;'>"
						+i.getUser().getIdCard()
						+ "</td><td style='text-align:center;'>"
						+ i.getInvestMoney()
						+ "</td><td style='text-align:center;'>"
						+ NumberUtil.insertComma(String.valueOf(i.getRepayRoadmap().getRepayInterest()),2)

						+ "</td></tr>");
				allInvestMoney += i.getInvestMoney();
				allInterest += i.getRepayRoadmap().getRepayInterest();
			}
		}
		tbody.append("<tr><td style='text-align:center;'>总计</td><td></td><td></td><td style='text-align:center;'>"
				+ ArithUtil.round( allInvestMoney ,2)
				+ "</td><td style='text-align:center;'>"
				+ ArithUtil.round( allInterest ,2) + "</td></tr>");
		Invest investmom = is.get(0);
		//获取项目列表
		//获取合同模板
		Node node = ht.get(Node.class, loan.getTransferType());
		FeeConfig feeConfig = ht.get(FeeConfig.class, "transfer");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String loantype=loan.getType().getId();
		//替换合同模板中的内容
		if (contractContent == null) {
			contractContent = node.getNodeBody().getBody();
			try {
				contractContent = contractContent
								//时间
						.replace("#{time}", DateUtil.DateToString(transferApply.getApplyTime(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN))
								//转让人(甲方)
						.replace("#{realname_zr}", invest.getUser().getRealname())
								//转让人用户名
						.replace("#{username_zr}", invest.getUser().getUsername())
								//转让人身份证
						.replace("#{idCard_zr}", invest.getUser().getIdCard())
								//受让人列表
						.replace("#{investList}", table.outerHtml())

								//项目编号
						.replace("#{loanId}", loan.getId())
								//借款人姓名/名称
						.replace("#{realname_loan}", loan.getContractName())
								//借款人证件类型及号码
						.replace("#{idCard_loan}", loan.getContractIdCard())
								//借款人用户名
						.replace("#{username_loan}", loan.getUser().getUsername())
								//借款本金总数额
						.replace("#{money_loan}", loan.getMoney().toString())

								//本次转让的债权本金数额
						.replace("#{corpus}",(transferApply.getTransferOutPrice() + transferApply.getPremium())+"")
								//借款年利率
						.replace("#{rate_loan}", loan.getRatePercent().toString())
								//借款用途
						.replace("#{purpose_loan}", loan.getLoanPurpose())
								//原借款期限
						.replace("#{deadline_loan}", loan.getDeadline().toString())
								//替换日期(月/天)
						.replace("#{deadline_format}", dictUtil.getValue("repay_unit",loan.getType().getRepayTimeUnit()))
								//起始日期
						.replace("#{jk_begin}",DateUtil.DateToString(loan.getGiveMoneyTime(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN))
								//结束日期
						.replace("#{jk_end}", DateUtil.DateToString(loan.getLoanRepays().get(investmom.getRepayRoadmap().getRepayPeriod()-1).getRepayDay(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN))
								//结息方式
						.replace("#{repayType_loan}", loan.getType().getDescription())
								//付息方式
						.replace("#{repayInterestType}",(loantype.equals("loan_type_2")||loantype.equals("loan_type_3"))?"按月付息":"到期付息")
								// 下一个付息日
						.replace("#{nextRepayDay}",investmom.getRepayRoadmap().getNextRepayDate() == null?"已还完":"" + DateUtil.DateToString(investmom.getRepayRoadmap().getNextRepayDate(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN))
								//还本方式
						.replace("#{repayCorpusType}",loantype.equals("loan_type_2")?"按月还本":"到期还本")
								// 还款日
						.replace("#{repayDay}",DateUtil.getDay(loan.getLoanRepays().get(0).getRepayDay())+"")
								//转让价款
						.replace("#{money_gm}", transferApply.getTransferOutPrice()+"")
								//转让管理费
						.replace("#{fee_gm}", transferApply.getFee()+"")
								//转让日期
						.replace("#{time_gm}",DateUtil.DateToString(transferApply.getApplyTime(), DateStyle.YYYY_MM_DD_HH_MM_SS_CN));
			}catch (Exception e){
			}
		}
		return contractContent;
	}
}
