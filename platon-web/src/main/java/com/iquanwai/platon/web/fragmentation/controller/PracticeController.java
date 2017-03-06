package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupResult;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.Account;
import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.ChallengePractice;
import com.iquanwai.platon.biz.po.HomeworkVote;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.DiscussDto;
import com.iquanwai.platon.web.fragmentation.dto.HomeworkVoteDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseWorkCommentDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseWorkInfoDto;
import com.iquanwai.platon.web.fragmentation.dto.SubmitDto;
import com.iquanwai.platon.web.fragmentation.dto.WarmupPracticeDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Autowired
    private AccountService accountService;

    private final static int DISCUSS_PAGE_SIZE = 100;

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
                improvementPlan.getOpenid(), improvementPlan.getId());
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
                improvementPlan.getOpenid(), improvementPlan.getId());

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


        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("专题训练")
                .action("打开专题训练页")
                .memo(challengeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(challengePractice);
    }

    @RequestMapping(value = "/challenge/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitChallenge(LoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody SubmitDto submitDto){
        Assert.notNull(loginUser, "用户不能为空");
        if(submitDto.getAnswer()==null){
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.submit(submitId, submitDto.getAnswer(), PracticePlan.CHALLENGE);
        if(result){
            // 提升提交数
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, submitId, Constants.ViewInfo.EventType.MOBILE_SUBMIT);
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("专题训练")
                .action("提交专题训练")
                .memo(submitId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/application/submit/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApplication(LoginUser loginUser,
                                                      @PathVariable Integer submitId,
                                                      @RequestBody SubmitDto submitDto){
        Assert.notNull(loginUser, "用户不能为空");
        if(submitDto.getAnswer()==null){
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
        Pair<Integer, String> voteResult;
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("碎片化")
                .function("挑战任务")
                .action("移动端点赞");
        operationLogService.log(operationLog);

        if (status == 1) {
            boolean result= practiceService.vote(vote.getType(), refer, openId);
            if(result){
                return WebUtils.success();
            } else {
                return WebUtils.error("点赞失败");
            }
        } else {
            // 取消点赞
            LOGGER.error("异常，禁止用户:{},取消点赞:{}",loginUser.getOpenId(),vote);
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
    public ResponseEntity<Map<String, Object>> loadOtherApplicationList(LoginUser loginUser, @PathVariable Integer applicationId, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户信息不能为空");
        Assert.notNull(applicationId, "应用训练不能为空");
        // 该计划的应用训练是否提交
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用任务")
                .action("移动端应用任务列表加载他人的应用任务")
                .memo(applicationId + "");
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = practiceService.loadApplicationSubmits(applicationId).stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId())).map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.APPLICATION, item.getId()));
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getUpdateTime()));
                    dto.setType(Constants.PracticeType.APPLICATION);
                    dto.setSubmitId(item.getId());
                    Profile account = accountService.getProfile(item.getOpenid(), false);
                    dto.setUserName(account.getNickname());
                    dto.setHeadImage(account.getHeadimgurl());
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.APPLICATION,item.getId()));
                    // 查询我对它的点赞状态
                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.APPLICATION, item.getId(), loginUser.getOpenId());
                    if (myVote != null && myVote.getDel() == 0) {
                        // 点赞中
                        dto.setVoteStatus(1);
                    } else {
                        dto.setVoteStatus(0);
                    }

                    return dto;
                }).sorted((left,right)->{
                    try {
                        int leftWeight = left.getCommentCount() + left.getVoteCount();
                        int rightWeight = right.getCommentCount() + right.getVoteCount();
                        return rightWeight - leftWeight;
                    } catch (Exception e){
                        LOGGER.error("应用任务文章排序异常",e);
                        return 0;
                    }
                }).skip(page.getOffset())
                .limit(page.getPageSize())
                .collect(Collectors.toList());
        submits.forEach(item->{
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.APPLICATION, item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW);
        });
        return WebUtils.result(submits);
    }

    @RequestMapping("/challenge/list/other/{challengeId}")
    public ResponseEntity<Map<String, Object>> showOtherChallengeList(LoginUser loginUser, @PathVariable("challengeId") Integer challengeId,@ModelAttribute Page page) {
        Assert.notNull(challengeId, "challengeId不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("挑战训练")
                .action("挑战训练列表加载他人的")
                .memo(challengeId + "");
        operationLogService.log(operationLog);
        List<RiseWorkInfoDto> submits = practiceService.getChallengeSubmitList(challengeId)
                .stream()
                .filter(item -> !item.getOpenid().equals(loginUser.getOpenId()))
                .map(item -> {
                    RiseWorkInfoDto dto = new RiseWorkInfoDto();
                    dto.setSubmitId(item.getId());
                    dto.setType(Constants.PracticeType.CHALLENGE);
                    dto.setContent(item.getContent());
                    dto.setVoteCount(practiceService.votedCount(Constants.VoteType.CHALLENGE, item.getId()));
                    Account account = accountService.getAccount(item.getOpenid(), false);
                    dto.setUserName(account.getNickname());
                    dto.setHeadImage(account.getHeadimgurl());
                    dto.setSubmitUpdateTime(DateUtils.parseDateToString(item.getUpdateTime()));
                    dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.CHALLENGE,item.getId()));
                    // 查询我对它的点赞状态
                    HomeworkVote myVote = practiceService.loadVoteRecord(Constants.VoteType.CHALLENGE, item.getId(), loginUser.getOpenId());
                    if (myVote != null && myVote.getDel() == 0) {
                        // 点赞中
                        dto.setVoteStatus(1);
                    } else {
                        dto.setVoteStatus(0);
                    }

                    return dto;
                }).sorted((left,right)->{
                    try {
                        int leftWeight = left.getCommentCount() + left.getVoteCount();
                        int rightWeight = right.getCommentCount() + right.getVoteCount();
                        return rightWeight - leftWeight;
                    } catch (Exception e){
                        LOGGER.error("挑战任务文章排序异常",e);
                        return 0;
                    }
                }).skip(page.getOffset())
                .limit(page.getPageSize())
                .collect(Collectors.toList());
        submits.forEach(item->{
            practiceService.riseArticleViewCount(Constants.ViewInfo.Module.CHALLENGE, item.getSubmitId(), Constants.ViewInfo.EventType.MOBILE_SHOW);
        });
        return WebUtils.result(submits);
    }

    @RequestMapping(value = "/comment/{moduleId}/{submitId}",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadComments(LoginUser loginUser,
                                                           @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                           @ModelAttribute Page page){
        Assert.notNull(moduleId, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("应用训练")
                .action("移动端加载评论")
                .memo(moduleId+":"+submitId);
        operationLogService.log(operationLog);
        List<RiseWorkCommentDto> comments = practiceService.loadComments(moduleId, submitId,page).stream().map(item->{
            Profile account = accountService.getProfile(item.getCommentOpenId(), false);
            if(account!=null){
                RiseWorkCommentDto dto = new RiseWorkCommentDto();
                dto.setId(item.getId());
                dto.setContent(item.getContent());
                dto.setUpTime(DateUtils.parseDateToString(item.getAddTime()));
                dto.setUpName(account.getNickname());
                dto.setHeadPic(account.getHeadimgurl());
                return dto;
            } else {
                LOGGER.error("未找到该评论用户:{}",item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());;
        return WebUtils.result(comments);
    }

    /**
     * 评论
     * @param loginUser 登陆人
     * @param moduleId 评论模块
     * @param submitId 文章id
     * @param dto 评论内容
     */
    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> comment(LoginUser loginUser,
                                                      @PathVariable("moduleId") Integer moduleId, @PathVariable("submitId") Integer submitId,
                                                      @RequestBody RiseWorkCommentDto dto) {
        Assert.notNull(loginUser,"登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        Pair<Boolean, String> result = practiceService.comment(moduleId, submitId, loginUser.getOpenId(), dto.getContent());
        if(result.getLeft()){
            OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                    .module("训练")
                    .function("碎片化")
                    .action("移动端评论")
                    .memo(moduleId+":"+submitId);
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

}
