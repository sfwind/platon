package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.ChallengePractice;
import com.iquanwai.platon.biz.po.OperationLog;
import com.iquanwai.platon.resolver.LoginUser;
import com.iquanwai.platon.util.WebUtils;
import com.iquanwai.platon.web.fragmentation.dto.ChallengeSubmitDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 16/12/11.
 */
@RestController
@RequestMapping("/challenge")
public class PCChallengeController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/start/{code}")
    public ResponseEntity<Map<String, Object>> loadChallenge(LoginUser loginUser,
                                                             @PathVariable String code){

        Assert.notNull(loginUser, "用户不能为空");
        ChallengePractice challengePractice = practiceService.getChallengePractice(code);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("打开PC挑战训练页")
                .memo(code);
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping("/submit/{code}")
    public ResponseEntity<Map<String, Object>> submit(LoginUser loginUser,
                                                      @PathVariable String code,
                                                      @RequestBody ChallengeSubmitDto challengeSubmitDto){

        Assert.notNull(loginUser, "用户不能为空");
        Boolean result = practiceService.submit(code, challengeSubmitDto.getAnswer());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("提交挑战训练答案")
                .memo(code);
        operationLogService.log(operationLog);
        if(result) {
            return WebUtils.success();
        }else{
            return WebUtils.error("提交失败");
        }
    }

}
