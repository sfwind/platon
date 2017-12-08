package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.PrizeCard;

import java.util.List;

public interface PrizeCardService {
    PrizeCard loadNoOwnerPrizeCard(Integer profileId);

    boolean exchangePrizeCard(Integer profileId, Integer prizeCardId);

    List<PrizeCard> loadPrizeCards(Integer profileId);

    Integer setReceived(String openId,Integer id);

    void setShared(Integer id);
}
