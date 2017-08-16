package com.iquanwai.platon.biz.domain.fragmentation.operation;

/**
 * Created by xfduan on 2017/8/14.
 */
public interface OperationEvaluateService {

    void clickHref(Integer profileId);

    void completeEvaluate(Integer profileId);

    boolean checkTrialAuthority(Integer profileId);

    void recordPayAction(Integer profileId);

    void startEvaluate(Integer profileId);

    void recordScan(Integer profileId, String source);

    void sendPromotionResult(Integer profileId, Integer score);

    boolean hasParticipateEvaluate(Integer profileId);
}
