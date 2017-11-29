package com.zw.p2p.safeloan.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.safeloan.common.SafeLoanConstants;
import com.zw.p2p.safeloan.model.SafeLoan;

/**
 * 无忧宝投资项目列表
 * 
 * @author zhenghaifeng
 * @date 2016-1-22 下午1:54:02
 */
@Component
@Scope(ScopeType.VIEW)
public class SafeLoanListFore extends EntityQuery<SafeLoan> implements Serializable {
    // 状态
    private int statusId;
    // 利率
    private String rateQuery;
    // 还款时间
    private String statusQuery;
    // 借款金额
    private String deadlineQuery;

    private Date curDate;

    public SafeLoanListFore() {
        final String[] RESTRICTIONS = {};
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

        setStatusId(-1);
        this.setPageSize(7);
        this.addOrder("safeLoan.status", "asc");
        this.addOrder("safeLoan.approveBeginTime", "desc");
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public void setMinAndMaxRate(Double min, Double max) {
    	if (rateQuery!=null){
            this.removeRestriction(rateQuery);
        }

        rateQuery= " safeLoan.rate >="+min+" and safeLoan.rate <="+max;
        this.addRestriction(rateQuery);
    }

    /**
     * 筛选借款期限
     *
     * @param minDeadline
     * @param maxDeadline
     */
    public void setMinAndMaxDeadline(Integer minDeadline,Integer maxDeadline) {
        if (deadlineQuery!=null){
            this.removeRestriction(deadlineQuery);
        }

        String monthQuery;

        monthQuery = "(safeLoan.deadline >= "+minDeadline+" and safeLoan.deadline < "+maxDeadline+" )";

        deadlineQuery= monthQuery;
        this.addRestriction(deadlineQuery);
    }

    public int getStatusId() {
        return statusId;
    }

    /**
     * 	筛选投资项目状态
     * @param sId
     */
    public void setStatusId(int sId) {
        if(statusQuery != null)
            this.removeRestriction(statusQuery);

        statusQuery="";
        switch (sId) {
            case -1://全部
                statusQuery = "safeLoan.status in ("+SafeLoanConstants.SafeLoanStatus.TZZ.getIndex()+","+SafeLoanConstants.SafeLoanStatus.FHZ.getIndex()+","+SafeLoanConstants.SafeLoanStatus.YMB.getIndex()+")";
                break;
            case 1://投资 中
            	statusQuery = "safeLoan.status="+SafeLoanConstants.SafeLoanStatus.TZZ.getIndex();
                break;
            case 2://复核 中
            	statusQuery = "safeLoan.status="+SafeLoanConstants.SafeLoanStatus.FHZ.getIndex();
            	break;
            case 3://已满标
            	statusQuery = "safeLoan.status="+SafeLoanConstants.SafeLoanStatus.YMB.getIndex();
                break;
        }

        this.addRestriction(statusQuery);
        this.statusId = sId;
    }

    @Override
    protected void initExample() {
    	SafeLoan loan = new SafeLoan();
    	loan.setCreatUser(new User());
        setExample(loan);
    }
}
