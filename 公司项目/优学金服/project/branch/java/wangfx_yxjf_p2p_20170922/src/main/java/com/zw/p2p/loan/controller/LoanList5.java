package com.zw.p2p.loan.controller;

import com.zw.core.annotations.ScopeType;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Scope(ScopeType.VIEW)
public class LoanList5 extends LoanList{
    public LoanList5() {
        final String[] RESTRICTIONS = { "loan.id like #{loanList5.example.id}",
                "loan.repayType like #{loanList5.example.repayType}",
                "loan.status like #{loanList5.example.status}",
                "loan.name like #{loanList5.example.name}",
                "loan.rate >=#{loanList5.minRate}",
                "loan.rate <=#{loanList5.maxRate}",
                "loan.status in (' #{loanList5.example.status} ')",
                "loan.riskLevel like #{loanList5.example.riskLevel}",
                "loan.type like #{loanList5.example.type}",
                "loan.user.id = #{loanList5.example.user.id}",
                "loan.user.username like #{loanList5.example.user.username}",
                "loan.businessType like #{loanList5.example.businessType}",
                "loan.deadline >= #{loanList5.minDeadline}",
                "loan.deadline <= #{loanList5.maxDeadline}",
                "loan.money >= #{loanList5.minMoney}",
                "loan.money <= #{loanList5.maxMoney}",
                "loan.loanMoney >= #{loanList5.minLoanMoney}",
                "loan.loanMoney <= #{loanList5.maxLoanMoney}",
                "loan.commitTime >= #{loanList5.searchCommitMinTime}",
                "loan.commitTime <= #{loanList5.searchCommitMaxTime}",
                "loan.loanPurpose like #{loanList5.example.loanPurpose}" };
        setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
    }
}
