package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupResult;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.DiscussDto;
import com.iquanwai.platon.web.fragmentation.dto.WarmupPracticeDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
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
 * Created by justin on 17/3/8.
 * 巩固练习相关的请求处理类
 */
@RestController
@RequestMapping("/rise/practice/warmup")
public class WarmupController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    private final static int DISCUSS_PAGE_SIZE = 100;


    @RequestMapping("/{id}")
    public ResponseEntity<Map<String, Object>> loadWarmup(LoginUser loginUser,
                                                          @PathVariable Integer id){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        WarmupPractice warmupPractice = practiceService.getWarmupPractice(id);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("打开巩固练习")
                .memo(id.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPractice);
    }

    @RequestMapping("/start/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> startWarmup(LoginUser loginUser,
                                                           @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getProblemId(), practicePlanId);
        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("打开巩固练习页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    @RequestMapping(value = "/answer/{practicePlanId}", method = RequestMethod.POST)
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
        planService.checkPlanComplete(improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("回答问题")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupResult);
    }

    @RequestMapping("/analysis/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> analysisWarmup(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractice(
                improvementPlan.getProblemId(), practicePlanId);
        List<Integer> questionIds = warmupPracticeList.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(improvementPlan.getId(), questionIds);
        setUserChoices(warmupPracticeList, submits);
        // 获取讨论信息
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(DISCUSS_PAGE_SIZE);
        Map<Integer, List<WarmupPracticeDiscuss>> discuss = practiceDiscussService.loadDiscuss(questionIds, page);
        setDiscuss(warmupPracticeList, discuss, loginUser.getOpenId());

        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("加载巩固练习解析页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDto);
    }

    private void setDiscuss(List<WarmupPractice> warmupPracticeList, Map<Integer, List<WarmupPracticeDiscuss>> discuss, String openid) {
        warmupPracticeList.stream().forEach(warmupPractice -> {
            List<WarmupPracticeDiscuss> list = discuss.get(warmupPractice.getId());
            list.stream().forEach(warmupPracticeDiscuss -> {
                //是否是学员本人的评论
                if(warmupPracticeDiscuss.getOpenid().equals(openid)){
                    warmupPracticeDiscuss.setIsMine(true);
                }
                warmupPracticeDiscuss.setRepliedOpenid(null);
                warmupPracticeDiscuss.setOpenid(null);
            });
            warmupPractice.setDiscussList(list);
        });
    }

    //根据用户提交记录匹配题目选项
    private void setUserChoices(List<WarmupPractice> warmupPracticeList, List<WarmupSubmit> submits) {
        for(WarmupSubmit warmupSubmit:submits){
            warmupPracticeList.stream().filter(warmupPractice -> warmupPractice.getId() == warmupSubmit.getQuestionId()).forEach(warmupPractice -> {
                String[] choices = warmupSubmit.getContent().split(",");
                List<Integer> choiceIds = Lists.newArrayList();
                for (String choice : choices) {
                    try {
                        choiceIds.add(Integer.parseInt(choice));
                    } catch (NumberFormatException e) {
                        LOGGER.error("No.{} warmup submit is invalid", warmupSubmit.getId());
                    }
                }
                warmupPractice.getChoiceList().stream().forEach(choice -> {
                    if (choiceIds.contains(choice.getId())) {
                        choice.setSelected(true);
                    } else {
                        choice.setSelected(false);
                    }
                });
            });
        }
    }

    @RequestMapping("/new/analysis/{practiceId}")
    public ResponseEntity<Map<String, Object>> newAnalysisWarmup(LoginUser loginUser,
                                                                 @PathVariable Integer practiceId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        WarmupPractice warmupPractice = practiceService.getWarmupPractice(practiceId);
        List<WarmupPractice> warmupPracticeList = Lists.newArrayList();
        warmupPracticeList.add(warmupPractice);
        List<Integer> questionIds = warmupPracticeList.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(improvementPlan.getId(), questionIds);
        setUserChoices(warmupPracticeList, submits);
        // 获取讨论信息
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(DISCUSS_PAGE_SIZE);
        Map<Integer, List<WarmupPracticeDiscuss>> discuss = practiceDiscussService.loadDiscuss(questionIds, page);
        setDiscuss(warmupPracticeList, discuss, loginUser.getOpenId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("打开巩固练习解析页")
                .memo(practiceId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeList.get(0));
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
            if(warmupPracticeDiscuss.getOpenid().equals(loginUser.getOpenId())){
                warmupPracticeDiscuss.setIsMine(true);
            }
            warmupPracticeDiscuss.setRepliedOpenid(null);
            warmupPracticeDiscuss.setOpenid(null);
        });
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("获取讨论")
                .memo(warmupPracticeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(discusses);
    }

    @RequestMapping(value = "/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> discuss(LoginUser loginUser, @RequestBody DiscussDto discussDto) {
        Assert.notNull(loginUser, "用户不能为空");

        if(discussDto.getComment()==null || discussDto.getComment().length()>300){
            LOGGER.error("{} 巩固练习讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discuss(loginUser.getOpenId(), discussDto.getWarmupPracticeId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("讨论")
                .memo(discussDto.getWarmupPracticeId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/delete/comment/{commentId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteComment(LoginUser loginUser, @PathVariable Integer commentId) {
        Assert.notNull(loginUser, "用户不能为空");

        practiceDiscussService.deleteComment(commentId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("巩固练习")
                .action("删除讨论")
                .memo(commentId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
