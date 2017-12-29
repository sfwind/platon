package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import com.iquanwai.platon.mq.LoginUserUpdateReceiver;
import com.iquanwai.platon.web.fragmentation.dto.OpenStatusDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Map;

/**
 * Created by justin on 17/7/25.
 */
@RestController
@RequestMapping("/rise/open")
public class TutorialController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher rabbitMQPublisher;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(LoginUserUpdateReceiver.TOPIC);
    }

    private void sendMqMessage(String openid) {
        try {
            rabbitMQPublisher.publish(openid);
        } catch (ConnectException e) {
            LOGGER.error("mq连接失败", e);
        }
    }

    @RequestMapping(value = "/navigator", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openNavigator(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        int count = accountService.updateOpenNavigator(loginUser.getId());
        if (count > 0) {
            sendMqMessage(loginUser.getOpenId());
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/rise", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openRise(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        int count = accountService.updateOpenRise(loginUser.getId());
        if (count > 0) {
            sendMqMessage(loginUser.getOpenId());
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/application", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openComprehension(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        int count = accountService.updateOpenApplication(loginUser.getId());
        if (count > 0) {
            sendMqMessage(loginUser.getOpenId());
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/consolidation", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openConsolidation(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        int count = accountService.updateOpenConsolidation(loginUser.getId());
        if (count > 0) {
            sendMqMessage(loginUser.getOpenId());
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/welcome", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openWelcome(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        int count = accountService.updateOpenWelcome(loginUser.getId());
        if (count > 0) {
            sendMqMessage(loginUser.getOpenId());
        }
        return WebUtils.result(loginUser.getRiseMember());
    }

    @RequestMapping("/status")
    public ResponseEntity<Map<String, Object>> getOpenStatus(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("数据")
                .action("查看打开状态");
        operationLogService.log(operationLog);
        OpenStatusDto dto = new OpenStatusDto();
        if (!loginUser.getOpenApplication() || !loginUser.getOpenConsolidation()
                || !loginUser.getOpenRise() || !loginUser.getOpenNavigator()) {
            // 没有点开其中一个
            Profile profile = accountService.getProfile(loginUser.getId());
            loginUser.setOpenRise(profile.getOpenRise());
            loginUser.setOpenConsolidation(profile.getOpenConsolidation());
            loginUser.setOpenApplication(profile.getOpenApplication());
            loginUser.setOpenNavigator(profile.getOpenNavigator());
        }

        dto.setOpenRise(loginUser.getOpenRise());
        dto.setOpenConsolidation(loginUser.getOpenConsolidation());
        dto.setOpenApplication(loginUser.getOpenApplication());
        dto.setOpenNavigator(loginUser.getOpenNavigator());
        return WebUtils.result(dto);
    }
}
