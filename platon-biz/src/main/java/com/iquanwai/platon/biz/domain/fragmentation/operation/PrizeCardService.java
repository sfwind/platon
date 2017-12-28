package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.PrizeCard;

import java.util.List;

public interface PrizeCardService {
    PrizeCard loadNoOwnerPrizeCard(Integer profileId);

    boolean exchangePrizeCard(Integer profileId, Integer prizeCardId);

    /**
     * 查询年度礼品卡
     * @param profileId
     * @return
     */
    List<PrizeCard> getAnnualPrizeCards(Integer profileId);

    /**
     * 领取礼品卡
     * @param id
     * @param profileId
     * @return
     */
    String  receiveAnnualPrizeCards(Integer id,Integer profileId);

    void generateAnnualPrizeCards(Integer profileId);

    Integer loadAnnualCounts(Integer profileId);

    /**
     * 是否成功领取预先生成的礼品卡
     * @param cardId
     * @param profileId
     * @return
     */
    String isPreviewCardReceived(String cardId,Integer profileId);

}
