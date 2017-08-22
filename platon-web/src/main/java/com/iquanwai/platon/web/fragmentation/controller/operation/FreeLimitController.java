package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        Integer percent = operationEvaluateService.completeEvaluate(loginUser.getId(), score);
        result.setPercent(percent);
//        operationEvaluateService.sendPromotionResult(loginUser.getId(), score);
        Boolean learnBefore = planService.hasProblemPlan(loginUser.getId(), ConfigUtils.getTrialProblemId());
        if (learnBefore || loginUser.getRiseMember() == 1) {
            result.setLearnFreeLimit(true);
        } else {
            result.setLearnFreeLimit(false);
        }

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
    public ResponseEntity<Map<String, Object>> shareResult(LoginUser loginUser, @RequestBody FreeLimitResult result) {
        Assert.notNull(loginUser, "用户不能为空");

        operationEvaluateService.sendPromotionResult(loginUser.getId(), result.getScore(),
                result.getPercent(), result.getLearnFreeLimit());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("领取推广卡片").memo(result.getScore().toString());
        operationLogService.log(operationLog);

        return WebUtils.success();
    }
}
