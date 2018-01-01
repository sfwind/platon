package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.operation.PrizeCardService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.PrizeCardDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rise/operation/prize")
public class PrizeCardController {

    @Autowired
    private PrizeCardService prizeCardService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadPrizeCard(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("礼品卡片")
                .function("优胜奖页面")
                .action("获取无主优胜奖");
        operationLogService.log(operationLog);

        PrizeCard prizeCard = prizeCardService.loadNoOwnerPrizeCard(loginUser.getId());
        if (prizeCard == null) {
            return WebUtils.error("抱歉，没有找到您的对应奖励哦");
        } else {
            return WebUtils.result(prizeCard);
        }
    }

    @RequestMapping("/exchange/{prizeCardId}")
    public ResponseEntity<Map<String, Object>> updateExchangeInfo(LoginUser loginUser, @PathVariable Integer prizeCardId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        boolean exchangeResult = prizeCardService.exchangePrizeCard(loginUser.getId(), prizeCardId);
        if (exchangeResult) {
            return WebUtils.success();
        } else {
            return WebUtils.error("奖励领取失败");
        }
    }

    @RequestMapping("/card/preview")
    public ResponseEntity<Map<String, Object>> receivePreviewCard(LoginUser loginUser, @RequestParam("cardId") String cardId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("领取预先礼品卡").action("领取预先礼品卡");
        operationLogService.log(operationLog);
        String result = prizeCardService.isPreviewCardReceived(cardId, loginUser.getId());
        if ("恭喜您获得该礼品卡".equals(result)) {
            return WebUtils.success();
        } else {
            return WebUtils.error(result);
        }
    }

    @RequestMapping("/card/send/message")
    public ResponseEntity<Map<String, Object>> sendMessage(LoginUser loginUser) {
        Asserts.notNull(loginUser, "登录用户不能为空");

        OperationLog operationLog = OperationLog.create().module("礼品卡管理").function("发送模板消息").action("发送成功领取消息");
        operationLogService.log(operationLog);
        Profile profile = accountService.getProfile(loginUser.getOpenId());

        if (profile == null) {
            return WebUtils.error("找不到该用户");
        }

        prizeCardService.sendReceiveCardMsgSuccessful(loginUser.getOpenId(), loginUser.getWeixinName());
        return WebUtils.success();
    }


    /**
     * 生成礼品卡，返回该用户对应的礼品卡信息
     */
    @RequestMapping(value = "/annual/summary/card", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generatePrizeCards(GuestUser guestUser, @RequestParam("riseId") String riseId) {
        Assert.notNull(guestUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId()).module("礼品卡管理").function("生成礼品卡").action("生成礼品卡");
        operationLogService.log(operationLog);
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


    /**
     * 领取礼品卡
     */
    @RequestMapping(value = "/annual/summary/card/receive", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> receiveAnnualCard(GuestUser guestUser, @RequestParam("prizeCardNo") String prizeCardNo) {
        //没有关注则弹出二维码
        if (guestUser == null || guestUser.getSubscribe() == null || guestUser.getSubscribe() == 0) {
            //没有注册
            String callback = "callback";
            String key = "annual_prize_card_" + prizeCardNo;
            String qrCode = accountService.createSubscribePush(guestUser != null ? guestUser.getOpenId() : null, callback, key);
            return WebUtils.error(201, qrCode);
        }

        OperationLog operationLog = OperationLog.create()
                .openid(guestUser.getOpenId()).module("礼品卡管理").function("领取礼品卡").action("领取礼品卡");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfile(guestUser.getOpenId());
        if (profile == null) {
            return WebUtils.error("找不到该用户");
        }
        //返回领取结果
        else {
            String result = prizeCardService.receiveAnnualPrizeCards(prizeCardNo, profile.getId());
            if ("领取成功".equals(result)) {
                prizeCardService.sendReceivedAnnualMsgSuccessful(profile.getOpenid(), profile.getNickname());
                return WebUtils.success();
            } else {
                prizeCardService.sendReceivedAnnualFailureMsg(profile.getOpenid(), result);
                return WebUtils.error(result);
            }
        }
    }

}
