package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.po.OperationLog;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemList;
import com.iquanwai.platon.resolver.LoginUser;
import com.iquanwai.platon.util.WebUtils;
import com.iquanwai.platon.web.fragmentation.dto.ProblemDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemIdListDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemListDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/8.
 */
@RestController
@RequestMapping("/fragment/problem")
public class ProblemController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProblemService problemService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadProblems(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");

        List<Problem> problemList = problemService.loadProblems();
        ProblemDto problemDto = new ProblemDto();
        problemDto.setName(loginUser.getWeixinName());
        problemDto.setProblemList(problemList);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("加载问题")
                .action("打开测评页");
        operationLogService.log(operationLog);
        return WebUtils.result(problemDto);
    }

    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> selectProblems(LoginUser loginUser,
                                                              @RequestBody ProblemIdListDto problemIdListDto){
        Assert.notNull(loginUser, "用户不能为空");
        problemService.saveProblems(problemIdListDto.getProblemIdList(), loginUser.getOpenId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("选择问题")
                .action("选择问题")
                .memo(StringUtils.join(problemIdListDto.getProblemIdList(),","));
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("/load/mine")
    public ResponseEntity<Map<String, Object>> loadMyProblems(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        List<ProblemList> problemLists = problemService.loadProblems(loginUser.getOpenId());
        problemLists.stream().forEach(problemList -> {
            String problem = problemService.getProblemContent(problemList.getProblemId());
            problemList.setProblem(problem);
            //openid置为0
            problemList.setOpenid(null);
        });
        ProblemListDto problemListDto = new ProblemListDto();
        problemListDto.setProblemList(problemLists);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("选择我的问题")
                .action("选择待解决问题页");
        operationLogService.log(operationLog);
        return WebUtils.result(problemListDto);
    }

    @RequestMapping("/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(LoginUser loginUser, @PathVariable Integer problemId){
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblem(problemId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("阅读问题报告")
                .action("打开问题报告页");
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }
}
