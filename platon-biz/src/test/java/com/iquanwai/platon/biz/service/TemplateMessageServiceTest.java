package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanServiceImpl;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanServiceImpl;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 16/10/12.
 */
public class TemplateMessageServiceTest extends TestBase {
    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private GeneratePlanServiceImpl generatePlanService;

    @Autowired
    private PlanServiceImpl planService;
    @Autowired
    private ProblemDao problemDao;

    @Test
    public void testSend(){
        Problem p = problemDao.load(Problem.class, 1);
        generatePlanService.sendWelcomeMsg("o5h6ywlXxHLmoGrLzH9Nt7uyoHbM", p);
    }

    @Test
    public void testSend2(){
        ImprovementPlan p = problemDao.load(ImprovementPlan.class, 11);
        planService.sendCloseMsg(p);
    }
}
