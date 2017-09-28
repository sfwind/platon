package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.forum.AnswerService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ForceOpenPlanParams;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.forum.dto.AnswerCommentDto;
import com.iquanwai.platon.web.forum.dto.AnswerDto;
import com.iquanwai.platon.web.fragmentation.dto.ErrorLogDto;
import com.iquanwai.platon.web.fragmentation.dto.MarkDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/rise/b")
public class BackendController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private PlanService planService;

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(HttpServletRequest request, @RequestBody ErrorLogDto errorLogDto, LoginUser loginUser) {
        String data = errorLogDto.getResult();
        StringBuilder sb = new StringBuilder();
        if (data.length() > 700) {
            data = data.substring(0, 700);
        }
        sb.append("url:");
        sb.append(errorLogDto.getUrl());
        sb.append(";ip:");
        sb.append(request.getHeader("X-Forwarded-For"));
        sb.append(";userAgent:");
        sb.append(request.getHeader("user-agent"));
        sb.append(";data:");
        sb.append(data);
        sb.append(";cookie:");
        if (sb.length() < 1024) {
            String cookie = errorLogDto.getCookie();
            int remain = 1024 - sb.length();
            if (remain < cookie.length()) {
                cookie = cookie.substring(0, remain);
            }
            sb.append(cookie);
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module("记录前端bug")
                .function("bug")
                .action("bug")
                .memo(sb.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser, @RequestBody MarkDto markDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/reply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forumReply(@RequestParam(value = "profileId") Integer profileId,
                                                          @RequestBody AnswerCommentDto answerCommentDto) {
        answerService.commentAnswer(answerCommentDto.getAnswerId(), answerCommentDto.getRepliedCommentId(),
                profileId, answerCommentDto.getComment());
        return WebUtils.success();
    }

    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forumAnswer(@RequestParam(value = "profileId") Integer profileId,
                                                           @RequestBody AnswerDto answerDto) {
        answerService.submitAnswer(answerDto.getAnswerId(), profileId,
                answerDto.getAnswer(), answerDto.getQuestionId());
        return WebUtils.success();
    }

    @RequestMapping(value = "/send/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forumAnswer(@RequestParam(value = "year") Integer year,
                                                           @RequestParam(value = "month") Integer month) {
        new Thread(() -> {
            certificateService.sendCertificate(year, month);
        }).start();
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/open/course", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forceOpen(@RequestBody ForceOpenPlanParams params) {
        OperationLog operationLog = OperationLog.create().openid("后台小课强开")
                .module("后台功能")
                .function("小课强开")
                .action("小课强开");
        operationLogService.log(operationLog);
        Integer profileId = params.getProfileId();
        Integer problemId = params.getProblemId();
        Date startDate = params.getStartDate();
        Date closeDate = params.getCloseDate();

        Integer result = planService.forceOpenProblem(profileId, problemId, startDate, closeDate);
        if (result > 0) {
            return WebUtils.result("接口调用成功，生成 PlanId：" + result);
        } else {
            return WebUtils.result("接口调用失败，返回 PlanId：" + result);
        }
    }

}
