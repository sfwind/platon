package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.common.Profile;

/**
 * Created by xfduan on 2017/7/14.
 */
public interface OperationService {

    /**
     * Promotion 记录新人 OpenId，以及所在推广 Level
     */
    void recordPromotionLevel(String openId, String scene);

    /**
     * 1、非新人报名成功，不做任何处理
     * 2、新人报名成功，记录报名信息，给其推广人发送推广成功信息
     * 3、若当前推广人成功推广人数超过 n 个，则发送优惠券信息
     */
    void recordOrderAndSendMsg(String openId, Integer newAction);

    String loadEssenceCard(Integer profileId, Integer problemId, Integer knowledgeId);

}
