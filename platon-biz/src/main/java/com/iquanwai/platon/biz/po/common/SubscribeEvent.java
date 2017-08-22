package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by justin on 17/7/14.
 */
@Data
public class SubscribeEvent {
    private String scene;
    private String openid;
    private String event;

    /**
     * 事件关注类型
     */
    public static final String SUBSCRIBE = "subscribe";
    public static final String SCAN = "scan";

}
