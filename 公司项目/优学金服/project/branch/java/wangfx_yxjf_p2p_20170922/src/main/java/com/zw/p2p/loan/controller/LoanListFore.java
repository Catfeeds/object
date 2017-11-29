package com.zw.p2p.loan.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.user.model.User;
import com.zw.core.annotations.ScopeType;
import com.zw.p2p.loan.model.Loan;
import com.zw.p2p.repay.RepayConstants;

/**
 * Created by lijin on 15/3/2.
 * LoanList 前端显示
 */

@Component
@Scope(ScopeType.VIEW)
public class LoanListFore extends EntityQuery<Loan> implements Serializable {
    //状态
    private int statusId;
    // 利率
    private Double minRate;
    private Double maxRate;
    // 还款时间
    private String statusQuery;
    // 借款金额
    private String deadlineQuery;
    private Integer minAndMaxDeadline;

    private Date curDate;

    public LoanListFore() {
        final String[] RESTRICTIONS = {
                "loan.rate >=#{loanListFore.minRate}",
                "loan.rate <=#{loanListFore.maxRate}",
                "loan.riskLevel like #{loanListFore.example.riskLevel}",
                "loan.loantypeb like #{loanListFore.example.loantypeb}"
        };
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));

        setStatusId(0);
        this.setPageSize(7);
     
        this.addOrder(" case loan.status when 'raising' then 5  when 'dqgs' then 4  when 'waiting_verify_affirm' then 3 when 'waiting_verify_affirm_user' then 2 when 'recheck' then 2 end" , "desc");
      	 this.addOrder("loan.commitTime", "desc");
        this.addOrder("loan.seqNum", "desc");
       
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public Double getMinRate() {
        return minRate;
    }

    public void setMinRate(Double minRate) {
        this.minRate = minRate;
    }

    public Double getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(Double maxRate) {
        this.maxRate = maxRate;
    }

    public void setMinAndMaxRate(Double min, Double max) {
        setMinRate(min);
        setMaxRate(max);
    }

    /**
     * 筛选借款期限
     *
     * @param minDeadline
     * @param maxDeadline
     */
    public void setMinAndMaxDeadline(Integer deadline) {
    	this.minAndMaxDeadline=deadline;
        if (deadlineQuery!=null){
            this.removeRestriction(deadlineQuery);
        }

        String monthQuery="1=1";
		if(deadline!=0){
			 monthQuery = "(loan.deadline = ("+deadline+" * loan.type.repayTimePeriod)  and loan.type.repayTimeUnit = '"+ RepayConstants.RepayUnit.MONTH+"')";
		}
       // dayQuery = "(loan.deadline >= ("+(minDeadline*30+1)+" * loan.type.repayTimePeriod) and loan.deadline < ("+(maxDeadline*30)+" * loan.type.repayTimePeriod) and loan.type.repayTimeUnit = '"+ RepayConstants.RepayUnit.DAY+"')";

        deadlineQuery="( "+ monthQuery +")";
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

        //招标中且已经过期的排除
        String qryRaising="(loan.status in ('raising') and loan.expectTime>NOW())";
        //满标待审且已经过期的排除
        String qryRecheck="(loan.status in ('recheck') and loan.expectTime>NOW())";
        //等待开标且已经过期的排除
        String qryWaitVerify="(loan.status in ('dqgs','waiting_verify_affirm','waiting_verify_affirm_user') and loan.expectTime>NOW())";
        //前台发起的标没有expectTime
       // String qryWaitVerifyFore="(loan.status in ('waiting_verify') and (loan.expectTime>NOW() or loan.expectTime is null))";
        //去掉待审核的数据
        String qryWaitVerifyFore="(  loan.expectTime is null)";

        statusQuery="";
        switch (sId) {
            case 0://全部
//                statusQuery = "(loan.status not in ('verify_fail','cancel'))";
                statusQuery = "(loan.status in ('repaying','complete','financialLoan','waiting_recheck_verify')";
                statusQuery+= " or "+ qryRaising;
                statusQuery+= " or "+qryRecheck;
               // statusQuery+= " or "+qryWaitVerifyFore;
                statusQuery+= " or "+qryWaitVerify+")";
//                statusQuery = "(loan.status in ('waiting_verify') and (loan.expectTime>current_time() or loan.expectTime is null))";
                break;
            case 1://等待开标
                statusQuery = "("+qryWaitVerify;
                statusQuery += " or "+ qryWaitVerifyFore+")";
                break;
            case 2://招标中
                statusQuery = qryRaising;
                break;
            case 3://满标待审
                statusQuery = "((loan.status in ('financialLoan','waiting_recheck_verify'))";
                statusQuery+=" or "+ qryRecheck+")";
                break;
            case 4://还款中
                statusQuery = "(loan.status in ('repaying','wait_repay_verify','overdue','bad_debt'))";
                break;
            case 5://已还完
                statusQuery = "(loan.status ='complete')";
                break;
        }

        this.addRestriction(statusQuery);
        this.statusId = sId;
    }

    @Override
    protected void initExample() {
        Loan loan = new Loan();
        loan.setUser(new User());
        setExample(loan);
    }

	public Integer getMinAndMaxDeadline() {
		return minAndMaxDeadline;
	}
    
}
