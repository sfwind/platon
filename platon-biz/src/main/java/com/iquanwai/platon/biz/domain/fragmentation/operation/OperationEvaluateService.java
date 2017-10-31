package com.iquanwai.platon.biz.domain.fragmentation.operation;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by xfduan on 2017/8/14.
 */
public interface OperationEvaluateService {

    /**
     * 点击链接
     */
    void clickHref(Integer profileId);
    /**
     * 完成测评
     */
    Pair<String, String> completeEvaluate(Integer profileId, Integer score, Boolean freeLimit, Integer percent);
    /**
     * 免费试用限免课权限校验
     */
    boolean checkTrialAuthority(Integer profileId);

    /**
     * 记录付费购买操作
     */
    void recordPayAction(Integer profileId);
    /**
     * 触发扫码时间，对应记录修改
     */
    void recordScan(Integer profileId, String source);
    /**
     * 微信后台推送结果卡片
     */
    String getResult(Integer profileId, Integer score, Integer percent);
}
