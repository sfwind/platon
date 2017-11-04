package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleInitDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleQuestionDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author justin
 * @version 2017/11/4
 */
@RestController
@RequestMapping("/rise/schedule")
public class ScheduleController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestions(LoginUser loginUser, @RequestBody ScheduleInitDto initDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习")
                .function("课程表")
                .action("初始化课程表");
        operationLogService.log(operationLog);
        Assert.notNull(initDto);
        List<ScheduleQuestionDto> questions = initDto.getQuestionList();
        
        return WebUtils.success();
    }


}
