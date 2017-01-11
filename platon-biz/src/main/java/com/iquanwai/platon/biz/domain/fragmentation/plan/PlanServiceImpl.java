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

import java.util.Comparator;
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
        improvementPlan.setSummary(isSummary(practicePlans));
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        improvementPlan.setSeries(improvementPlan.getCurrentSeries());
    }

    @Override
    public boolean buildSeriesPlanDetail(ImprovementPlan improvementPlan, Integer series) {
        Problem problem = problemDao.load(Problem.class, improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        //选择当前组的练习
        List<PracticePlan> runningPractice = pickPracticeBySeries(improvementPlan, series);
        //已经到最后一组解锁训练,返回空
        if(CollectionUtils.isEmpty(runningPractice)){
            return false;
        }
        PracticePlan firstPractice = runningPractice.get(0);
        //未解锁返回空
        if(!firstPractice.getUnlocked()){
            return false;
        }
        //找到所有挑战训练
        runningPractice.addAll(practicePlans.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.CHALLENGE).collect(Collectors.toList()));
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        improvementPlan.setSummary(false);
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        improvementPlan.setSeries(firstPractice.getSeries());
        return true;
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
            //跳过挑战训练
            if(practicePlan.getType()==PracticePlan.CHALLENGE){
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
        List<Practice> practiceList = Lists.newArrayList();
        runningPractice.sort((o1, o2) -> o1.getSequence()-o2.getSequence());

        //根据sequence构建对象
        for(PracticePlan practicePlan:runningPractice){
            practiceList.add(buildPractice(practicePlan));
        }

        return practiceList;
    }


    //映射
    private Practice buildPractice(PracticePlan practicePlan) {
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
        if(practice.getKnowledge()==null) {
            Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId(), practicePlan.getPlanId());
            practice.setKnowledge(knowledge);
        }
        return practice;
    }

    private List<PracticePlan> pickPracticeBySeries(ImprovementPlan improvementPlan, Integer series) {
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> practicePlanList = practicePlanDao.loadBySeries(improvementPlan.getId(), series);
        runningPractice.addAll(practicePlanList);
        return runningPractice;
    }

    private List<PracticePlan> pickRunningPractice(List<PracticePlan> practicePlans, ImprovementPlan improvementPlan) {
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> tempPractice = Lists.newArrayList();
        //找到挑战训练
        runningPractice.addAll(practicePlans.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.CHALLENGE).collect(Collectors.toList()));

        boolean hasKey = false;
        if(improvementPlan.getKeycnt()>0){
            hasKey = true;
        }
        int seriesCursor = 0; //当前组指针
        if(hasKey){
            //如果有解锁钥匙,找到第一组未完成的练习
            boolean running = false;

            for(PracticePlan practicePlan:practicePlans) {
                if (practicePlan.getType() == PracticePlan.CHALLENGE) {
                    //如果挑战训练没完成,直接获取第一天的数据
                    if(practicePlan.getStatus()==0){
                        runningPractice.addAll(pickPracticeBySeries(improvementPlan, 1));
                        return runningPractice;
                    }
                    continue;
                }
                if(practicePlan.getSeries()!=seriesCursor){
                    //找到正在进行的训练组
                    if(running){
                        break;
                    }
                    seriesCursor = practicePlan.getSeries();
                    tempPractice.clear();
                }
                tempPractice.add(practicePlan);
                //如果有解锁钥匙,找到第一组未完成的练习,如果没有解锁钥匙,找到最后一组已解锁的练习
                //找到第一个未完成的练习
                if(practicePlan.getStatus()==0){
                    running = true;
                }
            }

            boolean unlock = false;
            for(PracticePlan practicePlan:tempPractice){
                //如果练习未解锁,则解锁练习
                if(!practicePlan.getUnlocked()){
                    practicePlan.setUnlocked(true);
                    unlock = true;
                    practicePlanDao.unlock(practicePlan.getId());
                }
            }
            //如果解锁了新练习,更新进度和钥匙
            if(unlock) {
                PracticePlan practicePlan = tempPractice.get(0);
                improvementPlanDao.updateProgress(improvementPlan.getId(),
                        improvementPlan.getKeycnt() - 1, practicePlan.getSeries());
            }
        }else{
            //如果没有解锁钥匙,找到最后一组已解锁的练习
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
                    tempPractice.clear();
                }
                tempPractice.add(practicePlan);
            }
        }

        runningPractice.addAll(tempPractice);
        return runningPractice;
    }

    private Knowledge getKnowledge(Integer knowledgeId, Integer planId){
        if(knowledgeId==null){
            return null;
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
    public void completePlan(Integer planId) {
        //训练计划结束
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(plan.getWarmupComplete()+plan.getApplicationComplete()>=plan.getTotal()) {
            logger.info("{} is complete", planId);
            improvementPlanDao.updateStatus(planId, 2);
        }else{
            logger.info("{} is terminated", planId);
            improvementPlanDao.updateStatus(planId, 3);
        }
        problemPlanDao.updateStatus(plan.getOpenid(), plan.getProblemId(), 2);
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
}
