package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.*;
import com.iquanwai.platon.biz.exception.CreateCourseException;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.UserProblemSchedule;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.dto.ChapterDto;
import com.iquanwai.platon.web.fragmentation.dto.PlanListDto;
import com.iquanwai.platon.web.fragmentation.dto.SectionDto;
import com.iquanwai.platon.web.fragmentation.dto.SectionProgressDto;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 * 训练计划相关的请求处理类
 */
@RestController
@RequestMapping("/rise/plan")
public class PlanController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private StudyService studyService;

    /**
     * 选课程，生成学习计划<br/>
     * 如果是正在进行的课程，就直接返回计划id<br/>
     * 这里不修改旧的学习计划的状态<br/>
     */
    @RequestMapping(value = "/choose/problem/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlan(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");
        List<ImprovementPlan> improvementPlans = planService.getPlans(unionUser.getId());

        // 获取正在学习的课程

        ImprovementPlan curPlan = improvementPlans.stream()
                .filter(plan -> plan.getProblemId().equals(problemId))
                .filter(plan -> plan.getStatus() == ImprovementPlan.RUNNING || plan.getStatus() == ImprovementPlan.COMPLETE)
                .findFirst().orElse(null);
        if (curPlan != null) {
            // 正在学的包括这个课程
            return WebUtils.result(curPlan.getId());
        }

        try {
            // 检查是否能开新课
            planService.checkChooseNewProblem(improvementPlans, unionUser.getId(), problemId);
        } catch (CreateCourseException ex) {
            return WebUtils.error(ex.getMessage());
        }

        Integer planId = generatePlanService.generatePlan(unionUser.getId(), problemId);
        // 生成课程之后发送选课成功通知
        generatePlanService.sendOpenPlanMsg(unionUser.getOpenId(), problemId);

        return WebUtils.result(planId);
    }

    @RequestMapping(value = "/choose/problem/camp/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createCampPlan(UnionUser unionUser, @PathVariable Integer problemId) {
        Assert.notNull(unionUser, "用户不能为空");

        Pair<Boolean, String> campChosenCheck = planService.checkChooseCampProblem(unionUser.getId(), problemId);
        if (campChosenCheck.getLeft()) {
            Integer resultPlanId = generatePlanService.magicOpenProblem(unionUser.getId(), problemId, null, null, true);
            return WebUtils.result(String.valueOf(resultPlanId));
        } else {
            return WebUtils.error("课程开启失败，请后台联系管理员");
        }
    }

    @RequestMapping(value = "/choose/problem/camp/unlock/{planId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> unlockCampPlan(UnionUser unionUser, @PathVariable Integer planId) {
        planService.unlockCampPlan(unionUser.getId(), planId);
        return WebUtils.success();
    }

    /**
     * 加载学习计划，必须传planId
     */
    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> startPlan(UnionUser unionUser, @RequestParam Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            return WebUtils.result(null);
        }

        planService.buildPlanDetail(improvementPlan);
        return WebUtils.result(improvementPlan);
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> complete(UnionUser unionUser, @RequestParam Integer planId) {

        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }

        if (improvementPlan.getStatus() == ImprovementPlan.COMPLETE) {
            // 已经是完成状态
            return WebUtils.success();
        }

        Pair<Boolean, Integer> closeable = planService.checkCloseable(improvementPlan);
        // 只要完成必做就可以complete
        if (!closeable.getLeft()) {
            return WebUtils.error(-1, "");
        }

        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
            return WebUtils.error(-4, "您的课程已完成");
        }
        planService.completeCheck(improvementPlan);

        return WebUtils.success();
    }

    @RequestMapping(value = "/improvement/report", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> improvementReport(UnionUser unionUser, @RequestParam Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.error("您还没有制定训练计划哦");
        }
        if (improvementPlan.getStatus() == ImprovementPlan.RUNNING) {
            return WebUtils.error("您还没有完成训练计划哦");
        }

        ImprovementReport report = reportService.loadUserImprovementReport(improvementPlan);
        if (report.getApplicationShouldCount().equals(report.getApplicationCompleteCount())) {
            report.setDoneAllApps(true);
        } else {
            report.setDoneAllApps(false);
        }
        Pair<Boolean, Integer> check = planService.checkCloseable(improvementPlan);
        report.setMustStudyDays(check.getRight());
        return WebUtils.result(report);
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> close(UnionUser unionUser, @RequestParam Integer planId) {

        Assert.notNull(unionUser, "用户不能为空");

        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.error(-3, "您还没有制定训练计划哦");
        }

        Pair<Boolean, Integer> check = planService.checkCloseable(improvementPlan);

        if (!check.getLeft()) {
            // 没有完成必做
            return WebUtils.error(-1, "先完成所有的知识理解和巩固练习<br/>才能完成课程哦");
        }

        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
            // 如果plan已经关闭了，则不再重复调用service
            return WebUtils.success();
        }
        // 可以关闭，进行关闭
        planService.completePlan(improvementPlan.getId(), ImprovementPlan.CLOSE);


        return WebUtils.success();
    }

    @Deprecated
    @RequestMapping("/risemember")
    public ResponseEntity<Map<String, Object>> isRiseMember(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        return WebUtils.result(1);
    }

    @RequestMapping(value = "/check/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> practiceCheck(UnionUser unionUser, @PathVariable Integer series, @RequestParam Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }

        Integer result = planService.checkPractice(series, improvementPlan);
        if (result == -1) {
            // 前一组已完成 这一组未解锁
            // 会员都会解锁，未解锁应该都是非会员
            return WebUtils.error("该内容为付费内容，只有会员可以查看");
        } else if (result == -2) {
            // 前一组未完成
            return WebUtils.error("完成之前的任务，这一组才能解锁<br> 学习和内化，都需要循序渐进哦");
        } else if (result == -3) {
            // 课程已过期
            return WebUtils.error("抱歉哦，课程开放期间，你未能完成前面的练习，导致这个练习无法解锁");
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/chapter/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> chapterList(UnionUser unionUser, @RequestParam Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan plan = planService.getPlan(planId);
        if (plan == null) {
            return WebUtils.error(null);
        }
        List<UserProblemSchedule> chapterList = planService.getChapterList(plan);
        Map<Integer, ChapterDto> filterChapter = Maps.newHashMap();
        chapterList.forEach(item -> {
            ChapterDto chapterDto = filterChapter.computeIfAbsent(item.getChapter(), (chapterId) -> {
                ChapterDto dto = new ChapterDto();
                dto.setChapterId(chapterId);
                dto.setChapter(item.getChapterStr());
                dto.setSectionList(Lists.newArrayList());
                return dto;
            });
            SectionDto sectionDto = new SectionDto();
            sectionDto.setSeries(item.getSeries());
            sectionDto.setSection(item.getSectionStr());
            sectionDto.setSectionId(item.getSection());
            chapterDto.getSectionList().add(sectionDto);
        });
        return WebUtils.result(filterChapter.values());
    }

    @RequestMapping(value = "/mark/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> markSeries(UnionUser unionUser, @PathVariable Integer series, @RequestParam Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", unionUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        // fix,如果series数据不正常，则替换为边界值
        if (series < 1) {
            series = 1;
        } else if (series > improvementPlan.getTotalSeries()) {
            series = improvementPlan.getTotalSeries();
        }
        planService.markPlan(series, improvementPlan.getId());

        return WebUtils.success();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> listUserPlans(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        List<PlanDto> currentCampPlans = Lists.newArrayList();
        List<ImprovementPlan> currentCampImprovementPlans = planService.getCurrentCampPlanList(unionUser.getId());
        currentCampImprovementPlans.forEach(item -> {
            PlanDto planDto = new PlanDto();
            planDto.setPlanId(item.getId());
            planDto.setCompleteSeries(item.getCompleteSeries());
            planDto.setTotalSeries(item.getTotalSeries());
            planDto.setPoint(item.getPoint());
            planDto.setDeadline(item.getDeadline());
            planDto.setName(item.getProblem().getProblem());
            planDto.setPic(item.getProblem().getPic());
            planDto.setStartDate(item.getStartDate());
            planDto.setProblemId(item.getProblemId());
            planDto.setCloseTime(item.getCloseTime());

            // 设置 Problem 对象
            Problem itemProblem = cacheService.getProblem(item.getProblemId());
            planDto.setProblem(itemProblem.simple());
            currentCampPlans.add(planDto);
        });

        List<ImprovementPlan> personalImprovementPlans = planService.getPlanList(unionUser.getId());
        List<PlanDto> runningPlans = Lists.newArrayList();
        List<PlanDto> completedPlans = Lists.newArrayList();
        personalImprovementPlans.forEach(item -> {
            PlanDto plan = new PlanDto();
            plan.setPlanId(item.getId());
            plan.setCompleteSeries(item.getCompleteSeries());
            plan.setTotalSeries(item.getTotalSeries());
            plan.setPoint(item.getPoint());
            plan.setDeadline(item.getDeadline());
            plan.setName(item.getProblem().getProblem());
            plan.setPic(item.getProblem().getPic());
            plan.setStartDate(item.getStartDate());
            plan.setProblemId(item.getProblemId());
            plan.setCloseTime(item.getCloseTime());
            plan.setLearnable(item.getStartDate().compareTo(new Date()) <= 0);

            // 设置 Problem 对象
            Problem itemProblem = cacheService.getProblem(item.getProblemId());
            plan.setProblem(itemProblem.simple());

            if (item.getStatus() == ImprovementPlan.CLOSE) {
                completedPlans.add(plan);
            } else {
                runningPlans.add(plan);
            }
        });
        List<Problem> recommends = loadRecommendations(unionUser.getId(), runningPlans, completedPlans);

        PlanListDto planListDto = new PlanListDto();
        planListDto.setCurrentCampPlans(currentCampPlans);
        planListDto.setRunningPlans(runningPlans);
        planListDto.setCompletedPlans(completedPlans);
        planListDto.setRecommendations(recommends);
        Boolean isRiseMember = CollectionUtils.isNotEmpty(riseMemberManager.member(unionUser.getId()));
        // TODO: 待验证
        planListDto.setRiseMember(isRiseMember ? 1 : 0);
        planListDto.setCampBanner(ConfigUtils.getCampProblemBanner());
        runningPlans.sort(Comparator.comparing(PlanDto::getStartDate));
        completedPlans.sort(this::sortPlans);

        return WebUtils.result(planListDto);
    }

    // 查询推荐的课程
    private List<Problem> loadRecommendations(Integer profileId, List<PlanDto> runningPlans, List<PlanDto> completedPlans) {
        // 最后要返回的
        List<Problem> problems = Lists.newArrayList();
        // 用户已经有的课程
        List<Integer> userProblems = Lists.newArrayList();
        runningPlans.forEach(item -> userProblems.add(item.getProblemId()));
        completedPlans.forEach(item -> userProblems.add(item.getProblemId()));

        // 根据有用性评分排列课程
        List<Problem> usefulProblems = problemService.loadProblems().stream()
                .sorted((left, right) -> right.getUsefulScore() > left.getUsefulScore() ? 1 : -1)
                .collect(Collectors.toList());

        boolean inWhiteList = whiteListService.isInWhiteList(WhiteList.TRIAL, profileId);
        for (Problem problem : usefulProblems) {
            //非天使用户去除内测课程
            if (!inWhiteList) {
                if (problem.getTrial()) {
                    continue;
                }
            }
            if (!userProblems.contains(problem.getId())) {
                // 只推荐用户没有上过的课程
                problems.add(problem.simple());
                if (problems.size() >= ProblemService.MAX_RECOMMENDATION_SIZE) {
                    return problems;
                }
            }
        }

        return problems;
    }

    // 倒序排列
    private int sortPlans(PlanDto left, PlanDto right) {
        if (left.getCloseTime() == null) {
            return 1;
        } else if (right.getCloseTime() == null) {
            return 0;
        }
        return right.getCloseTime().compareTo(left.getCloseTime());
    }

    @RequestMapping(value = "/chapter/card/access/{problemId}/{practicePlanId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadChapterAccess(UnionUser unionUser, @PathVariable Integer problemId, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        Boolean authority = planService.loadChapterCardAccess(unionUser.getId(), problemId, practicePlanId);
        if (authority != null) {
            return WebUtils.result(authority);
        } else {
            return WebUtils.error("服务异常，请联系管理员重试");
        }
    }

    /**
     * 当用户做完某一章节的所有巩固练习后，后台回复章节卡片
     */
    @RequestMapping(value = "/chapter/card/{problemId}/{practicePlanId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadChapterCard(UnionUser unionUser, @PathVariable Integer problemId, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");

        String chapterCardData = planService.loadChapterCard(unionUser.getId(), problemId, practicePlanId);
        if (chapterCardData != null) {
            return WebUtils.result(chapterCardData);
        } else {
            return WebUtils.error("该卡片正在制作中，请期待~");
        }
    }

    @RequestMapping(value = "/load/studyline/{planId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadStudyLine(UnionUser unionUser, @PathVariable Integer planId) {
        Assert.notNull(unionUser, "用户不能为空");

        StudyLine studyLine = studyService.loadStudyLine(planId);

        return WebUtils.result(studyLine);
    }

    @RequestMapping(value = "/load/series/{practicePlanId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadPlanSeriesStatus(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "登录用户不能为空");
        List<PlanSeriesStatus> planSeriesStatuses = planService.loadPlanSeries(practicePlanId);
        Pair<String, Integer> planSeriesInfo = planService.loadPlanSeriesInfo(practicePlanId);

        if (planSeriesStatuses.size() == 0) {
            return WebUtils.error("小节数据完成情况为空");
        } else {
            SectionProgressDto sectionProgressDto = new SectionProgressDto();
            sectionProgressDto.setPlanSeriesTitle(planSeriesInfo.getLeft());
            sectionProgressDto.setIndex(planSeriesInfo.getRight());
            sectionProgressDto.setPlanSeriesStatuses(planSeriesStatuses);
            return WebUtils.result(sectionProgressDto);
        }
    }

    @RequestMapping(value = "/knowledge/review/{practicePlanId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadKnowledgeReview(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        Problem problem = problemService.getProblemForSchedule(practicePlanId);

        return WebUtils.result(problem);
    }

}

