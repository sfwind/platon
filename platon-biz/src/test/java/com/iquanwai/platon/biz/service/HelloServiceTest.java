package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 7/15/15.
 */
public class HelloServiceTest extends TestBase {
    @Autowired
    private InterlocutionService interlocutionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private PlanService planService;

    @Test
    public void sendTest() {
    }

    @Autowired
    private CustomerService customerService;

    @Test
    public void teamLearningTest() {
        int login = customerService.loadContinuousLoginCount(12957);
        System.out.println(login);
    }

}
