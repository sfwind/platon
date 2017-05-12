package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemScore;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogListDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 * 小课相关的请求处理类
 */
@RestController
@RequestMapping("/rise/problem")
public class ProblemController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProblemService problemService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private WhiteListService whiteListService;

    private static final String TRIAL = "RISE_PROBLEM_TRIAL";

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadProblems(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");

        List<Problem> problemList = problemService.loadProblems();
        //非天使用户去除试用版小课
        if(!whiteListService.isInWhiteList(TRIAL, loginUser.getOpenId())){
            problemList = problemList.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }
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


    @RequestMapping("/list/unchoose")
    public ResponseEntity<Map<String,Object>> loadUnChooseProblems(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        //非天使用户去除试用版小课
        if(!whiteListService.isInWhiteList(TRIAL, loginUser.getOpenId())){
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }
        // 用户的所有计划
        List<ImprovementPlan> userProblems = planService.getPlans(loginUser.getOpenId());
        // 用户选过的小课
        List<Integer> doneProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus()==3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 用户进行中的小课
        List<Integer> doingProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus()!=3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 获取所有分类
        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 可以展示的小课
        Map<Integer,List<Problem>> showProblems = Maps.newHashMap();
        problems.forEach(item -> {
            List<Problem> temp = showProblems.computeIfAbsent(item.getCatalogId(), k -> Lists.newArrayList());
            if (doneProblemIds.contains(item.getId())) {
                // 用户没做过这个小课
                item.setStatus(2);
            } else if(doingProblemIds.contains(item.getId())){
                item.setStatus(1);
            } else{
                item.setStatus(0);
            }
            temp.add(item);
        });

        ProblemCatalogDto result = new ProblemCatalogDto();
        List<ProblemCatalogListDto> catalogListDtos = problemCatalogs.stream()
                .map(item -> {
                    ProblemCatalogListDto dto = new ProblemCatalogListDto();
                    dto.setName(item.getName());
                    dto.setPic(item.getPic());
                    dto.setColor(item.getColor());
                    dto.setProblemList(showProblems.get(item.getId()));
                    return dto;
                }).collect(Collectors.toList());
        result.setName(loginUser.getWeixinName());
        result.setCatalogList(catalogListDtos);
        result.setRiseMember(loginUser.getRiseMember());
        return WebUtils.result(result);
    }


    @RequestMapping("/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(LoginUser loginUser, @PathVariable Integer problemId){
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblem(problemId);
        // 查看该用户是否对该问题评分
        problem.setHasProblemScore(problemService.hasProblemScore(loginUser.getOpenId(), problemId));
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("阅读问题报告")
                .action("打开问题报告页")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }

    @RequestMapping("/grade/{problemId}")
    public ResponseEntity<Map<String,Object>> gradeScore(LoginUser loginUser, @PathVariable Integer problemId, @RequestBody List<ProblemScore> problemScores){
        Assert.notNull(loginUser, "用户不能为空");
        problemService.gradeProblem(problemId, loginUser.getOpenId(), problemScores);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("评分")
                .action("移动端打分")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
