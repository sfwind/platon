package com.iquanwai.platon.biz.domain.operation;

/**
 * Created by xfduan on 2017/7/14.
 */
public interface OperationFreeLimitService {

    /**
     * Promotion 记录新人 OpenId，以及所在推广 Level
     */
    void recordPromotionLevel(String openId, String scene);

    /**
     * 是否获得推广优惠券
     */
    Boolean hasGetTheCoupon(Integer profileId);
}
