package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.interlocution.InterlocutionService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionDate;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionQuestion;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.RefreshListDto;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rise/interlocution")
public class InterlocutionController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private InterlocutionService interlocutionService;

    @RequestMapping(value = "/load/quesiton/list/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadQuestionList(GuestUser loginUser, @ModelAttribute Page page, @PathVariable String date) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("列表")
                .action("获取列表");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPageSize(20);
        List<InterlocutionQuestion> interlocutionQuestions = interlocutionService.loadQuestions(loginUser.getOpenId(), date, page);
        RefreshListDto<InterlocutionQuestion> dto = new RefreshListDto<>();
        dto.setList(interlocutionQuestions);
        dto.setEnd(page.isLastPage());
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/question/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestion(LoginUser loginUser, @RequestBody InterlocutionQuestion question) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("提问")
                .action("提交");
        operationLogService.log(operationLog);
        InterlocutionDate interlocutionDate = interlocutionService.loadInterlocutionDateInfo(question.getInterlocutionDate());
        if (interlocutionDate == null) {
            return WebUtils.error("圈圈问答时间异常，这天没有问答哦");
        }
        interlocutionService.publish(question.getId(), loginUser.getId(), question.getTopic(), question.getInterlocutionDate());
        return WebUtils.success();
    }

    @RequestMapping(value = "/interlocution/info/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadInterlocutionDateInfo(GuestUser loginUser, @PathVariable String date) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("主题")
                .action("查看主题")
                .memo(date);
        operationLogService.log(operationLog);
        Date interlocutionDate = null;
        try {
            interlocutionDate = DateUtils.parseStringToDate(date);
        } catch (Exception e) {
            logger.error("时间参数异常", e);
            return WebUtils.error("时间参数异常");
        }
        InterlocutionDate interlocutionDateInfo = interlocutionService.loadInterlocutionDateInfo(interlocutionDate);
        return WebUtils.result(interlocutionDateInfo);
    }


    /**
     * 关注问题
     *
     * @param questionId 问题id
     */
    @RequestMapping(value = "/follow/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> followQuestion(GuestUser loginUser,
                                                              @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        interlocutionService.followQuestion(loginUser.getOpenId(), questionId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("问题")
                .action("关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    /**
     * 取消问题关注
     *
     * @param questionId 问题id
     */
    @RequestMapping(value = "/follow/cancel/{questionId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unfollowQuestion(GuestUser loginUser,
                                                                @PathVariable Integer questionId) {
        Assert.notNull(loginUser, "用户不能为空");
        interlocutionService.unfollowQuestion(loginUser.getOpenId(), questionId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("问题")
                .action("取消关注问题")
                .memo(questionId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

        @RequestMapping(value = "/load/quanquan/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadQuanquanAnswer(GuestUser loginUser, @PathVariable String date) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("圈圈问答")
                .function("回答")
                .action("获取问题")
                .memo(date);
        operationLogService.log(operationLog);
        Date startDate = null;
        try {
            startDate = DateUtils.parseStringToDate(date);
        } catch (Exception e) {
            logger.error("时间参数异常", e);
            return WebUtils.error("时间参数异常");
        }
        InterlocutionQuestion interlocutionQuestion = interlocutionService.loadQuanQuanAnswer(startDate);
        if (interlocutionQuestion == null) {
            return WebUtils.error("这一天没有圈圈问答哦");
        } else {
            List<InterlocutionDate> interlocutionDates = interlocutionService.loadQuanQuanOtherAnswers(startDate).
                    stream().
                    filter(item -> new DateTime(item.getStartDate()).isBeforeNow()). // 这个时间在现在之前
                    collect(Collectors.toList());
            interlocutionQuestion.setOtherDates(interlocutionDates);
            return WebUtils.result(interlocutionQuestion);
        }
    }

    @RequestMapping(value = "/go/question/submit/{date}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> goQuestionSubmitPage(GuestUser loginUser, @PathVariable String date) {
        OperationLog operationLog = OperationLog.create()
                .openid(loginUser != null ? loginUser.getOpenId() : null)
                .module("圈圈问答")
                .function("去提问")
                .action("检查是否关注");
        operationLogService.log(operationLog);
        if (loginUser == null || loginUser.getSubscribe() == null || !loginUser.getSubscribe()) {
            // 没有loginUser，即没有关注,创建一个img
            OperationLog operationLog1 = OperationLog.create()
                    .openid(loginUser != null ? loginUser.getOpenId() : null)
                    .module("圈圈问答")
                    .function("去提问")
                    .action("创建关注链接")
                    .memo(date);
            operationLogService.log(operationLog1);

            return WebUtils.result(interlocutionService.goQuestionSubmitPageQr(date));
        } else {
            return WebUtils.success();
        }

    }
}
