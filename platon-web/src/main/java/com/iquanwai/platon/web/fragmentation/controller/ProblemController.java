package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemCard;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.dto.*;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
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
    private PlanService planService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private PracticeService practiceService;

    @RequestMapping("/list/unchoose")
    public ResponseEntity<Map<String, Object>> loadUnChooseProblems(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        // 所有问题
        List<Problem> problems = problemService.loadProblems();

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
        // TODO: 待验证
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

    @RequestMapping("/list/{catalog}")
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
//                    dto.setChosenPersonCount(problemService.loadChosenPersonCount(item.getId()));
                    dto.setAbbreviation(item.getAbbreviation());
                    return dto;
                })
                .collect(Collectors.toList());

        return WebUtils.result(list);
    }

    @RequestMapping("/get/{problemId}")
    public ResponseEntity<Map<String, Object>> loadProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, unionUser.getId());

        return WebUtils.result(problem);
    }

    @RequestMapping("/open/{problemId}")
    public ResponseEntity<Map<String, Object>> openProblemIntroduction(UnionUser unionUser, @PathVariable Integer problemId,
                                                                       @RequestParam(required = false) Boolean autoOpen,
                                                                       @RequestParam(required = false) Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(problemId, unionUser.getId());

        RiseCourseDto dto = new RiseCourseDto();
        ImprovementPlan plan = planService.getPlanByProblemId(unionUser.getId(), problemId);
        Integer buttonStatus;
        if (practicePlanId != null) {
            buttonStatus = 6;
        } else {
            buttonStatus = planService.problemIntroductionButtonStatus(unionUser.getId(), problemId, plan, autoOpen);
        }
        if (practicePlanId != null) {
            practiceService.learnProblemIntroduction(unionUser.getId(), practicePlanId);
        }

        if (plan != null) {
            dto.setPlanId(plan.getId());
        }
        dto.setFee(ConfigUtils.getRiseCourseFee());
        dto.setButtonStatus(buttonStatus);
        dto.setProblem(problem);
        Profile profile = accountService.getProfile(unionUser.getId());
        dto.setIsFull(new Integer(1).equals(profile.getIsFull()));
        dto.setBindMobile(StringUtils.isNotBlank(profile.getMobileNo()));

        dto.setProblemCollected(problemService.hasCollectedProblem(unionUser.getId(), problemId));

        return WebUtils.result(dto);
    }

    @RequestMapping("/grade/{problemId}")
    public ResponseEntity<Map<String, Object>> gradeScore(UnionUser unionUser, @PathVariable Integer problemId, @RequestBody List<ProblemScore> problemScores) {
        Assert.notNull(unionUser, "用户不能为空");
        problemService.gradeProblem(problemId, unionUser.getId(), problemScores);

        return WebUtils.success();
    }

    @RequestMapping(value = "/extension/{problemId}", method = RequestMethod.GET)
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

    @ApiOperation("获取所有课程卡片信息")
    @RequestMapping(value = "/card/list")
    public ResponseEntity<Map<String, Object>> loadCardList(UnionUser unionUser) {
        List<ProblemCard> problemCards = problemService.loadProblemCardsList(unionUser.getId());
        return WebUtils.result(problemCards);
    }

    @RequestMapping(value = "/cards/{planId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProblemCards(UnionUser unionUser, @PathVariable Integer planId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Pair<Problem, List<EssenceCard>> essenceCards = problemService.loadProblemCardsByPlanId(planId);

        if (essenceCards == null) {
            return WebUtils.error("未找到当前课程相关卡包信息");
        } else {
            CardCollectionDto dto = new CardCollectionDto();
            if (essenceCards.getLeft() != null) {
                // TODO: 待验证
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
    public ResponseEntity<Map<String, Object>> loadProblemEssenceCard(UnionUser unionUser,
                                                                      @PathVariable Integer problemId, @PathVariable Integer chapterId) {
        Assert.notNull(unionUser, "登录用户不能为空");

        String essenceCardImgBase64 = problemService.loadEssenceCardImg(unionUser.getId(), problemId, chapterId);
        if (essenceCardImgBase64 != null) {
            return WebUtils.result(essenceCardImgBase64);
        } else {
            return WebUtils.error("该精华卡片正在制作中，敬请期待");
        }
    }

    @RequestMapping(value = "/collect/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> collectProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(problemId, "课程不能为空");

        int result = problemService.collectProblem(unionUser.getId(), problemId);
        if (result > 0) {
            return WebUtils.result("收藏成功");
        } else {
            return WebUtils.error("收藏失败");
        }
    }

    @RequestMapping(value = "/discollect/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> disCollectProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(problemId, "课程不能为空");

        int result = problemService.disCollectProblem(unionUser.getId(), problemId);
        if (result > 0) {
            return WebUtils.result("取消收藏成功");
        } else {
            return WebUtils.error("取消收藏失败");
        }
    }

    @RequestMapping(value = "/get/my/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getMyProblem(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(problemId, "课程不能为空");

        ImprovementPlan improvementPlan = planService.getDetailByProblemId(unionUser.getId(), problemId);

        return WebUtils.result(improvementPlan);
    }

}
