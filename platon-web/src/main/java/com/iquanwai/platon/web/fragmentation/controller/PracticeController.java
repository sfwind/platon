package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.biz.po.common.UserRole;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.*;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 */
@RestController
@RequestMapping("/rise/practice")
@Api(description = "小目标和评论相关的请求处理类")
public class PracticeController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/challenge/start/{challengeId}", method = RequestMethod.GET)
    @ApiOperation("加载小目标")
    @ApiImplicitParams({@ApiImplicitParam(name = "challengeId", value = "小目标id"),
            @ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> startChallenge(UnionUser unionUser,
                                                              @PathVariable Integer challengeId,
                                                              @RequestParam(name = "planId") Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);

        if (improvementPlan == null) {
            logger.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId,
                unionUser.getId(), improvementPlan.getId(), false);

        return WebUtils.result(challengePractice);
    }

    @RequestMapping(value = "/challenge/submit/{planId}/{challengeId}", method = RequestMethod.POST)
    @ApiOperation("提交小目标")
    @ApiImplicitParams({@ApiImplicitParam(name = "challengeId", value = "小目标id"),
            @ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> submitChallenge(UnionUser unionUser,
                                                               @PathVariable("planId") Integer planId,
                                                               @PathVariable("challengeId") Integer challengeId,
                                                               @RequestBody SubmitDto submitDto) {
        Assert.notNull(unionUser, "用户不能为空");
        // 先生成，之后走之前逻辑
        ChallengePractice challengePractice = practiceService.getChallengePractice(challengeId,
                unionUser.getId(), planId, true);
        Integer submitId = challengePractice.getSubmitId();
        Assert.notNull(unionUser, "用户不能为空");
        if (submitDto.getAnswer() == null) {
            return WebUtils.error("您还未输入文字");
        }
        Boolean result = practiceService.challengeSubmit(submitId, submitDto.getAnswer());

        return WebUtils.result(result);
    }

    @RequestMapping(value = "/vote", method = RequestMethod.POST)
    @ApiOperation("点赞")
    public ResponseEntity<Map<String, Object>> vote(UnionUser unionUser, @RequestBody HomeworkVoteDto vote,
                                                    HttpServletRequest request) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.isTrue(vote.getStatus() == 1 || vote.getStatus() == 2, "点赞状态异常");
        Integer refer = vote.getReferencedId();
        Integer status = vote.getStatus();

        UnionUser.Platform platform = WebUtils.getPlatformType(request);

        Integer device;
        if (platform != null && platform.getValue() == Constants.Device.PC) {
            device = Constants.Device.PC;
        } else {
            device = Constants.Device.MOBILE;
        }
        if (status == 1) {
            boolean result = practiceService.vote(vote.getType(), refer, unionUser.getId(), device);
            if (result) {
                return WebUtils.success();
            } else {
                return WebUtils.error("点赞失败");
            }
        } else {
            // 取消点赞
            logger.error("异常，禁止用户:{},取消点赞:{}", unionUser.getOpenId(), vote);
            return WebUtils.error("禁止取消点赞");
        }
    }

    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.GET)
    @ApiOperation("加载文章评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "moduleId", value = "评论的媒体id"),
            @ApiImplicitParam(name = "submitId", value = "提交id")})
    public ResponseEntity<Map<String, Object>> loadComments(UnionUser unionUser,
                                                            @PathVariable("moduleId") Integer moduleId,
                                                            @PathVariable("submitId") Integer submitId,
                                                            @ModelAttribute Page page) {
        Assert.notNull(moduleId, "评论类型不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(page, "页码不能为空");

        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);

        RiseRefreshListDto<RiseWorkCommentDto> riseRefreshListDto = new RiseRefreshListDto<>();

        // 返回最新的 Comments 集合，如果存在是教练的评论，则将返回字段 feedback 置为 true
        List<RiseWorkCommentDto> commentDtos = practiceService.loadComments(moduleId, submitId, page).stream().map(item -> {
            Profile account = accountService.getProfile(item.getCommentProfileId());
            if (moduleId == Constants.CommentModule.APPLICATION) {
                boolean isModified = practiceService.isModifiedAfterFeedback(submitId,
                        item.getCommentProfileId(), item.getAddTime());
                riseRefreshListDto.setIsModifiedAfterFeedback(isModified);
            }
            RiseWorkCommentDto dto = new RiseWorkCommentDto();
            if (account != null) {
                dto.setId(item.getId());
                dto.setName(account.getNickname());
                dto.setAvatar(account.getHeadimgurl());
                dto.setDiscussTime(DateUtils.parseDateToString(item.getAddTime()));
                dto.setComment(item.getContent());
                dto.setRepliedComment(item.getRepliedComment());
                Profile repliedProfile = accountService.getProfile(item.getRepliedProfileId());
                if (repliedProfile != null) {
                    dto.setRepliedName(repliedProfile.getNickname());
                }
                dto.setSignature(account.getSignature());
                dto.setIsMine(unionUser.getId().equals(item.getCommentProfileId()));
                dto.setRole(account.getRole());
                dto.setRepliedDel(item.getRepliedDel());
                return dto;
            } else {
                logger.error("未找到该评论用户:{}", item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        riseRefreshListDto.setList(commentDtos);
        // 如果这个评论是自己的，则获取尚未被评价的应用题评论
        riseRefreshListDto.setCommentEvaluations(
                practiceService.loadUnEvaluatedCommentEvaluationBySubmitId(unionUser.getId(), submitId));

        riseRefreshListDto.setEnd(page.isLastPage());
        return WebUtils.result(riseRefreshListDto);
    }

    @RequestMapping(value = "/comment/message/{submitId}/{commentId}", method = RequestMethod.GET)
    @ApiOperation("加载应用练习某一条评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "submitId", value = "作业id"),
            @ApiImplicitParam(name = "commentId", value = "评论id")})
    public ResponseEntity<Map<String, Object>> loadApplicationReplyComment(UnionUser unionUser, @PathVariable Integer submitId,
                                                                           @PathVariable Integer commentId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(commentId, "评论不能为空");

        RiseRefreshListDto<RiseWorkCommentDto> riseRefreshListDto = new RiseRefreshListDto<>();

        Comment comment = practiceService.loadApplicationReplyComment(commentId);

        // 在评论之后是否被修改
        boolean isModified = practiceService.isModifiedAfterFeedback(submitId,
                comment.getCommentProfileId(), comment.getAddTime());
        riseRefreshListDto.setIsModifiedAfterFeedback(isModified);

        RiseWorkCommentDto dto = new RiseWorkCommentDto();
        Profile account = accountService.getProfile(comment.getCommentProfileId());
        if (account != null) {
            dto.setId(comment.getId());
            dto.setName(account.getNickname());
            dto.setAvatar(account.getHeadimgurl());
            dto.setDiscussTime(DateUtils.parseDateToString(comment.getAddTime()));
            dto.setComment(comment.getContent());
            dto.setRepliedComment(comment.getRepliedComment());
            Profile repliedProfile = accountService.getProfile(comment.getRepliedProfileId());
            if (repliedProfile != null) {
                dto.setRepliedName(repliedProfile.getNickname());
            }
            dto.setSignature(account.getSignature());
            dto.setIsMine(unionUser.getId().equals(comment.getCommentProfileId()));
            dto.setRole(account.getRole());
            dto.setRepliedDel(comment.getRepliedDel());
        } else {
            logger.error("未找到该评论用户：{}", comment);
            return null;
        }

        List<RiseWorkCommentDto> commentDtos = Lists.newArrayList();
        commentDtos.add(dto);
        riseRefreshListDto.setList(commentDtos);

        riseRefreshListDto.setCommentEvaluations(practiceService.loadUnEvaluatedCommentEvaluationByCommentId(commentId));

        return WebUtils.result(riseRefreshListDto);
    }

    @RequestMapping(value = "/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    @ApiOperation("提交评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "moduleId", value = "评论的媒体id"),
            @ApiImplicitParam(name = "submitId", value = "提交id")})
    public ResponseEntity<Map<String, Object>> comment(UnionUser unionUser,
                                                       @PathVariable("moduleId") Integer moduleId,
                                                       @PathVariable("submitId") Integer submitId,
                                                       @RequestBody RiseWorkCommentDto dto, HttpServletRequest request) {
        Assert.notNull(unionUser, "登陆用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "内容不能为空");
        UnionUser.Platform platform = WebUtils.getPlatformType(request);

        Integer device;
        if (platform != null && platform.getValue() == Constants.Device.PC) {
            device = Constants.Device.PC;
        } else {
            device = Constants.Device.MOBILE;
        }

        Pair<Integer, String> result = practiceService.comment(moduleId, submitId, unionUser.getId(),
                dto.getComment(), device);


        if (result.getLeft() > 0) {
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setComment(dto.getComment());
            resultDto.setName(unionUser.getNickName());
            resultDto.setAvatar(unionUser.getHeadImgUrl());
            resultDto.setDiscussTime(DateUtils.parseDateToString(new Date()));
            UserRole userRole = accountService.getUserRole(unionUser.getId());
            Integer roleId = 0;
            if (userRole != null) {
                roleId = userRole.getRoleId();
            }
            resultDto.setRole(roleId);
            resultDto.setIsMine(true);

            ApplicationSubmit applicationSubmit = practiceService.loadApplicationSubmitById(submitId);

            // 初始化教练回复的评论反馈评价
            if (Role.isAsst(roleId) && !applicationSubmit.getProfileId().equals(unionUser.getId())) {
                practiceService.initCommentEvaluation(submitId, resultDto.getId());
            }


            return WebUtils.result(resultDto);
        } else {
            return WebUtils.error("评论失败");
        }

    }

    @RequestMapping(value = "/comment/reply/{moduleId}/{submitId}", method = RequestMethod.POST)
    @ApiOperation("回复评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "moduleId", value = "评论的媒体id"),
            @ApiImplicitParam(name = "submitId", value = "提交id")})
    public ResponseEntity<Map<String, Object>> commentReply(UnionUser unionUser,
                                                            @PathVariable("moduleId") Integer moduleId,
                                                            @PathVariable("submitId") Integer submitId,
                                                            @RequestBody RiseWorkCommentDto dto,
                                                            HttpServletRequest request) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(moduleId, "评论模块不能为空");
        Assert.notNull(submitId, "文章不能为空");
        Assert.notNull(dto, "回复内容不能为空");
        UnionUser.Platform platform = WebUtils.getPlatformType(request);

        Integer device;
        if (platform != null && platform.getValue() == Constants.Device.PC) {
            device = Constants.Device.PC;
        } else {
            device = Constants.Device.MOBILE;
        }
        Pair<Integer, String> result = practiceService.replyComment(moduleId, submitId, unionUser.getId(),
                dto.getComment(), dto.getRepliedId(), device);

        if (result.getLeft() > 0) {
            Comment replyComment = practiceService.loadComment(dto.getRepliedId());
            RiseWorkCommentDto resultDto = new RiseWorkCommentDto();
            resultDto.setId(result.getLeft());
            resultDto.setComment(dto.getComment());
            resultDto.setName(unionUser.getNickName());
            resultDto.setAvatar(unionUser.getHeadImgUrl());
            resultDto.setDiscussTime(DateUtils.parseDateToString(new Date()));
            UserRole userRole = accountService.getUserRole(unionUser.getId());
            Integer roleId = 0;
            if (userRole != null) {
                roleId = userRole.getRoleId();
            }
            resultDto.setRole(roleId);
            Profile profile = accountService.getProfile(replyComment.getCommentProfileId());
            if (profile != null) {
                resultDto.setRepliedName(profile.getNickname());
            }

            resultDto.setRepliedComment(replyComment.getContent());
            resultDto.setIsMine(true);
            resultDto.setRepliedDel(replyComment.getDel());

            ApplicationSubmit applicationSubmit = practiceService.loadApplicationSubmitById(submitId);

            // 初始化教练回复的评论反馈评价
            if (Role.isAsst(roleId) && !applicationSubmit.getProfileId().equals(unionUser.getId())) {
                practiceService.initCommentEvaluation(submitId, resultDto.getId());
            }

            return WebUtils.result(resultDto);
        } else {
            return WebUtils.result("回复失败");
        }
    }

    @RequestMapping(value = "/subject/{submitId}", method = RequestMethod.GET)
    @ApiOperation("回复评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "submitId", value = "提交id")})
    public ResponseEntity<Map<String, Object>> loadSubject(UnionUser unionUser, @PathVariable("submitId") Integer submitId) {
        Assert.notNull(unionUser, "用户不能为空");
        SubjectArticle subjectArticle = practiceService.loadSubjectArticle(submitId);
        if (subjectArticle != null) {
            RiseWorkInfoDto dto = new RiseWorkInfoDto();
//            dto.setCommentCount(practiceService.commentCount(Constants.CommentModule.SUBJECT, submitId));
//            dto.setVoteCount(practiceService.votedCount(Constants.VoteType.SUBJECT, submitId));
//            dto.setVoteStatus(practiceService.loadVoteRecord(Constants.VoteType.SUBJECT, submitId, unionUser.getId()) != null ? 1 : 0);
            dto.setSubmitId(submitId);
            dto.setAuthorType(subjectArticle.getAuthorType());
            dto.setContent(subjectArticle.getContent());
            dto.setTitle(subjectArticle.getTitle());
            Profile profile = accountService.getProfile(subjectArticle.getProfileId());
            if (profile != null) {
                dto.setHeadImage(profile.getHeadimgurl());
                dto.setUserName(profile.getNickname());
                dto.setRole(profile.getRole());
                dto.setSignature(profile.getSignature());
            }
            dto.setIsMine(unionUser.getId().equals(subjectArticle.getProfileId()));
            dto.setProblemId(subjectArticle.getProblemId());
            dto.setPerfect(subjectArticle.getSequence() > 0);
            dto.setSubmitUpdateTime(DateUtils.parseDateToString(subjectArticle.getAddTime()));
            dto.setRequest(subjectArticle.getRequestFeedback());
//            dto.setRequestCommentCount(practiceService.hasRequestComment(subjectArticle.getProblemId(),
//                    unionUser.getId()));
//            dto.setLabelList(practiceService.loadArticleActiveLabels(Constants.LabelArticleModule.SUBJECT, submitId));

            return WebUtils.result(dto);
        } else {
            return WebUtils.error("课程分享不存在");
        }

    }

    @RequestMapping(value = "/request/comment/{moduleId}/{submitId}", method = RequestMethod.POST)
    @ApiOperation("求点评")
    @ApiImplicitParams({@ApiImplicitParam(name = "moduleId", value = "评论的媒体id"),
            @ApiImplicitParam(name = "submitId", value = "提交id")})
    public ResponseEntity<Map<String, Object>> requestComment(UnionUser unionUser,
                                                              @PathVariable Integer moduleId,
                                                              @PathVariable Integer submitId) {
        Assert.notNull(unionUser, "用户不能为空");
        boolean result = practiceService.requestComment(submitId, moduleId, unionUser.getId());

        if (result) {
            return WebUtils.success();
        } else {
            return WebUtils.error("本课程求点评次数已用完");
        }
    }

    @RequestMapping("/delete/comment/{commentId}")
    @ApiOperation("删除评论")
    @ApiImplicitParams({@ApiImplicitParam(name = "commentId", value = "评论id")})
    public ResponseEntity<Map<String, Object>> deleteComment(UnionUser unionUser,
                                                             @PathVariable Integer commentId) {
        Assert.notNull(unionUser, "用户不能为空");
        practiceService.deleteComment(commentId);

        return WebUtils.success();
    }

    @RequestMapping(value = "/article/show/{moduleId}/{submitId}", method = RequestMethod.GET)
    @Deprecated
    public ResponseEntity<Map<String, Object>> riseShowCount(UnionUser unionUser,
                                                             @PathVariable(value = "moduleId") Integer moduleId,
                                                             @PathVariable(value = "submitId") Integer submitId) {

        return WebUtils.success();
    }

    @RequestMapping(value = "/load/{practicePlanId}", method = RequestMethod.GET)
    @ApiOperation("加载某个练习")
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> loadKnowledgeReview(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        PracticePlan practicePlan = practiceService.getPractice(practicePlanId);

        return WebUtils.result(practicePlan);
    }

}
