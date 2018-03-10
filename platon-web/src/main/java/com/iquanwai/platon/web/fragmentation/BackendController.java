package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.FullAttendanceReward;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.web.fragmentation.dto.ErrorLogDto;
import com.iquanwai.platon.web.fragmentation.dto.ForceOpenPlanParams;
import com.iquanwai.platon.web.fragmentation.dto.MarkDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/rise/b")
@ApiIgnore
public class BackendController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private PlanService planService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(HttpServletRequest request, @RequestBody ErrorLogDto errorLogDto, UnionUser unionUser) {
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

        Integer profileId = unionUser.getId();
        Integer platform = null;
        UnionUser.Platform platformOrigin = unionUserService.getPlatformType(request);
        if (platformOrigin != null) {
            platform = platformOrigin.getValue();
        }

        ActionLog actionLog = ActionLog.create().uid(profileId)
                .module("bug")
                .function("前端bug")
                .action("记录bug")
                .memo(sb.toString())
                .platform(platform);
        operationLogService.log(actionLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(HttpServletRequest request, UnionUser unionUser, @RequestBody MarkDto markDto) {
        Integer profileId = unionUser.getId();
        Integer platform = null;
        UnionUser.Platform platformOrigin = unionUserService.getPlatformType(request);
        if (platformOrigin != null) {
            platform = platformOrigin.getValue();
        }

        ActionLog actionLog = ActionLog.create().uid(profileId)
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo())
                .platform(platform)
                .channel(WebUtils.getChannel(request));

        operationLogService.log(actionLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/generate/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateCertificate(@RequestBody RiseCertificate riseCertificate) {
        Integer month = riseCertificate.getMonth();
        Integer year = riseCertificate.getYear();
        ThreadPool.execute(() -> {
                    logger.info("开始进入生成证书线程");
                    certificateService.generateCertificate(year, month);
                }
        );
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/upload/certificate")
    public ResponseEntity<Map<String, Object>> uploadCertificatePngToQiNiu(@RequestParam Boolean isOnline) {
        ThreadPool.execute(() -> certificateService.uploadCertificateToQiNiu(isOnline));
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/generate/fullattendance", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateFullAttendanceReward(@RequestBody FullAttendanceReward fullAttendanceReward) {
        Integer month = fullAttendanceReward.getMonth();
        Integer year = fullAttendanceReward.getYear();
        ThreadPool.execute(() ->
                certificateService.generateFullAttendanceCoupon(year, month)
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
        OperationLog operationLog = OperationLog.create().openid("后台课程强开")
                .module("后台功能")
                .function("课程强开")
                .action("课程强开");
        operationLogService.log(operationLog);
        List<Integer> profileIds = params.getProfileIds();
        Integer problemId = params.getProblemId();
        Date startDate = params.getStartDate();
        Date closeDate = params.getCloseDate();
        Boolean sendWelcomeMsg = params.getSendWelcomeMsg();

        profileIds.forEach(profileId -> ThreadPool.execute(() -> {
            Integer result = generatePlanService.magicOpenProblem(profileId, problemId, startDate, closeDate, sendWelcomeMsg);
            logger.info("开课: profileId:{},planId:{}", profileId, result);
        }));

        return WebUtils.success();
    }

    @RequestMapping(value = "/adjust/plan", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> adjustPracticePlan() {
        logger.info("课程计划调整接口调用成功");
        ThreadPool.execute(() -> {
            logger.info("开始调整课程计划");
            planService.adjustPracticePlan();
            logger.info("课程计划调整成功");
        });
        return WebUtils.success();
    }
}
