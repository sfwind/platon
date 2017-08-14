package com.iquanwai.platon.biz.domain.fragmentation.operation;

/**
 * Created by xfduan on 2017/7/14.
 */
public interface OperationFreeLimitService {

    /**
     * Promotion 记录新人 OpenId，以及所在推广 Level
     */
    void recordPromotionLevel(String openId, String scene);

    /**
     * 初始化第一层用户
     * @param openId 用户OpenId
     * @param riseMember 是否会员
     */
    void initFirstPromotionLevel(String openId, Integer riseMember);

    /**
     * 1、非新人报名成功，不做任何处理
     * 2、新人报名成功，记录报名信息，给其推广人发送推广成功信息
     * 3、若当前推广人成功推广人数超过 n 个，则发送优惠券信息
     */
    void recordOrderAndSendMsg(String openId, Integer newAction);

    /**
     * 自动选课后,发送开课消息
     */
    void sendCustomerMsg(String openId);

    Boolean hasGetTheCoupon(Integer profileId);
}
