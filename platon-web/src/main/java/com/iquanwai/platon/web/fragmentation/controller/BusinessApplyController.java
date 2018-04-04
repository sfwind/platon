package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.ApplyException;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.dto.ApplyQuestionDto;
import com.iquanwai.platon.web.fragmentation.dto.ApplyQuestionGroupDto;
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

    /**
     * 加载商学院申请题目
     *
     * @param loginUser 用户信息
     */
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
        ApplyQuestionGroupDto dto = new ApplyQuestionGroupDto();
        dto.setPayApplyFlag(ConfigUtils.getPayApplyFlag());
        dto.setQuestions(result);
        return WebUtils.result(dto);
    }

    /**
     * 检查是否可以申请商学院
     *
     * @param loginUser 用户
     */
    @RequestMapping(value = "/check/submit/apply", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> checkApplySubmit(LoginUser loginUser) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("检查是否能够申请");
        operationLogService.log(operationLog);

        try {
            // 检查是否可以申请
            applyService.checkApplyPrivilege(loginUser.getId());
        } catch (ApplyException e) {

            return WebUtils.error(e.getMessage());
        }
        Account account = accountService.getAccountByUnionId(loginUser.getUnionId());

        if (account.getSubscribe() == 1) {
            return WebUtils.success();
        } else {
            return WebUtils.result(ConfigUtils.isDevelopment() ? "https://static.iqycamp.com/images/fragment/apply_qr_beta.jpeg?imageslim"
                    : "https://static.iqycamp.com/images/fragment/apply_qr_pro_1.jpeg?imageslim");
        }
    }

    /**
     * 商学院申请信息提交
     *
     * @param loginUser      用户
     * @param applySubmitDto 申请信息
     */
    @RequestMapping(value = "/submit/apply", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitApply(LoginUser loginUser, @RequestBody ApplySubmitDto applySubmitDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("商学院")
                .function("申请")
                .action("提交申请");
        operationLogService.log(operationLog);

        try {
            // 检查是否可以申请
            applyService.checkApplyPrivilege(loginUser.getId());
        } catch (ApplyException e) {
            return WebUtils.error(e.getMessage());
        }
//        String orderId = null;
//        if (ConfigUtils.getPayApplyFlag()) {
//            // 检查是否有可用申请订单
//            BusinessSchoolApplicationOrder order = applyService.loadUnAppliedOrder(loginUser.getId());
//            if (order == null) {
//                return WebUtils.error("您还没有成功支付哦");
//            }
//            orderId = order.getOrderId();
//        }

        // 提交申请信息
        List<BusinessApplySubmit> userApplySubmits = applySubmitDto.getUserSubmits().stream().map(applySubmitVO -> {
            BusinessApplySubmit submit = new BusinessApplySubmit();
            submit.setQuestionId(applySubmitVO.getQuestionId());
            submit.setChoiceId(applySubmitVO.getChoiceId());
            submit.setUserValue(applySubmitVO.getUserValue());
            return submit;
        }).collect(Collectors.toList());
        // 如果不需要支付，则直接有效，否则先设置为无效
        applyService.submitBusinessApply(loginUser.getId(), userApplySubmits, !ConfigUtils.getPayApplyFlag());
        return WebUtils.success();
    }
}
