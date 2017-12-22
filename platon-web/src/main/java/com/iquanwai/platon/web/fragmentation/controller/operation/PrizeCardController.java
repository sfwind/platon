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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * @param guestUser
     * @param riseId
     * @return
     */
    @RequestMapping("/load/annual/{riseId}")
    public ResponseEntity<Map<String,Object>> loadAnnualPrizeCards(GuestUser guestUser, @PathVariable String riseId){
        System.out.println("openId:"+guestUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId()).module("礼品卡管理").function("加载礼品卡").action("加载礼品卡");
        operationLogService.log(operationLog);
        Integer currentId;
        //判断是自己的礼品卡还是ta的礼品卡
        if(riseId.equals("0")){
            currentId = accountService.getProfile(guestUser.getOpenId()).getId();
        }else{
            Profile profile = accountService.getProfileByRiseId(riseId);
            if(profile == null){
                return WebUtils.result(Lists.newArrayList());
            }
            currentId = profile.getId();
        }
        System.out.println();
        Profile profile = accountService.getProfile(currentId);
        List<PrizeCard> prizeCards =  prizeCardService.getAnnualPrizeCards(currentId);
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
     * @param loginUser
     * @param cardId
     * @return
     */
    @RequestMapping("/annual/receive/{cardId}")
    public ResponseEntity<Map<String,Object>> receiveAnnualCards(LoginUser loginUser,@PathVariable  Integer cardId){
        //TODO:判断是否进行关注
        Assert.notNull(loginUser,"登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡管理").function("领取礼品卡").action("领取礼品卡");
        operationLogService.log(operationLog);
        String result = prizeCardService.receiveAnnualPrizeCards(cardId,loginUser.getId());
        return WebUtils.result(result);
    }
}
