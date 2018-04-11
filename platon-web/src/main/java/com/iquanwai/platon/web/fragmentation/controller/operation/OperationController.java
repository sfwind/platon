package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.web.fragmentation.dto.ForceOpenPlanParams;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by 三十文
 */
@RestController
@RequestMapping("/rise/operation/backend")
public class OperationController {

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/generate/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateCertificate(UnionUser unionUser, @RequestBody RiseCertificate riseCertificate) {
        Integer month = riseCertificate.getMonth();
        Integer year = riseCertificate.getYear();
        ThreadPool.execute(() -> {
                    logger.info("开始生成证书任务");
                    certificateService.generateCertificate(year, month);
                    templateMessageService.sendSelfCompleteMessage("生成证书", unionUser.getOpenId());
                }
        );

        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/send/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendCertificate(UnionUser unionUser, @RequestParam(value = "year") Integer year, @RequestParam(value = "month") Integer month) {
        ThreadPool.execute(() -> {
            logger.info("开始发送证书任务");
            certificateService.sendCertificate(year, month);
            templateMessageService.sendSelfCompleteMessage("发送证书", unionUser.getOpenId());
        });
        return WebUtils.result("正在进行中");
    }

    @RequestMapping(value = "/open/course/memberid", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> backendForceOpenCourse(UnionUser unionUser, @RequestBody ForceOpenPlanParams params) {
        logger.info("enter force open course");
        ActionLog actionLog = ActionLog.create().module("打点").action("课程强开").function("后台功能").uid(unionUser.getId());
        operationLogService.log(actionLog);

        List<String> memberIds = params.getMemberIds();
        List<Integer> profileIds = accountService.getProfileIdsByMemberId(memberIds);
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

}
