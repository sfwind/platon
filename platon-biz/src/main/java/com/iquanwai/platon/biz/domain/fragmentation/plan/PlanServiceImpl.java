package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ProblemScoreDao problemScoreDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EssenceCardDao essenceCardDao;
    @Autowired
    private RiseCourseDao riseCourseDao;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private QRCodeService qrCodeService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void buildPlanDetail(ImprovementPlan improvementPlan) {
        //解锁检查
        Integer series = improvementPlan.getCompleteSeries();
        Integer planId = improvementPlan.getId();
//        if ((!improvementPlan.getRiseMember() && series >= 3)) {
//            improvementPlan.setLockedStatus(-2);
//        } else
//
        //已过期不能解锁
        if (improvementPlan.getStatus() == ImprovementPlan.CLOSE || improvementPlan.getStatus() == ImprovementPlan.TRIALCLOSE) {
            improvementPlan.setLockedStatus(-3);
        } else {
            //解锁下一组
            List<PracticePlan> nextSeriesPracticePlans = practicePlanDao.loadBySeries(planId,
                    series + 1);
            if (CollectionUtils.isNotEmpty(nextSeriesPracticePlans)) {
                unlock(nextSeriesPracticePlans, planId);
            }
        }
        //写入字段
        // 关闭时间，1.已关闭 显示已关闭， 2.未关闭（学习中／已完成）-会员-显示关闭时间 3.未关闭-非会员-不显示
        calcDeadLine(improvementPlan);
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
        // 当前 Problem 是否为限免小课
        Integer freeLimitProblemId = ConfigUtils.getTrialProblemId();
        if (freeLimitProblemId != null && freeLimitProblemId == problem.getId()) {
            improvementPlan.setFree(true);
        } else {
            improvementPlan.setFree(false);
        }
        improvementPlan.setHasProblemScore(
                problemScoreDao.userProblemScoreCount(improvementPlan.getProfileId(), improvementPlan.getProblemId()) > 0);
        // 所有的综合练习是否完成
        List<PracticePlan> applications = practicePlanDao.loadApplicationPracticeByPlanId(improvementPlan.getId());
        // 拿到未完成的综合训练
        List<PracticePlan> unDoneApplications = applications.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.APPLICATION_REVIEW && practicePlan.getStatus() == 0)
                .collect(Collectors.toList());
        // 未完成未空则代表全部完成
        improvementPlan.setDoneAllIntegrated(CollectionUtils.isEmpty(unDoneApplications));

        if (improvementPlan.getStatus() == ImprovementPlan.RUNNING) {
            // 计划正在进行中,暂时不能显示学习报告，需要完成必做
            improvementPlan.setReportStatus(-2);
        } else if (improvementPlan.getStatus() == ImprovementPlan.COMPLETE) {

            Pair<Boolean, Integer> check = checkCloseable(improvementPlan);
            if (check.getRight() != 0) {
                // 未满足最小天数
                improvementPlan.setReportStatus(2);
                improvementPlan.setMustStudyDays(check.getRight());
            } else {
                // 计划已经完成，显示完成按钮
                improvementPlan.setReportStatus(1);
            }
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
            // 已关闭
            improvementPlan.setDeadline(0);
        } else {
            // 未关闭
            if (improvementPlan.getRiseMember()) {
                // 会员 显示关闭时间
                improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate()) + 1);
            } else {
                // 非会员，不显示
                improvementPlan.setDeadline(-1);
            }
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
            List<Practice> practices = practiceMap.get(section.getSeries());
            //添加小目标
            if (section.getSeries() == 1) {
                practices.add(buildPractice(practicePlanDao.loadChallengePractice(improvementPlan.getId())));
            }
            section.setPractices(practices);
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
                        practicePlan.getType() == PracticePlan.KNOWLEDGE_REVIEW) && practicePlan.getStatus() == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<ProblemSchedule> getChapterList(ImprovementPlan plan) {
        Assert.notNull(plan, "训练计划不能为空");
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(plan.getProblemId());
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

    private void unlock(List<PracticePlan> runningPractice, Integer planId) {
        Assert.notNull(planId, "训练计划不能为空");
        Assert.notNull(runningPractice, "练习计划不能为空");
        //如果练习未解锁,则解锁练习
        runningPractice.stream().filter(practicePlan -> !practicePlan.getUnlocked()).forEach(practicePlan -> {
            practicePlan.setUnlocked(true);
            practicePlanDao.unlock(practicePlan.getId());
        });
        Integer progress = runningPractice.get(0).getSeries();
        improvementPlanDao.updateProgress(planId, progress - 1);
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
//        if(practice.getKnowledge()== null) {
//            Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId());
//            practice.setKnowledge(knowledge);
//        }
        return practice;
    }

    private boolean isOptional(Integer type) {
        return type == PracticePlan.CHALLENGE ||
                type == PracticePlan.APPLICATION || type == PracticePlan.APPLICATION_REVIEW;
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
    public Pair<Integer, String> checkPayCourse(Integer profileId, Integer problemId) {
        //
        ImprovementPlan plan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        if (plan == null) {
            // 没有学过这个小课，可以购买
            return new MutablePair<>(1, "");
        } else {
            // 学过这个小课，查看status == 4
            if (plan.getStatus() == ImprovementPlan.TRIALCLOSE) {
                // 试用到期，可以购买
                return new MutablePair<>(1, "");
            } else if (plan.getStatus() == ImprovementPlan.RUNNING || plan.getStatus() == ImprovementPlan.COMPLETE) {
                return new MutablePair<>(-1, "该小课可以正常学习,无需购买");
            } else {
                return new MutablePair<>(-2, "该小课无需购买");
            }

        }
    }

    @Override
    public Pair<Integer, String> checkChooseNewProblem(List<ImprovementPlan> plans) {

//        if (riseMember) {
        if (plans.size() >= 2) {
            // 会员已经有两门再学
            return new MutablePair<>(-1, "为了更专注的学习，同时最多进行两门小课。先完成进行中的一门，再选新课哦");
        }
//        }

//        else {
//            if (plans.size() >= 1) {
//                // 非会员已经有一门了，则不可再选
//                return new MutablePair<>(-2, "试用版是能试用一门小课哦");
//            }
//        }

        return new MutablePair<>(1, "");
    }

    @Override
    public ImprovementPlan getLatestPlan(Integer profileId) {
        return improvementPlanDao.getLastPlan(profileId);
    }

    @Override
    public List<ImprovementPlan> getPlans(Integer profileId) {
        return improvementPlanDao.loadAllPlans(profileId);
    }

    @Override
    public RiseCourseOrder getEntryRiseCourseOrder(Integer profileId, Integer problemId) {
        return riseCourseDao.loadEntryOrder(profileId, problemId);
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
        templateMessage.setTouser(plan.getOpenid());
        templateMessage.setUrl("https://www.iquanwai.com/survey/wjx?activity=14941027");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        Problem problem = cacheService.getProblem(plan.getProblemId());
        templateMessage.setData(data);

        data.put("first", new TemplateMessage.Keyword("太棒了！你已完成这个小课，并获得了" + plan.getPoint()
                + "积分，打败了" + percent + "%的圈柚\n"));

        data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n小tip：已完成的小课，就在“我的”->”我的小课“中\n\n" +
                "【反馈】看过小课的学习报告了吗？除了目前的学习情况，你还想了解自己的哪些学习数据呢？点击详情告诉我们吧↓↓↓"));

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

    public boolean hasProblemPlan(Integer profileId, Integer problemId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        long count = improvementPlans.stream().filter(item -> item.getProblemId().equals(problemId)).count();
        return count > 0;
    }

    @Override
    public String loadSubjectDesc(Integer problemId) {
        Problem load = cacheService.getProblem(problemId);
        return load != null ? load.getSubjectDesc() : "";
    }

    @Override
    public List<Chapter> loadRoadMap(Integer problemId) {
        Problem problem = cacheService.getProblem(problemId);

        return problem != null ? problem.getChapterList() : Lists.newArrayList();
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

        int minStudyDays = Double.valueOf(Math.ceil(plan.getTotalSeries() / 2.0D)).intValue();
        Date minDays = DateUtils.afterDays(plan.getStartDate(), minStudyDays);
        // 如果4.1号10点开始  +1 = 4.2号0点是最早时间，4.2白天就可以了
        if (new Date().before(minDays)) {
            return new MutablePair<>(complete, minStudyDays);
        } else {
            return new MutablePair<>(complete, 0);
        }
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
    public ImprovementPlan getPlanByChallengeId(Integer id, Integer profileId) {
        List<ImprovementPlan> plans = improvementPlanDao.loadAllPlans(profileId);
        for (ImprovementPlan plan : plans) {
            if (plan.getProblemId().equals(id)) {
                return plan;
            }
        }
        return null;
    }

    @Override
    public Boolean loadChapterCardAccess(Integer profileId, Integer problemId, Integer practicePlanId) {
        List<Chapter> chapters = cacheService.getProblem(problemId).getChapterList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return false;
        }
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
        Integer completeSeries = improvementPlan.getCompleteSeries();
        // 获取当前完成的巩固练习所在顺序
        Integer currentSeries = practicePlan.getSeries();
        if (!currentSeries.equals(completeSeries)) {
            return false;
        }
        Boolean isLearningSuccess = false;
        for (Chapter chapter : chapters) {
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                // 用户当前学习的章节号对应到具体的 section
                if (section.getSeries().equals(currentSeries)) {
                    Long lgSeriesCount = sections.stream().filter(item -> item.getSeries() > currentSeries).count();
                    isLearningSuccess = lgSeriesCount.intValue() <= 0;
                    break;
                }
            }
        }
        return isLearningSuccess;
    }


    @Override
    public String loadChapterCard(Integer profileId, Integer problemId, Integer practicePlanId) {
        List<Chapter> chapters = cacheService.getProblem(problemId).getChapterList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            return null;
        }
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
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
                    Long lgSeriesCount = sections.stream().filter(item -> item.getSeries() > currentSeries).count();
                    isLearningSuccess = lgSeriesCount.intValue() <= 0;
                    targetChapterId = chapter.getChapter();
                    break;
                }
            }
        }
        // 当前章节 和 完成章节相等
        if (isLearningSuccess) {
            return problemService.loadEssenceCardImg(profileId, problemId, targetChapterId);
        } else {
            return null;
        }
    }

}
