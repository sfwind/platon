package com.iquanwai.platon.biz.domain.common.subscribe;

import com.iquanwai.platon.biz.po.common.SubscribeRouterConfig;

public interface SubscribeRouterService {
    SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName);
}
