package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.ErrorConstants;
import com.iquanwai.platon.biz.po.EssenceCard;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemActivity;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemExtension;
import com.iquanwai.platon.biz.po.ProblemScore;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.CardCollectionDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemCatalogListDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemDto;
import com.iquanwai.platon.web.fragmentation.dto.ProblemExploreDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseCourseDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
                    List<Problem> problemList = problemsTemp.stream().map(Problem::simple).collect(Collectors.toList());
                    dto.setProblemList(problemList);
                    return dto;
                }).collect(Collectors.toList());
        catalogListDtos.sort((o1, o2) -> o2.getSequence() - o1.getSequence());
        result.setName(loginUser.getWeixinName());
        result.setCatalogList(catalogListDtos);
        result.setRiseMember(loginUser.getRiseMember() != 0);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("小课列表")
                .action("加载小课信息")
                .memo("");
        operationLogService.log(operationLog);

        return WebUtils.result(result);
    }

    @RequestMapping("/list/{catalog}")
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(LoginUser loginUser, @PathVariable(value = "catalog") Integer catalogId) {

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

    @RequestMapping("/open/{problemId}")
    public ResponseEntity<Map<String, Object>> openProblemIntroduction(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Problem problem = problemService.getProblem(problemId);
        // 查看该用户是否对该问题评分
        RiseCourseDto dto = new RiseCourseDto();
        problem.setHasProblemScore(problemService.hasProblemScore(loginUser.getId(), problemId));
        List<ImprovementPlan> plans = planService.getPlans(loginUser.getId());
        ImprovementPlan plan = plans.stream().filter(item -> item.getProblemId().equals(problemId)).findFirst().orElse(null);
        Integer buttonStatus;
        if (plan == null) {
            // 没学过这个小课
            // 是否会员
            Boolean isMember = loginUser.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP;
            if (isMember) {
                // 是会员，显示按钮"选择"
                buttonStatus = 2;
            } else {
                // 不是会员，查询一下这个小课是不是限免小课
                if (problemId.equals(ConfigUtils.getTrialProblemId())) {
                    // 是限免小课,显示"限时免费"
                    buttonStatus = 5;
                } else {
                    // 不是限免小课,显示两个按钮
                    buttonStatus = 1;
                }
            }
        } else {
            // 学过这个小课
            dto.setPlanId(plan.getId());
            switch (plan.getStatus()) {
                case ImprovementPlan.RUNNING: {
                    buttonStatus = 3;
                    break;
                }
                case ImprovementPlan.COMPLETE:
                case ImprovementPlan.CLOSE: {
                    buttonStatus = 4;
                    break;
                }
                case ImprovementPlan.TRIALCLOSE: {
                    buttonStatus = 1;
                    break;
                }
                case ImprovementPlan.TEMP_TRIALCLOSE: {
                    buttonStatus = 6;
                    break;
                }
                default:
                    // 按钮状态有问题
                    buttonStatus = -1;
                    break;
            }
        }
        dto.setFee(ConfigUtils.getRiseCourseFee());
        dto.setButtonStatus(buttonStatus);
        dto.setProblem(problem);
        Profile profile = accountService.getProfile(loginUser.getId());
        dto.setIsFull(new Integer(1).equals(profile.getIsFull()));
        dto.setBindMobile(StringUtils.isNotBlank(profile.getMobileNo()));
        if (ConfigUtils.getRiseCoursePayTestStatus() && loginUser.getRiseMember() != 1) {
            //  开启测试
            boolean inWhite = whiteListService.isInWhiteList(WhiteList.FRAG_COURSE_PAY, loginUser.getId());
            if (!inWhite) {
                // 没在白名单里
                dto.setButtonStatus(-1);
            }
        }


        // 查询信息
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("查询小课信息")
                .action("打开小课介绍页")
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

    @RequestMapping("curId")
    public ResponseEntity<Map<String, Object>> loadCurProblemId(LoginUser pcLoginUser) {
        Assert.notNull(pcLoginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(pcLoginUser.getOpenId())
                .module("训练")
                .function("碎片化")
                .action("获取用户当前在解决的问题Id");
        operationLogService.log(operationLog);
        List<ImprovementPlan> runningPlan = planService.getRunningPlan(pcLoginUser.getId());
        if (runningPlan.size() == 0) {
            // 没有正在进行的主题，选一个之前做过的
            List<ImprovementPlan> plans = planService.loadUserPlans(pcLoginUser.getId());
            if (plans.isEmpty()) {
                // 没有买过难题
                logger.error("{} has no active plan", pcLoginUser.getOpenId());
                return WebUtils.error(ErrorConstants.NOT_PAY_FRAGMENT, "没找到进行中的圈外小课");
            } else {
                // 购买过直接选最后一个
                ImprovementPlan plan = plans.get(plans.size() - 1);
                return WebUtils.result(plan.getProblemId());
            }
        } else {
            // 有正在进行的主题，直接返回id
            return WebUtils.result(runningPlan.get(0).getProblemId());
        }
    }

    @RequestMapping(value = "/submit/extension", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateProblemExtension(LoginUser loginUser, @RequestBody ProblemExtension problemExtension) {
        Assert.notNull(loginUser, "用户不能为空");
        Assert.notNull(problemExtension.getProblemId(), "小课 Id 不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小课").function("小课扩展").action("更新小课扩展");
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
        Assert.notNull(problemActivity.getProblemId(), "小课 Id 不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小课").function("小课扩展").action("更新小课活动");
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
        OperationLog operationLog = OperationLog.create().module("小课").action("小课扩展").function("获取小课扩展数据");
        operationLogService.log(operationLog);
        ProblemExtension extension = problemService.loadProblemExtensionByProblemId(problemId);
        List<ProblemActivity> activities = problemService.loadProblemActivitiesByProblemId(problemId);
        if (extension != null && activities != null) {
            extension.setActivities(activities);
            extension.setOnlineActivities(activities.stream().filter(activity -> ProblemActivity.Online.equals(activity.getType())).collect(Collectors.toList()));
            extension.setOfflineActivities(activities.stream().filter(activity -> ProblemActivity.Offline.equals(activity.getType())).collect(Collectors.toList()));
            return WebUtils.result(extension);
        } else {
            return WebUtils.error("当前小课暂无延伸学习相关内容");
        }
    }

    @RequestMapping(value = "/cards/{planId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemCards(LoginUser loginUser, @PathVariable Integer planId) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Pair<Problem, List<EssenceCard>> essenceCards = problemService.loadProblemCards(planId);
        if (essenceCards == null) {
            return WebUtils.error("未找到当前小课相关卡包信息");
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
                .module("小课卡包")
                .function("小课卡包")
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


}
