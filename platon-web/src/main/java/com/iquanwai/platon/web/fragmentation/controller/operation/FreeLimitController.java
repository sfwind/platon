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

    @RequestMapping("/promotion/{openId}/{scene}")
    public ResponseEntity<Map<String, Object>> successPromotion(LoginUser loginUser, @PathVariable String openId, @PathVariable String scene) {
        operationService.recordPromotionLevel(openId, scene);
        return WebUtils.success();
    }

    @RequestMapping("/order/{openId}/{action}")
    public ResponseEntity<Map<String, Object>> orderAndSendMsg(LoginUser loginUser, @PathVariable String openId, @PathVariable Integer action) {
        operationService.recordOrderAndSendMsg(openId, action);
        return WebUtils.success();
    }

}
