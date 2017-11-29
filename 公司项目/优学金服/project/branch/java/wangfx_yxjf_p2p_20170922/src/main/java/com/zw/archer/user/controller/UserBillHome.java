package com.zw.archer.user.controller;

import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.user.UserBillConstants;
import com.zw.archer.user.UserConstants;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.model.UserBill;
import com.zw.archer.user.service.UserBillService;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.core.util.StringManager;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.risk.service.SystemBillService;
import com.zw.p2p.user.service.RechargeService;
import com.zw.p2p.user.service.WithdrawCashService;

@Component
@Scope(ScopeType.VIEW)
public class UserBillHome extends EntityHome<UserBill> implements Serializable {

	@Logger
	private static Log log;

	@Resource
	private UserBillService ubs;

	@Resource
	private RechargeService rechargeService;

	@Resource
	private WithdrawCashService wcService;

	@Resource
	private ConfigService cs;

	@Resource
	private HibernateTemplate ht;
	@Resource
	private SystemBillService sbs;

	private static StringManager sm = StringManager
			.getManager(UserConstants.Package);

	@Override
	protected UserBill createInstance() {
		UserBill bill = new UserBill();
		bill.setUser(new User());
		return bill;
	}

	/**
	 * 获取用户账户余额
	 * 
	 * @return
	 */
	public double getBalanceByUserId(String userId) {
		return ubs.getBalance(userId);
	}

	/**
	 * 获取用户账户冻结金额
	 * 
	 * @param userId
	 *            用户id
	 * @return 余额
	 */
	public double getFrozenMoneyByUserId(String userId) {
		return ubs.getFrozenMoney(userId);
	}

	/**
	 * 管理员操作借款账户
	 */
	public String managerUserBill() {
		if (!this.getInstance().getType().equals("手续费调整")){
			if (getInstance().getMoney()<0) {
				FacesUtil.addErrorMessage("金额不能小于0");
				return null;
			}

			if (getInstance().getUser()==null || getInstance().getUser().getId()==null) {
				FacesUtil.addErrorMessage("用户不能为空");
				return null;
			}
		}
		if (log.isInfoEnabled())
			log.info("管理员后台手工干预账户余额，干预类型：" + this.getInstance().getType()
					+ "，干预金额：" + this.getInstance().getMoney());
		try {
			if (this.getInstance().getType().equals("管理员-充值")) {
				rechargeService.rechargeByAdmin(getInstance());
			} else if (this.getInstance().getType().equals("管理员-提现")) {
				wcService.withdrawByAdmin(getInstance());
			} else if (this.getInstance().getType().equals("转入到余额")) {
				rechargeService.rechargeByAdmin(getInstance());
			} else if (this.getInstance().getType().equals("从余额转出")) {
				ubs.transferOutFromBalance(getInstance().getUser().getId(),
						getInstance().getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail());
			} else if (this.getInstance().getType().equals("管理员-冻结金额")) {
				ubs.freezeMoney(getInstance().getUser().getId(), getInstance()
						.getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail(),null,null);
			} else if (this.getInstance().getType().equals("管理员-解冻金额")) {
				ubs.unfreezeMoney(getInstance().getUser().getId(),
						getInstance().getMoney(), OperatorInfo.ADMIN_OPERATION,
						getInstance().getDetail(),null);
			} else if (this.getInstance().getType().equals("退还服务费")) {
				rechargeService.paybackByAdmin(getInstance());
			} else if (this.getInstance().getType().equals("手续费调整")) {
				sbs.adjustFee(getInstance().getMoney(), UserBillConstants.Type.ADJUST_FEE,getInstance().getDetail());
			} else {
				log.warn("未知的转账类型：" + this.getInstance().getType());
				FacesUtil.addErrorMessage("未知的转账类型："
						+ this.getInstance().getType());
				return null;
			}
		} catch (InsufficientBalance e) {
			FacesUtil.addErrorMessage("余额不足");
			return null;
		}
		FacesUtil.addInfoMessage("操作成功！");
		return FacesUtil.redirect("/admin/fund/userBillList");
	}
	private String fileName ;

	public String batchImport(){
		if( file == null){
			FacesUtil.addErrorMessage("未发现上传的文件！请先上传文件后，在导入表格！");
			return null ;
		}

		if(file.getFileName().matches("^.+\\.(?i)(xlsx)$")){
			FacesUtil.addErrorMessage("系统只支持Excel 2003格式的excel文件！");
			return null ;
		}
		int i = 1;
		try{

			InputStream fis = file.getInputstream();
			HSSFWorkbook wbs = new HSSFWorkbook(fis);
			HSSFSheet childSheet = wbs.getSheetAt(0);
            /*if(log.isDebugEnabled()){
            	log.debug("总行数:" + childSheet.getLastRowNum());

            }*/


//            for(int i = 1;i<=childSheet.getLastRowNum();i++){
			HSSFRow row = childSheet.getRow(i);
			while(row != null){
				row = childSheet.getRow(i);
				//System.out.println("列数:" + row.getPhysicalNumberOfCells());
				if(log.isDebugEnabled()){
					log.debug("当前行:" + i);
				}
				if(null != row){
					//FIXME: 判断用户是否存在
					final String userId = StringUtils.trim(row.getCell(0).getStringCellValue());
					if(StringUtils.isEmpty(userId)){
						break ;
					}
					if(ht.get(User.class, userId) == null){
						FacesUtil.addInfoMessage("批量导入，导入第"+i+"行失败！失败原因：用户名不存在。请检查上传结果！");
						return null ;
					}

					try {
						User user = ht.get(User.class,StringUtils.trim(row.getCell(0).getStringCellValue()));
						getInstance().setMoney(row.getCell(1).getNumericCellValue());
						getInstance().setUser(user);
						getInstance().setDetail(row.getCell(2).getStringCellValue());
						rechargeService.rechargeByAdmin(getInstance());
						/*ubs.transferIntoBalance(
								StringUtils.trim(row.getCell(0).getStringCellValue()),
								row.getCell(1).getNumericCellValue(),
								OperatorInfo.ADMIN_OPERATION,
								row.getCell(2).getStringCellValue());*/

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

		FacesUtil.addInfoMessage("文件上传成功！共导入"+i+"行。请检查上传结果！");
		//FIXME: 删除源文件
		return FacesUtil.redirect("/admin/fund/userBillList") ;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public boolean containsSubStr(String src,String tgt){
		return src.contains(tgt);
	}


}
