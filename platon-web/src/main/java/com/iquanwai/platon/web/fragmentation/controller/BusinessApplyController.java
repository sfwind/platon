package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.apply.ApplyService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.dto.ApplyQuestionDto;
import com.iquanwai.platon.web.fragmentation.dto.ApplyQuestionGroupDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

}
