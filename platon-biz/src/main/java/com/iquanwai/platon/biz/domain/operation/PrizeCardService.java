package com.iquanwai.platon.biz.domain.operation;

import com.iquanwai.platon.biz.po.PrizeCard;

public interface PrizeCardService {
    PrizeCard loadNoOwnerPrizeCard(Integer profileId);

    boolean exchangePrizeCard(Integer profileId, Integer prizeCardId);
}
