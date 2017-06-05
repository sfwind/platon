package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.platon.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScoreDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupSubmitDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.Optional;
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
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void buildPlanDetail(ImprovementPlan improvementPlan) {
        //解锁检查
        Integer series = improvementPlan.getCompleteSeries();
        Integer planId = improvementPlan.getId();
        //非会员只能解锁3章,已过期不能解锁
        if((!improvementPlan.getRiseMember() && series>=3)){
            improvementPlan.setLockedStatus(-2);
        }else if(improvementPlan.getStatus() == ImprovementPlan.CLOSE){
            improvementPlan.setLockedStatus(-3);
        }else{
            //解锁下一组
            List<PracticePlan> nextSeriesPracticePlans = practicePlanDao.loadBySeries(planId,
                    series+1);
            if(CollectionUtils.isNotEmpty(nextSeriesPracticePlans)){
                unlock(nextSeriesPracticePlans, planId);
            }
        }
        //写入字段
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
        improvementPlan.setHasProblemScore(
                problemScoreDao.userPorblemScoreCount(improvementPlan.getOpenid(), improvementPlan.getProblemId()) > 0);
        // 所有的综合练习是否完成
        List<PracticePlan> applications = practicePlanDao.loadApplicationPracticeByPlanId(improvementPlan.getId());
        // 拿到未完成的综合训练
        List<PracticePlan> unDoneApplications = applications.stream().filter(practicePlan -> practicePlan.getType()==PracticePlan.APPLICATION_REVIEW && practicePlan.getStatus() == 0)
                .collect(Collectors.toList());
        // 未完成未空则代表全部完成
        improvementPlan.setDoneAllIntegrated(CollectionUtils.isEmpty(unDoneApplications));

        //组装小节数据
        buildSections(improvementPlan);

    }

    private void buildSections(ImprovementPlan improvementPlan) {
        List<Chapter> chapters = improvementPlan.getProblem().getChapterList();
        //获取所有的练习
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        Map<Integer, List<Practice>> practiceMap = Maps.newHashMap();
        practicePlans.stream().forEach(practicePlan -> {
            List<Practice> practice = practiceMap.getOrDefault(practicePlan.getSeries(), Lists.newArrayList());
            practice.add(buildPractice(practicePlan));
            practiceMap.put(practicePlan.getSeries(), practice);
        });
        //组装小节
        List<Section> sections = Lists.newArrayList();
        chapters.stream().forEach(chapter -> {
            chapter.getSections().stream().forEach(section -> {
                List<Practice> practices = practiceMap.get(section.getSeries());
                //添加小目标
                if (section.getSeries() == 1) {
                    practices.add(buildPractice(practicePlanDao.loadChallengePractice(improvementPlan.getId())));
                }
                section.setPractices(practices);
                sections.add(section);
            });
        });
        improvementPlan.setSections(sections);
    }

    private boolean isDone(List<PracticePlan> runningPractices){
        if(CollectionUtils.isNotEmpty(runningPractices)) {
            for(PracticePlan practicePlan:runningPractices){
                //巩固练习或理解练习未完成时,返回false
                if((practicePlan.getType()==PracticePlan.WARM_UP ||
                        practicePlan.getType()==PracticePlan.WARM_UP_REVIEW ||
                        practicePlan.getType()==PracticePlan.KNOWLEDGE ||
                        practicePlan.getType()==PracticePlan.KNOWLEDGE_REVIEW) && practicePlan.getStatus()==0){
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
            if(!o1.getChapter().equals(o2.getChapter())){
                return o1.getChapter()-o2.getChapter();
            }
            return o1.getSection()-o2.getSection();
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
        if(isOptional(practicePlan.getType())){
            practice.setOptional(true);
        }else{
            practice.setOptional(false);
        }
        List<Integer> practiceIdList = Lists.newArrayList();
        for(String practiceId:practiceArr){
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
        return type==PracticePlan.CHALLENGE || type==PracticePlan.APPLICATION;
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId){
        //小目标的knowledgeId=null
        if(knowledgeId==null){
            Knowledge knowledge = new Knowledge();
            //文案写死
            knowledge.setKnowledge("让你的训练更有效");
            return knowledge;
        }
        Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
        WarmupPractice warmupPractice = warmupPracticeDao.loadExample(knowledgeId);
        if(warmupPractice!=null) {
            warmupPractice = cacheService.getWarmupPractice(warmupPractice.getId());
            knowledge.setExample(warmupPractice);
        }
        return knowledge;
    }

    @Override
    public ImprovementPlan getRunningPlan(Integer profileId) {
        return improvementPlanDao.loadRunningPlan(profileId);
    }

    @Override
    public ImprovementPlan getLatestPlan(Integer profileId) {
        return improvementPlanDao.getLastPlan(profileId);
    }

    @Override
    public List<ImprovementPlan> getPlans(Integer profileId){
        return improvementPlanDao.loadAllPlans(profileId);
    }

    @Override
    public ImprovementPlan getPlan(Integer planId) {
        return improvementPlanDao.load(ImprovementPlan.class, planId);
    }

    @Override
    public Integer completePlan(Integer planId, Integer status) {
        //训练计划结束
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        logger.info("{} is terminated", planId);
        //更新训练计划状态
        improvementPlanDao.updateStatus(planId, status);
        //解锁所有应用练习
//        practicePlanDao.unlockApplicationPractice(planId);

        Integer percent = improvementPlanDao.defeatOthers(plan.getProblemId(), plan.getPoint());
        //发送完成通知
        if(status == ImprovementPlan.CLOSE) {
            sendCloseMsg(plan, percent);
        }

        return percent;
    }

    private void sendCloseMsg(ImprovementPlan plan, Integer percent) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.courseCloseMsg());
        templateMessage.setTouser(plan.getOpenid());
        // templateMessage.setUrl("");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        Problem problem = cacheService.getProblem(plan.getProblemId());
        templateMessage.setData(data);

        data.put("first", new TemplateMessage.Keyword("太棒了！你已完成这个小课，并获得了" + plan.getPoint()
                + "积分，打败了" + percent + "%的Riser\n"));

        data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date())));
        // data.put("remark",new TemplateMessage.Keyword("\nP. S. 使用中有不爽的地方？我们已经想了几个优化的点子，点击进来看看，" +
        //        "是不是想到一起了→→→ （跳转调查链接）"));
        data.put("remark",new TemplateMessage.Keyword("\n微信和网页端的小课论坛和应用训练，仍可以继续补充和答题哦。有想法记得分享，和大家一起成长吧\n"));

        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public Pair<Boolean, Integer> completeCheck(ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        if (!isDone(practicePlans)) {
            return new ImmutablePair<>(false, -1);
        }

        //完成训练计划
        int percent = completePlan(improvementPlan.getId(), ImprovementPlan.COMPLETE);
        //更新完成时间
        if (improvementPlan.getCompleteTime() == null) {
            improvementPlanDao.updateCompleteTime(improvementPlan.getId());
        }
        improvementPlan.setStatus(ImprovementPlan.COMPLETE);
        return new ImmutablePair<>(true, percent);
    }

    private List<PracticePlan> pickPracticeBySeries(ImprovementPlan improvementPlan, Integer series) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        //如果节数<=0,直接返回空数据
        if(series<=0){
            return Lists.newArrayList();
        }
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> practicePlanList = practicePlanDao.loadBySeries(improvementPlan.getId(), series);
        runningPractice.addAll(practicePlanList);
        //第一节增加小目标,其余时间不显示小目标
        if(series==1) {
            runningPractice.add(practicePlanDao.loadChallengePractice(improvementPlan.getId()));
        }
        return runningPractice;
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
                    if(improvementPlan.getStatus()==ImprovementPlan.CLOSE){
                        return -3;
                    }
                    if(!improvementPlan.getRiseMember()){
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
        return load!=null?load.getSubjectDesc():"";
    }

    @Override
    public List<Chapter> loadRoadMap(Integer problemId) {
        Problem problem = cacheService.getProblem(problemId);

        return problem!=null?problem.getChapterList():Lists.newArrayList();
    }

    @Override
    public void checkPlanComplete(Integer practicePlanId) {
        PracticePlan practice = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if(practice==null){
            logger.error("{} is invalid", practicePlanId);
            return;
        }
        Integer planId = practice.getPlanId();
        Integer series = practice.getSeries();
        List<PracticePlan> seriesPracticePlans = practicePlanDao.loadBySeries(planId, series);
        //当节是否完成
        boolean complete = isDone(seriesPracticePlans);
        if(complete){
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            if(improvementPlan.getCompleteSeries()<practice.getSeries()){
                improvementPlanDao.updateProgress(planId, practice.getSeries());
            }
        }
        //所有练习是否完成
        List<PracticePlan> allPracticePlans = practicePlanDao.loadPracticePlan(planId);
        complete = isDone(allPracticePlans);
        if(complete){
            improvementPlanDao.updateCompleteTime(planId);
        }
    }

    @Override
    public Pair<Integer,Integer> checkCloseable(ImprovementPlan plan) {
        Integer planId = plan.getId();
        List<PracticePlan> allPracticePlans = practicePlanDao.loadPracticePlan(planId);
        Boolean complete = isDone(allPracticePlans);
        if (!complete) {
            return new MutablePair<>(-1, 0);
        }

        int minStudyDays = Double.valueOf(Math.ceil(plan.getTotalSeries() / 2.0D)).intValue();
        Date minDays = DateUtils.afterDays(plan.getStartDate(), minStudyDays);
        // 如果4.1号10点开始  +1 = 4.2号0点是最早时间，4.2白天就可以了
        if(new Date().before(minDays)){
            return new MutablePair<>(-2, minStudyDays);
        }

        return new MutablePair<>(1, 0);
    }

    @Override
    public void markPlan(Integer series, Integer planId) {
        improvementPlanDao.updateCurrentSeries(planId, series);
    }

    @Override
    public ImprovementReport loadUserImprovementReport(ImprovementPlan plan) {
        Problem problem = cacheService.getProblem(plan.getProblemId());
        ImprovementReport report = new ImprovementReport();
        // problem
        report.setProblem(problem.getProblem());
        // 用时
        Integer studyDays = DateUtils.interval(plan.getStartDate(), plan.getCompleteTime()) + 1;
        report.setStudyDays(studyDays);
        // 打败多少人
        Integer percent = improvementPlanDao.defeatOthers(plan.getProblemId(), plan.getPoint());
        report.setPercent(percent);
        // 总分
        report.setTotalScore(plan.getPoint());
        // 计算
        calculateReport(report, plan);
        // 点赞与被点赞
        Integer voteCount = homeworkVoteDao.voteCount(plan.getOpenid());
        Integer votedCount = homeworkVoteDao.votedCount(plan.getOpenid());
        report.setReceiveVoteCount(votedCount);
        report.setShareVoteCount(voteCount);
        return report;
    }

    private void calculateReport(ImprovementReport report,ImprovementPlan plan){
        // 获得所有练习
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(plan.getId());
        // 热身练习
        List<PracticePlan> warmPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.WARM_UP) || item.getType().equals(PracticePlan.WARM_UP_REVIEW))
                .collect(Collectors.toList());
        // 应用练习
        List<PracticePlan> applicationPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.APPLICATION))
                .collect(Collectors.toList());
        // 综合练习
        List<PracticePlan> integratedPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.APPLICATION_REVIEW))
                .collect(Collectors.toList());

        // 计算章节练习分数
        calculateWarmupScores(report, plan, warmPlanList);
        // 计算完成数量
        calculateCompleteCount(report, applicationPlanList, integratedPlanList);
        // 计算应用练习与综合练习分数
        calculateAppScores(report, applicationPlanList, integratedPlanList);
    }

    private void calculateAppScores(ImprovementReport report,List<PracticePlan> applicationPlanList, List<PracticePlan> integratedPlanList) {
        List<Integer> applicationIds = applicationPlanList.stream().map(item -> Integer.valueOf(item.getPracticeId())).collect(Collectors.toList());
        List<Integer> integratedIds = integratedPlanList.stream().map(item -> Integer.valueOf(item.getPracticeId())).collect(Collectors.toList());

        List<ApplicationPractice> applicationPractices = applicationPracticeDao.loadPracticeList(applicationIds);
        List<ApplicationPractice> integratedPractices = applicationPracticeDao.loadPracticeList(integratedIds);

        report.setApplicationTotalScore(0);
        report.setApplicationScore(0);
        report.setIntegratedTotalScore(0);
        report.setIntegratedScore(0);
        // 计算分数
        applicationPlanList.forEach(item->{
            // 已完成，计算分数
            Optional<ApplicationPractice> first = applicationPractices.stream().filter(app -> app.getId() == Integer.parseInt(item.getPracticeId())).findFirst();
            first.ifPresent(practice->{
                Integer point = PointRepoImpl.score.get(practice.getDifficulty());
                if (item.getStatus() == 1) {
                    report.setApplicationScore(report.getApplicationScore() + point);
                }
                report.setApplicationTotalScore(report.getApplicationTotalScore() + point);
            });
        });

        integratedPlanList.forEach(item->{
            // 已完成，计算分数
            Optional<ApplicationPractice> first = integratedPractices.stream().filter(app -> app.getId() == Integer.parseInt(item.getPracticeId())).findFirst();
            first.ifPresent(practice->{
                Integer point = PointRepoImpl.score.get(practice.getDifficulty());
                if (item.getStatus() == 1) {
                    report.setIntegratedScore(report.getIntegratedScore() + point);
                }
                report.setIntegratedTotalScore(report.getIntegratedTotalScore() + point);
            });
        });
    }

    private void calculateCompleteCount(ImprovementReport report, List<PracticePlan> applicationPlanList, List<PracticePlan> integratedPlanList) {
        // 数量计算
        Integer totalApplication = applicationPlanList.size();
        Integer integratedPlan = integratedPlanList.size();
        Long totalCompeleteApp = applicationPlanList.stream().filter(item -> item.getStatus() == 1).count();
        Long totalCompeleteIntegrated = integratedPlanList.stream().filter(item -> item.getStatus() == 1).count();
        report.setApplicationShouldCount(totalApplication);
        report.setIntegratedShouldCount(integratedPlan);
        report.setApplicationCompleteCount(totalCompeleteApp.intValue());
        report.setIntegratedCompleteCount(totalCompeleteIntegrated.intValue());
    }

    private void calculateWarmupScores(ImprovementReport report,ImprovementPlan plan,List<PracticePlan> warmPlanList){
        // 获得warmPlanList
        List<WarmupPractice> warmupPractices = initWarmupScores(warmPlanList);
        // 获得用户的提交记录
        List<Integer> questionLists = warmupPractices.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        List<WarmupSubmit> warmupSubmit = warmupSubmitDao.getWarmupSubmit(plan.getId(), questionLists);
        // 获得章
        List<Chapter> chapters = cacheService.loadRoadMap(plan.getProblemId());
        // 用户提交的小节
        Map<Integer, List<WarmupSubmit>> submitMap = Maps.newHashMap();
        // 总的小节题目
        Map<Integer,List<WarmupPractice>> totalMap = Maps.newHashMap();
        // 填充数据
        warmPlanList.forEach(item->{
            // 每一个就是一个小节
            Integer series = item.getSeries();
            List<WarmupSubmit> seriesSubmits = submitMap.computeIfAbsent(series, key -> Lists.newArrayList());
            List<WarmupPractice> warmupPracticeList = totalMap.computeIfAbsent(series, key -> Lists.newArrayList());
            String practiceId = item.getPracticeId();
            String[] split = practiceId.split(",");
            for (String p : split) {
                Integer id = Integer.parseInt(p);
                // 给用户提交的小节里填充
                Optional<WarmupSubmit> firstSubmit = warmupSubmit.stream().filter(submit -> submit.getQuestionId().equals(id)).findFirst();
                firstSubmit.ifPresent(seriesSubmits::add);
                // 给总分那里填充
                Optional<WarmupPractice> firstPractice = warmupPractices.stream().filter(practice -> practice.getId() == id).findFirst();
                firstPractice.ifPresent(warmupPracticeList::add);
            }
        });
        // 计算小节得分
        Map<Integer,Integer> seriesScores = Maps.newHashMap();
        Map<Integer,Integer> seriesTotalScores = Maps.newHashMap();
        submitMap.forEach((series,submits)->{
            // 综合小节内题目的得分
            seriesScores.putIfAbsent(series, 0);
            submits.forEach(item->{
                seriesScores.computeIfPresent(series, (key, oldValue) -> oldValue + item.getScore());
            });
        });
        totalMap.forEach((series,practice)->{
            // 综合小节内题目的得分
            seriesTotalScores.putIfAbsent(series, 0);
            practice.forEach(item -> seriesTotalScores.computeIfPresent(series, (key, oldValue) -> oldValue + item.getScore()));
        });
        // 按章来区分
        chapters.forEach(item->{
            List<Section> sections = item.getSections();
            item.setMyWarmScore(0);
            item.setTotalWarmScore(0);
            // 综合每章内小节得分
            sections.forEach(section -> {
                item.setMyWarmScore(item.getMyWarmScore() + seriesScores.getOrDefault(section.getSeries(), 0));
                item.setTotalWarmScore(item.getTotalWarmScore() + seriesTotalScores.getOrDefault(section.getSeries(), 0));
            });
        });
        report.setChapterList(chapters);
    }

    private List<WarmupPractice> initWarmupScores(List<PracticePlan> warmPlanList){
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        warmPlanList.forEach(item->{
            String practiceIds = item.getPracticeId();
            String[] practiceIdArr = practiceIds.split(",");
            for (String arrItem : practiceIdArr) {
                try {
                    Integer practiceId = Integer.parseInt(arrItem);
                    WarmupPractice warmupPractice = cacheService.getWarmupPractice(practiceId);
                    Integer score = null;
                    if (warmupPractice == null) {
                        logger.error("该题已被删除:{}", practiceId);
                        continue;
                    } else {
                        Integer difficulty = warmupPractice.getDifficulty();
                        if (difficulty == 1) {
                            score = PointRepo.EASY_SCORE;
                        } else if(difficulty == 2){
                            score = PointRepo.NORMAL_SCORE;
                        } else if(difficulty == 3){
                            score = PointRepo.HARD_SCORE;
                        } else {
                            logger.error("难度系数不正常,{},{}", difficulty, practiceId);
                            score = 0;
                        }
                    }
                    warmupPractice.setScore(score);
                    warmupPractices.add(warmupPractice);

                } catch (Exception e) {
                    // ignore
                }

            }
        });
        return warmupPractices;
    }
}
