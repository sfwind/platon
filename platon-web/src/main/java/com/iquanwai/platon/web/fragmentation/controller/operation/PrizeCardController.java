package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.PrizeCardService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.PrizeCard;
import com.iquanwai.platon.biz.po.common.OperationLog;
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
     * 获得
     * @param loginUser
     * @return
     */
    @RequestMapping("/unused/cards")
    public ResponseEntity<Map<String,Object>> loadUnUsedPrizeCards(LoginUser loginUser){
        Assert.notNull(loginUser,"登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡片").function("加载未使用礼品卡").action("获得礼品卡");
        operationLogService.log(operationLog);
        List<PrizeCard> prizeCards = prizeCardService.loadPrizeCards(loginUser.getId());

        return WebUtils.result(prizeCards);
    }

    /**
     *检查卡片是否已经被领取
     * @param loginUser
     * @return
     */
    @RequestMapping("/card/received/{id}")
    public ResponseEntity<Map<String,Object>> setReceived(LoginUser loginUser,@PathVariable Integer id){

        Assert.notNull(loginUser,"登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡片").function("更新礼品卡").action("设置礼品卡领取者");
        operationLogService.log(operationLog);
        return WebUtils.result(prizeCardService.setReceived(loginUser.getOpenId(),id));
    }

    /**
     *
     * @param loginUser
     * @param id
     * @return
     */
    @RequestMapping("/card/shared/{id}")
    public ResponseEntity<Map<String,Object>> setShared(LoginUser loginUser,@PathVariable Integer id){
        Assert.notNull(loginUser);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("礼品卡片").function("设置分享").action("设置礼品卡已经分享过");
        operationLogService.log(operationLog);
        prizeCardService.setShared(id);
        return WebUtils.success();
    }
}
