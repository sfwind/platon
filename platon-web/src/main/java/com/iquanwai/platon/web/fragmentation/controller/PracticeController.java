package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.*;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 * 非理解训练的其他训练相关的请求处理类
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
    private AccountService accountService;
    @Autowired
    private PictureService pictureService;
    //分页文章数量
    private static final int PAGE_SIZE = 20;

    @RequestMapping("/application/start/{applicationId}")
    public ResponseEntity<Map<String, Object>> startApplication(LoginUser loginUser,
                                                                @PathVariable Integer applicationId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        Integer planId = null;
        if(improvementPlan!=null){
            planId = improvementPlan.getId();
        }
        //TODO:改为富文本编辑器后,去掉planid校验
        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                loginUser.getOpenId(), planId);
        // 查询点赞数
        applicationPractice.setVoteCount(practiceService.votedCount(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId()));
        // 查询评论数
        applicationPractice.setCommentCount(practiceService.commentCount(Constants.CommentModule.APPLICATION, applicationPractice.getSubmitId()));
        // 查询我对它的点赞状态
        HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId(), loginUser.getOpenId());
        if (myVote != null && myVote.getDel() == 0) {
            // 点赞中
            applicationPractice.setVoteStatus(1);
        } else {
            applicationPractice.setVoteStatus(0);
        }
        applicationPractice.setPicList(pictureService.loadPicture(Constants.PictureType.APPLICATION, applicationPractice.getSubmitId())
                .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + pic.getRealName())
                .collect(Collectors.toList()));


        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("打开应用训练页")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(applicationPractice);
    }

    @RequestMapping("/challenge/start/{challengeId}")
    public ResponseEntity<Map<String, Object>> startChallenge(LoginUser loginUser,
                                                                         @PathVariable Integer challengeId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId,
                loginUser.getOpenId(), improvementPlan.getId());

        // 查询点赞数
        challengePractice.setVoteCount(practiceService.votedCount(Constants.VoteType.CHALLENGE, challengePractice.getSubmitId()));
        // 查询评论数
        challengePractice.setCommentCount(practiceService.commentCount(Constants.CommentModule.CHALLENGE, challengePractice.getSubmitId()));
        // 查询我对它的点赞状态
        HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.CHALLENGE, challengePractice.getSubmitId(), loginUser.getOpenId());
        if (myVote != null && myVote.getDel() == 0) {
            // 点赞中
            challengePractice.setVoteStatus(1);
        } else {
            challengePractice.setVoteStatus(0);
        }
        challengePractice.setPicList(pictureService.loadPicture(Constants.PictureType.CHALLENGE, challengePractice.getSubmitId())
                .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.CHALLENGE) + pic.getRealName())
                .collect(Collectors.toList()));

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("小目标")
                .action("打开小目标")
                .memo(challengeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping(value = "/challenge/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitChallenge(LoginUser loginUser,
                                                               @PathVariable Integer submitId,
                                                               @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.submit(submitId, submitDto.getAnswer(), PracticePlan.CHALLENGE);
        if (result) {
            // 提升提交数
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("小目标")
                .action("提交小目标")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/application/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApplication(LoginUser loginUser,
                                                                 @PathVariable Integer submitId,
                                                                 @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.submit(submitId, submitDto.getAnswer(), PracticePlan.APPLICATION);
        if (result) {
            // 提升提交数
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("提交应用训练")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping("/next/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> nextPractice(LoginUser loginUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if (improvementPlan == null) {
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

    /**
     * 点赞或者取消点赞
     *
     * @param vote 1：点赞，2：取消点赞
     */
    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> vote(LoginUser loginUser, @RequestBody HomeworkVoteDto vote) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
        Integer refer = vote.getReferencedId();
        Integer status = vote.getStatus();
        String openId = loginUser.getOpenId();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("碎片化")
                .function("挑战任务")
                .action("移动端点赞");
        operationLogService.log(operationLog);

        if (status == 1) {
            boolean result = practiceService.vote(vote.getType(), refer, openId);
            if (result) {
                return WebUtils.success();
            } else {
                return WebUtils.error("点赞失败");
            }
        } else {
            // 取消点赞
            LOGGER.error("异常，禁止用户:{},取消点赞:{}", loginUser.getOpenId(), vote);
            return WebUtils.error("禁止取消点赞");
        }
    }


    /**
     * 应用任务列表页加载他人的任务信息
     *
     * @param loginUser     登陆人
     * @param applicationId 应用任务Id
     */
    @RequestMapping("/application/list/other/{applicationId}")
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(LoginUser loginUser,
                                                                        @PathVariable Integer applicationId, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户信息不能为空");
        page.setPageSize(PAGE_SIZE);
        // 该计划的应用训练是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("移动端应用任务列表加载他人的应用任务")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = practiceService.loadApplicationSubmits(applicationId).stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId())).map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getUpdateTime()));
                    dto.setPublishTime(item.getPublishTime());
                    dto.setType(Constants.PracticeType.APPLICATION);
                    dto.setSubmitId(item.getId());
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    if(account!=null) {
                        dto.setUserName(account.getNickname());
                        dto.setHeadImage(account.getHeadimgurl());
                    }
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.APPLICATION, item.getId()));
                    dto.setPriority(item.getPriority());
                    // 查询我对它的点赞状态
                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, item.getId(), loginUser.getOpenId());
                    if (myVote != null && myVote.getDel() == 0) {
                        // 点赞中
                        dto.setVoteStatus(1);
                    } else {
                        dto.setVoteStatus(0);
                    }
                    dto.setPicList(pictureService.loadPicture(Constants.PictureType.APPLICATION, item.getId())
                            .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + pic.getRealName())
                            .collect(Collectors.toList()));
                    return dto;
                }).sorted((left, right) -> {
                    //按发布时间排序
                    try {
                        return DateUtils.interval(right.getPublishTime(), left.getPublishTime());
                    } catch (Exception e) {
                        LOGGER.error("应用任务文章排序异常", e);
                        return 0;
                    }
                }).collect(Collectors.toList());
        page.setTotal(submits.size());
        //区分精华和普通文章
        List<RiseWorkInfoDto> superbSubmit = submits.stream().filter(submit -> submit.getPriority() == 1)
                .collect(Collectors.toList());
        //普通文章分页
        List<RiseWorkInfoDto> normalSubmit = submits.stream().filter(submit -> submit.getPriority() == 0)
                .skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());
        //浏览量加1
        normalSubmit.forEach(item -> practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION,
                item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW));
        superbSubmit.forEach(item -> practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION,
                item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW));

        RefreshListDto<RiseWorkInfoDto> dto = new RefreshListDto<>();
        dto.setList(normalSubmit);
        dto.setHighlightList(superbSubmit);
        dto.setEnd(page.isLastPage());
        return WebUtils.result(dto);
    }

//    @RequestMapping("/challenge/list/other/{challengeId}")
//    public ResponseEntity<Map<String, Object>> showOtherChallengeList(LoginUser loginUser, @PathVariable("challengeId") Integer challengeId,@ModelAttribute Page page) {
//        Assert.notNull(challengeId, "challengeId不能为空");
//        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
//                .module("训练")
//                .function("小目标")
//                .action("小目标列表加载他人的")
//                .memo(challengeId + "");
//        operationLogService.log(operationLog);
//        List<RiseWorkInfoDto> submits = practiceService.getChallengeSubmitList(challengeId)
//                .stream()
//                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId()))
//                .map(item -> {
//                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
//                    dto.setSubmitId(item.getId());
//                    dto.setType(Constants.PracticeType.CHALLENGE);
//                    dto.setContent(item.getContent());
//                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.CHALLENGE, item.getId()));
//                    Account account = accountService.getAccount(item.getOpenid(), false);
//                    dto.setUserName(account.getNickname());
//                    dto.setHeadImage(account.getHeadimgurl());
//                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getUpdateTime()));
//                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.CHALLENGE,item.getId()));
//                    // 查询我对它的点赞状态
//                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.CHALLENGE, item.getId(), loginUser.getOpenId());
//                    if (myVote != null && myVote.getDel() == 0) {
//                        // 点赞中
//                        dto.setVoteStatus(1);
//                    } else {
//                        dto.setVoteStatus(0);
//                    }
//
//                    return dto;
//                }).sorted((left,right)->{
//                    try {
//                        int leftWeight = left.getCommentCount() + left.getVoteCount();
//                        int rightWeight = right.getCommentCount() + right.getVoteCount();
//                        return rightWeight - leftWeight;
//                    } catch (Exception e){
//                        LOGGER.error("挑战任务文章排序异常",e);
//                        return 0;
//                    }
//                }).collect(Collectors.toList());
//        page.setTotal(submits.size());
//        submits = submits.stream().skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());
//        submits.forEach(item->{
//            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW);
//        });
//        RefreshListDto<RiseWorkInfoDto> dto = new RefreshListDto<RiseWorkInfoDto>();
//        dto.setList(submits);
//        dto.setEnd(page.isLastPage());
//        return WebUtils.result(dto);
//    }

    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadComments(LoginUser loginUser,
                                                            @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                            @ModelAttribute Page page) {
        Assert.notNull(moduleId, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("移动端加载评论")
                .memo(moduleId + ":" + submitId);
        operationLogService.log(operationLog);
        List<RiseWorkCommentDto> comments = practiceService.loadComments(moduleId, submitId, page).stream().map(item -> {
            Profile account = accountService.getProfile(item.getCommentOpenId(), false);
            if (account != null) {
                RiseWorkCommentDto dto = new RiseWorkCommentDto();
                dto.setId(item.getId());
                dto.setContent(item.getContent());
                dto.setUpTime(DateUtils.parseDateToString(item.getAddTime()));
                dto.setUpName(account.getNickname());
                dto.setHeadPic(account.getHeadimgurl());
                return dto;
            } else {
                LOGGER.error("未找到该评论用户:{}", item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        RefreshListDto<RiseWorkCommentDto> dto = new RefreshListDto<>();
        dto.setList(comments);
        dto.setEnd(page.isLastPage());
        return WebUtils.result(dto);
    }

    /**
     * 评论
     *
     * @param loginUser 登陆人
     * @param moduleId  评论模块
     * @param submitId  文章id
     * @param dto       评论内容
     */
    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> comment(LoginUser loginUser,
                                                       @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                       @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser, "登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        Pair<Boolean, String> result = practiceService.comment(moduleId, submitId, loginUser.getOpenId(), dto.getContent());
        if (result.getLeft()) {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("移动端评论")
                    .memo(moduleId + ":" + submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setContent(dto.getContent());
            resultDto.setUpName(loginUser.getWeixinName());
            resultDto.setHeadPic(loginUser.getHeadimgUrl());
            resultDto.setUpTime(DateUtils.parseDateToString(new Date()));
            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }


    @RequestMapping(value = "/subject/submit/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitSubjectArticle(LoginUser loginUser,
                                                                    @PathVariable("problemId") Integer problemId,
                                                                    @RequestBody RiseWorkInfoDto workInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "难题不能为空");
        boolean b = planService.hasProblemPlan(loginUser.getOpenId(), problemId);
        if(!b){
            return WebUtils.error("您并没有该专题，无法提交");
        }
        Integer submitId = practiceService.submitSubjectArticle(new SubjectArticle(
                workInfoDto.getSubmitId(),
                loginUser.getOpenId(),
                problemId,
                1,
                0,
                workInfoDto.getTitle(),
                workInfoDto.getContent()
        ));
        OperationLog operationLog = OperationLog.create()
                .module("训练")
                .function("碎片化")
                .action("移动专题输出区提交")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        if(submitId==-1){
            return WebUtils.error("提交失败,请保存提交内容，并联系管理员");
        }
        practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
        workInfoDto.setPerfect(false);
        workInfoDto.setAuthorType(1);
        workInfoDto.setSubmitId(submitId);
        workInfoDto.setHeadImage(loginUser.getHeadimgUrl());
        workInfoDto.setUserName(loginUser.getWeixinName());
        workInfoDto.setSubmitUpdateTime(DateUtils.parseDateToString(new Date()));
        workInfoDto.setProblemId(problemId);
        workInfoDto.setVoteCount(0);
        workInfoDto.setVoteStatus(0);
        workInfoDto.setCommentCount(0);
        workInfoDto.setIsMine(true);
        workInfoDto.setLabelList(practiceService.updateLabels(Constants.LabelArticleModule.SUBJECT, submitId, workInfoDto.getLabelList()));
        return WebUtils.result(workInfoDto);
    }

    @RequestMapping(value = "/subject/list/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSubjectList(LoginUser loginUser, @PathVariable("problemId") Integer problemId, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "专题id不能为空");
        page.setPageSize(PAGE_SIZE);
        List<RiseWorkInfoDto> list = practiceService.loadSubjectArticles(problemId, page)
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setSubmitId(item.getId());
                    dto.setType(Constants.PracticeType.SUBJECT);
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.SUBJECT, item.getId()));
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    dto.setUserName(account.getNickname());
                    dto.setHeadImage(account.getHeadimgurl());
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getUpdateTime()));
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, item.getId()));
                    // 查询我对它的点赞状态
                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.SUBJECT, item.getId(), loginUser.getOpenId());
                    if (myVote != null && myVote.getDel() == 0) {
                        // 点赞中
                        dto.setVoteStatus(1);
                    } else {
                        dto.setVoteStatus(0);
                    }
                    dto.setPerfect(item.getSequence() != null && item.getSequence() > 0);
                    dto.setAuthorType(item.getAuthorType());
                    dto.setIsMine(item.getOpenid().equals(loginUser.getOpenId()));
                    dto.setTitle(item.getTitle());
                    dto.setPicList(pictureService.loadPicture(Constants.PictureType.SUBJECT, item.getId())
                            .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.SUBJECT) + pic.getRealName())
                            .collect(Collectors.toList()));
                    dto.setLabelList(practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT,item.getId()));
                    return dto;
                }).collect(Collectors.toList());
        list.forEach(item -> {
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW);
        });
        RefreshListDto<RiseWorkInfoDto> result = new RefreshListDto<>();
        result.setList(list);
        result.setEnd(page.isLastPage());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("移动端加载专题输出区")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/subject/desc/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubjectDesc(LoginUser loginUser, @PathVariable Integer problemId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("移动端查询专题输出区描述")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(planService.loadSubjectDesc(problemId));
    }

    @RequestMapping(value = "/subject/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubject(LoginUser loginUser, @PathVariable("submitId") Integer submitId) {

        SubjectArticle subjectArticle = practiceService.loadSubjectArticle(submitId);
        RiseWorkInfoDto dto = new RiseWorkInfoDto();
        dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, submitId));
        dto.setVoteCount(practiceService.votedCount(Constants.VoteType.SUBJECT, submitId));
        dto.setVoteStatus(practiceService.loadVoteRecord(Constants.VoteType.SUBJECT, submitId, loginUser.getOpenId()) != null ? 1 : 0);
        dto.setSubmitId(submitId);
        dto.setAuthorType(subjectArticle.getAuthorType());
        dto.setContent(subjectArticle.getContent());
        dto.setTitle(subjectArticle.getTitle());
        Profile profile = accountService.getProfile(subjectArticle.getOpenid(), false);
        dto.setHeadImage(profile.getHeadimgurl());
        dto.setUserName(profile.getNickname());
        dto.setIsMine(loginUser.getOpenId().equals(subjectArticle.getOpenid()));
        dto.setProblemId(subjectArticle.getProblemId());
        dto.setPerfect(subjectArticle.getSequence() > 0);
        dto.setSubmitUpdateTime(DateUtils.parseDateToString(subjectArticle.getUpdateTime()));
        dto.setPicList(pictureService.loadPicture(Constants.PictureType.SUBJECT, submitId)
                .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.SUBJECT) + pic.getRealName())
                .collect(Collectors.toList()));
        dto.setLabelList(practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT, submitId));
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("移动端加载专题分享文章")
                .memo(submitId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/check/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> practiceCheck(LoginUser loginUser,
                                                           @PathVariable Integer series){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Integer result = planService.checkPractice(series, improvementPlan);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("理解训练")
                .action("理解训练开始校验")
                .memo(series.toString());
        operationLogService.log(operationLog);
        if(result==-1){
            return WebUtils.error("每天早上6点解锁一组训练，请耐心等待");
        }else if(result==-2){
            return WebUtils.error("完成之前的理解训练，才能解锁该训练");
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/label/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLabels(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "专题不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("标签")
                .action("加载专题标签")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(practiceService.loadProblemLabels(problemId));
    }

}
