package com.iquanwai.platon.job;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Component
public class DailyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;

    @Scheduled(cron="${dailyJob.cron}")
    public void work(){
        logger.info("DailyJob start");
        dispatchKey();
        logger.info("DailyJob end");
    }

    private void dispatchKey() {
        List<ImprovementPlan> improvementPlanList = planService.loadAllRunningPlan();
        improvementPlanList.stream().forEach(improvementPlan -> {
            Date date = DateUtils.afterDays(improvementPlan.getCloseDate(), 1);
            //过期自动结束训练
            if(date.before(new Date())){
                planService.completePlan(improvementPlan.getId());
            }else{
                Integer key = improvementPlan.getKeycnt();
                planService.updateKey(improvementPlan.getId(), key+1);
            }
        });

    }
}
