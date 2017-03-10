package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/4.
 */
@Service
public class PlanServiceImpl implements PlanService {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ProblemPlanDao problemPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private KnowledgePlanDao knowledgePlanDao;
    @Autowired
    private NotifyMessageDao notifyMessageDao;
    @Autowired
    private CacheService cacheService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void buildPlanDetail(ImprovementPlan improvementPlan) {
        Problem problem = problemDao.load(Problem.class, improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        //选择正在进行的练习
        List<PracticePlan> runningPractice = pickRunningPractice(practicePlans, improvementPlan);
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        improvementPlan.setSummary(false);
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        improvementPlan.setSeries(getSeries(runningPractice));
        int messageNumber = notifyMessageDao.newMessageCount(improvementPlan.getOpenid());
        improvementPlan.setNewMessage(messageNumber>0);
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

    @Override
    public Integer buildSeriesPlanDetail(ImprovementPlan improvementPlan, Integer series) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Problem problem = problemDao.load(Problem.class, improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
        //上一组的练习必须先完成
        List<PracticePlan> lastPractice = pickPracticeBySeries(improvementPlan, series-1);

        if(CollectionUtils.isNotEmpty(lastPractice)) {
            for(PracticePlan practicePlan:lastPractice){
                //非应用训练未完成时,返回-2
                if(practicePlan.getType()!=PracticePlan.APPLICATION && practicePlan.getStatus()==0){
                    return -2;
                }
            }
        }
        //选择当前组的练习
        List<PracticePlan> runningPractice = pickPracticeBySeries(improvementPlan, series);
        //已经到最后一组解锁训练,返回false
        if(CollectionUtils.isEmpty(runningPractice)){
            return -1;
        }
        PracticePlan firstPractice = runningPractice.get(0);
        //未解锁返回false
        if(!firstPractice.getUnlocked()){
            //有钥匙就解锁,没有钥匙返回false
            if(improvementPlan.getKeycnt()>0){
                unlock(runningPractice, improvementPlan);
            }else {
                return -1;
            }
        }
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        improvementPlan.setSummary(false);
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        improvementPlan.setSeries(firstPractice.getSeries());
        int messageNumber = notifyMessageDao.newMessageCount(improvementPlan.getOpenid());
        improvementPlan.setNewMessage(messageNumber>0);
        return 0;
    }

    private void unlock(List<PracticePlan> runningPractice, ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Assert.notNull(runningPractice, "练习计划不能为空");
        //如果练习未解锁,则解锁练习
        runningPractice.stream().filter(practicePlan -> !practicePlan.getUnlocked()).forEach(practicePlan -> {
            practicePlan.setUnlocked(true);
            practicePlanDao.unlock(practicePlan.getId());
        });
        improvementPlanDao.updateKey(improvementPlan.getId(), improvementPlan.getKeycnt()-1);
    }

    private Boolean isSummary(List<PracticePlan> practices) {
        if(CollectionUtils.isEmpty(practices)){
            return false;
        }

        //找到最后一组已完成的练习
        List<PracticePlan> tempPractice = Lists.newArrayList();
        Integer seriesCursor = 0;
        boolean complete = true;

        //按照series倒序排
        practices.sort((o1, o2) -> o2.getSeries()-o1.getSeries());
        for(PracticePlan practicePlan:practices){
            //跳过应用训练
            if(practicePlan.getType()==PracticePlan.APPLICATION){
                continue;
            }
            if(!practicePlan.getSeries().equals(seriesCursor)){
                //如果该组所有练习都已完成,就找到
                if(seriesCursor!=0 && complete){
                    break;
                }
                //反之,则数据初始化
                seriesCursor = practicePlan.getSeries();
                tempPractice.clear();
                complete = true;
            }
            if(practicePlan.getStatus()==0){
                complete = false;
            }else{
                tempPractice.add(practicePlan);
            }
        }
        //已完成,但未总结
        if(CollectionUtils.isNotEmpty(tempPractice) && complete) {
            PracticePlan practicePlan = tempPractice.get(0);
            if (!practicePlan.getSummary()) {
                practicePlanDao.summary(practicePlan.getPlanId(), practicePlan.getSeries());
                return true;
            }
        }

        return false;
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
        String[] practiceArr = practicePlan.getPracticeId().split(",");
        List<Integer> practiceIdList = Lists.newArrayList();
        for(String practiceId:practiceArr){
            practiceIdList.add(Integer.parseInt(practiceId));
        }
        practice.setPracticeIdList(practiceIdList);
        practice.setType(practicePlan.getType());
        if(practice.getKnowledge()== null) {
            Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId(), practicePlan.getPlanId());
            practice.setKnowledge(knowledge);
        }
        return practice;
    }

    private List<PracticePlan> pickPracticeBySeries(ImprovementPlan improvementPlan, Integer series) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        //如果组数<=0,直接返回空数据
        if(series<=0){
            return Lists.newArrayList();
        }
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> practicePlanList = practicePlanDao.loadBySeries(improvementPlan.getId(), series);
        runningPractice.addAll(practicePlanList);
        //第一天增加专题训练,其余时间不显示专题训练
        if(series==1) {
            runningPractice.add(practicePlanDao.loadChallengePractice(improvementPlan.getId()));
        }
        return runningPractice;
    }

    private List<PracticePlan> pickRunningPractice(List<PracticePlan> practicePlans, ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Assert.notNull(practicePlans, "练习计划不能为空");
        List<PracticePlan> runningPractice = Lists.newArrayList();
        //如果专题训练未完成,直接返回第一组练习
        // TODO:不支持多组专题训练
        PracticePlan challengePractice = practicePlanDao.loadChallengePractice(improvementPlan.getId());
        if(challengePractice.getStatus()==0){
            List<PracticePlan> practicePlan = pickPracticeBySeries(improvementPlan, 1);
            if(CollectionUtils.isNotEmpty(practicePlan)){
                PracticePlan practice = practicePlan.get(0);
                if(!practice.getUnlocked()){
                    unlock(practicePlan, improvementPlan);
                }
            }
            return practicePlan;
        }
        //如果有解锁钥匙,找到第一组未完成的练习,如果没有解锁钥匙,找到最后一组已解锁的练习
        //未完成的练习
        List<PracticePlan> incompletePractice = getFirstImcompletePractice(practicePlans);

        if(CollectionUtils.isNotEmpty(incompletePractice)){
            PracticePlan practicePlan = incompletePractice.get(0);
            if(!practicePlan.getUnlocked()){
                if(improvementPlan.getKeycnt()>0) {
                    unlock(incompletePractice, improvementPlan);
                    runningPractice.addAll(incompletePractice);
                }else{
                    runningPractice.addAll(getLastUnlockPractice(practicePlans));
                }
            }else{
                runningPractice.addAll(incompletePractice);
            }
        }else{
            runningPractice.addAll(getLastUnlockPractice(practicePlans));
        }
        if(CollectionUtils.isNotEmpty(runningPractice)){
            //第一天增加专题训练,其余时间不显示专题训练
            PracticePlan plan = runningPractice.get(0);
            if(plan.getSeries()==1) {
                runningPractice.add(challengePractice);
            }
        }

        return runningPractice;
    }

    //获取第一组未完成的练习
    private List<PracticePlan> getFirstImcompletePractice(List<PracticePlan> practicePlans) {
        Assert.notNull(practicePlans, "练习计划不能为空");
        List<PracticePlan> incompletePractice = Lists.newArrayList();
        int seriesCursor = 0; //当前组指针
        boolean running = false;
        for(PracticePlan practicePlan:practicePlans) {
            if (practicePlan.getType() == PracticePlan.CHALLENGE) {
                continue;
            }
            if(practicePlan.getSeries()!=seriesCursor){
                //找到正在进行的训练组
                if(running){
                    break;
                }
                seriesCursor = practicePlan.getSeries();
                incompletePractice.clear();
            }
            incompletePractice.add(practicePlan);
            //找到第一个未完成的热身练习
            if(practicePlan.getType()==PracticePlan.WARM_UP && practicePlan.getStatus()==0){
                running = true;
            }
        }

        return incompletePractice;
    }

    //获取最后一组解锁的练习
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

    private Knowledge getKnowledge(Integer knowledgeId, Integer planId){
        //专题训练
        if(knowledgeId==null){
            Knowledge knowledge = new Knowledge();
            //文案写死
            knowledge.setKnowledge("首日必修，先定一个小目标");
            return knowledge;
        }
        Knowledge knowledge = getKnowledge(knowledgeId);
        KnowledgePlan knowledgePlan = knowledgePlanDao.getKnowledgePlan(planId, knowledgeId);
        if(knowledgePlan==null){
            knowledge.setAppear(0);
        }else {
            knowledge.setAppear(1);
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
    public List<ImprovementPlan> loadAllRunningPlan() {
        return improvementPlanDao.loadAllRunningPlan();
    }

    @Override
    public ImprovementPlan getPlan(Integer planId) {
        return improvementPlanDao.load(ImprovementPlan.class, planId);
    }

    @Override
    public void updateKey(Integer planId, Integer key) {
        improvementPlanDao.updateKey(planId, key);
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        return cacheService.getKnowledge(knowledgeId);
    }

    @Override
    public void learnKnowledge(Integer knowledgeId, Integer planId) {
        KnowledgePlan knowledgePlan = knowledgePlanDao.getKnowledgePlan(planId, knowledgeId);
        if(knowledgePlan==null){
            knowledgePlan = new KnowledgePlan();
            knowledgePlan.setPlanId(planId);
            knowledgePlan.setKnowledgeId(knowledgeId);
            knowledgePlan.setAppear(true);
            knowledgePlanDao.insert(knowledgePlan);
        }
    }

    @Override
    public void completePlan(Integer planId, Integer status) {
        //训练计划结束
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        logger.info("{} is terminated", planId);
        //更新训练计划状态
        if (status == ImprovementPlan.COMPLETE) {
            improvementPlanDao.updatePlanComplete(planId, status);
        } else {
            improvementPlanDao.updateStatus(planId, status);
        }
        //解锁所有应用训练
        practicePlanDao.unlockApplicationPractice(planId);
        //更新待完成的专题状态
        problemPlanDao.updateStatus(plan.getOpenid(), plan.getProblemId(), 2);
    }

    @Override
    public boolean completeCheck(ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        for(PracticePlan practicePlan:practicePlans){
            //应用训练可以不完成,其他训练必须完成,才算完成整个训练计划
            if(practicePlan.getType()!=PracticePlan.APPLICATION){
                if(practicePlan.getStatus()!=1){
                    return false;
                }
            }
        }
        //完成训练计划
        completePlan(improvementPlan.getId(), ImprovementPlan.COMPLETE);
        improvementPlan.setStatus(ImprovementPlan.COMPLETE);
        return true;
    }

    @Override
    public Practice nextPractice(Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        Integer series = practicePlan.getSeries();
        Integer sequence = practicePlan.getSequence();
        Integer planId = practicePlan.getPlanId();
        PracticePlan nextPractice = practicePlanDao.loadBySeriesAndSequence(planId, series, sequence+1);
        if(nextPractice==null){
            nextPractice = practicePlanDao.loadChallengePractice(planId);
        }
        return buildPractice(nextPractice);
    }

    @Override
    public boolean hasProblemPlan(String openId, Integer problemId) {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(openId);
        long count = improvementPlans.stream().filter(item -> item.getProblemId().equals(problemId)).count();
        return count > 0;
    }

    @Override
    public String loadSubjectDesc(Integer problemId) {
        Problem load = problemDao.load(Problem.class, problemId);
        return load.getSubjectDesc();
    }
}
