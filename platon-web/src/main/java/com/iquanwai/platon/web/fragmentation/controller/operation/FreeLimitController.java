package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

/**
 * 限免推广
 * Created by xfduan on 2017/7/14.
 */
@RestController
@RequestMapping("/rise/operation/free")
public class FreeLimitController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OperationFreeLimitService operationFreeLimitService;
    @Autowired
    private OperationEvaluateService operationEvaluateService;
    @Autowired
    private PlanService planService;

    /**
     * 查看当前用户是否已经领取了此次活动的优惠券
     */
    @RequestMapping(value = "/coupon")
    public ResponseEntity<Map<String, Object>> hasGetTheCoupon(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        if (loginUser.getRiseMember() == 1) {
            return WebUtils.result(false);
        } else {
            Boolean result = operationFreeLimitService.hasGetTheCoupon(loginUser.getId());
            return WebUtils.result(result);
        }
    }

    /**
     * 用户提交问卷结果
     */
    @RequestMapping(value = "/submit/{score}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitEva(LoginUser loginUser, @PathVariable Integer score) {
        Assert.notNull(loginUser, "用户不能为空");
        FreeLimitResult result = new FreeLimitResult();
        Boolean learnBefore = planService.hasProblemPlan(loginUser.getId(), ConfigUtils.getTrialProblemId());
        if (learnBefore || loginUser.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
            result.setLearnFreeLimit(true);
        } else {
            result.setLearnFreeLimit(false);
        }

        Integer percent = getDefeatPercent(score);
        result.setPercent(percent);

        // 发消息比较慢,异步发送
        new Thread(() -> {
            operationEvaluateService.sendPromotionResult(loginUser.getId(), score, result.getLearnFreeLimit(), percent);
        }).start();

        Pair<String, String> pairs = operationEvaluateService.completeEvaluate(loginUser.getId(), score,
                result.getLearnFreeLimit(), percent);
        result.setResult(pairs.getLeft());
        result.setSuggestion(pairs.getRight());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("提交测评").memo(score.toString());
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }

    /**
     * 用户打开问卷
     */
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> initEva(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        operationEvaluateService.clickHref(loginUser.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("打开测评");
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    /**
     * 用户提交问卷结果
     */
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> shareResult(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("领取推广卡片");
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    private static Integer getDefeatPercent(Integer score) {
        switch (score) {
            case 0:
                return new Random().nextInt(20) + 10;
            case 1:
                return new Random().nextInt(20) + 30;
            case 2:
                return new Random().nextInt(10) + 50;
            case 3:
                return new Random().nextInt(10) + 60;
            case 4:
                return new Random().nextInt(10) + 70;
            case 5:
                return new Random().nextInt(10) + 80;
            case 6:
                return new Random().nextInt(5) + 90;
            case 7:
                return new Random().nextInt(5) + 95;
            default:
                return new Random().nextInt(10);
        }
    }
}
