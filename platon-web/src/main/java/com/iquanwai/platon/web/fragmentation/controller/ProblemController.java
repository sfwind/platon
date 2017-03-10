package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemPlan;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogListDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
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
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 * 专题相关的请求处理类
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


    @RequestMapping("/list/unchoose")
    public ResponseEntity<Map<String,Object>> loadUnChooseProblems(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        // 用户的所有计划
        List<ImprovementPlan> userProblems = planService.getPlans(loginUser.getOpenId());
        // 用户选过的专题
        List<Integer> problemIds = userProblems.stream().map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 获取所有分类
        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 可以展示的专题
        Map<Integer,List<Problem>> showProblems = Maps.newHashMap();
        problems.forEach(item->{
            List<Problem> temp = showProblems.computeIfAbsent(item.getCatalogId(), k -> Lists.newArrayList());
            if(!problemIds.contains(item.getId())){
                // 用户没做过这个专题
                item.setDone(false);
            } else {
                item.setDone(true);
            }
            temp.add(item);
        });

        ProblemCatalogDto result = new ProblemCatalogDto();
        List<ProblemCatalogListDto> catalogListDtos = problemCatalogs.stream()
                .map(item->{
                    ProblemCatalogListDto dto = new ProblemCatalogListDto();
                    dto.setName(item.getName());
                    dto.setPic(item.getPic());
                    dto.setProblemList(showProblems.get(item.getId()));
                    return dto;
                }).collect(Collectors.toList());
        result.setName(loginUser.getWeixinName());
        result.setCatalogList(catalogListDtos);
        return WebUtils.result(result);
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
        List<ProblemPlan> problemPlans = problemService.loadProblems(loginUser.getOpenId());
        problemPlans.stream().forEach(problemList -> {
            String problem = problemService.getProblemContent(problemList.getProblemId());
            problemList.setProblem(problem);
            //openid置为0
            problemList.setOpenid(null);
        });
        ProblemListDto problemListDto = new ProblemListDto();
        problemListDto.setProblemList(problemPlans);
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
                .action("打开问题报告页")
                .module(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }
}
