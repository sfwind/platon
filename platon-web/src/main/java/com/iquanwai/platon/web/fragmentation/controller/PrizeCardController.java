package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.common.customer.PrizeCardService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.PrizeCardDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rise/prize")
public class PrizeCardController {
    @Autowired
    private PrizeCardService prizeCardService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/unreceived/cards")
    public ResponseEntity<Map<String, Object>> loadUnreceivedPrizeCard(UnionUser unionUser) {
        Assert.notNull(unionUser, "登录用户不能为空");

        List<PrizeCard> prizeCards = prizeCardService.loadNoOwnerPrizeCard(unionUser.getId());
        prizeCards.forEach(prizeCard -> {
            prizeCard.setReceiverProfileId(null);
        });
        return WebUtils.result(prizeCards);
    }

    @RequestMapping("/card/owner/check")
    public ResponseEntity<Map<String, Object>> receivePreviewCard(UnionUser unionUser, @RequestParam("cardId") String cardId) {
        Assert.notNull(unionUser, "登录用户不能为空");

        return WebUtils.result(prizeCardService.ownerCheck(cardId, unionUser.getOpenId()));
    }

    @RequestMapping("/card/send/message")
    public ResponseEntity<Map<String, Object>> sendMessage(UnionUser unionUser) {
        Assert.notNull(unionUser, "登录用户不能为空");

        Profile profile = accountService.getProfile(unionUser.getOpenId());

        if (profile == null) {
            return WebUtils.error("找不到该用户");
        }

        prizeCardService.sendReceivedAnnualMsgSuccessful(unionUser.getOpenId(), unionUser.getNickName());
        return WebUtils.success();
    }


    /**
     * 生成礼品卡，返回该用户对应的礼品卡信息
     */
    @RequestMapping(value = "/annual/summary/card", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generatePrizeCards(UnionUser unionUser, @RequestParam("riseId") String riseId) {
        Assert.notNull(unionUser, "登录用户不能为空");

        Profile profile = accountService.getProfileByRiseId(riseId);
        if (profile == null) {
            return WebUtils.error("用户不存在");
        }

        List<PrizeCard> prizeCards = prizeCardService.generateAnnualPrizeCards(profile.getId());
        prizeCards.sort((o1, o2) -> o1.getUsed() ? 1 : -1);
        List<PrizeCardDto> prizeCardDtos = Lists.newArrayList();

        prizeCards.forEach(prizeCard -> {
            PrizeCardDto prizeCardDto = new PrizeCardDto();
            BeanUtils.copyProperties(prizeCard, prizeCardDto);
            prizeCardDto.setReceived(prizeCard.getUsed());
            prizeCardDto.setRiseId(null);
            prizeCardDtos.add(prizeCardDto);
        });
        return WebUtils.result(prizeCardDtos);
    }

}
