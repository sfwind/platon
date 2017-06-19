package com.iquanwai.platon.web.forum.controller;

import com.iquanwai.platon.biz.domain.forum.QuestionService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.forum.dto.QuestionDto;
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

/**
 * Created by justin on 17/6/19.
 */
@RestController
@RequestMapping("/forum/question")
public class QuestionController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private QuestionService questionService;

    private static final int PAGE_SIZE = 10;

    @RequestMapping("/load/{tagId}")
    public ResponseEntity<Map<String, Object>> getQuestions(LoginUser loginUser,
                                                            @PathVariable Integer tagId,
                                                            @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        page.setPageSize(PAGE_SIZE);
        List<ForumQuestion> forumQuestionList = questionService.loadQuestions(tagId, page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("提问页")
                .action("查询已有问题")
                .memo(tagId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(forumQuestionList);
    }

    @RequestMapping("/load/tag")
    public ResponseEntity<Map<String, Object>> getTags(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        List<QuestionTag> questionTags = questionService.loadTags();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("提问页")
                .action("查询所有标签");
        operationLogService.log(operationLog);
        return WebUtils.result(questionTags);
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(LoginUser loginUser,
                                                      @RequestBody QuestionDto questionDto) {
        Assert.notNull(loginUser, "用户不能为空");

        questionService.publish(loginUser.getId(), questionDto.getTopic(), questionDto.getDescription(),
                questionDto.getTagIds());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("提问页")
                .action("提交问题");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/load/{questionId}")
    public ResponseEntity<Map<String, Object>> getQuestion(LoginUser loginUser,
                                                            @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        ForumQuestion forumQuestion = questionService.loadQuestion(questionId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题详情页")
                .action("打开问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(forumQuestion);
    }

    @RequestMapping(value = "/follow/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> followQuestion(LoginUser loginUser,
                                                           @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        questionService.followQuestion(questionId, loginUser.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题")
                .action("关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/cancel/follow/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unfollowQuestion(LoginUser loginUser,
                                                              @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        questionService.unfollowQuestion(questionId, loginUser.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题")
                .action("取消关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
