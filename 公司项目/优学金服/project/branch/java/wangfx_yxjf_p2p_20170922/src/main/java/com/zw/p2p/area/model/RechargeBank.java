package com.zw.p2p.area.model;

/**
 * 充值时使用的银行列表
 * Created by lijin on 15/3/16.
 */
public class RechargeBank {
    private int idx;
    private String code;
    private String name;
    private String img;
    private String phone;
    private String url;

    public RechargeBank(int idx,String code, String img,String name,String phone,String url) {
        this.idx= idx;
        this.code = code;
        this.img = img;
        this.name=name;
        this.phone= phone;
        this.url=url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
