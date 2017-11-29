package com.zw.archer.user.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.ctc.wstx.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.system.controller.DictUtil;
import com.zw.archer.user.model.User;
import com.zw.archer.user.model.UserBill;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.DateUtil;
/**
 * 
* @Description: TODO(与UserBillList完全相同只为在同一页面显示) 
* @date 2016-11-23 下午12:17:22
 */
@Component
@Scope(ScopeType.VIEW)
public class UserBillList1 extends EntityQuery<UserBill> {

	@Logger
	Log log;
	private Date startTime;
	private Date endTime;
	private String userName;
	private String realName;
	private String safeLoanid;
	@Autowired
	private DictUtil dictUtil;

	public UserBillList1() {
		final String[] RESTRICTIONS = { "id like #{userBillList.example.id}",
				"user.id like #{userBillList.example.user.id}",
				"user.id like #{userBillList.userName}",
				"user.realname like #{userBillList.realName}",
				"type like #{userBillList.example.type}",
				"loanId=  #{userBillList.safeLoanid}",
				"typeInfo like #{userBillList.example.typeInfo}",
				"time >= #{userBillList.startTime}",
				"time <= #{userBillList.endTime}",
				"money > 0"
				 };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		addOrder("time", super.DIR_DESC);
		addOrder("seqNum",super.DIR_DESC);
	}

	public String getRealName() {
//		if (realName==null) return null;
//		return realName.replaceAll("%","");
		return realName;
	}

	public void setRealName(String realName) {
		if (!StringUtils.isEmpty(realName))
			this.realName = "%"+realName+"%";
		else
			this.realName=null;
	}

	public String getUserName() {
//		if (userName!=null)
//			return userName.replaceAll("%","");
		return userName;
	}

	public void setUserName(String userName) {
		if (!StringUtils.isEmpty(userName))
			this.userName = "%"+userName+"%";
		else
			this.userName=null;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getSafeLoanid() {
		return safeLoanid;
	}

	public void setSafeLoanid(String safeLoanid) {
		this.safeLoanid = safeLoanid;
	}

	@Override
	protected void initExample() {
		UserBill example = new UserBill();
		example.setUser(new User());
		setExample(example);
	}

	public Date getSearchcommitMinTime() {
		return startTime;
	}

	public void setSearchcommitMinTime(Date searchcommitMinTime) {
		this.startTime = searchcommitMinTime;
	}

	public Date getSearchcommitMaxTime() {
		return endTime;
	}

	public void setSearchcommitMaxTime(Date searchcommitMaxTime) {
		if (searchcommitMaxTime != null) {
			searchcommitMaxTime = DateUtil.addDay(searchcommitMaxTime, 1);
		}
		this.endTime = searchcommitMaxTime;
	}

	/**
	 * 设置查询的起始和结束时间
	 */
	public void setSearchStartEndTime(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * 导出
	 */
	 private String getSign(String billType){
	 	if ( "ti_balance".equalsIgnoreCase(billType) )
	 		return "+";
	 	else if ( "to_balance".equalsIgnoreCase(billType) || "to_frozen".equalsIgnoreCase(billType) )
	 		return "-";
	 	return "";
	 }
	public void export() {
		UserBill userBill = getExample();
		StringBuilder hql = new StringBuilder("from UserBill where 1=1");
		if (userBill.getUser().getId() != null
				&& userBill.getUser().getId().length() > 0) {
			hql.append(" and user.id='").append(userBill.getUser().getId())
					.append("'");
		}
		if (userBill.getTypeInfo() != null
				&& userBill.getTypeInfo().length() > 0) {
			hql.append(" and typeInfo='").append(userBill.getTypeInfo())
					.append("'");
		}
		if (startTime != null && endTime != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			hql.append(" and time between '").append(format.format(startTime))
					.append("' and '").append(format.format(endTime)).append("'");
		}
		hql.append(" order by seqNum desc");
		List<UserBill> list = getHt().find(hql.toString());
		if (list != null && list.size() > 0) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			DecimalFormat numberFormat = new DecimalFormat("0.00");
			String[] excelHeader = { "时间", "类型|明细", "金额", "可用金额", "冻结金额", "备注" };
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("user_bill");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < excelHeader.length; i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(excelHeader[i]);
				sheet.autoSizeColumn(i);
			}
			for (int i = 0; i < list.size(); i++) {
				UserBill bill = list.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(format.format(bill.getTime()));
				row.createCell(1).setCellValue(
						dictUtil.getValue("bill_operator", bill.getTypeInfo()));
				row.createCell(2).setCellValue(getSign(bill.getType())+
						numberFormat.format(bill.getMoney()));
				row.createCell(3).setCellValue(
						numberFormat.format(bill.getBalance()));
				row.createCell(4).setCellValue(
						numberFormat.format(bill.getFrozenMoney()));
				row.createCell(5).setCellValue(bill.getDetail());
			}
			HttpServletResponse response = FacesUtil.getHttpServletResponse();
			response.setContentType("application/vnd.ms-excel");
			OutputStream stream = null;
			try {
				String filename = "账户流水_" + userBill.getUser().getId() + ".xls";
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
