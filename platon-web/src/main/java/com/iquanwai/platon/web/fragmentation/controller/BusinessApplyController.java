package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplicationOrder;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.ApplyQuestionDto;
import com.iquanwai.platon.web.fragmentation.dto.ApplySubmitDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nethunder
 * @version 2017-11-22
 */
@RestController
@RequestMapping("/rise/business")
public class BusinessApplyController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ApplyService applyService;
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/load/questions", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadBusinessApplyQuestions(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("获取申请问题");
        operationLogService.log(operationLog);
        List<BusinessApplyQuestion> questionList = applyService.loadBusinessApplyQuestions(loginUser.getId());
        Map<Integer, List<BusinessApplyQuestion>> collect = questionList.stream().collect(Collectors.groupingBy(BusinessApplyQuestion::getSeries));
        List<ApplyQuestionDto> result = Lists.newArrayList();
        collect.keySet().stream().sorted().forEach(item -> {
            ApplyQuestionDto dto = new ApplyQuestionDto();
            dto.setSeries(item);
            dto.setQuestions(collect.get(item));
            result.add(dto);
        });
        return WebUtils.result(result);
    }

    @RequestMapping(value = "/check/submit/apply", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkApplySubmit(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("检查是否能够申请");
        operationLogService.log(operationLog);
        BusinessSchoolApplication application = applyService.loadCheckingApply(loginUser.getId());
        Boolean applyPass = accountService.hasStatusId(loginUser.getId(), CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
        if (applyPass) {
            return WebUtils.error("您已经有报名权限,无需重复申请");
        }

        if (application == null) {
            return WebUtils.success();
        } else {
            return WebUtils.error("您的申请正在审核中哦");
        }
    }

    @RequestMapping(value = "/submit/apply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApply(LoginUser loginUser, @RequestBody ApplySubmitDto applySubmitDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("提交申请");
        operationLogService.log(operationLog);
        BusinessSchoolApplication application = applyService.loadCheckingApply(loginUser.getId());
        Boolean applyPass = accountService.hasStatusId(loginUser.getId(), CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
        if (applyPass) {
            return WebUtils.error("您已经有报名权限,无需重复申请");
        }

        if (application == null) {
//            BusinessSchoolApplicationOrder order = applyService.loadUnAppliedOrder(loginUser.getId());
//            if (order == null) {
//                return WebUtils.error("您还没有成功支付哦");
//            }

            List<BusinessApplySubmit> userApplySubmits = applySubmitDto.getUserSubmits().stream().map(applySubmitVO -> {
                BusinessApplySubmit submit = new BusinessApplySubmit();
                submit.setQuestionId(applySubmitVO.getQuestionId());
                submit.setChoiceId(applySubmitVO.getChoiceId());
                submit.setUserValue(applySubmitVO.getUserValue());
                return submit;
            }).collect(Collectors.toList());
//            applyService.submitBusinessApply(loginUser.getId(), userApplySubmits, order.getOrderId());
            applyService.submitBusinessApply(loginUser.getId(), userApplySubmits, null);
            return WebUtils.success();
        } else {
            return WebUtils.error("您的申请正在审核中哦");
        }
    }
}
