package com.zw.p2p.privilege.controller;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.privilege.model.PrivilegeModel;

/**
 * 特权本金模型控制器
 * 
 * @author zhenghaifeng
 * @date 2015-12-23 下午3:18:47
 */
@Component
@Scope(ScopeType.REQUEST)
public class PrivilegeModelList extends EntityQuery<PrivilegeModel> {
	private static final String lazyModelCount = "select "
			+ " count(privilege.userId) "
			+ " from Privilege privilege, com.zw.archer.user.model.User user  "
			+ " where privilege.userId=user.id and privilege.status ='U'";

	// 查询数据如果为空会报错，需要修改
	private static final String lazyModel = "select "
			+ "new com.zw.p2p.privilege.model.PrivilegeModel(user.id, user.username, user.mobileNumber, privilege.totleFee, privilege.inviteNumber, privilege.totleIncome, privilege.createTime) "
			+ " from Privilege privilege, com.zw.archer.user.model.User user   "
			+ " where privilege.userId=user.id and privilege.status ='U'";

	// 查询条件 start
	private String mobileNumber;
	private Date searchCommitMinTime, searchCommitMaxTime;
	private String userName;// 推荐人
	
	// 查询条件 end

	public PrivilegeModelList() {
		setCountHql(lazyModelCount);
		setHql(lazyModel);
		final String[] RESTRICTIONS = {
				"privilege.createTime >= #{privilegeModelList.searchCommitMinTime}",
				"privilege.createTime <= #{privilegeModelList.searchCommitMaxTime}",
				"user.username=#{privilegeModelList.userName}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public Date getSearchCommitMinTime() {
		return searchCommitMinTime;
	}

	public void setSearchCommitMinTime(Date searchCommitMinTime) {
		this.searchCommitMinTime = searchCommitMinTime;
	}

	public Date getSearchCommitMaxTime() {
		return searchCommitMaxTime;
	}

	public void setSearchCommitMaxTime(Date searchCommitMaxTime) {
		this.searchCommitMaxTime = searchCommitMaxTime;
	}
	
	/**
	 * 导出所有用户信息
	 */
	public void export() {
		String hql = parseHql(getHql()) + " order by privilege.createTime desc";
		List<PrivilegeModel> list = getHt().find(hql,getParameterValues());
		if (list != null && list.size() > 0) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String[] excelHeader = { "邀请人", "邀请人所获特权本金", "好友人数", "累计收益", "第一笔奖励时间"};
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet("特权本金");
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < excelHeader.length; i++) {
				HSSFCell cell = row.createCell(i);
				cell.setCellValue(excelHeader[i]);
				sheet.autoSizeColumn(i);
			}
			for (int i = 0; i < list.size(); i++) {
				PrivilegeModel p = list.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(p.getMobileNumber());
				row.createCell(1).setCellValue(getDecimalFormat(p.getTotleFee()));
				row.createCell(2).setCellValue(p.getInviteNumber());
				row.createCell(3).setCellValue(getDecimalFormat(p.getTotleIncome()));
				row.createCell(4).setCellValue(format.format(p.getCreateTime()));
				
			}
			HttpServletResponse response = FacesUtil.getHttpServletResponse();
			response.setContentType("application/vnd.ms-excel");
			OutputStream stream = null;
			try {
				String filename = "特权本金.xls";
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
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	 private String getDecimalFormat(Double d){  
        DecimalFormat   fmt   =   new   DecimalFormat("##,###,###,###,##0.00");    
        String outStr = null;  
        try {  
            outStr = fmt.format(d);  
        } catch (Exception e) {  
        }  
        return outStr;  
    }  
	 
}
