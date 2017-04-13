package com.iquanwai.platon.job;

import com.iquanwai.platon.web.resolver.LoginUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by nethunder on 2017/2/27.
 */
@Component
public class RiseMemberJob {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private LoginUserResolver resolver;

    @Scheduled(cron="${riseMemberJob.cron}")
    public void work(){
        logger.info("FragmentDailyDataJob start");
        resolver.refreshRiseMember();
        logger.info("FragmentDailyDataJob end");
    }
}
