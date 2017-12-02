package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.audition.AuditionService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.*;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.*;
import com.iquanwai.platon.web.fragmentation.dto.ChapterDto;
import com.iquanwai.platon.web.fragmentation.dto.PlanListDto;
import com.iquanwai.platon.web.fragmentation.dto.SectionDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.AuditionChooseDto;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private WhiteListService whiteListService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RiseMemberService riseMemberService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AuditionService auditionService;

    /**
     * 检查是否能选课<br/>
     * 逻辑：
     * 1.会员可以选两门<br/>
     */
    @RequestMapping(value = "/choose/problem/check/{problemId}/{type}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkChoosePlan(LoginUser loginUser, @PathVariable(value = "problemId") Integer problemId,
                                                               @PathVariable(value = "type") Integer type) {
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("检查是否能够重新选择")
                .memo(problemId.toString());
        operationLogService.log(operationLog);

        // 检查小课选择是否达到会员类型所应该有的上限
        Pair<Boolean, String> accessResult = planService.loadProblemChooseAccess(loginUser.getId());
        if (!accessResult.getLeft() && !problemId.equals(ConfigUtils.getTrialProblemId())) {
            return WebUtils.error(accessResult.getRight());
        }

        List<ImprovementPlan> improvementPlans = planService.getPlans(loginUser.getId());

        List<ImprovementPlan> runningPlans = improvementPlans.stream()
                .filter(item -> item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE)
                .collect(Collectors.toList());

        // 检查是否能选新课
        Pair<Integer, String> check = planService.checkChooseNewProblem(runningPlans);
        if (check.getLeft() < 0) {
            if (check.getLeft() == -1) {
                return WebUtils.error(202, check.getRight());
            } else if (check.getLeft() == -2) {
                return WebUtils.error(203, check.getRight());
            }
        }
        // 可以选新课，接下来判断这门课是不是已经学过
        ImprovementPlan plan = improvementPlans.stream().filter(item -> item.getProblemId().equals(problemId)).findFirst().orElse(null);
        // 该小课是不是限免小课
        Integer freeProblemId = ConfigUtils.getTrialProblemId();

        // 不同操作的检查
        switch (type) {
            case 2: {
                // 直接选小课
                if (loginUser.getRiseMember() != Constants.RISE_MEMBER.MEMBERSHIP) {
                    return WebUtils.error("您不是商学院会员，需要先购买会员哦");
                }
                break;
            }
            case 1: {
                // 购买小课
                if (plan != null) {
                    // 学过该小课
                    switch (plan.getStatus()) {
                        case ImprovementPlan.RUNNING:
                            // 正在进行中，不能购买
                            return WebUtils.error(204, "小课正在进行中");
                        case ImprovementPlan.COMPLETE:
                            // 无需购买
                            return WebUtils.error(205, "小课已经完成，无需购买");
                        case ImprovementPlan.CLOSE:
                            // 关闭状态
                            return WebUtils.error("该小课无需购买");
                        default:
                            return WebUtils.error("数据异常,请联系管理员");
                    }
                } else {
                    // 没有学过该小课
                    if (loginUser.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
                        // 已经是会员，无需购买
                        return WebUtils.error("您已经是会员，无需单独购买小课");
                    }
                    // 不是会员，需要购买
                }
                break;
            }
            case 5: {
                // 限免小课试用
                if (!problemId.equals(freeProblemId)) {
                    // 不是限免小课
                    return WebUtils.error("该小课不是限免小课");
                } else {
                    // 是限免小课
                    if (plan != null) {
                        // 已经试用过了
                        return WebUtils.error("您已经试用过该小课，无法重复试用");
                    }
                }
                break;
            }
            case 6: {
                // 之前刷的历史数据，这些人是可以回复状态的
                if (plan == null) {
                    // 数据异常
                    return WebUtils.error("数据异常,请联系管理员");
                }
                break;
            }
            case 7: {
                // 限免小课试用
                if (plan != null) {
                    // 已经试用过了
                    return WebUtils.error(204, "小课正在进行中");
                }
                break;
            }
            default:
                return WebUtils.error("数据异常,请联系管理员");
        }

        if (CollectionUtils.isNotEmpty(runningPlans)) {
            // 第二门需要提示一下
            return WebUtils.error(201, "为了更专注的学习，同时最多进行3门小课，确定选择吗？");
        }

        // 现在完成小课必须在learn页面，所以这里只需要判断是否是小课已完成
        return WebUtils.success();
    }

    /**
     * 选小课，生成学习计划<br/>
     * 如果是正在进行的小课，就直接返回计划id<br/>
     * 这里不修改旧的学习计划的状态<br/>
     */
    @RequestMapping(value = "/choose/problem/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlan(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        Integer trialProblemId = ConfigUtils.getTrialProblemId();
        List<ImprovementPlan> improvementPlans = planService.getPlans(loginUser.getId());

        // 获取正在学习的小课
        List<ImprovementPlan> runningPlans = improvementPlans.stream().filter(item -> item.getStatus() == ImprovementPlan.RUNNING
                || item.getStatus() == ImprovementPlan.COMPLETE).collect(Collectors.toList());
        ImprovementPlan curPlan = improvementPlans.stream().
                filter(plan -> plan.getProblemId().equals(problemId)
                        && (plan.getStatus() == ImprovementPlan.RUNNING || plan.getStatus() == ImprovementPlan.COMPLETE))
                .findFirst().orElse(null);
        if (curPlan != null) {
            // 正在学的包括这个小课
            return WebUtils.result(curPlan.getId());
        }

        Pair<Integer, String> check = planService.checkChooseNewProblem(runningPlans);

        if (check.getLeft() < 0) {
            return WebUtils.error(check.getRight());
        }

        // 之前是否学过这个小课，避免重复生成计划
        ImprovementPlan oldPlan = improvementPlans.stream().filter(plan -> plan.getProblemId().equals(problemId)).findFirst().orElse(null);
        if (oldPlan != null) {
            return WebUtils.error("你已经选过该门小课了，你可以在\"我的\"菜单里找到以前的学习记录哦");
        }

        // 这里生成小课训练计划，另外在检查一下是否是会员或者购买了这个小课
        Boolean isRiseMember = accountService.isRiseMember(loginUser.getId());
        if (!isRiseMember && !problemId.equals(trialProblemId)) {
            // 既没有购买过这个小课，又不是rise会员,也不是限免课程
            return WebUtils.error("您不是商学院会员，需要先购买会员哦");
        }

        Integer planId = generatePlanService.generatePlan(loginUser.getId(), problemId);
        // 生成小课之后发送选课成功通知
        generatePlanService.sendOpenPlanMsg(loginUser.getOpenId(), problemId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("选择小课")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(planId);
    }

    @RequestMapping(value = "/choose/problem/camp/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createCampPlan(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择训练营小课")
                .action("训练营开课")
                .memo(problemId.toString());
        operationLogService.log(operationLog);

        Pair<Boolean, String> campChosenCheck = planService.checkChooseCampProblem(loginUser.getId(), problemId);
        if (campChosenCheck.getLeft()) {
            Integer resultPlanId = generatePlanService.forceOpenProblem(loginUser.getId(), problemId, null, null);
            return WebUtils.result(String.valueOf(resultPlanId));
        } else {
            return WebUtils.error("小课开启失败，请后台联系管理员");
        }
    }

    @RequestMapping(value = "/choose/problem/camp/unlock/{planId}")
    public ResponseEntity<Map<String, Object>> unlockCampPlan(LoginUser loginUser, @PathVariable Integer planId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("解锁过期训练营小课")
                .action("训练营解锁")
                .memo(planId.toString());
        operationLogService.log(operationLog);

        planService.unlockCampPlan(loginUser.getId(), planId);
        return WebUtils.success();
    }

    /**
     * 加载学习计划，必须传planId
     */
    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> startPlan(LoginUser loginUser, HttpServletRequest request, @RequestParam Integer planId) {
        LOGGER.info(request.getHeader("User-Agent") + ", openid:" + loginUser.getOpenId());

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            return WebUtils.result(null);
        }

        planService.buildPlanDetail(improvementPlan);
        // openid置为null
        improvementPlan.setOpenid(null);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("开始训练")
                .action("加载训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
        return WebUtils.result(improvementPlan);
    }

    @RequestMapping("/knowledge/load/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser, @PathVariable Integer knowledgeId) {

        Assert.notNull(loginUser, "用户不能为空");
        Knowledge knowledge = planService.getKnowledge(knowledgeId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点回顾")
                .action("打开回顾页")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledge);
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> complete(LoginUser loginUser, @RequestParam Integer planId) {

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
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

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("结束训练")
                .action("结束训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
            return WebUtils.error(-4, "您的小课已完成");
        }
        planService.completeCheck(improvementPlan);

        return WebUtils.success();
    }

    @RequestMapping(value = "/improvement/report", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> improvementReport(LoginUser loginUser, @RequestParam Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.error("您还没有制定训练计划哦");
        }
        if (improvementPlan.getStatus() == ImprovementPlan.RUNNING) {
            return WebUtils.error("您还没有完成训练计划哦");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("学习报告")
                .action("查看学习报告")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
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
    public ResponseEntity<Map<String, Object>> close(LoginUser loginUser, @RequestParam Integer planId) {

        Assert.notNull(loginUser, "用户不能为空");

        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.error(-3, "您还没有制定训练计划哦");
        }
        // 关闭的时候点击查看是否可以关闭
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("完成训练")
                .action("完成训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);

        Pair<Boolean, Integer> check = planService.checkCloseable(improvementPlan);

        if (!check.getLeft()) {
            // 没有完成必做
            return WebUtils.error(-1, "先完成所有的知识理解和巩固练习<br/>才能完成小课哦");
        } else {
            // 完成必做,查看最小完成天数
            if (check.getRight() != 0) {
                return WebUtils.error("学得太猛了，再复习一下吧<br/>本小课推荐学习天数至少为" + check.getRight() + "天<br/>之后就可以完成小课了");
            }
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
    @RequestMapping("/welcome")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小课")
                .function("打开小课")
                .action("打开欢迎页");
        operationLogService.log(operationLog);
        return WebUtils.result(loginUser.getRiseMember());
    }

    @RequestMapping("/risemember")
    public ResponseEntity<Map<String, Object>> isRiseMember(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("打开小课")
                .action("查看是否为rise会员")
                .memo(loginUser.getRiseMember() + "");
        operationLogService.log(operationLog);
        return WebUtils.result(loginUser.getRiseMember());
    }

    @RequestMapping(value = "/check/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> practiceCheck(LoginUser loginUser, @PathVariable Integer series, @RequestParam Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }

        Integer result = planService.checkPractice(series, improvementPlan);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练校验")
                .action("训练开始校验")
                .memo(series.toString());
        operationLogService.log(operationLog);
        if (result == -1) {
            // 前一组已完成 这一组未解锁
            // 会员都会解锁，未解锁应该都是非会员
            return WebUtils.error("该内容为付费内容，只有会员可以查看");
        } else if (result == -2) {
            // 前一组未完成
            return WebUtils.error("完成之前的任务，这一组才能解锁<br> 学习和内化，都需要循序渐进哦");
        } else if (result == -3) {
            // 小课已过期
            return WebUtils.error("抱歉哦，课程开放期间，你未能完成前面的练习，导致这个练习无法解锁");
        }
        return WebUtils.success();
    }

    @RequestMapping("/chapter/list")
    public ResponseEntity<Map<String, Object>> chapterList(LoginUser loginUser, @RequestParam Integer planId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("章节")
                .action("查询章节列表")
                .memo(planId != null ? planId.toString() : null);
        operationLogService.log(operationLog);
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
    public ResponseEntity<Map<String, Object>> markSeries(LoginUser loginUser, @PathVariable Integer series, @RequestParam Integer planId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        // fix,如果series数据不正常，则替换为边界值
        if (series < 1) {
            series = 1;
        } else if (series > improvementPlan.getTotalSeries()) {
            series = improvementPlan.getTotalSeries();
        }
        planService.markPlan(series, improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练首页")
                .action("记录训练小结")
                .memo(series.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> listUserPlans(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        List<PlanDto> currentCampPlans = Lists.newArrayList();
        Integer auditionId = ConfigUtils.getTrialProblemId();
        List<ImprovementPlan> currentCampImprovementPlans = planService.getCurrentCampPlanList(loginUser.getId());
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
            itemProblem.setChosenPersonCount(problemService.loadChosenPersonCount(item.getProblemId()));
            planDto.setProblem(itemProblem.simple());
            currentCampPlans.add(planDto);
        });

        List<ImprovementPlan> personalImprovementPlans = planService.getPlanList(loginUser.getId());
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
            itemProblem.setChosenPersonCount(problemService.loadChosenPersonCount(item.getProblemId()));
            plan.setProblem(itemProblem.simple());

            if (item.getStatus() == ImprovementPlan.CLOSE) {
                completedPlans.add(plan);
            } else {
                runningPlans.add(plan);
            }
        });
        List<Problem> recommends = loadRecommendations(loginUser.getId(), runningPlans, completedPlans);

        AuditionClassMember auditionClassMember = auditionService.loadAuditionClassMember(loginUser.getId());
        List<PlanDto> auditions = Lists.newArrayList();
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        if (auditionClassMember != null && !(riseMember != null &&
                (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE))) {

            ImprovementPlan ownedAudition = planService.getPlanList(loginUser.getId()).stream()
                    .filter(plan -> plan.getProblemId().equals(auditionId))
                    .findFirst().orElse(null);

            PlanDto plan = new PlanDto();
            // 设置 Problem 对象
            Problem itemProblem = cacheService.getProblem(auditionId);
            itemProblem.setChosenPersonCount(problemService.loadChosenPersonCount(auditionId));
            plan.setProblem(itemProblem.simple());
            plan.setName(itemProblem.getProblem());
            plan.setPic(itemProblem.getPic());
            // TODO 临时逻辑，开课时间加两天
            Date startDate = new DateTime(auditionClassMember.getStartDate()).plusDays(2).toDate();
            plan.setLearnable(startDate.compareTo(new Date()) <= 0);
            if (!plan.getLearnable()) {
                plan.setErrMsg(DateUtils.parseDateToFormat8(startDate) + " 统一开课\n请耐心等待");
            }

            if (ownedAudition != null) {
                // 有试听课,从进行中去掉这个小课
                runningPlans.removeIf(item -> item.getProblemId().equals(auditionId));
                if (!auditionClassMember.getActive()) {
                    // 已经开课
                    plan.setPlanId(ownedAudition.getId());
                }
                plan.setCompleteSeries(ownedAudition.getCompleteSeries());
                plan.setTotalSeries(ownedAudition.getTotalSeries());
                plan.setPoint(ownedAudition.getPoint());
                plan.setDeadline(ownedAudition.getDeadline());
                plan.setName(ownedAudition.getProblem().getProblem());
                plan.setPic(ownedAudition.getProblem().getPic());
                plan.setStartDate(ownedAudition.getStartDate());
                plan.setProblemId(ownedAudition.getProblemId());
                plan.setCloseTime(ownedAudition.getCloseTime());
            }
            // 没有试听课或者试听课未完成时,显示试听课选项
            if (ownedAudition == null || ownedAudition.getStatus() != ImprovementPlan.CLOSE) {
                auditions.add(plan);
            }
        }

        PlanListDto planListDto = new PlanListDto();
        planListDto.setCurrentCampPlans(currentCampPlans);
        planListDto.setRunningPlans(runningPlans);
        planListDto.setCompletedPlans(completedPlans);
        planListDto.setRecommendations(recommends);
        planListDto.setRiseMember(loginUser.getRiseMember());
        planListDto.setAuditions(auditions);
        runningPlans.sort(Comparator.comparing(PlanDto::getStartDate));
        completedPlans.sort(this::sortPlans);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("计划列表")
                .action("查询计划列表");
        operationLogService.log(operationLog);
        return WebUtils.result(planListDto);
    }

    // 查询推荐的小课
    private List<Problem> loadRecommendations(Integer profileId, List<PlanDto> runningPlans, List<PlanDto> completedPlans) {
        // 最后要返回的
        List<Problem> problems = Lists.newArrayList();
        // 用户已经有的小课
        List<Integer> userProblems = Lists.newArrayList();
        runningPlans.forEach(item -> userProblems.add(item.getProblemId()));
        completedPlans.forEach(item -> userProblems.add(item.getProblemId()));

        // 根据有用性评分排列小课
        List<Problem> usefulProblems = problemService.loadProblems().stream()
                .sorted((left, right) -> right.getUsefulScore() > left.getUsefulScore() ? 1 : -1)
                .collect(Collectors.toList());

        boolean inWhiteList = whiteListService.isInWhiteList(WhiteList.TRIAL, profileId);
        for (Problem problem : usefulProblems) {
            //非天使用户去除内测小课
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

    @RequestMapping(value = "/chapter/card/access/{problemId}/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> loadChapterAccess(LoginUser loginUser, @PathVariable Integer problemId, @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        Boolean authority = planService.loadChapterCardAccess(loginUser.getId(), problemId, practicePlanId);
        if (authority != null) {
            return WebUtils.result(authority);
        } else {
            return WebUtils.error("服务异常，请联系管理员重试");
        }
    }

    /**
     * 当用户做完某一章节的所有巩固练习后，后台回复章节卡片
     */
    @RequestMapping(value = "/chapter/card/{problemId}/{practicePlanId}")
    public ResponseEntity<Map<String, Object>> loadChapterCard(LoginUser loginUser, @PathVariable Integer problemId, @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("小课学习").action("打开小课学习")
                .function("加载章节卡片").memo(practicePlanId.toString());
        operationLogService.log(operationLog);
        String chapterCardData = planService.loadChapterCard(loginUser.getId(), problemId, practicePlanId);
        if (chapterCardData != null) {
            return WebUtils.result(chapterCardData);
        } else {
            return WebUtils.error("该卡片正在制作中，请期待~");
        }
    }

    @RequestMapping(value = "/open/audition/course", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openAuditionCourse(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create()
                .openid(loginUser.getOpenId())
                .module("小课")
                .function("试听课")
                .action("开课");
        operationLogService.log(operationLog);
        Integer auditionId = ConfigUtils.getTrialProblemId();
        AuditionClassMember auditionClassMember = auditionService.loadAuditionClassMember(loginUser.getId());

        ImprovementPlan ownedAudition = planService.getPlanList(loginUser.getId()).stream().filter(plan -> plan.getProblemId().equals(auditionId)).findFirst().orElse(null);
        Integer planId;
        if (auditionClassMember != null && auditionClassMember.getActive()) {
            // TODO 临时逻辑，开课时间加两天
            Date startDate = new DateTime(auditionClassMember.getStartDate()).plusDays(2).toDate();
            // 检查是否到了开课时间
            if (startDate.compareTo(new Date()) > 0) {
                return WebUtils.error(DateUtils.parseDateToFormat8(startDate) + " 统一开课，请耐心等待");
            }
            if (ownedAudition != null) {
                // 已经拥有试听课
                // 没有试听课的状态，第一次学习试听课
                planId = generatePlanService.magicUnlockProblem(loginUser.getId(), auditionId,
                        DateUtils.afterDays(new Date(), GeneratePlanService.PROBLEM_MAX_LENGTH), false);
                generatePlanService.sendOpenPlanMsg(loginUser.getOpenId(), auditionId);
            } else {
                // 没有试听课，判断是不是会员
                RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
                if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
                    // 会员不需要重复选择试听课
                    return WebUtils.error("商学院员会员可以在发现页面选课哦");
                } else {
                    // 没有试听课，并且不是年费会员,可以选择试听课
                    planId = generatePlanService.generatePlan(loginUser.getId(), auditionId);
                    // 发送入群消息
                    generatePlanService.sendOpenPlanMsg(loginUser.getOpenId(), auditionId);
                }
            }
            // 开课
            auditionService.openAuditionCourse(auditionClassMember.getId());
            return WebUtils.result(planId);
        } else {
            if (ownedAudition != null) {
                return WebUtils.result(ownedAudition.getId());
            } else {
                return WebUtils.error("您没有试听课的权限哦");
            }
        }
    }

    @RequestMapping(value = "/choose/audition/course", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> chooseAuditionCourse(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create()
                .openid(loginUser.getOpenId())
                .module("小课")
                .function("试听课")
                .action("选择试听课");
        operationLogService.log(operationLog);
        // 检查是否能选择试听课
        // 标准：1.非会员，2.没有选过
        Integer auditionId = ConfigUtils.getTrialProblemId();
        AuditionClassMember auditionClassMember = auditionService.loadAuditionClassMember(loginUser.getId());
        AuditionChooseDto dto = new AuditionChooseDto();

        dto.setGoSuccess(false);
        ImprovementPlan ownedAudition = planService.getPlanList(loginUser.getId()).stream()
                .filter(plan -> plan.getProblemId().equals(auditionId)).findFirst().orElse(null);

        if (auditionClassMember == null) {
            //  没有试听过
            RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
            if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
                return WebUtils.error("商学院会员可以在发现页面选课哦");
            } else {
                String className = auditionService.signupAudition(loginUser.getId(), loginUser.getOpenId());
                dto.setClassName(className);
                customerMessageService.sendCustomerMessage(loginUser.getOpenId(), ConfigUtils.getValue("audition.choose.send.image"), Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
                ThreadPool.execute(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        customerMessageService.sendCustomerMessage(loginUser.getOpenId(), className, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }

                });
            }
            dto.setGoSuccess(true);
        } else {
            dto.setClassName(auditionClassMember.getClassName());
            // 未开课时也跳到售卖页
            // TODO 临时逻辑，开课时间加两天
            Date startDate = new DateTime(auditionClassMember.getStartDate()).plusDays(2).toDate();

            if (startDate.after(new Date())) {
                dto.setGoSuccess(true);
            }
        }
        if (ownedAudition != null && ownedAudition.getStatus() == ImprovementPlan.RUNNING) {
            dto.setPlanId(ownedAudition.getId());
        }
        dto.setSubscribe(true);
        try {
            accountService.getAccount(loginUser.getOpenId(), true);
        } catch (NotFollowingException e) {
            dto.setSubscribe(false);
        }

        return WebUtils.result(dto);
    }


}
