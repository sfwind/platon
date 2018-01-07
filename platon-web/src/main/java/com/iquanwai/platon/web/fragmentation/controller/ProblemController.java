package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.*;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
 * 课程相关的请求处理类
 */
@RestController
@RequestMapping("/rise/problem")
public class ProblemController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProblemService problemService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private AccountService accountService;

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadProblems(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");

        List<Problem> problemList = problemService.loadProblems();
        //非天使用户去除试用课程
        if (!whiteListService.isInWhiteList(WhiteList.TRIAL, loginUser.getId())) {
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

        // 获取每门小课已经选择的人数
//        for (Problem problem : problems) {
//            problem.setChosenPersonCount(problemService.loadChosenPersonCount(problem.getId()));
//        }

        //非天使用户去除试用版课程
        if (!whiteListService.isInWhiteList(WhiteList.TRIAL, loginUser.getId())) {
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }
        // 用户的所有计划
        List<ImprovementPlan> userProblems = planService.getPlans(loginUser.getId());
        // 用户选过的课程
        List<Integer> doneProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus() == 3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 用户进行中的课程
        List<Integer> doingProblemIds = userProblems.stream().filter(improvementPlan -> improvementPlan.getStatus() != 3).map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        // 获取所有分类
        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 可以展示的课程
        Map<Integer, List<Problem>> showProblems = Maps.newHashMap();
        problems.forEach(item -> {
            List<Problem> temp = showProblems.computeIfAbsent(item.getCatalogId(), k -> Lists.newArrayList());
            if (doneProblemIds.contains(item.getId())) {
                // 用户没做过这个课程
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
                    problemsTemp.sort(this::problemSort);
                    List<Problem> problemList = problemsTemp.stream().map(Problem::simple).collect(Collectors.toList());
                    dto.setProblemList(problemList);
                    return dto;
                }).collect(Collectors.toList());
        catalogListDtos.sort((o1, o2) -> o2.getSequence() - o1.getSequence());

        List<Problem> hotList = problemService.loadHotProblems(ConfigUtils.loadHotProblemList());
        hotList = hotList.stream().map(problem -> {
            if (doneProblemIds.contains(problem.getId())) {
                problem.setStatus(2);
            } else if (doingProblemIds.contains(problem.getId())) {
                problem.setStatus(1);
            } else {
                problem.setStatus(0);
            }
            return problem;
        }).collect(Collectors.toList());


        result.setHotList(hotList);
        result.setName(loginUser.getWeixinName());
        result.setCatalogList(catalogListDtos);
        result.setRiseMember(loginUser.getRiseMember() != 0);
        result.setBanners(problemService.loadExploreBanner());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("课程列表")
                .action("加载课程信息");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }

    private Integer problemSort(Problem left, Problem right) {
        // 限免》首发》new》有用度排序
        // 1000 > 500 > 300 > usefulScore
        Double leftScore;
        Double rightScore;
        if (left.getId() == 9) {
            leftScore = 1000d;
        } else if (left.getTrial()) {
            leftScore = 500d;
        } else if (left.getNewProblem()) {
            leftScore = 300d;
        } else {
            leftScore = left.getUsefulScore();
        }

        if (right.getId() == 9) {
            rightScore = 1000d;
        } else if (right.getTrial()) {
            rightScore = 500d;
        } else if (right.getNewProblem()) {
            rightScore = 300d;
        } else {
            rightScore = right.getUsefulScore();
        }
        return rightScore > leftScore ? 1 : -1;
    }

    @RequestMapping("/list/{catalog}")
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(LoginUser loginUser, @PathVariable(value = "catalog") Integer catalogId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(catalogId, "课程分类不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("课程类别")
                .action("加载课程类别")
                .memo(catalogId.toString());
        operationLogService.log(operationLog);

        ProblemCatalog problemCatalog = problemService.getProblemCatalog(catalogId);

        if (problemCatalog != null) {
            // 所有问题
            List<Problem> problems = problemService.loadProblems();
            //非天使用户去除试用课程
            if (!whiteListService.isInWhiteList(WhiteList.TRIAL, loginUser.getId())) {
                problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
            }
            problems = problems.stream().filter(item -> catalogId.equals(item.getCatalogId())).sorted(this::problemSort).collect(Collectors.toList());

            List<ProblemExploreDto> list = problems
                    .stream().map(item -> {
                        ProblemExploreDto dto = new ProblemExploreDto();
                        dto.setCatalogId(problemCatalog.getId());
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
//                        dto.setChosenPersonCount(problemService.loadChosenPersonCount(item.getId()));
                        dto.setAbbreviation(item.getAbbreviation());
                        return dto;
                    })
                    .collect(Collectors.toList());
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
                .function("课程信息")
                .action("加载所有课程信息");
        operationLogService.log(operationLog);

        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        //非天使用户去除试用课程
        if (!whiteListService.isInWhiteList(WhiteList.TRIAL, loginUser.getId())) {
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }

        Map<Integer, ProblemCatalog> catalogMap = Maps.newHashMap();
        problemCatalogs.forEach((item) -> catalogMap.put(item.getId(), item));


        List<ProblemExploreDto> list = problems.stream()
                .map(item -> {
                    ProblemExploreDto dto = new ProblemExploreDto();
                    dto.setCatalog(catalogMap.get(item.getCatalogId()).getName());
                    dto.setCatalogId(item.getCatalogId());
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
//                    dto.setChosenPersonCount(problemService.loadChosenPersonCount(item.getId()));
                    dto.setAbbreviation(item.getAbbreviation());
                    return dto;
                })
                .collect(Collectors.toList());

        return WebUtils.result(list);
    }

    @RequestMapping("/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("阅读问题报告")
                .action("打开问题报告页")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(problem);
    }

    @RequestMapping("/open/{problemId}")
    public ResponseEntity<Map<String, Object>> openProblemIntroduction(LoginUser loginUser, @PathVariable Integer problemId, @RequestParam(required = false) Boolean autoOpen) {
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, loginUser.getId());
        // 设置当前课程已学习人数
//        problem.setChosenPersonCount(problemService.loadChosenPersonCount(problemId));
//        problem.setMonthlyCampMonth(problemService.loadCoursePlanSchedule(loginUser.getId(), problemId));

        RiseCourseDto dto = new RiseCourseDto();
        ImprovementPlan plan = planService.getPlanByProblemId(loginUser.getId(), problemId);

        Boolean isMember = loginUser.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP;

        Integer buttonStatus = planService.problemIntroductionButtonStatus(loginUser.getId(), problemId, plan, autoOpen);

        if (plan != null) {
            dto.setPlanId(plan.getId());
        }
        dto.setFee(ConfigUtils.getRiseCourseFee());
        dto.setButtonStatus(buttonStatus);
        dto.setProblem(problem);
        Profile profile = accountService.getProfile(loginUser.getId());
        dto.setIsFull(new Integer(1).equals(profile.getIsFull()));
        dto.setBindMobile(StringUtils.isNotBlank(profile.getMobileNo()));

//        if (isMember) {
//            // 是会员，才会继续
//            String monthStr = problemService.loadProblemScheduleMonth(loginUser.getId(), problemId);
//            if (monthStr != null) {
//                dto.setTogetherClassMonth(monthStr);
//            }
//        }

        dto.setProblemCollected(problemService.hasCollectedProblem(loginUser.getId(), problemId));

        // 查询信息
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("查询课程信息")
                .action("打开课程介绍页")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(dto);
    }

    @RequestMapping("/grade/{problemId}")
    public ResponseEntity<Map<String, Object>> gradeScore(LoginUser loginUser, @PathVariable Integer problemId, @RequestBody List<ProblemScore> problemScores) {
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

    @RequestMapping(value = "/submit/extension", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateProblemExtension(LoginUser loginUser, @RequestBody ProblemExtension problemExtension) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemExtension.getProblemId(), "课程 Id 不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程").function("课程扩展").action("更新课程扩展");
        operationLogService.log(operationLog);
        Integer result = problemService.insertProblemExtension(problemExtension);
        if (result > 0) {
            return WebUtils.success();
        } else {
            return WebUtils.error("更新失败");
        }
    }

    @RequestMapping(value = "/submit/activity", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateProblemActivity(LoginUser loginUser, @RequestBody ProblemActivity problemActivity) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemActivity.getProblemId(), "课程 Id 不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程").function("课程扩展").action("更新课程活动");
        operationLogService.log(operationLog);
        Integer result = problemService.insertProblemActivity(problemActivity);
        if (result > 0) {
            return WebUtils.result("更新成功");
        } else {
            return WebUtils.error("更新失败");
        }
    }

    @RequestMapping(value = "/extension/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemExtension(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "请求 ProblemId 不能为空");
        OperationLog operationLog = OperationLog.create().module("课程").action("课程扩展").function("获取课程扩展数据").openid(loginUser.getOpenId());
        operationLogService.log(operationLog);
        ProblemExtension extension = problemService.loadProblemExtensionByProblemId(problemId);
        List<ProblemActivity> activities = problemService.loadProblemActivitiesByProblemId(problemId);
        if (extension != null && activities != null) {
            extension.setActivities(activities);
            extension.setOnlineActivities(activities.stream().filter(activity -> ProblemActivity.ONLINE.equals(activity.getType())).collect(Collectors.toList()));
            extension.setOfflineActivities(activities.stream().filter(activity -> ProblemActivity.OFFLINE.equals(activity.getType())).collect(Collectors.toList()));
            return WebUtils.result(extension);
        } else {
            return WebUtils.error("当前课程暂无延伸学习相关内容");
        }
    }

    @RequestMapping(value = "/cards/{planId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemCards(LoginUser loginUser, @PathVariable Integer planId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Pair<Problem, List<EssenceCard>> essenceCards = problemService.loadProblemCards(planId);
        OperationLog operationLog = OperationLog.create()
                .module("课程")
                .action("课程卡包")
                .function("获取课程卡包")
                .openid(loginUser.getOpenId());
        operationLogService.log(operationLog);
        if (essenceCards == null) {
            return WebUtils.error("未找到当前课程相关卡包信息");
        } else {
            CardCollectionDto dto = new CardCollectionDto();
            if (essenceCards.getLeft() != null) {
                dto.setIsRiseMember(loginUser.getRiseMember() == 1);
                dto.setProblemId(essenceCards.getLeft().getId());
                dto.setProblem(essenceCards.getLeft().getProblem());
            }
            dto.setCards(essenceCards.getRight());
            return WebUtils.result(dto);
        }
    }

    @RequestMapping(value = "/card/{problemId}/{chapterId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemEssenceCard(LoginUser loginUser, @PathVariable Integer problemId, @PathVariable Integer chapterId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程卡包")
                .function("课程卡包")
                .action("点开卡包卡片")
                .memo(chapterId.toString());
        operationLogService.log(operationLog);
        String essenceCardImgBase64 = problemService.loadEssenceCardImg(loginUser.getId(), problemId, chapterId);
        if (essenceCardImgBase64 != null) {
            return WebUtils.result(essenceCardImgBase64);
        } else {
            return WebUtils.error("该精华卡片正在制作中，敬请期待");
        }
    }

    @RequestMapping(value = "/collect/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> collectProblem(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemId, "课程不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程学习").function("课程收藏").action("点击收藏").memo(Integer.toString(problemId));
        operationLogService.log(operationLog);

        int result = problemService.collectProblem(loginUser.getId(), problemId);
        if (result > 0) {
            return WebUtils.result("收藏成功");
        } else {
            return WebUtils.error("收藏失败");
        }
    }

    @RequestMapping(value = "/discollect/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> disCollectProblem(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(problemId, "课程不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("课程学习").function("课程取消收藏").action("取消收藏").memo(Integer.toString(problemId));
        operationLogService.log(operationLog);

        int result = problemService.disCollectProblem(loginUser.getId(), problemId);
        if (result > 0) {
            return WebUtils.result("取消收藏成功");
        } else {
            return WebUtils.error("取消收藏失败");
        }
    }

}
