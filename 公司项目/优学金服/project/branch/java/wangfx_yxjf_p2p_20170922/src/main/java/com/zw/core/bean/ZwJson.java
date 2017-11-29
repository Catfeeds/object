package com.zw.core.bean;

/**
 * json对象
 * @author majie
 * @date 2016年8月17日 下午2:32:02
 */
public class ZwJson implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private boolean success = false;

	private String msg = "";

	private Object obj = null;
	
	public ZwJson(){}
	
    public ZwJson(boolean success, String msg){
        super();
        this.success = success;
        this.msg = msg;
    } 

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

}
