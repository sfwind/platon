package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.survey.SurveyService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionGroupDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyResultDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveySubmitDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import java.util.Comparator;
import java.util.Date;
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
    public ResponseEntity<Map<String, Object>> submitSurvey(GuestUser guestUser, @PathVariable(value = "category") String category, @RequestBody SurveySubmitDto submits) {
        Assert.notNull(guestUser);
        OperationLog operationLog = OperationLog.create().openid(guestUser.getOpenId())
                .module("问卷")
                .function("提交问卷")
                .action(category);
        operationLogService.log(operationLog);
        Integer result = surveyService.submitQuestions(guestUser.getOpenId(), category, submits.getReferId(), submits.getUserSubmits());
        if (result > 0) {
            if (submits.getReferId() != null && SurveyQuestion.EVALUATION_OTHER.equals(category)) {
                // 其他人提交的
                SurveyResult refer = surveyService.loadSubmit(submits.getReferId());
                if (refer != null) {
                    Profile profile = accountService.getProfile(refer.getOpenid());
                    if (profile != null) {
                        // 价值观测试，需要发消息
                        TemplateMessage templateMessage = new TemplateMessage();
                        templateMessage.setTouser(refer.getOpenid());
                        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                        templateMessage.setData(data);
                        templateMessage.setTemplate_id(ConfigUtils.getMessageReplyCode());
                        data.put("first", new TemplateMessage.Keyword("Hi " + profile.getNickname() + "，你的职业发展核心能力和心理品质量表，有新的他评问卷完成，请知晓。\n"));
                        data.put("keyword1", new TemplateMessage.Keyword(guestUser.getWeixinName()));
                        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                        data.put("keyword3", new TemplateMessage.Keyword("职业发展核心能力和心理品质量表-他评"));
                        templateMessageService.sendMessage(templateMessage);
                    }
                }
            }
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

        Boolean subscribe = guestUser.getSubscribe() != null && guestUser.getSubscribe() != 0;
        SurveyResultDto dto = new SurveyResultDto();
        dto.setSubscribe(subscribe);
        dto.setSubscribeQrCode(ConfigUtils.isDevelopment() ?
                "https://static.iqycamp.com/images/fragment/self_test_qr_beta.jpeg?imageslim" :
                "https://static.iqycamp.com/images/fragment/self_test_qr_pro.jpeg?imageslim");
        dto.setResultId(result != null ? result.getId() : null);
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

}
