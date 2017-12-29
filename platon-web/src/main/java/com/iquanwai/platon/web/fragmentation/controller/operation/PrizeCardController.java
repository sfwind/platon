package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.operation.PrizeCardService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.PrizeCardDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    /**
     * 加载年度礼品卡
     *
     * @param guestUser
     * @param riseId
     * @return
     */
    @RequestMapping("/load/annual/{riseId}")
    public ResponseEntity<Map<String, Object>> loadAnnualPrizeCards(GuestUser guestUser, @PathVariable String riseId) {
        System.out.println("openId:" + guestUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId()).module("礼品卡管理").function("加载礼品卡").action("加载礼品卡");
        operationLogService.log(operationLog);
        Integer currentId;
        Profile targetProfile = accountService.getProfileByRiseId(riseId);
        if (targetProfile == null) {
            return WebUtils.result(Lists.newArrayList());
        }
        currentId = targetProfile.getId();

        Profile profile = accountService.getProfile(currentId);
        List<PrizeCard> prizeCards = prizeCardService.getAnnualPrizeCards(currentId);
        List<PrizeCardDto> prizeCardDtos = Lists.newArrayList();

        prizeCards.forEach(prizeCard -> {
            PrizeCardDto prizeCardDto = new PrizeCardDto();
            prizeCardDto.setId(prizeCard.getId());
            prizeCardDto.setRiseId(profile.getRiseId());
            prizeCardDto.setUsed(prizeCard.getUsed());
            prizeCardDtos.add(prizeCardDto);
        });

        return WebUtils.result(prizeCardDtos);
    }

    @RequestMapping("/load/annual")
    public ResponseEntity<Map<String, Object>> loadPersonalAnnualPrizeCards(LoginUser loginUser) {
        System.out.println("openId:" + loginUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("加载礼品卡").action("加载礼品卡");
        operationLogService.log(operationLog);
        Integer currentProfileId = loginUser.getId();

        Profile profile = accountService.getProfile(currentProfileId);
        List<PrizeCard> prizeCards = prizeCardService.getAnnualPrizeCards(currentProfileId);
        List<PrizeCardDto> prizeCardDtos = Lists.newArrayList();

        prizeCards.forEach(prizeCard -> {
            PrizeCardDto prizeCardDto = new PrizeCardDto();
            prizeCardDto.setId(prizeCard.getId());
            prizeCardDto.setRiseId(profile.getRiseId());
            prizeCardDto.setUsed(prizeCard.getUsed());
            prizeCardDtos.add(prizeCardDto);
        });

        return WebUtils.result(prizeCardDtos);
    }


    /**
     * 领取礼品卡
     *
     * @param loginUser
     * @param cardId
     * @return
     */
    @RequestMapping("/annual/receive/{cardId}")
    public ResponseEntity<Map<String, Object>> receiveAnnualCards(LoginUser loginUser, @PathVariable Integer cardId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("领取礼品卡").action("领取礼品卡");
        operationLogService.log(operationLog);
        String result = prizeCardService.receiveAnnualPrizeCards(cardId, loginUser.getId());
        return WebUtils.result(result);
    }

    @RequestMapping("/annual/load/count")
    public ResponseEntity<Map<String, Object>> loadAnnualCounts(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("加载礼品卡数量").action("加载礼品卡数量");
        operationLogService.log(operationLog);
        return WebUtils.result(prizeCardService.loadAnnualCounts(loginUser.getId()));
    }

    @RequestMapping("/card/preview")
    public ResponseEntity<Map<String, Object>> receivePreviewCard(LoginUser loginUser, @RequestParam("cardId") String cardId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("领取预先礼品卡").action("领取预先礼品卡");
        operationLogService.log(operationLog);
        String result = prizeCardService.isPreviewCardReceived(cardId,loginUser.getId());
        if("恭喜您获得该礼品卡".equals(result)){
            return WebUtils.success();
        }
        else {
            return WebUtils.error(result);
        }
    }

    @RequestMapping("/card/send/message")
    public ResponseEntity<Map<String,Object>> sendMessage(LoginUser loginUser){
        Asserts.notNull(loginUser, "登录用户不能为空");

        OperationLog operationLog = OperationLog.create().module("礼品卡管理").function("发送模板消息").action("发送成功领取消息");
        operationLogService.log(operationLog);
        Profile profile = accountService.getProfile(loginUser.getOpenId());

        if(profile==null){
            return WebUtils.error("找不到该用户");
        }

        prizeCardService.sendReceiveCardMsgSuccessful(loginUser.getOpenId(), loginUser.getWeixinName());
        return WebUtils.success();
    }
}
