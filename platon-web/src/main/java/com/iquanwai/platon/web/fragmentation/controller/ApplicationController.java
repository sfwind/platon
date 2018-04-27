package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.DiscussDistrictDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseRefreshListDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.platon.web.fragmentation.dto.SubmitDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;
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
@Api(description = "应用题的请求处理类")
public class ApplicationController {
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;
    //分页文章数量
    private static final int PAGE_SIZE = 10;

    @RequestMapping(value = "/start/{applicationId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载应用题", response = ApplicationPractice.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "applicationId", value = "应用练习id"),
            @ApiImplicitParam(name = "planId", value = "计划id", required = false)})
    public ResponseEntity<Map<String, Object>> startApplication(UnionUser unionUser, @PathVariable Integer applicationId, @RequestParam(name = "planId", required = false) Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");

        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                unionUser.getId(), planId, false).getLeft();

        return WebUtils.result(applicationPractice);
    }

    @RequestMapping(value = "/submit/{planId}/{applicationId}", method = RequestMethod.POST)
    @ApiOperation(value = "提交应用题", response = Integer.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "applicationId", value = "应用练习id"),
            @ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> submitApplication(UnionUser unionUser, @PathVariable("planId") Integer planId, @PathVariable("applicationId") Integer applicationId, @RequestBody SubmitDto submitDto) {
        Assert.notNull(unionUser, "用户不能为空");
        // 如果没有则生成，之后走之前逻辑
        Pair<ApplicationPractice, Boolean> applicationPracticeBooleanPair = practiceService.getApplicationPractice(applicationId,
                unionUser.getId(), planId, true);

        ApplicationPractice applicationPractice = applicationPracticeBooleanPair.getLeft();
        Boolean isNewApplication = applicationPracticeBooleanPair.getRight();

        Integer submitId = applicationPractice.getSubmitId();
        Assert.notNull(unionUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }

        // 当用户提交答案时，将 draft 草稿表一起更新
        practiceService.insertApplicationSubmitDraft(unionUser.getId(), applicationId, planId, submitDto.getAnswer());
        Integer practicePlanId = practiceService.applicationSubmit(submitId, submitDto.getAnswer());

        if (practicePlanId != null) {
            planService.checkPlanComplete(practicePlanId);
        }

        Integer completedApplication = 0;
        if (isNewApplication) {
            completedApplication = practiceService.loadCompletedApplicationCnt(planId);
        }

        return WebUtils.result(completedApplication);
    }

    @RequestMapping(value = "/completed/count/{planId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载某个课程的应用题完成数", response = Integer.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> loadCompletedApplicationCnt(UnionUser unionUser, @PathVariable Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        Integer completedApplicationCnt = practiceService.loadCompletedApplicationCnt(planId);
        return WebUtils.result(completedApplicationCnt);
    }

    @RequestMapping(value = "/autosave/{planId}/{applicationId}", method = RequestMethod.POST)
    @ApiOperation("应用题自动保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "applicationId", value = "应用练习id"),
            @ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> autoSaveApplication(UnionUser unionUser, @PathVariable("planId") Integer planId, @PathVariable("applicationId") Integer applicationId, @RequestBody SubmitDto submitDto) {
        Assert.notNull(unionUser, "用户不能为空");
        Integer result = practiceService.insertApplicationSubmitDraft(unionUser.getId(), applicationId, planId, submitDto.getDraft());
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("自动存储失败");
        }
    }

    /**
     * @param unionUser 登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping(value = "/list/other/{applicationId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载其他用户的应用题作业", response = RiseRefreshListDto.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "applicationId", value = "应用练习id")})
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(UnionUser unionUser, @PathVariable Integer applicationId, @ModelAttribute Page page) {
        Assert.notNull(unionUser, "用户信息不能为空");
        page.setPageSize(PAGE_SIZE);
        // 该计划的应用练习是否提交
        RiseRefreshListDto<RiseWorkInfoDto> riseRefreshListDto = getRiseWorkInfoDtoRefreshListDto(unionUser, applicationId, page);
        return WebUtils.result(riseRefreshListDto);
    }

    /**
     * 应用任务列表页加载他人的任务信息
     * @param unionUser 登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping(value = "/list/other/{applicationId}/{pageIndex}", method = RequestMethod.GET)
    @ApiOperation(value = "分页加载其他用户的应用题作业", response = RiseRefreshListDto.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "applicationId", value = "应用练习id"),
            @ApiImplicitParam(name = "pageIndex", value = "页码")})
    public ResponseEntity<Map<String, Object>> loadOtherApplicationListBatch(UnionUser unionUser, @PathVariable Integer applicationId, @PathVariable Integer pageIndex) {
        Assert.notNull(unionUser, "用户信息不能为空");
        Page page = new Page();
        page.setPageSize(PAGE_SIZE * pageIndex);
        // 该计划的应用练习是否提交
        RiseRefreshListDto<RiseWorkInfoDto> riseRefreshListDto = getRiseWorkInfoDtoRefreshListDto(unionUser, applicationId, page);
        return WebUtils.result(riseRefreshListDto);
    }

    private RiseRefreshListDto<RiseWorkInfoDto> getRiseWorkInfoDtoRefreshListDto(UnionUser unionUser, @PathVariable Integer applicationId, Page page) {
        List<ApplicationSubmit> applicationSubmits = practiceService.loadAllOtherApplicationSubmits(applicationId, page);
        Map<Integer, Integer> commentCounts = practiceService.commentCount(applicationSubmits);
        Map<Integer, List<HomeworkVote>> homeworkVotes = practiceService.getHomeworkVotes(applicationSubmits);

        List<RiseWorkInfoDto> riseWorkInfoDtos = applicationSubmits.stream().filter(item -> !item.getProfileId().equals(unionUser.getId()))
                .map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent());
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
                    // 处理评论点赞
                    List<HomeworkVote> applicationHomeworkVote = homeworkVotes.get(item.getId());
                    dto.setCommentCount(commentCounts.get(item.getId()));
                    dto.setVoteCount(applicationHomeworkVote.size());
                    dto.setPriority(item.getPriority());

                    HomeworkVote myVote = applicationHomeworkVote.stream()
                            .filter(homeworkVote -> homeworkVote.getVoteProfileId().equals(unionUser.getId()))
                            .findFirst().orElse(null);
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
    @ApiOperation(value = "加载某一个作业", response = ApplicationSubmit.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "submitId", value = "作业id")})
    public ResponseEntity<Map<String, Object>> loadApplicationPracticeBySubmitId(UnionUser unionUser, @PathVariable Integer submitId) {
        Assert.notNull(unionUser, "用户不能为空");
        ApplicationSubmit applicationSubmit = practiceService.getApplicationSubmit(submitId, unionUser.getId());

        return WebUtils.result(applicationSubmit);
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST)
    @ApiOperation("给教练的作业点评打分")
    public ResponseEntity<Map<String, Object>> submitApplicationEvaluation(UnionUser unionUser, @RequestBody CommentEvaluation evaluation) {
        Assert.notNull(unionUser, "用户不能为空");
        Integer commentId = evaluation.getCommentId();
        Integer useful = evaluation.getUseful();
        String reason = evaluation.getReason();
        practiceService.updateEvaluation(commentId, useful, reason);
        return WebUtils.success();
    }

    @RequestMapping(value = "/load/priority/submits", method = RequestMethod.GET)
    @ApiOperation(value = "获取精华作业", response = DiscussDistrictDto.class)
    public ResponseEntity<Map<String, Object>> loadPriorityApplicationSubmits(UnionUser unionUser, @RequestParam("applicationId") Integer applicationId, @RequestParam("planId") Integer planId) {
        DiscussDistrictDto districtDto = new DiscussDistrictDto();
        districtDto.setPersonal(practiceDiscussService.loadPersonalApplicationSubmitDiscussList(unionUser.getId(), applicationId, planId));
        districtDto.setPriorities(practiceDiscussService.loadPriorityApplicationSubmitDiscussList(unionUser.getId(), applicationId));
        return WebUtils.result(districtDto);
    }

}
