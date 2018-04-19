package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.FullAttendanceReward;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.web.fragmentation.dto.ErrorLogDto;
import com.iquanwai.platon.web.fragmentation.dto.ForceOpenPlanParams;
import com.iquanwai.platon.web.fragmentation.dto.MarkDto;
import com.iquanwai.platon.web.fragmentation.dto.UserDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.UserInsertPlanDto;
import com.iquanwai.platon.web.resolver.UnionUser;
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
    private CertificateService certificateService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private AccountService accountService;

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
        UnionUser.Platform platformOrigin = WebUtils.getPlatformType(request);
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
        UnionUser.Platform platformOrigin = WebUtils.getPlatformType(request);
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

    @RequestMapping(value = "/send/camp/offer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendOffer(@RequestParam(value = "year") Integer year, @RequestParam(value = "month") Integer month) {
        ThreadPool.execute(() -> certificateService.sendOfferMsg(year, month));
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/open/course", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> forceOpen(@RequestBody ForceOpenPlanParams params) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("课程强开")
                .action("课程强开");
        operationLogService.log(operationLog);
        List<Integer> profileIds = params.getProfileIds();
        Integer problemId = params.getProblemId();
        Date startDate = params.getStartDate();
        Date closeDate = params.getCloseDate();
        Boolean sendWelcomeMsg = params.getSendWelcomeMsg();

        ThreadPool.execute(() -> {
            profileIds.forEach(profileId->{
                Integer result = generatePlanService.magicOpenProblem(profileId, problemId, startDate, closeDate, sendWelcomeMsg);
                logger.info("开课: profileId:{},planId:{}", profileId, result);
            });
        });


        return WebUtils.success();
    }

    @RequestMapping(value = "/get/memberid", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getMemberid(@RequestBody UserDto userDto) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("根据openid获取学号")
                .action("根据openid获取学号");
        operationLogService.log(operationLog);

        String memberId = riseMemberManager.getMemberId(userDto.getOpenid());
        if (memberId == null) {
            return WebUtils.error(201, "该用户没有学号");
        } else {
            return WebUtils.result(memberId);
        }
    }

    @RequestMapping(value = "/get/openid", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getOpenid(@RequestBody UserDto userDto) {
        OperationLog operationLog = OperationLog.create()
                .module("后台功能")
                .function("根据学号获取openid")
                .action("根据学号获取openid");
        operationLogService.log(operationLog);

        String openid = accountService.getOpenidByMemberId(userDto.getMemberid());
        if (openid == null) {
            return WebUtils.error(201, "没有查到学员");
        } else {
            return WebUtils.result(openid);
        }
    }

    @RequestMapping(value = "/insert/plan",method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> insertPlan(UnionUser unionUser,@RequestBody UserInsertPlanDto userInsertPlanDto){

        logger.info("==========="+userInsertPlanDto.toString());
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("后台功能").function("增加章节").action("增加章节");

        operationLogService.log(operationLog);
        Integer problemId = userInsertPlanDto.getProblemId();
        Integer startSeries = userInsertPlanDto.getStartSeries();
        Integer endSeries = userInsertPlanDto.getEndSeries();

        List<Integer> profileIds = userInsertPlanDto.getProfileIds();

        ThreadPool.execute(() -> {
            profileIds.forEach(profileId->{
                generatePlanService.createPartPracticePlans(profileId,problemId,startSeries,endSeries);
            });
        });


        return WebUtils.success();
    }

}
