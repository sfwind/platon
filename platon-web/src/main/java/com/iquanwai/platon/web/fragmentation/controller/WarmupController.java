package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.*;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.WarmupPracticeDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(description = "选择题相关接口")
@RequestMapping("/rise/practice/warmup")
public class WarmupController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "获取某个选择题", response = WarmupPractice.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "选择题id")})
    public ResponseEntity<Map<String, Object>> loadWarmup(LoginUser loginUser,
                                                          @PathVariable Integer id) {
        Assert.notNull(loginUser, "用户不能为空");
        List<ImprovementPlan> improvementPlan = planService.getRunningPlan(loginUser.getId());
        if (improvementPlan.size() == 0) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        WarmupPractice warmupPractice = practiceService.getWarmupPractice(id);

        return WebUtils.result(warmupPractice);
    }

    @RequestMapping(value = "/answer/{practicePlanId}", method = RequestMethod.POST)
    @ApiOperation(value = "回答选择题", response = WarmupResult.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> answer(LoginUser loginUser,
                                                      @PathVariable Integer practicePlanId,
                                                      @RequestBody WarmupPracticeDto warmupPracticeDto) {
        Assert.notNull(loginUser, "用户不能为空");

        WarmupResult warmupResult;
        try {
            warmupResult = practiceService.answerWarmupPractice(
                    warmupPracticeDto.getPractice(), practicePlanId, loginUser.getId());
        } catch (AnswerException e) {
            return WebUtils.error("您已做完这套练习");
        }
        planService.checkPlanComplete(practicePlanId);

        return WebUtils.result(warmupResult);
    }

    @RequestMapping(value = "/analysis/{practicePlanId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取选择题解析", response = WarmupPracticeDto.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> analysisWarmup(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        List<WarmupPractice> warmupPracticeList = practiceService.getWarmupPractices(practicePlanId);
        List<Integer> questionIds = warmupPracticeList.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(practicePlanId, questionIds);
        setUserChoices(warmupPracticeList, submits);
        // 获取讨论信息
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        Map<Integer, List<WarmupComment>> discuss = practiceDiscussService.loadDiscuss(loginUser.getId(), questionIds, page);
        setDiscuss(warmupPracticeList, discuss);

        WarmupPracticeDto warmupPracticeDto = new WarmupPracticeDto();
        warmupPracticeDto.setPractice(warmupPracticeList);

        return WebUtils.result(warmupPracticeDto);
    }

    @RequestMapping(value = "/analysis/priority/{practicePlanId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载选择题的评论", response = WarmupDiscussDistrict.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> loadWarmUpPriorityDiscuss(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        List<WarmupPractice> warmUpPracticeList = practiceService.getWarmupPractices(practicePlanId);
        List<Integer> questionIds = warmUpPracticeList.stream().map(WarmupPractice::getId).collect(Collectors.toList());

        // 获取用户提交
        List<WarmupSubmit> submits = practiceService.getWarmupSubmit(practicePlanId, questionIds);
        setUserChoices(warmUpPracticeList, submits);

        Map<Integer, WarmupDiscussDistrict> warmUpDiscuss = practiceDiscussService.loadWarmUpDiscuss(unionUser.getId(), questionIds);
        warmUpPracticeList.forEach(practice -> {
            practice.setWarmupDiscussDistrict(warmUpDiscuss.getOrDefault(practice.getId(), null));
        });

        WarmupPracticeDto warmUpPracticeDto = new WarmupPracticeDto();
        warmUpPracticeDto.setPractice(warmUpPracticeList);
        return WebUtils.result(warmUpPracticeDto);
    }

    @RequestMapping(value = "/analysis/priority/single/{warmupPracticeId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载单个选择题的评论", response = WarmupDiscussDistrict.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "warmupPracticeId", value = "选择题id")})
    public ResponseEntity<Map<String, Object>> loadSingleWarmUpPriorityDiscuss(UnionUser unionUser, @PathVariable Integer warmupPracticeId) {
        WarmupDiscussDistrict warmupDiscussDistrict = practiceDiscussService.loadSingleWarmUpDiscuss(unionUser.getId(), warmupPracticeId);
        return WebUtils.result(warmupDiscussDistrict);
    }

    private void setDiscuss(List<WarmupPractice> warmupPracticeList, Map<Integer, List<WarmupComment>> discuss) {
        warmupPracticeList.forEach(warmupPractice -> {
            List<WarmupComment> list = discuss.get(warmupPractice.getId());
            warmupPractice.setDiscussList(list);
        });
    }

    //根据用户提交记录匹配题目选项
    private void setUserChoices(List<WarmupPractice> warmupPracticeList, List<WarmupSubmit> submits) {
        for (WarmupSubmit warmupSubmit : submits) {
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
                warmupPractice.getChoiceList().forEach(choice -> {
                    if (choiceIds.contains(choice.getId())) {
                        choice.setSelected(true);
                    } else {
                        choice.setSelected(false);
                    }
                });
            });
        }
    }

    @RequestMapping(value = "/new/analysis/{practiceId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载选择题解析", response = WarmupPractice.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "practiceId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> newAnalysisWarmup(LoginUser loginUser,
                                                                 @PathVariable Integer practiceId) {
        Assert.notNull(loginUser, "用户不能为空");
        WarmupPractice warmupPractice = practiceService.getWarmupPractice(practiceId);
        List<WarmupPractice> warmupPracticeList = Lists.newArrayList();
        warmupPracticeList.add(warmupPractice);
        List<Integer> questionIds = warmupPracticeList.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        // 获取用户提交
        WarmupSubmit submit = practiceService.getWarmupSubmit(loginUser.getId(), practiceId);
        List<WarmupSubmit> warmupSubmits = Lists.newArrayList();
        if (submit != null) {
            warmupSubmits.add(submit);
        }
        setUserChoices(warmupPracticeList, warmupSubmits);
        // 获取讨论信息
        Page page = new Page();
        page.setPage(1);
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        Map<Integer, List<WarmupComment>> discuss = practiceDiscussService.loadDiscuss(loginUser.getId(), questionIds, page);
        setDiscuss(warmupPracticeList, discuss);

        return WebUtils.result(warmupPracticeList.get(0));
    }

    @RequestMapping(value = "/load/discuss/{warmupPracticeId}/{offset}", method = RequestMethod.GET)
    @ApiOperation(value = "分页加载评论", response = WarmupComment.class, responseContainer = "List")
    @ApiImplicitParams({@ApiImplicitParam(name = "warmupPracticeId", value = "选择题id"),
            @ApiImplicitParam(name = "offset", value = "分页")})
    public ResponseEntity<Map<String, Object>> loadMoreDiscuss(LoginUser loginUser,
                                                               @PathVariable Integer warmupPracticeId,
                                                               @PathVariable Integer offset) {
        Assert.notNull(loginUser, "用户不能为空");
        Page page = new Page();
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        page.setPage(offset);
        List<WarmupComment> warmupComments = practiceDiscussService.loadDiscuss(loginUser.getId(), warmupPracticeId, page);

        return WebUtils.result(warmupComments);
    }

    @RequestMapping(value = "/discuss", method = RequestMethod.POST)
    @ApiOperation("评论")
    public ResponseEntity<Map<String, Object>> discuss(LoginUser loginUser, @RequestBody WarmupPracticeDiscuss discussDto) {
        Assert.notNull(loginUser, "用户不能为空");

        if (discussDto.getComment() == null || discussDto.getComment().length() > 1000) {
            LOGGER.error("{} 巩固练习讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discuss(loginUser.getId(), discussDto.getReferenceId(),
                discussDto.getComment(), discussDto.getRepliedId());

        return WebUtils.success();
    }

    @RequestMapping(value = "/delete/comment/{commentId}", method = RequestMethod.POST)
    @ApiOperation("删除评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "commentId", value = "评论id")})
    public ResponseEntity<Map<String, Object>> deleteComment(LoginUser loginUser, @PathVariable Integer commentId) {
        Assert.notNull(loginUser, "用户不能为空");

        practiceDiscussService.deleteComment(commentId);

        return WebUtils.success();
    }
}
