package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 发送自动选限免课的客服消息
     */
    @RequestMapping(value = "/choose/problem/msg", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendChooseProblemMsg(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        operationFreeLimitService.sendCustomerMsg(loginUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("自动选课").action("发送微信客服消息");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    /**
     * 查看当前用户是否已经领取了此次活动的优惠券
     */
    @RequestMapping(value = "/coupon")
    public ResponseEntity<Map<String, Object>> hasGetTheCoupon(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        if(loginUser.getRiseMember() == 1) {
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
        operationEvaluateService.completeEvaluate(loginUser.getId());
        operationEvaluateService.sendPromotionResult(loginUser.getId(), score);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("提交测评").memo(score.toString());
        operationLogService.log(operationLog);

        return WebUtils.success();
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

}
