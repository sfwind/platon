package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemScore;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogListDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemExploreDto;
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
    public ResponseEntity<Map<String, Object>> loadProblems(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");

        List<Problem> problemList = problemService.loadProblems();
        //非天使用户去除试用版小课
        if (!whiteListService.isInWhiteList(TRIAL, loginUser.getId())) {
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
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        //非天使用户去除试用版小课
        if (!whiteListService.isInWhiteList(TRIAL, loginUser.getId())) {
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }
        // 用户的所有计划
        List<ImprovementPlan> userProblems = planService.getPlans(loginUser.getId());
        // 用户选过的小课
        List<Integer> doneProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus() == 3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 用户进行中的小课
        List<Integer> doingProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus() != 3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 获取所有分类
        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 可以展示的小课
        Map<Integer, List<Problem>> showProblems = Maps.newHashMap();
        problems.forEach(item -> {
            List<Problem> temp = showProblems.computeIfAbsent(item.getCatalogId(), k -> Lists.newArrayList());
            if (doneProblemIds.contains(item.getId())) {
                // 用户没做过这个小课
                item.setStatus(2);
            } else if (doingProblemIds.contains(item.getId())) {
                item.setStatus(1);
            } else {
                item.setStatus(0);
            }
            temp.add(item);
        });

        ProblemCatalogDto result = new ProblemCatalogDto();
        List<ProblemCatalogListDto> catalogListDtos = problemCatalogs.stream()
                .map(item -> {
                    ProblemCatalogListDto dto = new ProblemCatalogListDto();
                    dto.setSequence(item.getSequence());
                    dto.setDescription(item.getDescription());
                    dto.setCatalogId(item.getId());
                    dto.setName(item.getName());
                    dto.setPic(item.getPic());
                    dto.setColor(item.getColor());
                    List<Problem> problemsTemp = showProblems.get(item.getId());
                    problemsTemp.sort((o1, o2) -> o2.getId() - o1.getId());
                    dto.setProblemList(problemsTemp);
                    return dto;
                }).collect(Collectors.toList());
        catalogListDtos.sort((o1, o2) -> o2.getSequence() - o1.getSequence());
        result.setName(loginUser.getWeixinName());
        result.setCatalogList(catalogListDtos);
        result.setRiseMember(loginUser.getRiseMember());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("小课列表")
                .action("加载小课信息")
                .memo("");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }


    @RequestMapping("/list/{catalog}")
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(LoginUser loginUser,
                                                                    @PathVariable(value = "catalog") Integer catalogId) {

        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(catalogId, "小课分类不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("小课类别")
                .action("加载小课类别")
                .memo(catalogId.toString());
        operationLogService.log(operationLog);

        ProblemCatalog problemCatalog = problemService.getProblemCatalog(catalogId);

        if (problemCatalog != null) {
            // 所有问题
            List<Problem> problems = problemService.loadProblems();
            //非天使用户去除试用版小课
            if (!whiteListService.isInWhiteList(TRIAL, loginUser.getId())) {
                problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
            }

            List<ProblemExploreDto> list = problems.stream().filter(item -> catalogId.equals(item.getCatalogId()))
                    .map(item -> {
                        ProblemExploreDto dto = new ProblemExploreDto();
                        dto.setCatalog(problemCatalog.getName());
                        dto.setCatalogDescribe(problemCatalog.getDescription());
                        if (item.getSubCatalogId() != null) {
                            ProblemSubCatalog problemSubCatalog = problemService.getProblemSubCatalog(item.getSubCatalogId());
                            dto.setSubCatalog(problemSubCatalog.getName());
                            dto.setSubCatalogId(problemSubCatalog.getId());
                        }
                        dto.setPic(item.getPic());
                        dto.setAuthor(item.getAuthor());
                        dto.setDifficulty(item.getDifficultyScore() == null ? "0" : item.getDifficultyScore().toString());
                        dto.setName(item.getProblem());
                        dto.setId(item.getId());
                        return dto;
                    })
                    .collect(Collectors.toList());
            list.sort((o1, o2) -> o2.getId() - o1.getId());
            return WebUtils.result(list);
        } else {
            return WebUtils.error("分类不能为空");
        }
    }


    @RequestMapping("/list/all")
    public ResponseEntity<Map<String, Object>> loadAllProblem(LoginUser loginUser) {

        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("小课信息")
                .action("加载所有小课信息")
                .memo("");
        operationLogService.log(operationLog);

        List<ProblemCatalog> problemCatalog = problemService.getProblemCatalogs();
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        //非天使用户去除试用版小课
        if (!whiteListService.isInWhiteList(TRIAL, loginUser.getId())) {
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }

        Map<Integer, ProblemCatalog> catalogMap = Maps.newHashMap();
        problemCatalog.forEach((item) -> catalogMap.put(item.getId(), item));


        List<ProblemExploreDto> list = problems.stream()
                .map(item -> {
                    ProblemExploreDto dto = new ProblemExploreDto();
                    dto.setCatalog(catalogMap.get(item.getCatalogId()).getName());
                    if (item.getSubCatalogId() != null) {
                        ProblemSubCatalog problemSubCatalog = problemService.getProblemSubCatalog(item.getSubCatalogId());
                        dto.setSubCatalog(problemSubCatalog.getName());
                        dto.setSubCatalogId(problemSubCatalog.getId());
                    }
                    dto.setPic(item.getPic());
                    dto.setAuthor(item.getAuthor());
                    dto.setDifficulty(item.getDifficultyScore() == null ? "0" : item.getDifficultyScore().toString());
                    dto.setName(item.getProblem());
                    dto.setId(item.getId());
                    return dto;
                })
                .collect(Collectors.toList());

        return WebUtils.result(list);
    }

    @RequestMapping("/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblem(problemId);
        // 查看该用户是否对该问题评分
        problem.setHasProblemScore(problemService.hasProblemScore(loginUser.getId(), problemId));
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("阅读问题报告")
                .action("打开问题报告页")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }

    @RequestMapping("/grade/{problemId}")
    public ResponseEntity<Map<String, Object>> gradeScore(LoginUser loginUser, @PathVariable Integer problemId,
                                                          @RequestBody List<ProblemScore> problemScores) {
        Assert.notNull(loginUser, "用户不能为空");
        problemService.gradeProblem(problemId, loginUser.getOpenId(), loginUser.getId(), problemScores);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("评分")
                .action("移动端打分")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping("curId")
    public ResponseEntity<Map<String, Object>> loadCurProblemId(LoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("获取用户当前在解决的问题Id");
        operationLogService.log(operationLog);
        ImprovementPlan runningPlan = planService.getRunningPlan(pcLoginUser.getId());
        if (runningPlan == null) {
            // 没有正在进行的主题，选一个之前做过的
            List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getId());
            if (plans.isEmpty()) {
                // 没有买过难题
                LOGGER.error("{} has no active plan", pcLoginUser.getOpenId());
                return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的RISE训练");
            } else {
                // 购买过直接选最后一个
                ImprovementPlan plan = plans.get(plans.size() - 1);
                return WebUtils.result(plan.getProblemId());
            }
        } else {
            // 有正在进行的主题，直接返回id
            return WebUtils.result(runningPlan.getProblemId());
        }
    }
}
