package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupResult;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.DiscussDto;
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
@RequestMapping("/rise/practice")
public class PracticeController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    private final static int DISCUSS_PAGE_SIZE = 20;

    @RequestMapping("/warmup/start/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> startWarmup(LoginUser loginUser,
                                                           @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getId(), practicePlanId);
        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("打开热身训练页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    @RequestMapping(value = "/warmup/answer/{practicePlanId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> answer(LoginUser loginUser,
                                                      @PathVariable Integer practicePlanId,
                                                      @RequestBody WarmupPracticeDto warmupPracticeDto){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        WarmupResult warmupResult;
        try {
            warmupResult = practiceService.answerWarmupPractice(
                    warmupPracticeDto.getPractice(), practicePlanId,
                    improvementPlan.getId(), loginUser.getOpenId());
        } catch (AnswerException e) {
            return WebUtils.error("您已做完这套练习");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("回答问题")
                .memo(practicePlanId.toString());
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
                .function("专题训练")
                .action("打开专题训练页")
                .memo(challengeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping("/warmup/analysis/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> analysisWarmup(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getId(), practicePlanId);
        List<Integer> questionIds = warmupPracticeList.stream().map(warmupPractice -> warmupPractice.getId()).collect(Collectors.toList());
        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(improvementPlan.getId(), questionIds);
        setUserChoices(warmupPracticeList, submits);
        // 获取讨论信息
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(DISCUSS_PAGE_SIZE);
        Map<Integer, List<WarmupPracticeDiscuss>> discuss = practiceDiscussService.loadDiscuss(questionIds, page);
        setDiscuss(warmupPracticeList, discuss);

        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("打开热身训练页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    private void setDiscuss(List<WarmupPractice> warmupPracticeList, Map<Integer, List<WarmupPracticeDiscuss>> discuss) {
        warmupPracticeList.stream().forEach(warmupPractice -> {
            List<WarmupPracticeDiscuss> list = discuss.get(warmupPractice.getId());
            list.stream().forEach(warmupPracticeDiscuss -> {
                warmupPracticeDiscuss.setRepliedOpenid(null);
                warmupPracticeDiscuss.setOpenid(null);
            });
            warmupPractice.setDiscussList(list);
        });
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

    @RequestMapping("/next/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> nextPractice(LoginUser loginUser, @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Practice practice = planService.nextPractice(practicePlanId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练")
                .action("打开下一训练")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(practice);
    }

    @RequestMapping(value = "/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> discuss(LoginUser loginUser, @RequestBody DiscussDto discussDto){
        Assert.notNull(loginUser, "用户不能为空");

        if(discussDto.getComment()==null && discussDto.getComment().length()>300){
            LOGGER.error("{} 热身训练讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discuss(loginUser.getOpenId(), discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/load/discuss/{warmupPracticeId}/{offset}")
    public ResponseEntity<Map<String, Object>> loadMoreDiscuss(LoginUser loginUser,
                                                               @PathVariable Integer warmupPracticeId,
                                                               @PathVariable Integer offset){
        Assert.notNull(loginUser, "用户不能为空");

        Page page = new Page();
        page.setPageSize(DISCUSS_PAGE_SIZE);
        page.setPage(offset);
        List<WarmupPracticeDiscuss> discusses = practiceDiscussService.loadDiscuss(warmupPracticeId, page);

        //清空openid
        discusses.stream().forEach(warmupPracticeDiscuss -> {
            warmupPracticeDiscuss.setRepliedOpenid(null);
            warmupPracticeDiscuss.setOpenid(null);
        });
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("热身训练")
                .action("获取讨论")
                .memo(warmupPracticeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(discusses);
    }
}
