package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.PrizeCard;

import java.util.List;

public interface PrizeCardService {
    PrizeCard loadNoOwnerPrizeCard(Integer profileId);

    boolean exchangePrizeCard(Integer profileId, Integer prizeCardId);

    /**
     * 领取年度礼品卡
     * @param cardNo
     * @param profileId
     * @return
     */
    String  receiveAnnualPrizeCards(String cardNo,Integer profileId);

    List<PrizeCard> generateAnnualPrizeCards(Integer profileId);

    /**
     * 是否成功领取预先生成的礼品卡
     * @param cardId
     * @param profileId
     * @return
     */
    String isPreviewCardReceived(String cardId,Integer profileId);

    void sendReceiveCardMsgSuccessful(String openid, String nickname);

    void sendReceivedAnnualMsgSuccessful(String openid,String nickName);


}
