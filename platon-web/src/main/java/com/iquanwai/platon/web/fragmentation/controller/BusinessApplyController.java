package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.resolver.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author nethunder
 * @version 2017-11-22
 */
@RestController
@RequestMapping("/rise/business")
public class BusinessApplyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/load/questions", method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadBusinessApplyQuestions(LoginUser loginUser){
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("获取申请问题");
        operationLogService.log(operationLog);

    }

}
