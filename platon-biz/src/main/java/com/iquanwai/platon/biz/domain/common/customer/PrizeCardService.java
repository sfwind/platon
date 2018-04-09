package com.iquanwai.platon.biz.domain.common.customer;

import com.iquanwai.platon.biz.po.PrizeCard;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface PrizeCardService {
    /**
     * 获得还未过期的礼品卡
     */
    List<PrizeCard> loadNoOwnerPrizeCard(Integer profileId);

    /**
     * 是否是本人的礼品卡
     * @param cardNo
     * @param openid
     * */
    boolean ownerCheck(String cardNo, String openid);

    /**
     * 自动生成年终回顾礼品卡
     * @param profileId
     * */
    List<PrizeCard> generateAnnualPrizeCards(Integer profileId);

    /**
     * 领取卡片成功消息
     * @param nickName
     * @param openid
     * */
    void sendReceivedAnnualMsgSuccessful(String openid, String nickName);

    /**
     * 领取卡片失败消息
     * @param result
     * @param openid
     * */
    void sendReceivedAnnualFailureMsg(String openid, String result);

    /**
     * 通知老用户卡已经被领取
     *
     * @param cardNum
     */
    void sendAnnualOwnerMsg(String cardNum, String receiver);

}
