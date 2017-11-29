package com.zw.archer.invite.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.zw.archer.common.controller.EntityHome;
import com.zw.archer.config.service.ConfigService;
import com.zw.archer.invite.InviteService;
import com.zw.archer.invite.model.invite;
import com.zw.archer.system.controller.LoginUserInfo;
import com.zw.archer.user.UserBillConstants.OperatorInfo;
import com.zw.archer.user.model.User;
import com.zw.archer.user.service.UserBillService;
import com.zw.archer.user.service.impl.UserBillBO;
import com.zw.archer.util.ImgUtil;
import com.zw.archer.util.QRUtilEncode;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;
import com.zw.core.jsf.util.FacesUtil;
import com.zw.p2p.loan.exception.InsufficientBalance;
import com.zw.p2p.risk.service.SystemBillService;

@Component
@Controller
@Scope(ScopeType.VIEW)
public class InviteHome extends EntityHome<invite> implements java.io.Serializable{
	
	@Logger
	static Log log;
	@Resource
	private HibernateTemplate ht;

	@Resource
	private LoginUserInfo loginUserInfo;
	
	@Resource
	private UserBillBO userBillBO;
	
	@Resource
	private UserBillService userBillService;
	
	@Resource
	private InviteService inviteService;
	
	@Resource
	private SystemBillService sbs;
	@Resource
	private ConfigService configService;
	
	private List<invite> getInviteList(){
		String fromuserid = loginUserInfo.getLoginUserId();
		String hql=" from invite i where i.fromuserid=?";
		List<invite> invite=getBaseService().find(hql,fromuserid);
		return invite;
	}
	
	public int getinvitefriend(){
		int num=0;
		List<invite> invite=getInviteList();
		if(null==invite||invite.size()<=0){
			return num;
		}
		return invite.size();
	}
	
	public int getReward(int id) throws InsufficientBalance{
		//0失败  1成功
		User user=loginUserInfo.getUser();
		String hql=" from invite i where id=? and fromuserid=? ";
		List<invite> invite=getBaseService().find(hql,id,user.getId());
		if(null==invite||invite.size()<=0){
			return 0;
		}
		invite in=invite.get(0);
		String hqluser="from User u where id=?";
		if(in.getTouserid() == null||in.getTouserid().equals("")){
			return 0;
		}
		User touser=(User) getBaseService().find(hqluser,in.getTouserid()).get(0);
		
		
		if(in.getBandcardstatus()==1&&in.getBandcardlqstatus()==0){
			in.setBandcardlqstatus(1);
			userBillService.transferIntoBalance(user.getId(), new Double(in.getBandcardrewmoney()), OperatorInfo.TG_BINDCARD, "用户"+touser.getId()+"绑定银行卡的奖励。");
			sbs.transferOut(in.getBandcardrewmoney(), "tg_bindcard","客户领取邀请"+touser.getId()+"的奖金", null, null, null, null, null, null, null, null, user);
		}
		if(in.getLoantyjstatus()==1&&in.getLoantyjlqstatus()==0){
			in.setLoantyjlqstatus(1);
			userBillService.transferIntoBalance(user.getId(), new Double(in.getLoantyjrewmoney()), OperatorInfo.TG_LOANTYJ, "用户"+touser.getId()+"投资体验金的奖励。");
			sbs.transferOut(in.getLoantyjrewmoney(), "tg_loantyj","客户领取邀请"+touser.getId()+"的奖金", null, null, null, null, null, null, null, null, user);
		}
		if(in.getRealnamestatus()==1&&in.getRealnamelqstatus()==0){
			in.setRealnamelqstatus(1);
			userBillService.transferIntoBalance(user.getId(), new Double(in.getRealnamerewmoney()), OperatorInfo.TG_REALNAME, "用户"+touser.getId()+"实名认证的奖励。");
			sbs.transferOut(in.getRealnamerewmoney(), "tg_realname","客户领取邀请"+touser.getId()+"的奖金", null, null, null, null, null, null, null, null, user);
		}
		if(in.getTyjstatus()==1&&in.getTyjlqstatus()==0){
			in.setTyjlqstatus(1);
			userBillService.transferIntoBalance(user.getId(), new Double(in.getTyjrewmoney()), OperatorInfo.TG_LQTYJ, "用户"+touser.getId()+"领取体验金的奖励。");
			sbs.transferOut(in.getTyjrewmoney(), "tg_lqtyj","客户领取邀请"+touser.getId()+"的奖金", null, null, null, null, null, null, null, null, user);
		}
		inviteService.save(in);
		return 1;
	}
	//待领取奖金  目前前台与后台共用  后期再改
	public int getTotalrew(String touserid){
		if(touserid == null||touserid.equals("")){
			return 0;
		}
		String hql=" from invite i where touserid=?";
		List<invite> invite=getBaseService().find(hql,touserid);
		int totalrew=0;
		if(null==invite||invite.size()<=0){
			return 0;
		}
		invite in=invite.get(0);
		if(in.getBandcardstatus()==1&&in.getBandcardlqstatus()==0){
			totalrew=totalrew + in.getBandcardrewmoney();
		}
		if(in.getLoantyjstatus()==1&&in.getLoantyjlqstatus()==0){
			totalrew=totalrew + in.getLoantyjrewmoney();
		}
		if(in.getRealnamestatus()==1&&in.getRealnamelqstatus()==0){
			totalrew=totalrew + in.getRealnamerewmoney();
		}
		if(in.getTyjstatus()==1&&in.getTyjlqstatus()==0){
			totalrew=totalrew + in.getTyjrewmoney();
		}
		inviteService.save(in);
		return totalrew;
		
	}
	//总价
	public int getTotal(){
		int total=0;
		int money=0;
		List<invite> inviteList=getInviteList();
		for(invite in:inviteList){
			total=getTotalrew(in.getTouserid());
			money=money+total;
		}
		return money;
	}
	//一键领取
	public int getTotalMoney() throws InsufficientBalance{
		int money=0;
		List<invite> inviteList=getInviteList();
		for(invite in:inviteList){
			getReward(in.getId());
		}
		return money;
	}
	//后台已领取合计
	public int getYlqTotal(int id){
		User user=loginUserInfo.getUser();
		
		String fromuserid = loginUserInfo.getLoginUserId();
		
		String hql=" from invite i where id=?";
		List<invite> invite=getBaseService().find(hql,id);
		if(null==invite||invite.size()<=0){
			return 0;
		}
		invite in=invite.get(0);
		int totalrew=0;
		if(in.getTyjstatus()==1&&in.getTyjlqstatus()==1){
			totalrew=totalrew + in.getTyjrewmoney();
		}
		if(in.getRealnamestatus()==1&&in.getRealnamelqstatus()==1){
			totalrew=totalrew + in.getRealnamerewmoney();
		}
		if(in.getLoantyjstatus()==1&&in.getLoantyjlqstatus()==1){
			totalrew=totalrew + in.getLoantyjrewmoney();
		}
		if(in.getBandcardstatus()==1&&in.getBandcardlqstatus()==1){
			totalrew=totalrew + in.getBandcardrewmoney();
		}
		inviteService.save(in);
		return totalrew;
	}
	
	
	
	
	
	
	public void download() throws IOException {
		//String realpath = this.getClass().getResource("/").getPath().substring(1).replace("/WEB-INF/classes/", "/");
		User user=loginUserInfo.getUser();
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		String savepath="/qrShareScanImg/"+user.getMobileNumber()+".jpg";
		File file = new File(request.getRealPath("/")+savepath);
        if (!file.exists()) {
            return;
        }
        InputStream fis = new FileInputStream(file);
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext ec = facesContext.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType("application/octet-stream");
		ec.setResponseContentLength((int)file.length());
		ec.setResponseHeader("Content-Disposition", "attachment; filename="+file.getName());
        byte[] buf = new byte[1024];
        int len = 0;
        OutputStream output = ec.getResponseOutputStream();
        while ((len = fis.read(buf)) > 0)
        	output.write(buf, 0, len);
        fis.close();
        output.close();
        facesContext.responseComplete();
    }   
	//获得显示得二维码
	public String inviteQRShare(){
		
		User user=loginUserInfo.getUser();
		HttpServletRequest request=	FacesUtil.getHttpServletRequest();
		/*String urlPath=serverurlWECHAT+"qrShareImg/"+user.getMobileNumber()+".jpg" ;
		if(urlEnable(urlPath)){
			//微信端已生成直接显示微信端生成的
			return urlPath;
		}else{*/
		String imgname=user.getId();
		String imgLogoName=imgname+"logo";
			String savepath="/qrShareScanImg/"+imgname+".jpg";
			//微信端没生成则生成用户PC端并提示用户扫一扫生成
			String reql = getEwmAddr();
				//微信需要扫得地址
				
				/*String appid=configService.getConfigValue("wechatappid");
				String reUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appid + "&redirect_uri=" + URLEncoder.encode(reql, "utf-8") + "&response_type=code&scope=snsapi_base&state=#wechat_redirect";*/
			QRUtilEncode.encode_500(reql, request.getRealPath("/")+savepath);
			
			ImgUtil.composePicTowLoc(request.getRealPath("/")+savepath, request.getRealPath("/")+"tgqrlogo.jpg", request.getRealPath("/")+"/qrShareScanImg/"+imgLogoName+".jpg", 89, 89, 90, 90);
			return request.getContextPath()+"/qrShareScanImg/"+imgLogoName+".jpg";
		
		
		
	}
	
	//获得二维码链接地址
	public String getEwmAddr(){
		User user=loginUserInfo.getUser();
		String serverurlWECHAT=configService.getConfigValue("serverurlWECHAT");
		String reql = serverurlWECHAT+ "/mobile/p2p/turnToRedPackage?userkey="+user.getId();
		return reql;
	}

	//格式化手机号
	public String formatphone(String phone){
		if(phone==null||phone.equals("")){
			return "";
		}
		String tophone=phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
		return tophone;
	}
}
