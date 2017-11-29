package com.zw.p2p.invest.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.common.exception.NoMatchingObjectsException;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.invest.model.Invest;
import com.zw.p2p.invest.model.TransferApply;
import com.zw.p2p.invest.service.TransferService;
import com.zw.p2p.loan.model.Loan;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@Component
@Scope(ScopeType.VIEW)
public class TransferApplyList extends EntityQuery<TransferApply> {
	@Autowired
	private DictUtil dictUtil;
	@Logger
	Log log;
	@Resource
	TransferService transferService;
	@Resource
	TransferApplyHome transferApplyHome;

	private Double minCorpus;
	private Double maxCorpus;
	private Double maxRate;
	private Double minRate;
	private Double maxPremium;
	private Double minPremium;

	private Date searchcommitMinTime;

	private Date searchcommitMaxTime;

	public Date getSearchcommitMinTime() {
		return searchcommitMinTime;
	}

	public void setSearchcommitMinTime(Date searchcommitMinTime) {
		this.searchcommitMinTime = searchcommitMinTime;
	}

	public Date getSearchcommitMaxTime() {
		return searchcommitMaxTime;
	}

	public void setSearchcommitMaxTime(Date searchcommitMaxTime) {
		this.searchcommitMaxTime = searchcommitMaxTime;
	}

	public void setMinAndMaxCorpus(double minCorpus, double maxCorpus) {
		this.minCorpus = minCorpus;
		this.maxCorpus = maxCorpus;
	}

	public void setMinAndMaxRate(double minRate, double maxRate) {
		this.minRate = minRate;
		this.maxRate = maxRate;
	}

	public void setMinAndMaxPremium(double minPremium, double maxPremium) {
		this.minPremium = minPremium;
		this.maxPremium = maxPremium;
	}

	public TransferApplyList() {
		final String[] RESTRICTIONS = {
				"transferApply.status not in #{transferApplyList.example.status}",
				"transferApply.corpus >= #{transferApplyList.minCorpus}",
				"transferApply.corpus <= #{transferApplyList.maxCorpus}",
				"transferApply.invest.rate <= #{transferApplyList.maxRate}",
				"transferApply.invest.rate >= #{transferApplyList.minRate}",
				"transferApply.premium <= #{transferApplyList.maxPremium}",
				"transferApply.premium >= #{transferApplyList.minPremium}",
				"transferApply.applyTime >= #{transferApplyList.searchcommitMinTime}",
				"transferApply.applyTime <= #{transferApplyList.searchcommitMaxTime}",
				"transferApply.invest.loan.id like #{transferApplyList.example.invest.loan.id}",
				"transferApply.invest.user.id like #{transferApplyList.example.invest.user.id}",
				"transferApply.invest.user.username like #{transferApplyList.example.invest.user.username}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	@Override
	protected void initExample() {
		TransferApply ta = new TransferApply();
		Invest i = new Invest();
		i.setUser(new User());
		Loan loan = new Loan();
		i.setLoan(loan);
		ta.setInvest(i);
		this.setExample(ta);
	}

	public Double getMinCorpus() {
		return minCorpus;
	}

	public void setMinCorpus(Double minCorpus) {
		this.minCorpus = minCorpus;
	}

	public Double getMaxCorpus() {
		return maxCorpus;
	}

	public void setMaxCorpus(Double maxCorpus) {
		this.maxCorpus = maxCorpus;
	}

	public Double getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(Double maxRate) {
		this.maxRate = maxRate;
	}

	public Double getMinRate() {
		return minRate;
	}

	public void setMinRate(Double minRate) {
		this.minRate = minRate;
	}

	public Double getMaxPremium() {
		return maxPremium;
	}

	public void setMaxPremium(Double maxPremium) {
		this.maxPremium = maxPremium;
	}

	public Double getMinPremium() {
		return minPremium;
	}

	public void setMinPremium(Double minPremium) {
		this.minPremium = minPremium;
	}

	public void exportInvestList() {
		String hql = "from TransferApply where 1=1";
		List<TransferApply> tas= getHt().find(hql);
		if (tas != null && tas.size() > 0) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			DecimalFormat numberFormat = new DecimalFormat("0.00");
			String[] excelHeader = { "项目编号", "项目名称", "转让人","转让人真实姓名", "折让金", "转让时间", "剩余本金" ,"转让进度","受让人","受让人真实姓名","购买时间","转让本金","购买本金","状态" };
			String[] failExcelHeader = {"项目编号", "项目名称", "转让人","转让人真实姓名", "折让金", "转让时间", "剩余本金" ,"转让进度","状态"};
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("债权转让明细");
			HSSFSheet transferFail = wb.createSheet("流标债权");
			HSSFRow row = sheet.createRow(0);
			HSSFRow failRow = transferFail.createRow(0);
			for (int i = 0; i < excelHeader.length; i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(excelHeader[i]);
				sheet.autoSizeColumn(i);
			}
			for (int i = 0; i < failExcelHeader.length; i++) {
				HSSFCell cell = failRow.createCell(i);
				cell.setCellValue(failExcelHeader[i]);
				transferFail.autoSizeColumn(i);
			}
			int count = 0;
			int z = 0;
			for (int i = 0; i < tas.size(); i++) {
				TransferApply ta = tas.get(i);
				if (!"cancel".equals(ta.getStatus())){
					List<Invest> invests = getHt().find("from Invest i where i.transferApply is not null and i.transferApply.id = ?",ta.getId());
					for (int j = 0; j< invests.size();j++){
						count ++;
						row = sheet.createRow(count);
						Invest invest = invests.get(j);
						row.createCell(0).setCellValue(ta.getInvest().getLoan().getId());
						row.createCell(1).setCellValue(ta.getInvest().getLoan().getName());
						row.createCell(2).setCellValue(ta.getInvest().getUser().getUsername());
						row.createCell(3).setCellValue(ta.getInvest().getUser().getRealname());
						row.createCell(4).setCellValue(numberFormat.format((invest.getInvestMoney()/ta.getCorpus())*ta.getPremium()));
						row.createCell(5).setCellValue(format.format(ta.getApplyTime()));
						row.createCell(6).setCellValue(numberFormat.format(transferApplyHome.getTransferCorpus(ta.getInvest().getId(),transferService.calculateRemainCorpus(ta.getId()))));
						try {
							row.createCell(7).setCellValue(transferService.calculateInvestTransferCompletedRate(ta.getId()) + "%");
						} catch (NoMatchingObjectsException e) {
							e.printStackTrace();
						}
						row.createCell(8).setCellValue(invest.getUser().getUsername());
						row.createCell(9).setCellValue(invest.getUser().getRealname());
						row.createCell(10).setCellValue(format.format(invest.getTime()));
						row.createCell(11).setCellValue(numberFormat.format((invest.getInvestMoney()/ta.getCorpus())*ta.getTransferOutPrice()));
						row.createCell(12).setCellValue(numberFormat.format((invest.getInvestMoney()/ta.getCorpus())*ta.getTransferOutPrice()));
						row.createCell(13).setCellValue(dictUtil.getValue("transfer", ta.getStatus()));
					}
				}else {

					failRow = transferFail.createRow(z + 1);
					failRow.createCell(0).setCellValue(ta.getInvest().getLoan().getId());
					failRow.createCell(1).setCellValue(ta.getInvest().getLoan().getName());
					failRow.createCell(2).setCellValue(ta.getInvest().getUser().getUsername());
					failRow.createCell(3).setCellValue(numberFormat.format(ta.getPremium()));
					failRow.createCell(4).setCellValue(format.format(ta.getApplyTime()));
					failRow.createCell(5).setCellValue(numberFormat.format(transferApplyHome.getTransferCorpus(ta.getInvest().getId(),transferService.calculateRemainCorpus(ta.getId()))));
					try {
						failRow.createCell(6).setCellValue(transferService.calculateInvestTransferCompletedRate(ta.getId()) + "%");
					} catch (NoMatchingObjectsException e) {
						e.printStackTrace();
					}
					failRow.createCell(7).setCellValue(dictUtil.getValue("transfer", ta.getStatus()));
					z += 1;
				}
			}
			HttpServletResponse response = FacesUtil.getHttpServletResponse();
			response.setContentType("application/vnd.ms-excel");
			OutputStream stream = null;
			try {
				String filename = "债权转让明细表" + ".xls";
				String agent = FacesUtil.getHttpServletRequest().getHeader(
						"USER-AGENT");
				if (null != agent && -1 != agent.indexOf("MSIE")) {
					filename = URLEncoder.encode(filename, "utf-8");
				} else {
					filename = new String(filename.getBytes("utf-8"),
							"iso8859-1");
				}
				response.setHeader("Content-disposition",
						"attachment;filename=" + filename);
				stream = response.getOutputStream();
				wb.write(stream);
				stream.flush();
				stream.close();
				stream = null;
				response.flushBuffer();
				FacesUtil.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
