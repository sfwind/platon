package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.MonthlyCampOrderDao;
import com.iquanwai.platon.biz.dao.common.QuanwaiEmployeeDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.CardManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.Section;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.exception.CreateCourseException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.MonthlyCampOrder;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/4.
 */
@Service
public class PlanServiceImpl implements PlanService {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private UserProblemScheduleDao userProblemScheduleDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProblemScoreDao problemScoreDao;
    @Autowired
    private EssenceCardDao essenceCardDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private CardManager cardManager;
    @Autowired
    private ProblemScheduleManager problemScheduleManager;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private MonthlyCampScheduleDao monthlyCampScheduleDao;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private QuanwaiEmployeeDao quanwaiEmployeeDao;
    @Autowired
    private BusinessSchoolConfigDao businessSchoolConfigDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 精英会员年费版最大选课数
    private static final int MAX_ELITE_PROBLEM_LIMIT = 36;
    // 精英会员半年版最大选课数
    private static final int MAX_HALF_ELITE_PROBLEM_LIMIT = 18;
    // 普通人最大进行课程数
    private static final int MAX_NORMAL_RUNNING_PROBLEM_NUMBER = 3;

    @Override
    public void buildPlanDetail(ImprovementPlan improvementPlan) {
        //写入字段
        // 关闭时间，1.已关闭 显示已关闭， 2.未关闭（学习中／已完成）-会员-显示关闭时间 3.未关闭-非会员-不显示
        calcDeadLine(improvementPlan);
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        List<Chapter> chapters = problemScheduleManager.loadRoadMap(improvementPlan.getId());
        problem.setChapterList(chapters);
        improvementPlan.setProblem(problem);
        // 当前 Problem 是否为限免课程
        Integer freeLimitProblemId = ConfigUtils.getTrialProblemId();
        if (freeLimitProblemId != null && freeLimitProblemId == problem.getId()) {
            improvementPlan.setFree(true);
        } else {
            improvementPlan.setFree(false);
        }
        improvementPlan.setHasProblemScore(problemScoreDao.userProblemScoreCount(improvementPlan.getProfileId(),
                improvementPlan.getProblemId()) > 0);

        if (improvementPlan.getStatus() == ImprovementPlan.RUNNING) {
            // 计划正在进行中,暂时不能显示学习报告，需要完成必做
            improvementPlan.setReportStatus(-2);
        } else if (improvementPlan.getStatus() == ImprovementPlan.COMPLETE) {
            // 计划已经完成，显示完成按钮
            improvementPlan.setReportStatus(1);
        } else {
            if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
                Pair<Boolean, Integer> check = this.checkCloseable(improvementPlan);
                if (check.getLeft()) {
                    // 完成必做，可以查看
                    improvementPlan.setReportStatus(3);
                } else {
                    // 未完成必做，不能查看
                    improvementPlan.setReportStatus(-1);
                }
            }
        }

        //组装小节数据
        buildSections(improvementPlan);
    }

    private void calcDeadLine(ImprovementPlan improvementPlan) {
        //写入字段
        // 关闭时间，1.已关闭 显示已关闭， 2.未关闭（学习中／已完成）-会员-显示关闭时间 3.未关闭-非会员-不显示
        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
            // 关闭 ， 试用到期，暂时设置试用到期
            // 已关闭
            improvementPlan.setDeadline(0);
        } else {
            // 未关闭的都显示
            // 计算关闭时间
            // CloseDate设置为25号的，在26号0点会关闭，所以在25号查看的时候，是一天
            int deadLine = DateUtils.interval(DateUtils.startDay(new Date()), improvementPlan.getCloseDate());

            improvementPlan.setDeadline(deadLine);
        }
    }

    private void buildSections(ImprovementPlan improvementPlan) {
        List<Chapter> chapters = improvementPlan.getProblem().getChapterList();
        //获取所有的练习
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        Map<Integer, List<Practice>> practiceMap = Maps.newHashMap();
        practicePlans.forEach(practicePlan -> {
            List<Practice> practice = practiceMap.getOrDefault(practicePlan.getSeries(), Lists.newArrayList());
            practice.add(buildPractice(practicePlan));
            practiceMap.put(practicePlan.getSeries(), practice);
        });
        //组装小节
        List<Section> sections = Lists.newArrayList();
        chapters.forEach(chapter -> chapter.getSections().forEach(section -> {
            List<Practice> newPractices = Lists.newArrayList();
            List<Practice> practices = practiceMap.get(section.getSeries());
            practices = practices.stream().sorted(Comparator.comparing(Practice::getSequence)).collect(Collectors.toList());
            //添加小课介绍
            if (section.getSeries() == 1) {
                PracticePlan practicePlan = practicePlanDao.loadProblemIntroduction(improvementPlan.getId());
                if (practicePlan != null) {
                    newPractices.add(buildPractice(practicePlan));
                }
            }
            //添加小目标
            if (section.getSeries() == 1) {
                PracticePlan practicePlan = practicePlanDao.loadChallengePractice(improvementPlan.getId());
                if (practicePlan != null) {
                    newPractices.add(buildPractice(practicePlan));
                }
            }
            newPractices.addAll(practices);
            section.setPractices(newPractices);
            sections.add(section);
        }));
        improvementPlan.setSections(sections);
    }

    private boolean isDone(List<PracticePlan> runningPractices) {
        if (CollectionUtils.isNotEmpty(runningPractices)) {
            for (PracticePlan practicePlan : runningPractices) {
                //巩固练习或理解练习未完成时,返回false
                if ((practicePlan.getType() == PracticePlan.WARM_UP ||
                        practicePlan.getType() == PracticePlan.WARM_UP_REVIEW ||
                        practicePlan.getType() == PracticePlan.KNOWLEDGE ||
                        practicePlan.getType() == PracticePlan.KNOWLEDGE_REVIEW) &&
                        practicePlan.getStatus() == PracticePlan.STATUS.UNCOMPLETED) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<UserProblemSchedule> getChapterList(ImprovementPlan plan) {
        Assert.notNull(plan, "训练计划不能为空");
        List<UserProblemSchedule> problemSchedules = userProblemScheduleDao.loadUserProblemSchedule(plan.getId());
        problemSchedules.sort((o1, o2) -> {
            if (!o1.getChapter().equals(o2.getChapter())) {
                return o1.getChapter() - o2.getChapter();
            }
            return o1.getSection() - o2.getSection();
        });
        problemSchedules.forEach(item -> {
            Integer knowledgeId = item.getKnowledgeId();

            Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
            if (knowledge != null) {
                item.setChapterStr(knowledge.getStep());
                item.setSectionStr(knowledge.getKnowledge());
            } else {
                logger.error("缺少知识点,{}", knowledgeId);
                item.setChapterStr("缺少知识点");
                item.setSectionStr("缺少知识点");
            }
        });
        return problemSchedules;
    }

    //映射
    private Practice buildPractice(PracticePlan practicePlan) {
        Assert.notNull(practicePlan, "练习计划不能为空");
        Practice practice = new Practice();
        //NOTE:不能用modelmapper
        practice.setStatus(practicePlan.getStatus());
        practice.setUnlocked(practicePlan.getUnlocked());
        practice.setSeries(practicePlan.getSeries());
        practice.setPracticePlanId(practicePlan.getId());
        practice.setSequence(practicePlan.getSequence());
        practice.setPlanId(practicePlan.getPlanId());
        String[] practiceArr = practicePlan.getPracticeId().split(",");
        //设置选做标签,巩固练习和知识理解是必做,其他为选做
        if (isOptional(practicePlan.getType())) {
            practice.setOptional(true);
        } else {
            practice.setOptional(false);
        }
        List<Integer> practiceIdList = Lists.newArrayList();
        for (String practiceId : practiceArr) {
            practiceIdList.add(Integer.parseInt(practiceId));
        }
        practice.setPracticeIdList(practiceIdList);
        practice.setType(practicePlan.getType());
        return practice;
    }

    private boolean isOptional(Integer type) {
        return type == PracticePlan.APPLICATION_BASE || type == PracticePlan.APPLICATION_UPGRADED;
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        //小目标的knowledgeId=null
        if (knowledgeId == null) {
            Knowledge knowledge = new Knowledge();
            //文案写死
            knowledge.setKnowledge("让你的训练更有效");
            return knowledge;
        }
        Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
        WarmupPractice warmupPractice = warmupPracticeDao.loadExample(knowledgeId);
        if (warmupPractice != null) {
            warmupPractice = cacheService.getWarmupPractice(warmupPractice.getId());
            knowledge.setExample(warmupPractice);
        }
        return knowledge;
    }

    @Override
    public List<ImprovementPlan> getRunningPlan(Integer profileId) {
        return improvementPlanDao.loadRunningPlan(profileId);
    }

    @Override
    public Boolean checkChooseNewProblem(List<ImprovementPlan> allPlans, Integer profileId, Integer problemId) throws CreateCourseException {
        List<ImprovementPlan> plans = allPlans.stream()
                .filter(item -> item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE)
                .collect(Collectors.toList());

        // 是精英会员用户才会有选课上限
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        //员工没有选课限制
        if (quanwaiEmployeeDao.loadEmployee(profileId) != null) {
            return true;
        }

        if (riseMember == null) {
            throw new CreateCourseException("您暂时没有开课权限哦");
        }
        if (new DateTime().isBefore(riseMember.getOpenDate().getTime())) {
            throw new CreateCourseException("您在 " + DateUtils.parseDateToFormat5(riseMember.getOpenDate()) + " 才能开课哦");
        }

        Integer memberTypeId = riseMember.getMemberTypeId();
        //商学院用户
        if (memberTypeId == RiseMember.ELITE || memberTypeId == RiseMember.HALF_ELITE) {
            CourseSchedule courseSchedule = courseScheduleDao.loadSingleCourseSchedule(profileId, problemId);
            if (courseSchedule == null) {
                throw new CreateCourseException("请先去学习计划中勾选该课程");
            }
            if (plans.size() >= MAX_NORMAL_RUNNING_PROBLEM_NUMBER) {
                //当月主修课可以强开
                if (!(courseSchedule.getMonth().equals(ConfigUtils.getLearningMonth())
                        && courseSchedule.getType() == CourseScheduleDefault.Type.MAJOR)) {
                    // 会员已经有三门再学
                    throw new CreateCourseException("为了更专注的学习，同时最多进行" + MAX_NORMAL_RUNNING_PROBLEM_NUMBER
                            + "门课程。先完成进行中的一门，再选新课哦");
                }
            }

            BusinessSchoolConfig businessSchoolConfig = businessSchoolConfigDao.loadByYearAndMonth(ConfigUtils.getLearningYear(), ConfigUtils.getLearningMonth());
            Date openDate = businessSchoolConfig.getOpenDate();
            int year = DateUtils.getYear(openDate);
            int month = DateUtils.getMonth(openDate);
            if (year == courseSchedule.getYear() && month == courseSchedule.getMonth() && courseSchedule.getType() == CourseScheduleDefault.Type.MAJOR) {
                if (new Date().before(openDate)) {
                    // 未到开营日的主修课不能提前选择
                    throw new CreateCourseException(courseSchedule.getMonth() + "月主修课将于" + DateUtils.getDay(openDate) + "号开放选课，请等待当天开课仪式通知吧!");
                }
            }

            switch (memberTypeId) {
                case RiseMember.ELITE:
                    //商学院用户，限制36门
                    Date startTime1 = riseMember.getAddTime(); // 会员开始时间
                    if (startTime1.compareTo(ConfigUtils.getRiseMemberSplitDate()) > 0) {
                        List<ImprovementPlan> startPlans1 = improvementPlanDao.loadRiseMemberPlans(profileId, startTime1);
                        Long countLong1 = startPlans1.stream().filter(plan -> !plan.getProblemId().equals(ConfigUtils.getTrialProblemId())).count();
                        if (countLong1.intValue() >= MAX_ELITE_PROBLEM_LIMIT) {
                            throw new CreateCourseException("亲爱的商学院会员，你的选课数量已达" + MAX_ELITE_PROBLEM_LIMIT +
                                    "门。");
                        }
                    }
                    break;
                case RiseMember.HALF_ELITE:
                    Date startTime2 = riseMember.getAddTime(); // 会员开始时间
                    //精英版半年，限制18门
                    if (startTime2.compareTo(ConfigUtils.getRiseMemberSplitDate()) > 0) {
                        List<ImprovementPlan> startPlans2 = improvementPlanDao.loadRiseMemberPlans(profileId, startTime2);
                        Long countLong2 = startPlans2.stream().filter(plan -> !plan.getProblemId().equals(ConfigUtils.getTrialProblemId())).count();
                        if (countLong2.intValue() >= MAX_HALF_ELITE_PROBLEM_LIMIT) {
                            throw new CreateCourseException("亲爱的商学院会员，你的选课数量已达" + MAX_HALF_ELITE_PROBLEM_LIMIT
                                    + "门。如需升级或续费，请联系班主任");
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            //非商学院用户
            if (plans.size() >= MAX_NORMAL_RUNNING_PROBLEM_NUMBER) {
                throw new CreateCourseException("为了更专注的学习，同时最多进行" + MAX_NORMAL_RUNNING_PROBLEM_NUMBER
                        + "门课程。先完成进行中的一门，再选新课哦");
            }
        }

        Problem problem = cacheService.getProblem(problemId);

        if (!problem.getPublish()) {
            //TODO:专业版都过期后逻辑删除
            if(riseMember.getMemberTypeId() == RiseMember.ANNUAL || riseMember.getMemberTypeId() == RiseMember.HALF){
                if(problem.getId() == 11 || problem.getId() == 13){
                    //专业版可以学习problemId = 11, 13
                }else{
                    throw new CreateCourseException("该课程还在开发中，敬请期待");
                }
            }else{
                throw new CreateCourseException("该课程还在开发中，敬请期待");
            }
        }

        // 之前是否学过这个课程，避免重复生成计划
        ImprovementPlan oldPlan = allPlans.stream().filter(plan -> plan.getProblemId().equals(problemId)).findFirst().orElse(null);
        if (oldPlan != null) {
            throw new CreateCourseException("你已经选过该门课程了，你可以在\"我的\"菜单里找到以前的学习记录哦");
        }

        // 这里生成课程训练计划，另外在检查一下是否是会员或者购买了这个课程
        Boolean isRiseMember = accountService.isRiseMember(profileId);
        Integer trialProblemId = ConfigUtils.getTrialProblemId();
        if (!isRiseMember && !problemId.equals(trialProblemId)) {
            // 既没有购买过这个课程，又不是rise会员,也不是限免课程
            throw new CreateCourseException("您不是商学院会员，需要先购买会员哦");
        }
        return true;
    }

    @Override
    public Pair<Boolean, String> checkChooseCampProblem(Integer profileId, Integer problemId) {
        Integer learningMonth = ConfigUtils.getLearningMonth();

        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);
        List<Integer> problemIds = courseScheduleDefaults.stream()
                .filter(scheduleDefault -> learningMonth.equals(scheduleDefault.getMonth()))
                .map(CourseScheduleDefault::getProblemId).collect(Collectors.toList());

        Boolean tag = true;
        String checkStr = null;

        if (!problemIds.contains(problemId)) {
            tag = false;
            checkStr = "报名专项课不是当前开发的专项课";
        } else {
            Profile profile = accountService.getProfile(profileId);
            if (profile.getRiseMember() != Constants.RISE_MEMBER.MEMBERSHIP) {
                tag = false;
                checkStr = "非会员用户不能在此开启专项课";
            }
        }
        return new MutablePair<>(tag, checkStr);
    }

    @Override
    public void unlockCampPlan(Integer profileId, Integer planId) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Assert.notNull(improvementPlan);

        if (profileId.equals(improvementPlan.getProfileId())
                && ImprovementPlan.CLOSE == improvementPlan.getStatus()
                && improvementPlan.getCompleteTime() == null) {
            generatePlanService.magicUnlockProblem(profileId, improvementPlan.getProblemId(), null);
        }
    }

    @Override
    public List<ImprovementPlan> getPlans(Integer profileId) {
        return improvementPlanDao.loadAllPlans(profileId);
    }

    @Override
    public ImprovementPlan getPlan(Integer planId) {
        return improvementPlanDao.load(ImprovementPlan.class, planId);
    }

    @Override
    public void completePlan(Integer planId, Integer status) {
        //训练计划结束
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        logger.info("{} is terminated", planId);
        //更新训练计划状态
        improvementPlanDao.updateStatus(planId, status);
        //解锁所有应用练习
//        practicePlanDao.unlockApplicationPractice(planId);

        Integer percent = improvementPlanDao.defeatOthers(plan.getProblemId(), plan.getPoint());
        //发送完成通知
        if (status == ImprovementPlan.CLOSE) {
            // 设置关闭时间
            improvementPlanDao.updateCloseTime(planId);
            sendCloseMsg(plan, percent);
        }
    }

    private void sendCloseMsg(ImprovementPlan plan, Integer percent) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.courseCloseMsg());
        Profile profile = accountService.getProfile(plan.getProfileId());
        templateMessage.setTouser(profile.getOpenid());
        templateMessage.setUrl("https://www.iquanwai.com/survey/wjx?activity=14941027");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        Problem problem = cacheService.getProblem(plan.getProblemId());
        templateMessage.setData(data);

        data.put("first", new TemplateMessage.Keyword("太棒了！你已完成这个课程，并获得了" + plan.getPoint()
                + "积分，打败了" + percent + "%的圈柚\n"));

        data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n小tip：已完成的课程，就在“我的”->”我的课程“中\n\n" +
                "【反馈】看过课程的学习报告了吗？除了目前的学习情况，你还想了解自己的哪些学习数据呢？点击详情告诉我们吧↓↓↓"));

        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public boolean completeCheck(ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        if (!isDone(practicePlans)) {
            return false;
        }

        //完成训练计划
        completePlan(improvementPlan.getId(), ImprovementPlan.COMPLETE);
        //更新完成时间
        if (improvementPlan.getCompleteTime() == null) {
            improvementPlanDao.updateCompleteTime(improvementPlan.getId());
        }
        improvementPlan.setStatus(ImprovementPlan.COMPLETE);
        return true;
    }

    @Override
    public Integer checkPractice(Integer series, ImprovementPlan improvementPlan) {
        //当前第一节返回0
        if (series == 1) {
            return 0;
        }
        //获取前一节训练
        List<PracticePlan> prePracticePlans = practicePlanDao.loadBySeries(improvementPlan.getId(), series - 1);
        if (isDone(prePracticePlans)) {
            List<PracticePlan> practicePlans = practicePlanDao.loadBySeries(improvementPlan.getId(), series);
            for (PracticePlan practicePlan : practicePlans) {
                if (!practicePlan.getUnlocked()) {
                    //已过期返回-3
                    if (improvementPlan.getStatus() == ImprovementPlan.CLOSE) {
                        return -3;
                    }
                    if (!improvementPlan.getRiseMember()) {
                        //未付费返回-1
                        return -1;
                    }
                }
            }
            return 0;
        }
        //未完成返回-2
        return -2;
    }

    @Override
    public boolean hasProblemPlan(Integer profileId, Integer problemId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        long count = improvementPlans.stream().filter(item -> item.getProblemId().equals(problemId)).count();
        return count > 0;
    }

    @Override
    public ImprovementPlan getPlanByProblemId(Integer profileId, Integer problemId) {
        return improvementPlanDao.loadPlanByProblemId(profileId, problemId);
    }

    @Override
    public ImprovementPlan getDetailByProblemId(Integer profileId, Integer problemId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        if (improvementPlan != null) {
            improvementPlan.setProblem(cacheService.getProblem(improvementPlan.getProblemId()));
            CourseSchedule courseSchedule = courseScheduleDao.loadSingleCourseSchedule(profileId, problemId);
            if (courseSchedule != null) {
                improvementPlan.setMonth(courseSchedule.getMonth());
                improvementPlan.setTypeDesc(courseSchedule.getType() == CourseScheduleDefault.Type.MAJOR ? "主修" : "辅修");
            } else {
                Date startDate = improvementPlan.getStartDate();
                improvementPlan.setMonth(DateUtils.getMonth(startDate));
                improvementPlan.setTypeDesc("专项课");
            }
            //设置截止时间
            if (improvementPlan.getStatus() != ImprovementPlan.CLOSE) {
                improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate()));
            } else {
                improvementPlan.setDeadline(0);
            }
        }

        return improvementPlan;
    }

    @Override
    public void checkPlanComplete(Integer practicePlanId) {
        PracticePlan practice = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practice == null) {
            logger.error("{} is invalid", practicePlanId);
            return;
        }
        Integer planId = practice.getPlanId();
        Integer series = practice.getSeries();
        List<PracticePlan> seriesPracticePlans = practicePlanDao.loadBySeries(planId, series);
        //当节是否完成
        boolean complete = isDone(seriesPracticePlans);
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        // 更新进度
        if (complete && improvementPlan.getCompleteSeries() < practice.getSeries()) {
            improvementPlanDao.updateProgress(planId, practice.getSeries());
        }
        //所有练习是否完成
        List<PracticePlan> allPracticePlans = practicePlanDao.loadPracticePlan(planId);
        complete = isDone(allPracticePlans);
        if (complete && improvementPlan.getStatus() == ImprovementPlan.RUNNING) {
            // 过期了不让改
            improvementPlanDao.updateCompleteTime(planId);
            improvementPlanDao.updateStatus(planId, ImprovementPlan.COMPLETE);
        }
    }

    @Override
    public Pair<Boolean, Integer> checkCloseable(ImprovementPlan plan) {
        Integer planId = plan.getId();
        List<PracticePlan> allPracticePlans = practicePlanDao.loadPracticePlan(planId);
        Boolean complete = isDone(allPracticePlans);

        return new MutablePair<>(complete, 0);
    }

    @Override
    public void markPlan(Integer series, Integer planId) {
        improvementPlanDao.updateCurrentSeries(planId, series);
    }

    @Override
    public List<ImprovementPlan> loadUserPlans(Integer profileId) {
        return improvementPlanDao.loadUserPlans(profileId);
    }

    @Override
    public List<ImprovementPlan> getPlanList(Integer profileId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        improvementPlans.forEach(plan -> {
            Problem problem = cacheService.getProblem(plan.getProblemId());
            plan.setProblem(problem);
            plan.setProblemId(problem.getId());
            calcDeadLine(plan);
        });
        return improvementPlans;
    }

    @Override
    public List<ImprovementPlan> getCurrentCampPlanList(Integer profileId) {
        List<ImprovementPlan> plans = Lists.newArrayList();

        Integer learningMonth = ConfigUtils.getLearningMonth();
        if (learningMonth == null) {
            return plans;
        }

        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);
        List<Integer> problemIds = courseScheduleDefaults.stream()
                .filter(scheduleDefault -> learningMonth.equals(scheduleDefault.getMonth()))
                .map(CourseScheduleDefault::getProblemId).collect(Collectors.toList());

        for (Integer problemId : problemIds) {
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
            if (improvementPlan != null) {
                improvementPlan.setProblemId(problemId);
                improvementPlan.setProblem(cacheService.getProblem(problemId));
                calcDeadLine(improvementPlan);
                plans.add(improvementPlan);
            } else {
                improvementPlan = new ImprovementPlan();
                improvementPlan.setProblemId(problemId);
                improvementPlan.setProblem(cacheService.getProblem(problemId));
                plans.add(improvementPlan);
            }
        }
        return plans;
    }

    @Override
    public Boolean loadChapterCardAccess(Integer profileId, Integer problemId, Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return false;
        }
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
        List<Chapter> chapters = problemScheduleManager.loadRoadMap(improvementPlan.getId());
        Integer completeSeries = improvementPlan.getCompleteSeries();
        // 获取当前完成的巩固练习所在顺序
        Integer currentSeries = practicePlan.getSeries();
        if (!currentSeries.equals(completeSeries)) {
            return false;
        }
        Boolean isLearningSuccess = false;
        Integer targetChapterId = 0;
        for (Chapter chapter : chapters) {
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                // 用户当前学习的章节号对应到具体的 section
                if (section.getSeries().equals(currentSeries)) {
                    // 当一章中所有的小节完成，或者该小节是综合练习时，则完成
                    Long lgSeriesCount = sections.stream().filter(item -> item.getSeries() > currentSeries).count();
                    isLearningSuccess = lgSeriesCount.intValue() <= 0;
                    targetChapterId = chapter.getChapter();
                    break;
                }
            }
        }

        EssenceCard essenceCard = essenceCardDao.loadEssenceCard(problemId, targetChapterId);
        return isLearningSuccess && essenceCard != null;
    }

    @Override
    public String loadChapterCard(Integer profileId, Integer problemId, Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return null;
        }
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());

        List<Chapter> chapters = problemScheduleManager.loadRoadMap(improvementPlan.getId());
        Integer completeSeries = improvementPlan.getCompleteSeries();
        // 获取当前完成的巩固练习所在顺序
        Integer currentSeries = practicePlan.getSeries();
        if (!currentSeries.equals(completeSeries)) {
            return null;
        }
        Boolean isLearningSuccess = false;
        Integer targetChapterId = 0;
        for (Chapter chapter : chapters) {
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                // 用户当前学习的章节号对应到具体的 section
                if (section.getSeries().equals(currentSeries)) {
                    // 当一章中所有的小节完成，或者该小节是综合练习时，则完成
                    Long lgSeriesCount = sections.stream().filter(item -> item.getSeries() > currentSeries).count();
                    isLearningSuccess = lgSeriesCount.intValue() <= 0;
                    targetChapterId = chapter.getChapter();
                    break;
                }
            }
        }
        // 当前章节 和 完成章节相等
        if (isLearningSuccess) {
            return cardManager.loadEssenceCardImg(profileId, problemId, targetChapterId, improvementPlan.getId());
        } else {
            return null;
        }
    }

    @Override
    public Integer problemIntroductionButtonStatus(Integer profileId, Integer problemId, ImprovementPlan plan, Boolean autoOpen) {
        // 不显示按钮
        int buttonStatus = -1;

        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (plan == null) {
            // 2 - 去上课 1 - 加入商学院 5 - 选择该课程
            if (riseMember == null) {
                buttonStatus = 1;
            } else {
                if (riseMember.getMemberTypeId() == RiseMember.HALF_ELITE || riseMember.getMemberTypeId() == RiseMember.ELITE) {
                    buttonStatus = 2;
                } else if (riseMember.getMemberTypeId() == RiseMember.HALF || riseMember.getMemberTypeId() == RiseMember.ANNUAL) {
                    buttonStatus = 5;
                } else {
                    buttonStatus = 1;
                }
            }
        } else if (plan.getStatus().equals(ImprovementPlan.RUNNING)) {
            // 课程已开始，去上课
            buttonStatus = 3;
        } else if (plan.getStatus().equals(ImprovementPlan.COMPLETE) || plan.getStatus().equals(ImprovementPlan.CLOSE)) {
            // 课程已开始，去复习
            buttonStatus = 4;
        }
        return buttonStatus;
    }

    @Override
    public void magicOpenCampOrder(String orderId) {
        MonthlyCampOrder monthlyCampOrder = monthlyCampOrderDao.loadTrainOrder(orderId);
        Integer profileId = monthlyCampOrder.getProfileId();

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        Integer sellingYear = monthlyCampConfig.getSellingYear();
        Integer sellingMonth = monthlyCampConfig.getSellingMonth();

        List<MonthlyCampSchedule> monthlyCampSchedules = monthlyCampScheduleDao.loadAll();
        List<Integer> problemIds = monthlyCampSchedules.stream()
                .filter(monthlyCampSchedule -> monthlyCampSchedule.getYear().equals(sellingYear) && monthlyCampSchedule.getMonth().equals(sellingMonth))
                .map(MonthlyCampSchedule::getProblemId).collect(Collectors.toList());

        Date closeDate;
        if (new DateTime(monthlyCampConfig.getOpenDate()).isAfterNow()) {
            // 开营时间在现在之后
            closeDate = DateUtils.afterDays(monthlyCampConfig.getOpenDate(), 30);
        } else {
            // 开营时间在现在之前
            closeDate = DateUtils.afterDays(new Date(), 30);
        }

        for (Integer problemId : problemIds) {

            Integer planId = generatePlanService.magicOpenProblem(profileId, problemId, monthlyCampConfig.getOpenDate(), closeDate, false);

            if (planId != null) {
                // 如果 Profile 中不存在求点评此数，则将求点评此数置为 1
                Profile profile = accountService.getProfile(profileId);
                if (profile.getRequestCommentCount() == 0) {
                    improvementPlanDao.updateRequestComment(planId, 1);
                }
            }
        }
    }

    @Override
    public void unlockNeverUnlockPlans(Integer profileId) {
        List<ImprovementPlan> plans = improvementPlanDao.loadAllPlans(profileId);
        List<Integer> planIds = plans.stream().map(ImprovementPlan::getId).collect(Collectors.toList());
        List<PracticePlan> unlockPractices = practicePlanDao.loadNeverUnlockPlan(planIds);
        List<Integer> unlockPlanIds = unlockPractices.stream().map(PracticePlan::getPlanId).distinct().collect(Collectors.toList());


        plans.stream().filter(item -> unlockPlanIds.contains(item.getId())).forEach(item -> {
            DateTime otherCloseDate = new DateTime().plusDays(30);
            Date closeDate;
            if (otherCloseDate.isAfter(item.getCloseDate().getTime())) {
                closeDate = otherCloseDate.toDate();
            } else {
                closeDate = item.getCloseDate();
            }

            // 解锁
            generatePlanService.magicUnlockProblem(profileId, item.getProblemId(), closeDate);
        });
    }

    @Override
    public List<PlanSeriesStatus> loadPlanSeries(Integer practicePlanId) {
        List<PlanSeriesStatus> planSeriesStatuses = Lists.newArrayList();

        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        Integer planId = practicePlan.getPlanId();
        Integer series = practicePlan.getSeries();

        List<PracticePlan> practicePlans = practicePlanDao.loadBySeries(planId, series);
        for (PracticePlan plan : practicePlans) {
            PlanSeriesStatus seriesStatus = new PlanSeriesStatus();
            seriesStatus.setPracticePlanId(plan.getId());
            seriesStatus.setPlanId(planId);
            seriesStatus.setPracticeId(plan.getPracticeId());
            seriesStatus.setSeries(series);
            seriesStatus.setSequence(plan.getSequence());
            seriesStatus.setType(plan.getType());
            seriesStatus.setUnlock(plan.getUnlocked());
            seriesStatus.setComplete(plan.getStatus() == 1);
            planSeriesStatuses.add(seriesStatus);
        }

        return planSeriesStatuses;
    }

    @Override
    public String loadPlanSeriesTitle(Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return null;
        }

        Integer planId = practicePlan.getPlanId();
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Integer problemId = improvementPlan.getProblemId();
        Integer series = practicePlan.getSeries();
        List<PracticePlan> practicePlans = practicePlanDao.loadBySeries(planId, series);
        PracticePlan planWithKnowledgeId = practicePlans.stream().filter(item -> item.getKnowledgeId() != null)
                .findAny().orElse(null);

        if (planWithKnowledgeId == null) {
            return null;
        }

        int knowledgeId = planWithKnowledgeId.getKnowledgeId();
        ProblemSchedule schedule = problemScheduleDao.loadByKnowledgeId(knowledgeId, problemId);
        StringBuilder titleBuilder = new StringBuilder();
        if (schedule != null) {
            titleBuilder.append(schedule.getChapter())
                    .append(".")
                    .append(schedule.getSection())
                    .append(" ");
            String knowledgeStr = cacheService.getKnowledge(knowledgeId).getKnowledge();
            titleBuilder.append(knowledgeStr);
        }
        return titleBuilder.toString();
    }

    @Override
    public List<CourseSchedule> loadAllCourseSchedules(Integer profileId) {
        return courseScheduleDao.getAllScheduleByProfileId(profileId);
    }

}
