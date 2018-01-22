package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.survey.SurveyService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionGroupDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rise/survey")
public class SurveyController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private SurveyService surveyService;

    @RequestMapping(value = "load/{category}")
    public ResponseEntity<Map<String, Object>> loadQuestions(@PathVariable(value = "category") String category, GuestUser guestUser) {
        OperationLog operationLog = OperationLog.create().openid(guestUser != null ? guestUser.getOpenId() : "")
                .module("问卷")
                .function("加载问卷题目")
                .action("进入问卷");
        operationLogService.log(operationLog);
        List<SurveyQuestion> surveyQuestions = surveyService.loadQuestionsByCategory(category);
        Map<Integer, List<SurveyQuestion>> questionMap = surveyQuestions
                .stream()
                .sorted((Comparator.comparingInt(SurveyQuestion::getSeries)))
                .collect(Collectors.groupingBy(SurveyQuestion::getSeries));
        List<SurveyQuestionDto> dtos = questionMap.keySet().stream().map(item -> {
            SurveyQuestionDto dto = new SurveyQuestionDto();
            dto.setSeries(item);
            dto.setQuestions(questionMap.get(item));
            return dto;
        }).collect(Collectors.toList());
        SurveyQuestionGroupDto dto = new SurveyQuestionGroupDto();
        return WebUtils.result(dto);
    }

}
