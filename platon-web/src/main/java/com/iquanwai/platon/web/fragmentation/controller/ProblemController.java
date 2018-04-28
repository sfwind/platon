package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemCard;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.po.user.StudyInfo;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.*;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
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
@Api(description = "课程相关的请求处理类")
public class ProblemController {
    @Autowired
    private ProblemService problemService;
    @Autowired
    private PlanService planService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private CustomerService customerService;

    @RequestMapping(value = "/list/unchoose", method = RequestMethod.GET)
    @ApiOperation(value = "发现页面拉取课程列表", response = ProblemCatalogDto.class)
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        // 所有问题
        List<Problem> problems = problemService.loadProblems();

        //专业版只能选核心能力项目
        problems = problems.stream().filter(problem1 ->
                problem1.getProject() == Constants.Project.CORE_PROJECT).collect(Collectors.toList());

        //TODO:专业版可以学习problemId = 5, 11, 13, 19
        List<Integer> problemIds = problems.stream().map(Problem::getId).collect(Collectors.toList());
        // 逻辑谬误
        if (!problemIds.contains(5)) {
            problems.add(problemService.getProblem(5));
        }
        // 行为分析
        if (!problemIds.contains(11)) {
            problems.add(problemService.getProblem(11));
        }
        // 公开演讲
        if (!problemIds.contains(13)) {
            problems.add(problemService.getProblem(13));
        }
        // 结识牛人
        if (!problemIds.contains(19)) {
            problems.add(problemService.getProblem(19));
        }

        //非天使用户去除试用版课程
        if (!whiteListService.isInWhiteList(WhiteList.TRIAL, unionUser.getId())) {
            problems = problems.stream().filter(problem -> !problem.getTrial()).collect(Collectors.toList());
        }
        // 用户的所有计划
        List<ImprovementPlan> userProblems = planService.getPlans(unionUser.getId());
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
        result.setName(unionUser.getNickName());
        result.setCatalogList(catalogListDtos);

        List<RiseMember> riseMembers = riseMemberManager.member(unionUser.getId());
        result.setRiseMember(CollectionUtils.isNotEmpty(riseMembers));
        result.setBanners(problemService.loadExploreBanner());
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

    @RequestMapping(value = "/list/{catalog}", method = RequestMethod.GET)
    @ApiOperation(value = "获取某个分类的课程", response = ProblemCatalog.class, responseContainer = "List")
    @ApiImplicitParams({@ApiImplicitParam(name = "catalogId", value = "类别id")})
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(UnionUser unionUser, @PathVariable(value = "catalog") Integer catalogId) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(catalogId, "课程分类不能为空");

        ProblemCatalog problemCatalog = problemService.getProblemCatalog(catalogId);

        if (problemCatalog != null) {
            // 所有问题
            List<Problem> problems = problemService.loadProblems();
            //非天使用户去除试用课程
            if (!whiteListService.isInWhiteList(WhiteList.TRIAL, unionUser.getId())) {
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
                        dto.setAbbreviation(item.getAbbreviation());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return WebUtils.result(list);
        } else {
            return WebUtils.error("分类不能为空");
        }
    }

    @RequestMapping(value = "/list/all", method = RequestMethod.GET)
    @ApiOperation(value = "获取所有的课程", response = ProblemExploreDto.class)
    public ResponseEntity<Map<String, Object>> loadAllProblem(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");

        List<ProblemCatalog> problemCatalogs = problemService.getProblemCatalogs();
        // 所有问题
        List<Problem> problems = problemService.loadProblems();
        //非天使用户去除试用课程
        if (!whiteListService.isInWhiteList(WhiteList.TRIAL, unionUser.getId())) {
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
                    dto.setAbbreviation(item.getAbbreviation());
                    return dto;
                })
                .collect(Collectors.toList());

        return WebUtils.result(list);
    }

    @RequestMapping(value = "/get/{problemId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取的课程信息", response = Problem.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "problemId", value = "课程id")})
    public ResponseEntity<Map<String, Object>> loadProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, unionUser.getId());

        return WebUtils.result(problem);
    }

    @RequestMapping(value = "/open/{problemId}", method = RequestMethod.GET)
    @ApiOperation(value = "打开课程介绍页信息", response = RiseCourseDto.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "autoOpen", value = "是否自动开课"),
            @ApiImplicitParam(name = "practicePlanId", value = "课程介绍练习id")})
    public ResponseEntity<Map<String, Object>> openProblemIntroduction(UnionUser unionUser, @PathVariable Integer problemId,
                                                                       @RequestParam(required = false) Boolean autoOpen,
                                                                       @RequestParam(required = false) Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, unionUser.getId());

        RiseCourseDto dto = new RiseCourseDto();
        ImprovementPlan plan = planService.getPlanByProblemId(unionUser.getId(), problemId);
        Integer buttonStatus;
        //如果practicePlanId不为空,则请求来自于课程介绍练习
        if (practicePlanId != null) {
            buttonStatus = 6;
        } else {
            buttonStatus = planService.problemIntroductionButtonStatus(unionUser.getId(), problemId, plan, autoOpen);
        }
        if (practicePlanId != null) {
            practiceService.learnPracticePlan(unionUser.getId(), practicePlanId);
        }

        dto.setButtonStatus(buttonStatus);
        dto.setProblem(problem);

        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/grade/{problemId}", method = RequestMethod.POST)
    @ApiOperation("获取的课程信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "problemId", value = "课程id")})
    public ResponseEntity<Map<String, Object>> gradeScore(UnionUser unionUser, @PathVariable Integer problemId, @RequestBody List<ProblemScore> problemScores) {
        Assert.notNull(unionUser, "用户不能为空");
        problemService.gradeProblem(problemId, unionUser.getId(), problemScores);

        return WebUtils.success();
    }

    @RequestMapping(value = "/extension/{problemId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取所有课程衍生学习信息", response = ProblemActivity.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "problemId", value = "课程id")})
    public ResponseEntity<Map<String, Object>> loadProblemExtension(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(problemId, "请求 ProblemId 不能为空");

        ProblemExtension extension = problemService.loadProblemExtensionByProblemId(problemId);
        List<ProblemActivity> activities = problemService.loadProblemActivitiesByProblemId(problemId);
        if (extension != null && activities != null) {
            extension.setActivities(activities);
            extension.setOnlineActivities(activities.stream()
                    .filter(activity -> ProblemActivity.ONLINE.equals(activity.getType())).collect(Collectors.toList()));
            extension.setOfflineActivities(activities.stream()
                    .filter(activity -> ProblemActivity.OFFLINE.equals(activity.getType())).collect(Collectors.toList()));
            return WebUtils.result(extension);
        } else {
            return WebUtils.error("当前课程暂无延伸学习相关内容");
        }
    }

    @RequestMapping(value = "/card/list", method = RequestMethod.GET)
    @ApiOperation(value = "获取所有课程卡片信息", response = ProblemCard.class)
    public ResponseEntity<Map<String, Object>> loadCardList(UnionUser unionUser) {
        List<ProblemCard> problemCards = problemService.loadProblemCardsList(unionUser.getId());
        return WebUtils.result(problemCards);
    }

    @RequestMapping(value = "/cards/{planId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取某个课程的卡片", response = CardCollectionDto.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "planId", value = "计划id")})
    public ResponseEntity<Map<String, Object>> loadProblemCards(UnionUser unionUser, @PathVariable Integer planId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Pair<Problem, List<EssenceCard>> essenceCards = problemService.loadProblemCardsByPlanId(planId);

        if (essenceCards == null) {
            return WebUtils.error("未找到当前课程相关卡包信息");
        } else {
            CardCollectionDto dto = new CardCollectionDto();
            if (essenceCards.getLeft() != null) {
                List<RiseMember> riseMembers = riseMemberManager.member(unionUser.getId());
                dto.setIsRiseMember(CollectionUtils.isNotEmpty(riseMembers));
                dto.setProblemId(essenceCards.getLeft().getId());
                dto.setProblem(essenceCards.getLeft().getProblem());
            }
            dto.setCards(essenceCards.getRight());
            return WebUtils.result(dto);
        }
    }

    @RequestMapping(value = "/card/{problemId}/{chapterId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取某一张卡片的base64信息", response = String.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "problemId", value = "课程id"),
            @ApiImplicitParam(name = "chapterId", value = "章id")})
    public ResponseEntity<Map<String, Object>> loadProblemEssenceCard(UnionUser unionUser,
                                                                      @PathVariable Integer problemId, @PathVariable Integer chapterId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Integer profileId = unionUser.getId();
        StudyInfo studyInfo = new StudyInfo();

        studyInfo.setLearnedDay(customerService.loadContinuousLoginCount(profileId));
        studyInfo.setLearnedKnowledge(customerService.loadLearnedKnowledgesCount(profileId));
        studyInfo.setDefeatPercent(customerService.calSyncDefeatPercent(profileId,problemId));

        String essenceCardImgBase64 = problemService.loadEssenceCardImg(unionUser.getId(), problemId, chapterId,studyInfo);
        if (essenceCardImgBase64 != null) {
            return WebUtils.result(essenceCardImgBase64);
        } else {
            return WebUtils.error("该精华卡片正在制作中，敬请期待");
        }
    }

    @RequestMapping(value = "/get/my/{problemId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取我的课程计划", response = ImprovementPlan.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "problemId", value = "课程id")})
    public ResponseEntity<Map<String, Object>> getMyProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(problemId, "课程不能为空");

        ImprovementPlan improvementPlan = planService.getDetailByProblemId(unionUser.getId(), problemId);

        return WebUtils.result(improvementPlan);
    }

}
