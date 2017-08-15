package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.Constants;
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
    private CustomerMessageService customerMessageService;

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
        sendMessage(score, loginUser.getOpenId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("测评").action("提交测评").memo(score.toString());
        operationLogService.log(operationLog);

        return WebUtils.success();
    }

    private void sendMessage(Integer score, String openid) {
        if(score >= 8){
            customerMessageService.sendCustomerMessage(openid,
                    "你的洞察力基因在身体中的占比极高！一眼就能看透问题的本质。看来你已经不需要圈外同学的“洞察力强化包”了，千万别点开！",
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else if(score>=4){
            customerMessageService.sendCustomerMessage(openid,
                    "你的洞察力基因在身体中占比很高！但是有时候，你会觉得自己的努力和付出得不到应有的回报。试着换一个姿势努力吧，点击获取“洞察力强化包”，让你掌握职场努力的正确姿势，成为职场上的人生赢家！",
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        } else {
            customerMessageService.sendCustomerMessage(openid,
                    "你的洞察力基因在身体中占比较高！但是有时在工作中，你可能会觉得自己的辛苦努力，总是很难得到认可。试着换一个姿势努力吧，点击查看“洞察力强化”包！让你的努力变得四两拨千斤，迅速走上加薪升职之路。",
                    Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        }
    }

}
