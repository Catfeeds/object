package com.zw.huifu.bean.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;
import com.zw.core.util.IdGenerator;
@Entity
@Table(name ="huifu_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class HuiFuLog {
	private String id;
	private String orderId;			//订单号
	private String cmdIds;			//消息类型
	private String version;			//版本号
	private String merCustId;		//商户客户号
	private String respCode;		//返回码
	private String respDesc;		//返回码中文描述
	private String merPriv;			//商户私有域
	
	private String param;			// 请求业务数据报文，JSON 格式
	private String respData;		// 回调业务数据报文，JSON 格式
	private String createDate;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getMerPriv() {
		return merPriv;
	}
	public void setMerPriv(String merPriv) {
		this.merPriv = merPriv;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getCmdIds() {
		return cmdIds;
	}
	public void setCmdIds(String cmdIds) {
		this.cmdIds = cmdIds;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMerCustId() {
		return merCustId;
	}
	public void setMerCustId(String merCustId) {
		this.merCustId = merCustId;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getRespData() {
		return respData;
	}
	public void setRespData(String respData) {
		this.respData = respData;
	}
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getRespDesc() {
		return respDesc;
	}
	public void setRespDesc(String respDesc) {
		this.respDesc = respDesc;
	}
	
}
