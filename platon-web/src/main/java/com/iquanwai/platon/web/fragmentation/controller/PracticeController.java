package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
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
 * 非巩固练习的其他练习相关的请求处理类
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
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    //分页文章数量
    private static final int PAGE_SIZE = 10;

    @RequestMapping("/application/start/{applicationId}")
    public ResponseEntity<Map<String, Object>> startApplication(LoginUser loginUser,
                                                                @PathVariable Integer applicationId, @RequestParam(name = "planId",required = false) Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");
        // 兼容性代码，在每日首页中传planId过来，只需要检查planId的正确性
        if (planId != null) {
            // 传了planId
            // 检查这个planId是不是他的
            ImprovementPlan plan = planService.getPlan(planId);
            if (plan == null || !plan.getProfileId().equals(loginUser.getId())) {
                return WebUtils.error("参数错误，可以联系小Q反馈哦");
            }
        } else {
            // 没有planId，消息中心中查询
            // 通过applicationId反查,查看是哪个PlanId,
            ApplicationSubmit applicationSubmit = practiceService.loadApplicationSubmitByApplicationId(applicationId,
                    loginUser.getId());
            if (applicationSubmit == null) {
                // 没有提交过，查询当前的planId
                ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getId());
                if (improvementPlan != null) {
                    planId = improvementPlan.getId();
                }
            } else {
                planId = applicationSubmit.getPlanId();
            }
        }

        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                loginUser.getOpenId(), loginUser.getId(), planId, false);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用练习")
                .action("打开应用练习页")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(applicationPractice);
    }

    @RequestMapping("/challenge/start/{challengeId}")
    public ResponseEntity<Map<String, Object>> startChallenge(LoginUser loginUser,
                                                                         @PathVariable Integer challengeId,
                                                              @RequestParam(name = "planId",required = false) Integer planId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId,
                loginUser.getOpenId(), loginUser.getId(), improvementPlan.getId(), false);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("小目标")
                .action("打开小目标")
                .memo(challengeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping(value = "/challenge/submit/{planId}/{challengeId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitChallenge(LoginUser loginUser,
                                                               @PathVariable("planId") Integer planId,
                                                               @PathVariable("challengeId") Integer challengeId,
                                                               @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        // 先生成，之后走之前逻辑
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId, loginUser.getOpenId(),
                loginUser.getId(), planId, true);
        Integer submitId = challengePractice.getSubmitId();
        Assert.notNull(loginUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.challengeSubmit(submitId, submitDto.getAnswer());
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

    @RequestMapping(value = "/application/submit/{planId}/{applicationId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApplication(LoginUser loginUser,
                                                                 @PathVariable("planId") Integer planId,
                                                                 @PathVariable("applicationId") Integer applicationId,
                                                                 @RequestBody SubmitDto submitDto) {
        Assert.notNull(loginUser, "用户不能为空");
        // 如果没有则生成，之后走之前逻辑
        ApplicationPractice applicationPractice = practiceService.getApplicationPractice(applicationId,
                loginUser.getOpenId(), loginUser.getId(), planId, true);
        Integer submitId = applicationPractice.getSubmitId();
        Assert.notNull(loginUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.applicationSubmit(submitId, submitDto.getAnswer());
        if (result) {
            // 提升提交数
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用练习")
                .action("提交应用练习")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(result);
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
        OperationLog operationLog = OperationLog.create().openid(openId)
                .module("碎片化")
                .function("挑战任务")
                .action("移动端点赞");
        operationLogService.log(operationLog);

        if (status == 1) {
            boolean result = practiceService.vote(vote.getType(), refer, loginUser.getId(), openId);
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
        // 该计划的应用练习是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("移动端应用任务列表加载他人的应用任务")
                .memo(applicationId.toString());
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = practiceService.loadApplicationSubmits(applicationId).stream()
                .filter(item -> !item.getProfileId().equals(loginUser.getId())).map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getPublishTime()));
                    dto.setPublishTime(item.getPublishTime());
                    dto.setType(Constants.PracticeType.APPLICATION);
                    dto.setSubmitId(item.getId());
                    Profile account = accountService.getProfile(item.getProfileId());
                    if (account != null) {
                        dto.setUserName(account.getNickname());
                        dto.setHeadImage(account.getHeadimgurl());
                        dto.setRole(account.getRole());
                        dto.setSignature(account.getSignature());
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
//                    dto.setPicList(pictureService.loadPicture(Constants.PictureType.APPLICATION, item.getId())
//                            .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.APPLICATION) + pic.getRealName())
//                            .collect(Collectors.toList()));
                    return dto;
                }).sorted((left, right) -> {
                    //按发布时间排序
                    try {
                        return (int) ((right.getPublishTime().getTime() - left.getPublishTime().getTime()) / 1000);
                    } catch (Exception e) {
                        LOGGER.error("应用任务文章排序异常", e);
                        return 0;
                    }
                }).collect(Collectors.toList());
        //区分精华和普通文章
        List<RiseWorkInfoDto> superbSubmit = submits.stream().filter(submit -> submit.getPriority() == 1)
                .collect(Collectors.toList());
        //普通文章分页
        List<RiseWorkInfoDto> normalSubmit = submits.stream().filter(submit -> submit.getPriority() == 0).collect(Collectors.toList());
        page.setTotal(normalSubmit.size());
        normalSubmit = normalSubmit.stream().skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());
        //浏览量加1
//        normalSubmit.forEach(item -> practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION,
//                item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW));
//        superbSubmit.forEach(item -> practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION,
//                item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW));

        RefreshListDto<RiseWorkInfoDto> dto = new RefreshListDto<>();
        dto.setList(normalSubmit);
        dto.setHighlightList(superbSubmit);
        dto.setEnd(page.isLastPage());
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadComments(LoginUser loginUser,
                                                            @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                            @ModelAttribute Page page) {
        Assert.notNull(moduleId, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用练习")
                .action("移动端加载评论")
                .memo(moduleId + ":" + submitId);
        operationLogService.log(operationLog);
        List<RiseWorkCommentDto> commentDtos = practiceService.loadComments(moduleId, submitId, page).stream().map(item -> {
            Profile account = accountService.getProfile(item.getCommentOpenId(), false);
            RiseWorkCommentDto dto = new RiseWorkCommentDto();
            if (account != null) {
                dto.setId(item.getId());
                dto.setName(account.getNickname());
                dto.setAvatar(account.getHeadimgurl());
                dto.setDiscussTime(DateUtils.parseDateToString(item.getAddTime()));
                dto.setComment(item.getContent());
                dto.setRepliedComment(item.getRepliedComment());
                dto.setRepliedName(account.getNickname());
                dto.setSignature(account.getSignature());
                dto.setIsMine(loginUser.getOpenId().equals(item.getCommentOpenId()));
                dto.setRole(account.getRole());
                dto.setRepliedDel(item.getRepliedDel());
                return dto;
            } else {
                LOGGER.error("未找到该评论用户:{}", item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        RefreshListDto<RiseWorkCommentDto> dto = new RefreshListDto<>();
        dto.setList(commentDtos);
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
        Pair<Integer, String> result = practiceService.comment(moduleId, submitId, loginUser.getOpenId(), dto.getComment());
        if (result.getLeft()>0) {
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("移动端评论")
                    .memo(moduleId + ":" + submitId);
            operationLogService.log(operationLog);
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setComment(dto.getComment());
            resultDto.setName(loginUser.getWeixinName());
            resultDto.setAvatar(loginUser.getHeadimgUrl());
            resultDto.setDiscussTime(DateUtils.parseDateToString(new Date()));
            resultDto.setRole(loginUser.getRole());
            resultDto.setSignature(loginUser.getSignature());
            resultDto.setIsMine(true);
            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }

    /**
     * 移动应用练习评论回复
     * @param loginUser
     * @param moduleId
     * @param submitId
     * @param dto
     * @return
     */
    @RequestMapping(value = "/comment/reply/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> commentReply(LoginUser loginUser,
            @PathVariable("moduleId") Integer moduleId,
            @PathVariable("submitId") Integer submitId,
            @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "回复内容不能为空");
        Pair<Integer, String> result = practiceService.replyComment(moduleId, submitId,
                loginUser.getOpenId(), dto.getComment(), dto.getRepliedId());
        if(result.getLeft() > 0) {
            Comment replyComment = practiceService.loadComment(dto.getRepliedId());
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setComment(dto.getComment());
            resultDto.setName(loginUser.getWeixinName());
            resultDto.setAvatar(loginUser.getHeadimgUrl());
            resultDto.setDiscussTime(DateUtils.parseDateToString(new Date()));
            resultDto.setRole(loginUser.getRole());
            resultDto.setSignature(loginUser.getSignature());
            Profile profile = accountService.getProfile(replyComment.getCommentOpenId(), false);
            if(profile!=null){
                resultDto.setRepliedName(profile.getNickname());
            }

            resultDto.setRepliedComment(replyComment.getContent());
            resultDto.setIsMine(true);
            resultDto.setRepliedDel(replyComment.getDel());
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("移动端评论回复")
                    .memo(dto.getRepliedId().toString());
            operationLogService.log(operationLog);
            return WebUtils.result(resultDto);
        } else {
            return WebUtils.result("回复失败");
        }
    }


    @RequestMapping(value = "/subject/submit/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitSubjectArticle(LoginUser loginUser,
                                                                    @PathVariable("problemId") Integer problemId,
                                                                    @RequestBody RiseWorkInfoDto workInfoDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "难题不能为空");
        boolean b = planService.hasProblemPlan(loginUser.getId(), problemId);
        if(!b){
            return WebUtils.error("您并没有该小课，无法提交");
        }
        Integer submitId = practiceService.submitSubjectArticle(new SubjectArticle(
                workInfoDto.getSubmitId(),
                loginUser.getOpenId(),
                loginUser.getId(),
                problemId,
                1,
                0,
                workInfoDto.getTitle(),
                workInfoDto.getContent()
        ));
        OperationLog operationLog = OperationLog.create()
                .module("训练")
                .function("碎片化")
                .action("移动小课输出区提交")
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
        Assert.notNull(problemId, "小课id不能为空");
        page.setPageSize(PAGE_SIZE);
        List<RiseWorkInfoDto> list = practiceService.loadSubjectArticles(problemId, page)
                .stream().map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setSubmitId(item.getId());
                    dto.setType(Constants.PracticeType.SUBJECT);
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.SUBJECT, item.getId()));
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    if(account!=null) {
                        dto.setUserName(account.getNickname());
                        dto.setHeadImage(account.getHeadimgurl());
                        dto.setRole(account.getRole());
                        dto.setSignature(account.getSignature());
                    }
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getAddTime()));
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
                    dto.setRequest(item.getRequestFeedback());
                    //设置剩余请求次数
                    dto.setRequestCommentCount(practiceService.hasRequestComment(problemId,
                            loginUser.getId(), loginUser.getOpenId()));
                    dto.setLabelList(practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT,item.getId()));
                    return dto;
                }).collect(Collectors.toList());

//        list.forEach(item -> {
//            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.SUBJECT, item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW);
//        });
        RefreshListDto<RiseWorkInfoDto> result = new RefreshListDto<>();
        result.setList(list);
        result.setEnd(page.isLastPage());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("移动端加载小课论坛")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/subject/desc/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubjectDesc(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("移动端查询小课论坛描述")
                .memo(problemId + "");
        operationLogService.log(operationLog);
        return WebUtils.result(planService.loadSubjectDesc(problemId));
    }

    @RequestMapping(value = "/subject/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubject(LoginUser loginUser, @PathVariable("submitId") Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        SubjectArticle subjectArticle = practiceService.loadSubjectArticle(submitId);
        if(subjectArticle!=null){
            RiseWorkInfoDto dto = new RiseWorkInfoDto();
            dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, submitId));
            dto.setVoteCount(practiceService.votedCount(Constants.VoteType.SUBJECT, submitId));
            dto.setVoteStatus(practiceService.loadVoteRecord(Constants.VoteType.SUBJECT, submitId, loginUser.getOpenId()) != null ? 1 : 0);
            dto.setSubmitId(submitId);
            dto.setAuthorType(subjectArticle.getAuthorType());
            dto.setContent(subjectArticle.getContent());
            dto.setTitle(subjectArticle.getTitle());
            dto.setDesc(planService.loadSubjectDesc(subjectArticle.getProblemId()));
            Profile profile = accountService.getProfile(subjectArticle.getOpenid(), false);
            if(profile!=null) {
                dto.setHeadImage(profile.getHeadimgurl());
                dto.setUserName(profile.getNickname());
                dto.setRole(profile.getRole());
                dto.setSignature(profile.getSignature());
            }
            dto.setIsMine(loginUser.getOpenId().equals(subjectArticle.getOpenid()));
            dto.setProblemId(subjectArticle.getProblemId());
            dto.setPerfect(subjectArticle.getSequence() > 0);
            dto.setSubmitUpdateTime(DateUtils.parseDateToString(subjectArticle.getAddTime()));
            dto.setRequest(subjectArticle.getRequestFeedback());
            dto.setRequestCommentCount(practiceService.hasRequestComment(subjectArticle.getProblemId(),
                    loginUser.getId(), loginUser.getOpenId()));
//        dto.setPicList(pictureService.loadPicture(Constants.PictureType.SUBJECT, submitId)
//                .stream().map(pic -> pictureService.getModulePrefix(Constants.PictureType.SUBJECT) + pic.getRealName())
//                .collect(Collectors.toList()));
            dto.setLabelList(practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT, submitId));
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("移动端加载小课分享文章")
                    .memo(submitId.toString());
            operationLogService.log(operationLog);
            return WebUtils.result(dto);
        }else{
            return WebUtils.error("小课分享不存在");
        }

    }

    @RequestMapping(value = "/label/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadLabels(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "小课不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("标签")
                .action("加载小课标签")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(practiceService.loadProblemLabels(problemId));
    }

    @RequestMapping("/knowledge/start/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> startKnowledge(LoginUser loginUser,
                                                                @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        List<Knowledge> knowledges = practiceService.loadKnowledges(practicePlanId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("知识点")
                .action("打开知识点页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledges);
    }

    @RequestMapping("/knowledge/{id}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser, @PathVariable Integer id) {
        Assert.notNull(id, "知识点id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("知识点")
                .action("加载知识点信息")
                .memo(id.toString());
        operationLogService.log(operationLog);
        Knowledge knowledge = practiceService.loadKnowledge(id);
        return WebUtils.result(knowledge);
    }

    @RequestMapping("/knowledge/learn/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> learnKnowledge(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId){
        Assert.notNull(loginUser, "用户不能为空");
        practiceService.learnKnowledge(practicePlanId);
        planService.checkPlanComplete(practicePlanId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点")
                .action("学习知识点")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/request/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> requestComment(LoginUser loginUser,
                                                              @PathVariable Integer moduleId,
                                                              @PathVariable Integer submitId){
        Assert.notNull(loginUser, "用户不能为空");
        boolean result = practiceService.requestComment(submitId, moduleId, loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("写文章")
                .action("求点评")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        if(result){
            return WebUtils.success();
        }else{
            return WebUtils.error("本小课求点评次数已用完");
        }
    }

    @RequestMapping("/delete/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(LoginUser loginUser,
                                                              @PathVariable Integer commentId) {
        Assert.notNull(loginUser, "用户不能为空");
        practiceService.deleteComment(commentId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("评论")
                .action("删除评论")
                .memo(commentId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/knowledge/discuss/{knowledgeId}/{offset}")
    public ResponseEntity<Map<String, Object>> loadMoreDiscuss(LoginUser loginUser,
                                                               @PathVariable Integer knowledgeId,
                                                               @PathVariable Integer offset){
        Assert.notNull(loginUser, "用户不能为空");
        Page page = new Page();
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        page.setPage(offset);
        List<KnowledgeDiscuss> discusses = practiceDiscussService.loadKnowledgeDiscusses(knowledgeId, page);

        //清空openid
        discusses.forEach(knowledgeDiscuss -> {
            knowledgeDiscuss.setIsMine(loginUser.getOpenId().equals(knowledgeDiscuss.getOpenid()));
            knowledgeDiscuss.setRepliedOpenid(null);
            knowledgeDiscuss.setOpenid(null);
            knowledgeDiscuss.setReferenceId(knowledgeDiscuss.getKnowledgeId());
        });
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("练习")
                .function("理解练习")
                .action("获取讨论")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(discusses);
    }

    @RequestMapping(value = "/knowledge/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> discuss(LoginUser loginUser, @RequestBody KnowledgeDiscuss discussDto) {
        Assert.notNull(loginUser, "用户不能为空");
        if(discussDto.getComment()==null || discussDto.getComment().length()>300){
            LOGGER.error("{} 理解练习讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discussKnowledge(loginUser.getOpenId(), discussDto.getReferenceId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("练习")
                .function("理解练习")
                .action("讨论")
                .memo(discussDto.getReferenceId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/knowledge/discuss/del/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> deleteKnowledgeDiscuss(LoginUser loginUser, @PathVariable Integer id) {
        int result = practiceDiscussService.deleteKnowledgeDiscussById(id);
        String respMsg;
        if(result > 0) {
            respMsg = "删除成功";
        } else {
            respMsg = "操作失败";
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小课")
                .function("知识理解")
                .action("删除回复")
                .memo("KnowledgeId:" + id);
        operationLogService.log(operationLog);
        return WebUtils.result(respMsg);
    }

    /**
     * 根据ApplicationPractice中id获取ApplicationPractice对象
     */
    @RequestMapping(value = "/application/article/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadApplicationPracticeById(LoginUser loginUser, @PathVariable Integer submitId) {
        Assert.notNull(loginUser, "用户不能为空");
        ApplicationSubmit applicationSubmit = practiceService.getApplicationSubmit(submitId);
        AppMsgCommentReplyDto dto = new AppMsgCommentReplyDto();
        dto.setTopic(applicationSubmit.getTopic());
        dto.setDescription(applicationSubmit.getContent());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("应用练习")
                .function("获取文章正文")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/article/show/{moduleId}/{submitId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> riseShowCount(LoginUser loginUser, @PathVariable(value = "moduleId") Integer moduleId, @PathVariable(value = "submitId") Integer submitId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("文章")
                .function(moduleId.toString())
                .action("增加浏览数")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        practiceService.riseArticleViewCount(moduleId, submitId, Constants.ViewInfo.EventType.MOBILE_SHOW);
        return WebUtils.success();
    }

}
