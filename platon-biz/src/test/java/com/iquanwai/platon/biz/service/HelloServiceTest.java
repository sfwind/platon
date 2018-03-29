package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
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
    @Autowired
    private CustomerMessageService customerMessageService;
    @Test
    public void sendTest() {
    }

    @Test
    public void teamLearningTest() {
        customerMessageService.sendCustomerMessage(accountService.getProfile(30).getOpenid(), ConfigUtils.getTeamPromotionCodeImage(), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
    }
}
