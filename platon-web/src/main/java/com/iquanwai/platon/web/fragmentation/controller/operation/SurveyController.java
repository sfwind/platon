package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.survey.SurveyService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.po.survey.report.SurveyReport;
import com.iquanwai.platon.biz.po.survey.report.SurveyVariableInfo;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.RadarDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionGroupDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyReportDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyResultDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveySubmitDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

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

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "load/{category}")
    public ResponseEntity<Map<String, Object>> loadQuestions(@PathVariable(value = "category") String category, GuestUser guestUser) {
        Assert.notNull(guestUser);
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId())
                .module("问卷")
                .function("加载问卷题目")
                .action("进入问卷");
        operationLogService.log(operationLog);
        List<SurveyQuestion> surveyQuestions = surveyService.loadQuestionsByCategory(category);
        Profile profile = accountService.getProfile(guestUser.getOpenId());
        Map<Integer, List<SurveyQuestion>> questionMap = surveyQuestions
                .stream()
                .filter(item -> {
                    if (profile != null && item.getMemo() != null) {
                        try {
                            JSONObject memo = JSON.parseObject(item.getMemo());
                            if (memo.containsKey(SurveyQuestion.MEMO_TYPE.IDENTIFY) && memo.getBoolean(SurveyQuestion.MEMO_TYPE.IDENTIFY)) {
                                if (profile.getMobileNo() != null || profile.getWeixinId() != null) {
                                    // 有一个个人信息，则不显示
                                    return false;
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        }
                    }
                    return true;
                })
                .collect(Collectors.groupingBy(SurveyQuestion::getSeries));
        List<SurveyQuestionDto> dtos = questionMap
                .keySet()
                .stream()
                .sorted((Comparator.comparingInt(o -> o)))
                .map(item -> {
                    SurveyQuestionDto dto = new SurveyQuestionDto();
                    dto.setSeries(item);
                    dto.setQuestions(questionMap.get(item).stream().sorted((Comparator.comparingInt(SurveyQuestion::getSequence))).collect(Collectors.toList()));
                    return dto;
                }).collect(Collectors.toList());
        SurveyQuestionGroupDto dto = new SurveyQuestionGroupDto();
        dto.setSurveyQuestions(dtos);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "submit/{category}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitSurvey(UnionUser unionUser, @PathVariable(value = "category") String category, @RequestBody SurveySubmitDto submits) {
        Assert.notNull(unionUser);
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("问卷")
                .function("提交问卷")
                .action(category);
        operationLogService.log(operationLog);
        Integer result = surveyService.submitQuestions(unionUser.getId(), unionUser.getOpenId(), category, submits.getReferId(), submits.getUserSubmits());
        if (result > 0) {
            return WebUtils.result(result);
        } else {
            return WebUtils.error("提交失败");
        }
    }

    @RequestMapping(value = "load/submit/{category}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubmit(GuestUser guestUser, @PathVariable(value = "category") String category) {
        Assert.notNull(guestUser);
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId())
                .module("问卷")
                .function("查看问卷提交记录")
                .action(category);
        operationLogService.log(operationLog);
        SurveyResult result = surveyService.loadSubmit(guestUser.getOpenId(), category);

        Boolean subscribe = guestUser.getSubscribe() != null && guestUser.getSubscribe();
        SurveyResultDto dto = new SurveyResultDto();
        dto.setSubscribe(subscribe);
        dto.setSubscribeQrCode(ConfigUtils.isDevelopment() ?
                "https://static.iqycamp.com/images/fragment/self_test_qr_beta.jpeg?imageslim" :
                "https://static.iqycamp.com/images/fragment/self_test_qr_pro.jpeg?imageslim");
        dto.setResultId(result != null ? result.getId() : null);
        dto.setPrivilege(true);
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "load/submit/refer/{refer}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSubmit(GuestUser guestUser, @PathVariable(value = "refer") Integer referId) {
        Assert.notNull(guestUser);
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId())
                .module("问卷")
                .function("查看问卷提交记录")
                .action(referId + "");
        operationLogService.log(operationLog);
        SurveyResult result = surveyService.loadSubmitByReferId(guestUser.getOpenId(), referId);
        SurveyResult referResult = surveyService.loadSubmit(referId);
        SurveyResultDto dto = new SurveyResultDto();
        if (result != null) {
            dto.setResultId(result.getId());
        }
        dto.setSelf(guestUser.getOpenId().equals(referResult.getOpenid()));
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "load/submit/upname/{id}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadSurveySubmitUpname(GuestUser guestUser, @PathVariable(value = "id") Integer id) {
        Assert.notNull(guestUser);
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId())
                .module("问卷")
                .function("查看问卷")
                .action("查看")
                .memo(id + "");
        operationLogService.log(operationLog);
        SurveyResult result = surveyService.loadSubmit(id);
        Profile profile = accountService.getProfile(result.getOpenid());
        return WebUtils.result(profile.getNickname());
    }

    @RequestMapping(value = "/simple/report", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> load(UnionUser guestUser, Integer submitId) {
        Assert.notNull(guestUser);
        SurveyReport report = surveyService.loadSurveyReport(submitId);

        SurveyReportDto dto = new SurveyReportDto();
        dto.setCategoryInfos(report.getCategoryInfos());
        dto.setOtherSurveyCount(report.getOtherSurveyCount());
        dto.setShowComplete(report.getShowComplete());

        if (CollectionUtils.isNotEmpty(report.getCategoryInfos())) {
            RadarDto mainRadar = RadarDto.init();
            mainRadar.setTitle("自评结果");
            report.getCategoryInfos().forEach(item -> {
                if (CollectionUtils.isNotEmpty(item.getDetail())) {
                    Double value = item.getDetail().stream().mapToDouble(SurveyVariableInfo::getValue).average().orElse(0);
                    Double max = item.getDetail().stream().mapToInt(SurveyVariableInfo::getMax).average().orElse(0);
                    mainRadar.addDetail(item.getLegend(), value, max);
                }
            });
            dto.setMainRadar(mainRadar);
        }
        SurveyResult result = surveyService.loadSubmit(submitId);
        dto.setCharacter(report.getCharacter());
        dto.setGeneratedReport(result.getGeneratedReport());
        for (Pair<String, String> pair : report.getNamePicPair()) {
            dto.addNamePic(pair);
        }

        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/generate/report", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> generateReport(UnionUser unionUser, @RequestParam Integer submitId) {
        surveyService.generateReport(submitId);
        return WebUtils.success();
    }

}