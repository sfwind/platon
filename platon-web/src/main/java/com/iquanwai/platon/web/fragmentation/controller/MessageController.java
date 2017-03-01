package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.NotifyMessage;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/27.
 */
@RestController
@RequestMapping("/rise/message")
public class MessageController {
    @Autowired
    private PracticeDiscussService practiceDiscussService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private OperationLogService operationLogService;

    private final static int MESSAGE_PER_PAGE = 20;

    @RequestMapping("/warmup/discuss/reply/{discussId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer discussId){
        Assert.notNull(loginUser, "用户不能为空");
        WarmupPracticeDiscuss warmupPracticeDiscuss = practiceDiscussService.loadDiscuss(discussId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("讨论区回复")
                .action("打开讨论区回复页")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDiscuss);
    }

    @RequestMapping("/load/{offset}")
    public ResponseEntity<Map<String, Object>> loadMessage(LoginUser loginUser, @PathVariable Integer offset){
        Assert.notNull(loginUser, "用户不能为空");
        Page page = new Page();
        page.setPageSize(MESSAGE_PER_PAGE);
        page.setPage(offset);
        List<NotifyMessage> notifyMessage = messageService.getNotifyMessage(loginUser.getOpenId(), page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("打开消息中心")
                .action("加载消息");
        operationLogService.log(operationLog);
        return WebUtils.result(notifyMessage);
    }

    @RequestMapping(value = "/read/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> readMessage(LoginUser loginUser, @PathVariable Integer id){
        Assert.notNull(loginUser, "用户不能为空");
        messageService.readMessage(id);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("打开消息中心")
                .action("读消息")
                .memo(id.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        messageService.mark(loginUser.getOpenId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("打开消息中心")
                .action("标记消息非最新");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
