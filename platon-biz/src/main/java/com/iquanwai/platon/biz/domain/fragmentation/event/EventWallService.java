package com.iquanwai.platon.biz.domain.fragmentation.event;

import com.iquanwai.platon.biz.po.common.EventWall;

import java.util.List;

/**
 * Created by justin on 17/6/5.
 */
public interface EventWallService {

    /**
     * 获取活动墙信息
     * @param openid 用户id
     * */
    List<EventWall> getEventWall(String openid);
}
