package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupResult;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import com.iquanwai.platon.web.fragmentation.dto.WarmupPracticeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 * 各类训练相关的请求处理类
 */
@RestController
@RequestMapping("/practice")
public class PracticeController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/warmup/start/{series}/{sequence}")
    public ResponseEntity<Map<String, Object>> startWarmup(LoginUser loginUser,
                                                           @PathVariable Integer series,
                                                           @PathVariable Integer sequence){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getId(), series, sequence);
        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("打开热身训练页");
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    @RequestMapping(value = "/warmup/answer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> answer(LoginUser loginUser,
                                                      @RequestBody WarmupPracticeDto warmupPracticeDto){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        WarmupResult warmupResult = null;
        try {
            warmupResult = practiceService.answerWarmupPractice(
                    warmupPracticeDto.getPractice(), improvementPlan.getId(), loginUser.getOpenId());
        } catch (AnswerException e) {
            return WebUtils.error("您已做完这套练习");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("回答问题");
        operationLogService.log(operationLog);
        return WebUtils.result(warmupResult);
    }

    @RequestMapping("/application/start/{applicationId}")
    public ResponseEntity<Map<String, Object>> startApplication(LoginUser loginUser,
                                                                @PathVariable Integer applicationId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("打开应用训练页")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(applicationPractice);
    }

    @RequestMapping("/challenge/start/{challengeId}")
    public ResponseEntity<Map<String, Object>> startChallengeApplication(LoginUser loginUser,
                                                                         @PathVariable Integer challengeId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId,
                improvementPlan.getOpenid(), improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("打开挑战训练页")
                .memo(challengeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping("/warmup/analysis/{series}/{sequence}")
    public ResponseEntity<Map<String, Object>> analysisWarmup(LoginUser loginUser,
                                                           @PathVariable Integer series,
                                                           @PathVariable Integer sequence){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getId(), series, sequence);
        List<Integer> questionIds = warmupPracticeList.stream().map(warmupPractice -> warmupPractice.getId()).collect(Collectors.toList());
        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(improvementPlan.getId(), questionIds);
        setUserChoices(warmupPracticeList, submits);
        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("打开热身训练页");
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    //根据用户提交记录匹配题目选项
    private void setUserChoices(List<WarmupPractice> warmupPracticeList, List<WarmupSubmit> submits) {
        for(WarmupSubmit warmupSubmit:submits){
            for(WarmupPractice warmupPractice:warmupPracticeList){
                if(warmupPractice.getId()==warmupSubmit.getQuestionId()){
                    String[] choices = warmupSubmit.getContent().split(",");
                    List<Integer> choiceIds = Lists.newArrayList();
                    for(String choice:choices){
                        try {
                            choiceIds.add(Integer.parseInt(choice));
                        }catch (NumberFormatException e){
                            LOGGER.error("No.{} warmup submit is invalid", warmupSubmit.getId());
                        }
                    }
                    warmupPractice.getChoiceList().stream().forEach(choice -> {
                        if(choiceIds.contains(choice.getId())){
                            choice.setSelected(true);
                        }else{
                            choice.setSelected(false);
                        }
                    });
                }
            }
        }
    }

    @RequestMapping("/next")
    public ResponseEntity<Map<String, Object>> nextPractice(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Practice practice = planService.nextPractice(improvementPlan);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练")
                .action("打开下一训练");
        operationLogService.log(operationLog);
        return WebUtils.result(practice);
    }

}
