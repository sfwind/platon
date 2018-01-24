package com.iquanwai.platon.web.fragmentation.controller.operation;

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
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SurveyQuestionGroupDto;
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
        Map<Integer, List<SurveyQuestion>> questionMap = surveyQuestions
                .stream()
                .sorted((Comparator.comparingInt(SurveyQuestion::getSeries)))
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
                    // 价值观测试，需要发消息
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(refer.getOpenid());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
                    templateMessage.setUrl(ConfigUtils.domainName() + "/rise/static/value/evaluation/self");
                    data.put("first", new TemplateMessage.Keyword(guestUser.getWeixinName() + "已接受邀请\n"));
                    data.put("keyword1", new TemplateMessage.Keyword("《认识自己》7天免费互助学习"));
                    data.put("keyword2", new TemplateMessage.Keyword("截止1月7日晚20:00"));
                    data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
                    data.put("remark", new TemplateMessage.Keyword("\n点击详情分享邀请链接，邀请更多好友。如有疑问请在下方留言。"));
                    templateMessageService.sendMessage(templateMessage);
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
        if (result != null) {
            return WebUtils.result(result.getId());
        } else {
            return WebUtils.error("没有提交记录");
        }
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
        if (result != null) {
            return WebUtils.result(result.getId());
        } else {
            return WebUtils.error("没有提交记录");
        }
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
