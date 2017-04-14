package com.iquanwai.platon.job;

import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created by nethunder on 2017/2/27.
 */
@Component
public class RiseMemberJob {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RiseMemberDao riseMemberDao;

    @Scheduled(cron="${riseMemberJob.cron}")
    public void work(){
        logger.info("FragmentDailyDataJob start");
        refreshStatus();
        logger.info("FragmentDailyDataJob end");
    }

    // 刷新缓存，返回当前登录人数
    public Integer refreshStatus(){
        Collection<LoginUser> allUsers = LoginUserResolver.getAllUsers();
        for (LoginUser user : allUsers) {
            try {
                if (user.getRiseMember()) {
                    // 是会员，查询现在还是不是
                    RiseMember riseMember = riseMemberDao.validRiseMember(user.getOpenId());
                    if(riseMember == null){
                        // 不是会员了
                        user.setRiseMember(false);
                        logger.info("openId:{},expired member", user.getOpenId());
                    }
                }
            } catch (Exception e){
                logger.error("会员过期检查失败", e);
            }
        }
        return allUsers.size();
    }
}
