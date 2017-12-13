package com.iquanwai.platon.biz.domain.common.message;

import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.ActivityPopupMessageDao;
import com.iquanwai.platon.biz.po.common.ActivityPopupMessage;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by justin on 2017/12/13.
 */
@Service
public class ActivityMessageServiceImpl implements ActivityMessageService {
    @Autowired
    private ActivityPopupMessageDao activityPopupMessageDao;
    @Autowired
    private RedisUtil redisUtil;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String LOGIN_REDIS_KEY = "login:";
    private static final String WELCOME_MSG_REDIS_KEY = "welcome:msg:";

    @Override
    public ActivityMsg getWelcomeMessage(Integer profileId) {
        String msg = redisUtil.get(WELCOME_MSG_REDIS_KEY + profileId);
        ActivityMsg activityMsg = null;
        if (msg != null) {
            logger.info("删除key {}", WELCOME_MSG_REDIS_KEY + profileId);
            redisUtil.deleteByKey(WELCOME_MSG_REDIS_KEY + profileId);
            String json = ConfigUtils.getWelcomeMsg();
            Gson gson = new Gson();
            activityMsg = gson.fromJson(json, ActivityMsg.class);
        }

        return activityMsg;
    }

    @Override
    public void loginMsg(Integer profileId) {
        try {
            String json = ConfigUtils.getWelcomeMsg();
            if (json != null) {
                Gson gson = new Gson();
                ActivityMsg msg = gson.fromJson(json, ActivityMsg.class);
                //判断是否定向推送
                if (msg.getEventKey() != null) {
                    ActivityPopupMessage activityPopupMessage = activityPopupMessageDao.load(profileId, msg.getEventKey());
                    //该用户不再定向推送列表中,不用推送
                    if (activityPopupMessage == null) {
                        return;
                    }
                }
                Date start = DateUtils.parseStringToDateTime(msg.getStartTime());
                Date end = DateUtils.parseStringToDateTime(msg.getEndTime());
                //获取最后登录时间
                String lastLoginTime = redisUtil.get(LOGIN_REDIS_KEY + profileId);
                //活动未过期 且已开始
                if (end.after(new Date()) && start.before(new Date())) {
                    //很久未登录
                    if (lastLoginTime == null) {
                        //保存60秒
                        logger.info("{}很久未登录", profileId);
                    } else {
                        Date lastLogin = DateUtils.parseStringToDateTime(lastLoginTime);
                        //上次登录时间早于活动开始时间
                        if (lastLogin.before(start)) {
                            //保存60秒
                            logger.info("{}上次登录时间早于活动时间", profileId);
                            redisUtil.set(WELCOME_MSG_REDIS_KEY + profileId, true, 60L);
                        } else {
                            logger.info("{}上次登录时间晚于活动时间", profileId);
                        }
                    }
                } else {
                    logger.info("活动已过期", profileId);
                }
            }

            //保存30天最后登录时间
            redisUtil.set(LOGIN_REDIS_KEY + profileId,
                    DateUtils.parseDateTimeToString(new Date()), 60 * 60 * 24 * 30L);
        } catch (Exception e) {
            logger.error("加载首屏消息错误", e);
        }
    }
}
