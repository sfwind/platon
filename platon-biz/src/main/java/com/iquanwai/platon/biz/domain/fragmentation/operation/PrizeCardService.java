package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.PrizeCard;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface PrizeCardService {
    /**
     * 获得还未过期的礼品卡
     */
    List<PrizeCard> loadNoOwnerPrizeCard(Integer profileId);

    /**
     * 领取年度礼品卡
     *
     * @param cardNo
     * @param profileId
     * @return
     */
    String receiveAnnualPrizeCards(String cardNo, Integer profileId);

    /**
     * 是否是本人的礼品卡
     * @param cardNo
     * @param openid
     * */
    boolean ownerCheck(String cardNo, String openid);

    List<PrizeCard> generateAnnualPrizeCards(Integer profileId);

    /**
     * 是否成功领取预先生成的礼品卡
     *
     * @param cardId
     * @param profileId
     * @return
     */
    Pair<Integer, String> isPreviewCardReceived(String cardId, Integer profileId);

    void sendReceiveCardMsgSuccessful(String openid, String nickname);

    void sendReceivedAnnualMsgSuccessful(String openid, String nickName);

    void sendReceivedAnnualFailureMsg(String openid, String result);

    /**
     * 通知老用户卡已经被领取
     *
     * @param cardNum
     */
    void sendAnnualOwnerMsg(String cardNum, String receiver);

    /**
     * 根据年终回顾的数据生成礼品卡
     */
//    void genPrizeCardsByAnnSummary();


    boolean checkJanPay(Integer profileId);

}
