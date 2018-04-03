package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 2018/2/16.
 */
@RestController
@RequestMapping("/rise/practice/knowledge")
public class KnowledgeController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PlanService planService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    @RequestMapping("/start/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> startKnowledge(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        List<Knowledge> knowledges = practiceService.loadKnowledges(practicePlanId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("知识点")
                .action("打开知识点页")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledges);
    }

    @RequestMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser, @PathVariable Integer knowledgeId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(knowledgeId, "知识点id不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("知识点")
                .action("加载知识点信息")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        Knowledge knowledge = practiceService.loadKnowledge(knowledgeId);
        return WebUtils.result(knowledge);
    }

    @RequestMapping(value = "/learn/{practicePlanId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> learnKnowledge(LoginUser loginUser,
                                                              @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        practiceService.learnKnowledge(loginUser.getId(), practicePlanId);
        planService.checkPlanComplete(practicePlanId);


        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点")
                .action("学习知识点")
                .memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/discuss/{knowledgeId}/{offset}")
    public ResponseEntity<Map<String, Object>> loadMoreDiscuss(LoginUser loginUser,
                                                               @PathVariable Integer knowledgeId,
                                                               @PathVariable Integer offset) {
        Assert.notNull(loginUser, "用户不能为空");
        Page page = new Page();
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        page.setPage(offset);
        List<KnowledgeDiscuss> discusses = practiceDiscussService.loadKnowledgeDiscusses(knowledgeId, page);

        discusses.forEach(knowledgeDiscuss -> {
            knowledgeDiscuss.setIsMine(loginUser.getId().equals(knowledgeDiscuss.getProfileId()));
            knowledgeDiscuss.setReferenceId(knowledgeDiscuss.getKnowledgeId());
        });
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("练习")
                .function("理解练习")
                .action("获取讨论")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(discusses);
    }

    @RequestMapping(value = "/discuss", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> discuss(LoginUser loginUser, @RequestBody KnowledgeDiscuss discussDto) {
        Assert.notNull(loginUser, "用户不能为空");
        if (discussDto.getComment() == null || discussDto.getComment().length() > 1000) {
            logger.error("{} 理解练习讨论字数过长", loginUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discussKnowledge(loginUser.getId(), discussDto.getReferenceId(),
                discussDto.getComment(), discussDto.getRepliedId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("练习")
                .function("理解练习")
                .action("讨论")
                .memo(discussDto.getReferenceId().toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/discuss/del/{id}")
    public ResponseEntity<Map<String, Object>> deleteKnowledgeDiscuss(LoginUser loginUser, @PathVariable Integer id) {
        Assert.notNull(loginUser, "用户不能为空");
        int result = practiceDiscussService.deleteKnowledgeDiscussById(id);
        String respMsg;
        if (result > 0) {
            respMsg = "删除成功";
        } else {
            respMsg = "操作失败";
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程")
                .function("知识理解")
                .action("删除回复")
                .memo("KnowledgeId:" + id);
        operationLogService.log(operationLog);
        return WebUtils.result(respMsg);
    }
}
