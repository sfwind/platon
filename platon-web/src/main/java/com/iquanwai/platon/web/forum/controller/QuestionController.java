package com.iquanwai.platon.web.forum.controller;

import com.iquanwai.platon.biz.domain.forum.QuestionService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.ForumTag;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.forum.dto.QuestionDto;
import com.iquanwai.platon.biz.util.RefreshListDto;
import com.iquanwai.platon.web.forum.dto.SearchDto;
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

    /**
     * 首页，加载问题列表
     * @param page 默认10条每页
     */
    @RequestMapping("/load/list")
    public ResponseEntity<Map<String, Object>> getQuestionList(LoginUser loginUser, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(PAGE_SIZE);
        List<ForumQuestion> forumQuestions = questionService.loadQuestions(loginUser.getId(), page);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("首页")
                .action("查询问题列表");
        operationLogService.log(operationLog);
        RefreshListDto<ForumQuestion> result = new RefreshListDto<>();
        result.setList(forumQuestions);
        result.setEnd(page.isLastPage());
        return WebUtils.result(result);
    }

    /**
     * 加载tag
     */
    @RequestMapping("/tag/load")
    public ResponseEntity<Map<String, Object>> getTags(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        List<ForumTag> tags = questionService.loadTags();

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("提问页")
                .action("查询所有标签");
        operationLogService.log(operationLog);
        return WebUtils.result(tags);
    }

    /**
     * 提交／编辑问题
     * @param questionDto 问题dto，如果没传questionId则代表第一次提交，否则为编辑
     */
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submit(LoginUser loginUser, @RequestBody QuestionDto questionDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(questionDto.getTopic(), "问题标题不能为空");
        Assert.notNull(questionDto.getDescription(), "问题描述不能为空");

        int questionId = questionService.publish(questionDto.getQuestionId(), loginUser.getId(),
                questionDto.getTopic(), questionDto.getDescription(),
                questionDto.getTagIds());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("提问页")
                .action("提交问题");
        operationLogService.log(operationLog);
        return WebUtils.result(questionId);
    }

    /**
     * 加载问题，问题详情页
     * @param questionId 问题id
     */
    @RequestMapping("/load/{questionId}")
    public ResponseEntity<Map<String, Object>> getQuestion(LoginUser loginUser,
                                                           @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        ForumQuestion forumQuestion = questionService.loadQuestion(questionId, loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题详情页")
                .action("打开问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(forumQuestion);
    }

    /**
     * 关注问题
     * @param questionId 问题id
     */
    @RequestMapping(value = "/follow/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> followQuestion(LoginUser loginUser,
                                                              @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        questionService.followQuestion(loginUser.getId(), questionId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题")
                .action("关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    /**
     * 取消问题关注
     * @param questionId 问题id
     */
    @RequestMapping(value = "/follow/cancel/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unfollowQuestion(LoginUser loginUser,
                                                                @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        questionService.unfollowQuestion(loginUser.getId(), questionId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题")
                .action("取消关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> searchQuestion(LoginUser loginUser,
                                                                @ModelAttribute Page page,
                                                                @RequestBody SearchDto searchDto) {
        Assert.notNull(loginUser, "用户不能为空");
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(PAGE_SIZE);
        List<ForumQuestion> forumQuestions = questionService.searchQuestions(loginUser.getId(),
                searchDto.getQuery(), page);

        RefreshListDto<ForumQuestion> result = new RefreshListDto<>();
        result.setList(forumQuestions);
        result.setEnd(page.isLastPage());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("论坛")
                .function("问题")
                .action("搜索问题")
                .memo(searchDto.getQuery());
        operationLogService.log(operationLog);
        return WebUtils.result(result);
    }

}
