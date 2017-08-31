package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.common.WechatMessage;

/**
 * Created by nethunder on 2017/8/30.
 */
public interface TheatreService {

    /**
     * 处理剧本活动消息
     * @param wechatMessage 微信消息
     */
    void handleTheatreMessage(WechatMessage wechatMessage);

    Boolean isPlayingTheatre(WechatMessage wechatMessage);
}
