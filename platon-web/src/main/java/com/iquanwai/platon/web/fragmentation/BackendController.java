package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.forum.AnswerService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ForceOpenPlanParams;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.FullAttendanceReward;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.web.forum.dto.AnswerCommentDto;
import com.iquanwai.platon.web.forum.dto.AnswerDto;
import com.iquanwai.platon.web.fragmentation.dto.ErrorLogDto;
import com.iquanwai.platon.web.fragmentation.dto.MarkDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(HttpServletRequest request, @RequestBody ErrorLogDto errorLogDto, LoginUser loginUser, GuestUser guestUser) {
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

        String openId = loginUser == null ? null : loginUser.getOpenId();
        if (openId == null) {
            openId = guestUser == null ? null : guestUser.getOpenId();
        }

        OperationLog operationLog = OperationLog.create().openid(openId)
                .module("记录前端bug")
                .function("bug")
                .action("bug")
                .memo(sb.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser, GuestUser guestUser, @RequestBody MarkDto markDto) {
        String openId = loginUser == null ? null : loginUser.getOpenId();
        if (openId == null) {
            openId = guestUser == null ? null : guestUser.getOpenId();
        }

        OperationLog operationLog = OperationLog.create().openid(openId)
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/reply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forumReply(@RequestParam(value = "profileId") Integer profileId, @RequestBody AnswerCommentDto answerCommentDto) {
        answerService.commentAnswer(answerCommentDto.getAnswerId(), answerCommentDto.getRepliedCommentId(),
                profileId, answerCommentDto.getComment());
        return WebUtils.success();
    }

    @RequestMapping(value = "/answer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forumAnswer(@RequestParam(value = "profileId") Integer profileId, @RequestBody AnswerDto answerDto) {
        answerService.submitAnswer(answerDto.getAnswerId(), profileId,
                answerDto.getAnswer(), answerDto.getQuestionId());
        return WebUtils.success();
    }

    @RequestMapping(value = "/generate/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateCertificate(@RequestBody RiseCertificate riseCertificate) {
        Integer month = riseCertificate.getMonth();
        Integer year = riseCertificate.getYear();
        Integer problemId = riseCertificate.getProblemId();
        ThreadPool.execute(() ->
                certificateService.generateCertificate(year, month, problemId)
        );
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/generate/fullattendance", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateFullAttendanceReward(@RequestBody FullAttendanceReward fullAttendanceReward) {
        Integer month = fullAttendanceReward.getMonth();
        Integer year = fullAttendanceReward.getYear();
        Integer problemId = fullAttendanceReward.getProblemId();
        ThreadPool.execute(() ->
                certificateService.generateFullAttendanceCoupon(year, month, problemId)
        );
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/send/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendCertificate(@RequestParam(value = "year") Integer year, @RequestParam(value = "month") Integer month) {
        ThreadPool.execute(() -> certificateService.sendCertificate(year, month));
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/send/fullattendance", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendFullAttendanceCoupon(@RequestParam("year") Integer year, @RequestParam("month") Integer month) {
        ThreadPool.execute(() -> certificateService.sendFullAttendanceCoupon(year, month));
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/send/camp/offer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendOffer(@RequestParam(value = "year") Integer year, @RequestParam(value = "month") Integer month) {
        ThreadPool.execute(() -> certificateService.sendOfferMsg(year, month));
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/open/course", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forceOpen(@RequestBody ForceOpenPlanParams params) {
        OperationLog operationLog = OperationLog.create().openid("后台小课强开")
                .module("后台功能")
                .function("小课强开")
                .action("小课强开");
        operationLogService.log(operationLog);
        List<Integer> profileIds = params.getProfileIds();
        Integer problemId = params.getProblemId();
        Date startDate = params.getStartDate();
        Date closeDate = params.getCloseDate();

        profileIds.forEach(profileId -> {
            Integer result = planService.forceOpenProblem(profileId, problemId, startDate, closeDate);
            logger.info("开课: profileId:{},planId:{}", profileId, result);
        });

        return WebUtils.success();
    }

}
