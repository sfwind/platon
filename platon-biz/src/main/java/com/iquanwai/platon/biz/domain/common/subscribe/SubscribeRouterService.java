package com.iquanwai.platon.biz.domain.common.subscribe;

import com.iquanwai.platon.biz.po.common.SubscribeRouterConfig;

public interface SubscribeRouterService {
    /**
     * 根据当前 url 获取跳转路由配置
     */
    SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName);
}
