package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WechatMessage;

/**
 * Created by nethunder on 2017/8/30.
 */
public interface TheatreService {

    /**
     * 处理剧本活动消息
     * @param wechatMessage 微信台词
     */
    void handleTheatreMessage(WechatMessage wechatMessage);

    /**
     * 是否正在玩话剧游戏
     * @param wechatMessage 微信台词
     * @return 是否在玩游戏
     */
    Boolean isPlayingTheatre(WechatMessage wechatMessage);

    /**
     * 开始游戏
     * @param profile 用户
     */
    void startGame(Profile profile);
}
