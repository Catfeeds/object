package com.zw.p2p.cashticket.controller;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.cashticket.CashticketConstants;
import com.zw.p2p.cashticket.model.CashTicket;
import com.zw.p2p.coupons.CouponConstants;
import com.zw.p2p.coupons.model.Coupons;

@Component
@Scope(ScopeType.VIEW)
public class CashTicketHome extends EntityHome<CashTicket> implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -743762611543652168L;
	
	@Logger
	private static Log log;
	@Resource
	private HibernateTemplate ht;
	private int useLine;
	@Resource
	private ConfigService configService;
	public CashTicketHome() {
		setUpdateView(FacesUtil.redirect("/admin/cashTicket/cashTicketList"));
	}
	
	@Override
	@Transactional(readOnly = false)
	public String save() {
		String cashticketCreateTime = configService.getConfigValue("cashticketCreateTime");//每种现金券的发放开始日期和结束日期可单独配置
		String cashticketEndTime = configService.getConfigValue("cashticketEndTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		try {
			if(now.compareTo(sdf.parse(cashticketCreateTime))>0 && sdf.parse(cashticketEndTime).compareTo(now)>0){
				getInstance().setId( UUID.randomUUID().toString().replaceAll("-", ""));
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				getInstance().setCreateTime(format.format(new Date()));
				getInstance().setResource(CashticketConstants.RESOURCETYPE.admindo.getName());
				getInstance().setStatus(CouponConstants.CouponStatus.ENABLE);
				getInstance().setEndTime(format.format(new Date(new Date().getTime() + 1000l * 60 * 60 * 24 * getUseLine())));
				return super.save();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;	
		
	}
	
	@Override
	@Transactional(readOnly = false)
	public String delete(String id){
		return super.delete(id);
	}
	
	public int getUseLine() {
		return useLine;
	}

	public void setUseLine(int useLine) {
		this.useLine = useLine;
	}
	
	@Transactional(readOnly = false)
	public String batchImport(){
		if( file == null){
			FacesUtil.addErrorMessage("未发现上传的文件！请先上传文件后，在导入表格！");
			return null ;
		}

		/*if(file.getFileName().matches("^.+\\.(?i)(xlsx)$")){
			FacesUtil.addErrorMessage("系统只支持Excel 2003格式的excel文件！");
			return null ;
		}*/
		int i = 1;
		try{
			Workbook book = null;
			
			InputStream fis = file.getInputstream();
			try {
	            book = new XSSFWorkbook(fis);
	        } catch (Exception ex) {
	            book = new HSSFWorkbook(file.getInputstream());
	        }
			
//			HSSFWorkbook wbs = new HSSFWorkbook(fis);
//			HSSFSheet childSheet = wbs.getSheetAt(0);
			
			Sheet childSheet = book.getSheetAt(0);  
			
            /*if(log.isDebugEnabled()){
            	log.debug("总行数:" + childSheet.getLastRowNum());

            }*/


//            for(int i = 1;i<=childSheet.getLastRowNum();i++){
			Row row = childSheet.getRow(i);
			while(row != null){
				row = childSheet.getRow(i);
				//System.out.println("列数:" + row.getPhysicalNumberOfCells());
				if(log.isDebugEnabled()){
					log.debug("当前行:" + i);
				}
				if(null != row){
					DecimalFormat df = new DecimalFormat("0");  
					  
					String whatYourWant = df.format(row.getCell(0).getNumericCellValue());  
					//FIXME: 判断用户是否存在
					final String userId = StringUtils.trim(whatYourWant);
					if(StringUtils.isEmpty(userId)){
						break ;
					}
					if(ht.get(User.class, userId) == null){
						FacesUtil.addInfoMessage("批量导入，导入第"+i+"行失败！失败原因：用户名不存在。请检查上传结果！");
						return null ;
					}

					try {
						User user = ht.get(User.class,whatYourWant);
						getInstance().setMoney(row.getCell(1).getNumericCellValue());
						getInstance().setUser(user);
						double userLine = row.getCell(2).getNumericCellValue();
						setUseLine((int)userLine);
						this.save();

					} catch (Exception e) {
						log.error("导入失败"+row.getRowNum(),e);
						FacesUtil.addErrorMessage("批量导入失败！请检查上传结果！"+"失败行数第："+i +"行。");
						e.printStackTrace();
						return null ;
//						break ;
					}

					i++;
				}
			}
//            }
		}catch(Exception e){
			FacesUtil.addErrorMessage("批量导入失败！请检查上传结果！"+"失败行数第："+i +"行。");
			e.printStackTrace();
			return null ;
		}

		FacesUtil.addInfoMessage("文件上传成功！共导入"+(i-1)+"行。请检查上传结果！");
		//FIXME: 删除源文件
		return FacesUtil.redirect("/admin/cashTicket/cashTicketList") ;
	}
	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
}
