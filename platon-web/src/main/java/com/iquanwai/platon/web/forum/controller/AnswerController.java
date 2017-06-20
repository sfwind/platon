package com.iquanwai.platon.web.forum.controller;

import com.iquanwai.platon.biz.domain.forum.AnswerService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumComment;
import com.iquanwai.platon.web.forum.dto.AnswerCommentDto;
import com.iquanwai.platon.web.forum.dto.AnswerDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 17/6/19.
 */
@RestController
@RequestMapping("/forum/answer")
public class AnswerController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AnswerService answerService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping(value = "/approve/{answerId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> approveAnwser(LoginUser loginUser,
                                                             @PathVariable Integer answerId) {
        Assert.notNull(loginUser, "用户不能为空");
        answerService.approveAnswer(loginUser.getId(), answerId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("答案")
                .action("赞同答案")
                .memo(answerId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> answer(LoginUser loginUser,
                                                      @ModelAttribute AnswerDto answerDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(answerDto, "答案不能为空");
        ForumAnswer result = answerService.submitAnswer(answerDto.getAnswerId(), loginUser.getId(), answerDto.getAnswer(), answerDto.getQuestionId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("答案")
                .action("提交答案");
        operationLogService.log(operationLog);
        if (result != null) {
            result.setAuthorHeadPic(loginUser.getHeadimgUrl());
            result.setAuthorUserName(loginUser.getWeixinName());
            return WebUtils.result(result);
        } else {
            return WebUtils.error("提交失败");
        }
    }

    @RequestMapping(value = "/load/{answerId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> loadAnswer(LoginUser loginUser,@PathVariable Integer answerId){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("答案")
                .action("加载答案页面");
        operationLogService.log(operationLog);
        ForumAnswer forumAnswer = answerService.loadAnswer(answerId, loginUser.getId());
        return WebUtils.result(forumAnswer);
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> commentAnswer(LoginUser loginUser, @ModelAttribute AnswerCommentDto commentDto) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("答案")
                .action("评论答案");
        operationLogService.log(operationLog);

        ForumComment forumComment = answerService.commentAnswer(commentDto.getAnswerId(),
                commentDto.getRepliedCommentId(), loginUser.getId(), commentDto.getComment());
        if (forumComment != null) {
            return WebUtils.result(forumComment);
        } else {
            logger.error("评论失败:{}", commentDto);
            return WebUtils.error("评论失败");
        }
    }

    @RequestMapping(value = "/delete/comment/{commentId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteComment(LoginUser loginUser, @PathVariable Integer commentId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("答案")
                .action("删除评论");
        operationLogService.log(operationLog);
        ForumComment forumComment = answerService.loadComment(commentId);
        if (forumComment == null) {
            logger.error("无法删除，该评论异常,commentId:{}", commentId);
            return WebUtils.error("无法删除，该评论异常");
        } else if (!forumComment.getCommentProfileId().equals(loginUser.getId())) {
            logger.error("无法删除，该评论是其他人发布的:{}", commentId);
            return WebUtils.error("无法删除，该评论是其他人发布的");
        }
        Boolean result = answerService.deleteComment(commentId);
        if (result) {
            return WebUtils.success();
        } else {
            logger.error("删除评论失败,{}", commentId);
            return WebUtils.error("删除失败");
        }
    }


}
