package com.iquanwai.platon.biz.domain.common.message;

/**
 * Created by justin on 2017/12/13.
 */
public interface ActivityMessageService {

    /**
     * 加载首屏活动消息
     * */
    ActivityMsg getWelcomeMessage(Integer profileId);

    /**
     * 计算登录时弹出活动消息
     * */
    void loginMsg(Integer profileId);
}
