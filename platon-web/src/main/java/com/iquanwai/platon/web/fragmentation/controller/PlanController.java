package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.common.whitelist.WhiteListService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ImprovementReport;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ReportService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.Recommendation;
import com.iquanwai.platon.biz.po.RiseCourseOrder;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.WhiteList;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.ChapterDto;
import com.iquanwai.platon.web.fragmentation.dto.PlanListDto;
import com.iquanwai.platon.web.fragmentation.dto.SectionDto;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
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
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private OperationFreeLimitService operationFreeLimitService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private WhiteListService whiteListService;

    /**
     * 检查是否能选课<br/>
     * 逻辑：
     * 1.会员可以选两门<br/>
     */
    @RequestMapping(value = "/choose/problem/check/{problemId}/{type}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkChoosePlan(LoginUser loginUser,
                                                               @PathVariable(value = "problemId") Integer problemId,
                                                               @PathVariable(value = "type") Integer type) {
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("检查是否能够重新选择")
                .memo(problemId.toString());
        operationLogService.log(operationLog);

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
                    return WebUtils.error("您不是年费会员，需要单独购买小课哦");
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
                        case ImprovementPlan.TRIALCLOSE:
                            // 试学结束，查看会员类型
                            if (loginUser.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP) {
                                // 是会员，不需要购买
                                return WebUtils.error("您已经是会员，无需单独购买小课");
                            }
                            // 不是会员，需要购买
                            // ignore
                            break;
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
            case 3: {
                // 限免小课试用
                if (!problemId.equals(freeProblemId)) {
                    // 不是限免小课
                    return WebUtils.error("该小课不是限免小课");
                } else {
                    // 是限免小课
                    if (plan != null) {
                        // 已经试用过了
                        return WebUtils.error("您已经试用过该小课，无法重复试用");
                    } else {
                        // 没有试用过
                        LOGGER.error("数据异常，请联系管理员 {}", loginUser.getOpenId());
                    }
                }
                break;
            }
            case 6: {
                // 之前刷的历史数据，这些人是可以回复状态的
                if (plan == null) {
                    // 数据异常
                    return WebUtils.error("数据异常,请联系管理员");
                } else {
                    if (plan.getStatus() != ImprovementPlan.TEMP_TRIALCLOSE) {
                        // 不是这样的特殊数据
                        LOGGER.error("选小课check接口异常,profileId:{},problemId:{}", loginUser.getId(), problemId);
                        return WebUtils.error("数据异常，请联系管理员");
                    }
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
        }

        if (CollectionUtils.isNotEmpty(runningPlans)) {
            // 第二门需要提示一下
            return WebUtils.error(201, "为了更专注的学习，同时最多进行两门小课，确定选择吗？");
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
    public ResponseEntity<Map<String, Object>> createPlan(LoginUser loginUser,
                                                          @PathVariable Integer problemId) {
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
            if (oldPlan.getStatus() == ImprovementPlan.TEMP_TRIALCLOSE || oldPlan.getStatus() == ImprovementPlan.TRIALCLOSE) {
                // 老得是试用版
                // 将它解锁
                generatePlanService.reopenTrialPlan(oldPlan);
                // 解锁了
                if (problemId.equals(trialProblemId)) {
                    // 限免小课
                    operationFreeLimitService.recordOrderAndSendMsg(loginUser.getOpenId(), PromotionUser.TRIAL);
                }
                OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                        .module("RISE")
                        .function("选择小课")
                        .action("选择小课")
                        .memo(problemId.toString());
                operationLogService.log(operationLog);
                return WebUtils.result(oldPlan.getId());
            } else {
                // 不是试用版那些手动修改的数据，不能再次选择
                return WebUtils.error("你已经选过该门小课了，你可以在\"我的\"菜单里找到以前的学习记录哦");
            }
        }

        // 这里生成小课训练计划，另外在检查一下是否是会员或者购买了这个小课
        RiseCourseOrder riseCourseOrderOrder = planService.getEntryRiseCourseOrder(loginUser.getId(), problemId);
        Boolean isRiseMember = accountService.isRiseMember(loginUser.getId());
        if (!isRiseMember && riseCourseOrderOrder == null && !problemId.equals(trialProblemId)) {
            // 既没有购买过这个小课，又不是rise会员,也不是限免课程
            return WebUtils.error("非rise会员需要单独购买小课哦");
        }
        Integer planId = generatePlanService.generatePlan(loginUser.getOpenId(), loginUser.getId(), problemId);
        // 初始化第一层promotionUser

        if (problemId.equals(trialProblemId)) {
            // TODO 活动结束后删除,如果是自然增长，就插入
            operationFreeLimitService.initFirstPromotionLevel(loginUser.getOpenId(), loginUser.getRiseMember());
            // 限免小课
            operationFreeLimitService.recordOrderAndSendMsg(loginUser.getOpenId(), PromotionUser.TRIAL);
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("选择小课")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(planId);
    }

    /**
     * 加载学习计划，必须传planId
     */
    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> startPlan(LoginUser loginUser, HttpServletRequest request,
                                                         @RequestParam Integer planId) {
        LOGGER.info(request.getHeader("User-Agent") + ", openid:" + loginUser.getOpenId());

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if (improvementPlan == null) {
            return WebUtils.result(null);
        }

        planService.buildPlanDetail(improvementPlan);
        // openid置为null
        improvementPlan.setOpenid(null);
        if (!loginUser.getOpenRise()) {
            // 没有点开rise
            Profile profile = accountService.getProfile(loginUser.getId());
            loginUser.setOpenRise(profile.getOpenRise());
            improvementPlan.setOpenRise(profile.getOpenRise());
        }
        improvementPlan.setOpenRise(loginUser.getOpenRise());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("开始训练")
                .action("加载训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
        return WebUtils.result(improvementPlan);
    }

    @RequestMapping("/knowledge/load/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer knowledgeId) {

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
    public ResponseEntity<Map<String, Object>> complete(LoginUser loginUser,
                                                        @RequestParam Integer planId) {

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

    @RequestMapping(value = "/improvement/report/recommendation/{problemId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRecommendation(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "当前用户不能为空");
        List<Recommendation> recommendations = reportService.loadRecommendationByProblemId(problemId);
        if (recommendations.size() > 0) {
            return WebUtils.result(recommendations);
        } else {
            return WebUtils.error("未找到推荐小课信息");
        }
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> close(LoginUser loginUser,
                                                     @RequestParam Integer planId) {

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
    public ResponseEntity<Map<String, Object>> practiceCheck(LoginUser loginUser,
                                                             @PathVariable Integer series,
                                                             @RequestParam Integer planId) {
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
        List<ProblemSchedule> chapterList = planService.getChapterList(plan);
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
    public ResponseEntity<Map<String, Object>> markSeries(LoginUser loginUser,
                                                          @PathVariable Integer series,
                                                          @RequestParam Integer planId) {
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
        List<ImprovementPlan> plans = planService.getPlanList(loginUser.getId());
        List<PlanDto> trialClosedPlans = Lists.newArrayList();
        List<PlanDto> runningPlans = Lists.newArrayList();
        List<PlanDto> completedPlans = Lists.newArrayList();
        plans.forEach(item -> {
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
            if (item.getStatus() == ImprovementPlan.CLOSE) {
                completedPlans.add(plan);
            } else if (item.getStatus() == ImprovementPlan.TRIALCLOSE || item.getStatus() == ImprovementPlan.TEMP_TRIALCLOSE) {
                trialClosedPlans.add(plan);
            } else {
                runningPlans.add(plan);
            }
        });
        List<Problem> recommends = loadRecommendations(loginUser.getId(), runningPlans, trialClosedPlans, completedPlans);

        PlanListDto planListDto = new PlanListDto();
        planListDto.setOpenNavigator(loginUser.getOpenNavigator());
        planListDto.setRunningPlans(runningPlans);
        planListDto.setCompletedPlans(completedPlans);
        planListDto.setTrialClosedPlans(trialClosedPlans);
        planListDto.setRecommendations(recommends);
        runningPlans.sort(Comparator.comparing(PlanDto::getStartDate));
        completedPlans.sort(this::sortPlans);
        trialClosedPlans.sort(this::sortPlans);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("计划列表")
                .action("查询计划列表");
        operationLogService.log(operationLog);
        return WebUtils.result(planListDto);
    }

    // 查询推荐的小课
    private List<Problem> loadRecommendations(Integer porfileId, List<PlanDto> runningPlans,
                                              List<PlanDto> trialClosePlans, List<PlanDto> completedPlans) {
        // 最后要返回的
        List<Problem> problems = Lists.newArrayList();
        // 用户已经有的小课
        List<Integer> userProblems = Lists.newArrayList();
        List<Integer> runningProblemId = runningPlans.stream().map(PlanDto::getProblemId).collect(Collectors.toList());
        runningPlans.forEach(item -> userProblems.add(item.getProblemId()));
        trialClosePlans.forEach(item -> userProblems.add(item.getProblemId()));
        completedPlans.forEach(item -> userProblems.add(item.getProblemId()));
        List<Integer> problemIds;

        // 根据有用性评分排列小课
        List<Integer> usefulProblems = problemService.loadProblems().stream()
                .sorted((left, right) -> right.getUsefulScore() > left.getUsefulScore() ? 1 : -1)
                .map(Problem::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(runningPlans)) {
            // 没有进行中的练习,根据实用度排序
            problemIds = usefulProblems;
        } else {
            // 有进行中的,有用性评分的小课排在后面
            List<Integer> collect = usefulProblems.stream()
                    .filter(item -> !runningProblemId.contains(item)).collect(Collectors.toList());
            runningProblemId.addAll(collect);
            problemIds = runningProblemId.stream().distinct().collect(Collectors.toList());
        }
        // 对这些小课设定分数，用于排序
        Map<Integer, Integer> problemScores = Maps.newHashMap();
        for (int i = 0; i < problemIds.size(); i++) {
            int score = problemIds.size() - i;
            problemScores.putIfAbsent(problemIds.get(i), score);
        }
        // 获取所有推荐，对这些推荐排序
        List<Recommendation> recommendationLists = reportService.loadAllRecommendation().stream().sorted((left, right) -> {
            Integer rightScore = problemScores.get(right.getProblemId());
            Integer leftScore = problemScores.get(left.getProblemId());
            if (leftScore == null) {
                return 1;
            } else if (rightScore == null) {
                return -1;
            } else {
                return rightScore - leftScore;
            }
        }).collect(Collectors.toList());
        boolean inWhiteList = whiteListService.isInWhiteList(WhiteList.TRIAL, porfileId);
        for (Recommendation recommendation : recommendationLists) {
            // 开始过滤,这个推荐里的小课
            List<Problem> recommendProblems = recommendation.getRecommendProblems();
            for (Problem problem : recommendProblems) {
                //非天使用户去除试用版小课
                if (!inWhiteList) {
                    if (problem.getTrial()) {
                        continue;
                    }
                }
                if (!userProblems.contains(problem.getId())) {
                    // 用户没有做过 ignore
                    if (!problems.stream().map(Problem::getId).collect(Collectors.toList()).contains(problem.getId())) {
                        // 没有添加进去
                        problems.add(problem.simple());
                    }
                    if (problems.size() >= ProblemService.MAX_RECOMMENDATION_SIZE) {
                        return problems;
                    }
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
    public ResponseEntity<Map<String, Object>> loadChapterAccess(LoginUser loginUser, @PathVariable Integer problemId,
                                                                 @PathVariable Integer practicePlanId) {
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
    public ResponseEntity<Map<String, Object>> loadChapterCard(LoginUser loginUser, @PathVariable Integer problemId,
                                                               @PathVariable Integer practicePlanId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId()).module("小课学习").action("打开小课学习")
                .function("加载章节卡片").memo(loginUser.getOpenId());
        operationLogService.log(operationLog);
        String chapterCardData = planService.loadChapterCard(loginUser.getId(), problemId, practicePlanId);
        if (chapterCardData != null) {
            return WebUtils.result(chapterCardData);
        } else {
            return WebUtils.error("该卡片正在制作中，请期待~");
        }
    }

}
