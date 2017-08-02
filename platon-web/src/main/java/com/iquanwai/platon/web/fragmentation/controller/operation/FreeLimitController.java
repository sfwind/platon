package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 限免推广
 * Created by xfduan on 2017/7/14.
 */
@RestController
@RequestMapping("/operation/free")
public class FreeLimitController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OperationService operationService;

    /**
     * 数据保存倾向，数据记录
     */
    @RequestMapping("/card/save/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> markSaveAttention(LoginUser loginUser, @PathVariable Integer knowledgeId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("图片分享").action("图片保存").memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    /**
     * 发送自动选限免课的客服消息
     */
    @RequestMapping("/choose/problem/msg")
    public ResponseEntity<Map<String, Object>> sendChooseProblemMsg(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        operationService.sendCustomerMsg(loginUser.getOpenId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("限免推广").function("自动选课").action("发送微信客服消息");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

}
