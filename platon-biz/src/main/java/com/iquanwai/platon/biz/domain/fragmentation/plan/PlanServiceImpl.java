package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
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

import java.util.*;
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
    private NotifyMessageDao notifyMessageDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void buildPlanDetail(ImprovementPlan improvementPlan) {
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
//        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        //选择正在进行的练习
//        List<PracticePlan> runningPractice = pickRunningPractice(practicePlans, improvementPlan);
        List<PracticePlan> runningPractice = pickPracticeBySeries(improvementPlan, improvementPlan.getCurrentSeries());
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        setLogicParam(improvementPlan, runningPractice);

    }

    private Integer getSeries(List<PracticePlan> runningPractice) {
        Assert.notNull(runningPractice, "练习计划不能为空");
        for(PracticePlan practicePlan:runningPractice){
            if(practicePlan.getType()!=PracticePlan.CHALLENGE){
               return practicePlan.getSeries();
            }
        }
        return 0;
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

    /**
     * 当前组的应用训练，综合训练是否满足完成逻辑
     * @param runningPractices 当前组
     * @return left:是否完成，right:提示语
     */
    private Pair<Boolean,String> isDoneApplication(List<PracticePlan> runningPractices){

        boolean left = true;
        String right = null;

        if(CollectionUtils.isNotEmpty(runningPractices)){
            List<PracticePlan> applications = runningPractices.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.APPLICATION).collect(Collectors.toList());
            List<PracticePlan> applicationReviews = runningPractices.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.APPLICATION_REVIEW).collect(Collectors.toList());
            if (applications.size() > 0) {
                // 有应用训练，查看是否完成
                long count = applications.stream().filter(practicePlan -> practicePlan.getStatus() == 1).count();
                if (count == 0) {
                    // 一个应用训练也没完成
                    left = false;
                    right = PracticePlan.APPLICATION_NOTICE;
                }
            }

            if (applicationReviews.size() > 0) {
                // 有综合训练，查看是否有没完成的
                long count = applicationReviews.stream().filter(practicePlan -> practicePlan.getStatus() == 0).count();
                if (count > 0) {
                    // 有没完成的
                    left = false;
                    right = PracticePlan.APPLICATION_REVIEW_NOTICE;
                }
            }

            return new MutablePair<>(left, right);
        } else {
            // 当前节没有应用练习,综合练习，默认返回true
            return new MutablePair<>(true, null);
        }
    }


    /**
     * 获取最大完成组序号
     * @param practicePlans 所有训练
     * @return 最大完成组序号
     */
    private Integer completeSeriesCount(List<PracticePlan> practicePlans) {
        Set<Integer> completeSeries = Sets.newHashSet();
        Map<Integer,List<PracticePlan>> seriesPlan = Maps.newHashMap();
        // 分组
        for(PracticePlan plan : practicePlans){
            Integer series = plan.getSeries();
            List<PracticePlan> plans = seriesPlan.computeIfAbsent(series, (k) -> Lists.newArrayList());
            plans.add(plan);
        }
        // 判断是否完成
        for (Integer key : seriesPlan.keySet()) {
            if (isDone(seriesPlan.get(key))) {
                completeSeries.add(key);
            }
        }
        // 获取最大的完成组，没有的话则是0
        Optional<Integer> maxComplete = completeSeries.stream().max(Integer::compareTo);
        return maxComplete.orElse(0);
    }

    @Override
    public Integer buildSeriesPlanDetail(ImprovementPlan improvementPlan, Integer series, Boolean riseMember) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);

        //选择当前节的练习
        List<PracticePlan> runningPractice = pickPracticeBySeries(improvementPlan, series);
        //已经到最后一节解锁训练,返回false
        if(CollectionUtils.isEmpty(runningPractice)){
            return -1;
        }
        PracticePlan firstPractice = runningPractice.get(0);
        //未解锁返回false
        if (!firstPractice.getUnlocked()) {
            // 判断是否是付费用户 || 获取前一节训练
            if(riseMember || improvementPlan.getRiseMember() || series <= ConfigUtils.preStudySerials()) {
                List<PracticePlan> prePracticePlans = pickPracticeBySeries(improvementPlan, series - 1);
                //前一节必做练习已完成且小课非关闭状态
                if (isDone(prePracticePlans) && improvementPlan.getStatus()!=ImprovementPlan.CLOSE) {
                    unlock(runningPractice, improvementPlan);
                }
            }
        }
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        setLogicParam(improvementPlan, runningPractice);
        // 不是会员并且是第四节，则提示一下
        if (!riseMember && series > ConfigUtils.preStudySerials() && !improvementPlan.getRiseMember()) {
            return -3;
        }
        // 更新当前组的状态
        improvementPlanDao.updateCurrentSeries(improvementPlan.getId(), series);
        return 0;
    }

    private void setLogicParam(ImprovementPlan improvementPlan, List<PracticePlan> runningPractice) {
        improvementPlan.setSummary(false);
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        int series = getSeries(runningPractice);
        improvementPlan.setSeries(series);
        setTitleInfo(improvementPlan, series, improvementPlan.getProblemId());
        int messageNumber = notifyMessageDao.newMessageCount(improvementPlan.getOpenid());
        improvementPlan.setNewMessage(messageNumber>0);
        // 所有的综合练习是否完成
        List<PracticePlan> applications = practicePlanDao.loadApplicationPracticeByPlanId(improvementPlan.getId());
        // 拿到未完成的综合训练
        List<PracticePlan> disDoneApplications = applications.stream().filter(practicePlan -> practicePlan.getType()==PracticePlan.APPLICATION_REVIEW && practicePlan.getStatus() == 0)
                .collect(Collectors.toList());
        // 未完成未空则代表全部完成
        improvementPlan.setDoneAllIntegrated(CollectionUtils.isEmpty(disDoneApplications));

        Pair<Boolean, String> doneApps = isDoneApplication(runningPractice);
        // 当前组的综合训练是否全部完成
        improvementPlan.setDoneCurSeriesApplication(doneApps.getLeft());

        if (!improvementPlan.getDoneAllIntegrated()) {
            // tips:存在所有综合训练中，有未完成的情况，提示语设置，会被之后覆盖
            improvementPlan.setAlertMsg(PracticePlan.APPLICATION_REVIEW_NOTICE);
        }

        if (!doneApps.getLeft()) {
            // 未完成当前组的应用训练，综合训练,提示语优先级较高
            improvementPlan.setAlertMsg(doneApps.getRight());
        }

//        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
//        improvementPlan.setCompleteSeries(completeSeriesCount(practicePlans));
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
        problemSchedules.forEach(item->{
            Integer knowledgeId = item.getKnowledgeId();
            //章序号
            Integer chapter = item.getChapter();
            //节序号
            Integer section = item.getSection();

            Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
            if(knowledge!=null){
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


    private void setTitleInfo(ImprovementPlan improvementPlan, int series, Integer problemId) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        problemSchedules.sort((o1, o2) -> {
            if(!o1.getChapter().equals(o2.getChapter())){
                return o1.getChapter()-o2.getChapter();
            }
            return o1.getSection()-o2.getSection();
        });
        //获取课程表
        if(problemSchedules.size()>series - 1) {
            ProblemSchedule problemSchedule = problemSchedules.get(series - 1);
            Integer knowledgeId = problemSchedule.getKnowledgeId();
            //章序号
            Integer chapter = problemSchedule.getChapter();
            //节序号
            Integer section = problemSchedule.getSection();

            Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
            if (knowledge != null) {
                improvementPlan.setChapter("第" + chapter + "章 " + knowledge.getStep());
                improvementPlan.setSection(chapter + "." + section + " " + knowledge.getKnowledge());
            }
        }
    }

    private void unlock(List<PracticePlan> runningPractice, ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Assert.notNull(runningPractice, "练习计划不能为空");
        //如果练习未解锁,则解锁练习
        runningPractice.stream().filter(practicePlan -> !practicePlan.getUnlocked()).forEach(practicePlan -> {
            practicePlan.setUnlocked(true);
            practicePlanDao.unlock(practicePlan.getId());
        });
        Integer progress = runningPractice.get(0).getSeries();
        improvementPlanDao.updateProgress(improvementPlan.getId(), progress - 1);
        improvementPlan.setCompleteSeries(progress - 1);
    }

    private List<Practice> createPractice(List<PracticePlan> runningPractice) {
        Assert.notNull(runningPractice, "练习计划不能为空");
        List<Practice> practiceList = Lists.newArrayList();
        runningPractice.sort((o1, o2) -> o1.getSequence() - o2.getSequence());

        //根据sequence构建对象
        practiceList.addAll(runningPractice.stream().map(this::buildPractice).collect(Collectors.toList()));

        return practiceList;
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
        if(practice.getKnowledge()== null) {
            Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId());
            practice.setKnowledge(knowledge);
        }
        return practice;
    }

    private boolean isOptional(Integer type) {
        return type==PracticePlan.CHALLENGE || type==PracticePlan.APPLICATION;
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

    //获取第一节未完成的练习
    private List<PracticePlan> getFirstIncompletePractice(List<PracticePlan> practicePlans) {
        Assert.notNull(practicePlans, "练习计划不能为空");
        List<PracticePlan> incompletePractice = Lists.newArrayList();
        Map<Integer,List<PracticePlan>> practiceMap = Maps.newHashMap();

        practicePlans.forEach(item->{
            List<PracticePlan> plans = practiceMap.get(item.getSeries());
            if (plans == null) {
                plans = Lists.newArrayList();
            }
            plans.add(item);
            practiceMap.put(item.getSeries(), plans);
        });

        for (Integer key : practiceMap.keySet()) {
            if (key == 0) {
                continue;
            }
            if (!isDone(practiceMap.get(key))) {
                // 没有完成
                incompletePractice.addAll(practiceMap.get(key));
                break;
            }
        }

        return incompletePractice;
    }

    //获取最后一节解锁的练习
    private List<PracticePlan> getLastUnlockPractice(List<PracticePlan> practicePlans) {
        Assert.notNull(practicePlans, "练习计划不能为空");
        int seriesCursor =0;
        List<PracticePlan> unlockPractice = Lists.newArrayList();
        for(PracticePlan practicePlan:practicePlans) {
            //如果练习未解锁,跳出循环
            if(!practicePlan.getUnlocked()){
                break;
            }
            if (practicePlan.getType() == PracticePlan.CHALLENGE) {
                continue;
            }
            if(practicePlan.getSeries()!=seriesCursor){
                seriesCursor = practicePlan.getSeries();
                unlockPractice.clear();
            }
            unlockPractice.add(practicePlan);
        }
        return unlockPractice;
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
    public ImprovementPlan getRunningPlan(String openid) {
        return improvementPlanDao.loadRunningPlan(openid);
    }

    @Override
    public ImprovementPlan getLatestPlan(String openid) {
        return improvementPlanDao.getLastPlan(openid);
    }

    @Override
    public List<ImprovementPlan> getPlans(String openid){
        return improvementPlanDao.loadAllPlans(openid);
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
//        if (status == ImprovementPlan.COMPLETE) {
//            improvementPlanDao.updatePlanComplete(planId, status);
//        } else {
        improvementPlanDao.updateStatus(planId, status);
//        }
        //解锁所有应用练习
//        practicePlanDao.unlockApplicationPractice(planId);
        //发送完成通知
        if(status == ImprovementPlan.CLOSE) {
            sendCloseMsg(plan);
        }

        return improvementPlanDao.defeatOthers(plan.getProblemId(), plan.getPoint());
    }

    private void sendCloseMsg(ImprovementPlan plan) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.courseCloseMsg());
        templateMessage.setTouser(plan.getOpenid());
//        templateMessage.setUrl("");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        Problem problem = cacheService.getProblem(plan.getProblemId());
        templateMessage.setData(data);

        data.put("first",new TemplateMessage.Keyword("太棒了！你已完成以下小课，并获得了"+plan.getPoint()+"积分\n"));
        data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date())));
//        data.put("remark",new TemplateMessage.Keyword("\nP. S. 使用中有不爽的地方？我们已经想了几个优化的点子，点击进来看看，" +
//                "是不是想到一起了→→→ （跳转调查链接）"));
        data.put("remark",new TemplateMessage.Keyword("\n应用练习/小课论坛PC端永久开放，完成仍然加积分：www.iquanwai.com/community"));


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

    @Override
    public Integer checkPractice(Integer series, ImprovementPlan improvementPlan) {
        //当前第一节返回0
        if (series == 1) {
            return 0;
        }
        //获取前一节训练
        List<PracticePlan> prePracticePlans = pickPracticeBySeries(improvementPlan, series - 1);
        if (isDone(prePracticePlans)) {
            List<PracticePlan> practicePlans = pickPracticeBySeries(improvementPlan, series);

            for (PracticePlan practicePlan : practicePlans) {
                if (!practicePlan.getUnlocked()) {
                    //已过期返回-3
                    if(improvementPlan.getStatus()==ImprovementPlan.CLOSE){
                        return -3;
                    }
                    //未付费返回-1
                    return -1;
                }
            }
            return 0;
        }
        //未完成返回-2
        return -2;
    }

    public boolean hasProblemPlan(String openId, Integer problemId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(openId);
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
        PracticePlan plan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(plan.getPlanId());
        boolean complete = true;
        for(PracticePlan practicePlan:practicePlans){
            if(practicePlan.getType()==PracticePlan.KNOWLEDGE ||
                    practicePlan.getType()==PracticePlan.KNOWLEDGE_REVIEW ||
                    practicePlan.getType()==PracticePlan.WARM_UP ||
                    practicePlan.getType()==PracticePlan.WARM_UP_REVIEW){
                //理解练习和巩固练习必须要完成
                if(practicePlan.getStatus()!=1){
                    complete = false;
                }
            }
        }
        if(complete){
            improvementPlanDao.updateCompleteTime(plan.getPlanId());
        }
    }
}
