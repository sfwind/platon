package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.RiseRefreshListDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.platon.web.fragmentation.dto.SubmitDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
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
 * Created by justin on 2018/2/16.
 */
@RestController
@RequestMapping("/rise/practice/application")
public class ApplicationController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    //分页文章数量
    private static final int PAGE_SIZE = 10;

    @RequestMapping("/start/{applicationId}")
    public ResponseEntity<Map<String, Object>> startApplication(LoginUser loginUser, @PathVariable Integer applicationId, @RequestParam(name = "planId", required = false) Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");

        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                loginUser.getId(), planId, false).getLeft();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用练习")
                .action("打开应用练习页")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(applicationPractice);
    }

    @RequestMapping(value = "/submit/{planId}/{applicationId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApplication(LoginUser loginUser,
                                                                 @PathVariable("planId") Integer planId,
                                                                 @PathVariable("applicationId") Integer applicationId,
                                                                 @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        // 如果没有则生成，之后走之前逻辑
        Pair<ApplicationPractice, Boolean> applicationPracticeBooleanPair = practiceService.getApplicationPractice(applicationId,
                loginUser.getId(), planId, true);

        ApplicationPractice applicationPractice = applicationPracticeBooleanPair.getLeft();
        Boolean isNewApplication = applicationPracticeBooleanPair.getRight();

        Integer submitId = applicationPractice.getSubmitId();
        Assert.notNull(loginUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }

        // 当用户提交答案时，将 draft 草稿表一起更新
        practiceService.insertApplicationSubmitDraft(loginUser.getId(), applicationId, planId, submitDto.getAnswer());
        Integer practicePlanId = practiceService.applicationSubmit(submitId, submitDto.getAnswer());

        if (practicePlanId != null) {
            // 提升提交数
            if (loginUser.getDevice() == Constants.Device.PC) {
                practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, submitId, Constants.ViewInfo.EventType.PC_SUBMIT);
            } else {
                practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
            }
            planService.checkPlanComplete(practicePlanId);
        }

        Integer completedApplication = 0;
        if (isNewApplication) {
            completedApplication = practiceService.loadCompletedApplicationCnt(planId);
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用练习")
                .action("提交应用练习")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(completedApplication);
    }

    @RequestMapping(value = "/completed/count/{planId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadCompletedApplicationCnt(LoginUser loginUser, @PathVariable Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");
        Integer completedApplicationCnt = practiceService.loadCompletedApplicationCnt(planId);
        return WebUtils.result(completedApplicationCnt);
    }

    @RequestMapping(value = "/autosave/{planId}/{applicationId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> autoSaveApplication(LoginUser loginUser,
                                                                   @PathVariable("planId") Integer planId,
                                                                   @PathVariable("applicationId") Integer applicationId,
                                                                   @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Integer result = practiceService.insertApplicationSubmitDraft(loginUser.getId(), applicationId, planId, submitDto.getDraft());
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("自动存储失败");
        }
    }

    /**
     * 应用任务列表页加载他人的任务信息
     *
     * @param loginUser     登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping("/list/other/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(LoginUser loginUser,
                                                                        @PathVariable Integer applicationId,
                                                                        @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户信息不能为空");
        page.setPageSize(PAGE_SIZE);
        // 该计划的应用练习是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("移动端应用任务列表加载他人的应用任务")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        RiseRefreshListDto<RiseWorkInfoDto> riseRefreshListDto = getRiseWorkInfoDtoRefreshListDto(loginUser, applicationId, page);
        return WebUtils.result(riseRefreshListDto);
    }

    /**
     * 应用任务列表页加载他人的任务信息
     *
     * @param loginUser     登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping("/list/other/{applicationId}/{pageIndex}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationListBatch(LoginUser loginUser,
                                                                             @PathVariable Integer applicationId,
                                                                             @PathVariable Integer pageIndex) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Page page = new Page();
        page.setPageSize(PAGE_SIZE * pageIndex);
        // 该计划的应用练习是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("移动端应用任务列表加载他人的应用任务")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);

        RiseRefreshListDto<RiseWorkInfoDto> riseRefreshListDto = getRiseWorkInfoDtoRefreshListDto(loginUser, applicationId, page);
        return WebUtils.result(riseRefreshListDto);
    }

    private RiseRefreshListDto<RiseWorkInfoDto> getRiseWorkInfoDtoRefreshListDto(LoginUser loginUser, @PathVariable Integer applicationId, Page page) {
        List<ApplicationSubmit> applicationSubmits = practiceService.loadAllOtherApplicationSubmits(applicationId, page);
        List<RiseWorkInfoDto> riseWorkInfoDtos = applicationSubmits.stream().filter(item -> !item.getProfileId().equals(loginUser.getId()))
                .map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent());
                    //TODO: 性能瓶颈 可以放入redis维护
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getPublishTime()));
                    dto.setPublishTime(item.getPublishTime());
                    dto.setType(PracticePlan.APPLICATION_BASE);
                    dto.setSubmitId(item.getId());
                    dto.setFeedback(item.getFeedback());
                    Profile account = accountService.getProfile(item.getProfileId());
                    if (account != null) {
                        dto.setUserName(account.getNickname());
                        dto.setHeadImage(account.getHeadimgurl());
                        dto.setRole(account.getRole());
                        dto.setSignature(account.getSignature());
                    }
                    //TODO: 性能瓶颈 可以放入redis维护
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.APPLICATION, item.getId()));
                    dto.setPriority(item.getPriority());
                    //TODO: 性能瓶颈 未来可以一次拉取后设置
                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, item.getId(),
                            loginUser.getId());
                    if (myVote != null && myVote.getDel() == 0) {
                        // 点赞中
                        dto.setVoteStatus(1);
                    } else {
                        dto.setVoteStatus(0);
                    }
                    return dto;
                }).collect(Collectors.toList());

        RiseRefreshListDto<RiseWorkInfoDto> riseRefreshListDto = new RiseRefreshListDto<>();
        riseRefreshListDto.setList(riseWorkInfoDtos);
        riseRefreshListDto.setEnd(page.isLastPage());
        return riseRefreshListDto;
    }

    @RequestMapping(value = "/article/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadApplicationPracticeById(LoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        ApplicationSubmit applicationSubmit = practiceService.getApplicationSubmit(submitId, loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("应用练习")
                .function("浏览文章")
                .action("获取文章正文")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(applicationSubmit);
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApplicationEvaluation(LoginUser loginUser, @RequestBody CommentEvaluation evaluation) {
        Assert.notNull(loginUser, "用户不能为空");
        Integer commentId = evaluation.getCommentId();
        Integer useful = evaluation.getUseful();
        String reason = evaluation.getReason();
        practiceService.updateEvaluation(commentId, useful, reason);
        return WebUtils.success();
    }
}
