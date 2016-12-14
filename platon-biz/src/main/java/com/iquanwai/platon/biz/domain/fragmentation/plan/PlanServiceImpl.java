package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    private PracticePlanDao practicePlanDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KnowledgePlanDao knowledgePlanDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();

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

        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate()));
    }

    private List<Practice> createPractice(List<PracticePlan> runningPractice) {
        List<Practice> practiceList = Lists.newArrayList();
        int largestSequence = getLargestSequence(runningPractice);
        Map<Integer, Practice> maps = Maps.newHashMap();
        for(int i=1;i<=largestSequence;i++){
            Practice practice = new Practice();
            practice.setPracticeIdList(Lists.newArrayList());
            practiceList.add(practice);
            maps.put(i, practice);
        }
        //根据sequence构建对象
        for(PracticePlan practicePlan:runningPractice){
            buildPractice(maps.get(practicePlan.getSequence()), practicePlan);
        }

        return practiceList;
    }

    //动态获取练习的套数
    private int getLargestSequence(List<PracticePlan> runningPractice) {
        int largestSequence = 0;
        for(PracticePlan practicePlan:runningPractice) {
            if(practicePlan.getSequence()>largestSequence){
                largestSequence = practicePlan.getSequence();
            }
        }
        return largestSequence;
    }

    private void buildPractice(Practice practice, PracticePlan practicePlan) {
        practice.setStatus(practicePlan.getStatus());
        practice.setUnlock(practicePlan.getUnlocked());
        practice.getPracticeIdList().add(practicePlan.getPracticeId());
        practice.setType(practicePlan.getType());
        Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId(), practicePlan.getPlanId());
        practice.setKnowledge(knowledge);
    }

    private List<PracticePlan> pickRunningPractice(List<PracticePlan> practicePlans, ImprovementPlan improvementPlan) {
        boolean running = false;
        int seriesCursor = 0;
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> tempPractice = Lists.newArrayList();
        for(PracticePlan practicePlan:practicePlans){
            //TODO:挑战只能一个!!
            if(practicePlan.getType() == PracticePlan.CHALLENGE){
                runningPractice.add(practicePlan);
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
            //找到第一个未完成的练习
            if(practicePlan.getStatus()==0){
                running = true;
            }
        }

        boolean unlock = false;
        for(PracticePlan practicePlan:tempPractice){
            //解锁练习
            if(!practicePlan.getUnlocked()){
                if(improvementPlan.getKeycnt()>0) {
                    practicePlan.setUnlocked(true);
                    unlock = true;
                    practicePlanDao.unlock(practicePlan.getId());
                }
            }
        }
        if(unlock) {
            improvementPlanDao.updateKey(improvementPlan.getId(), improvementPlan.getKeycnt() - 1);
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
            knowledgePlan = new KnowledgePlan();
            knowledgePlan.setAppear(true);
            knowledgePlan.setKnowledgeId(knowledgeId);
            knowledgePlan.setPlanId(planId);
            knowledgePlanDao.insert(knowledgePlan);
            knowledge.setAppear(false);
        }else {
            knowledge.setAppear(true);
        }
        return knowledge;
    }

    @Override
    public ImprovementPlan getRunningPlan(String openid) {
        return improvementPlanDao.loadRunningPlan(openid);
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
        if(knowledgeMap.get(knowledgeId)==null){
            Knowledge knowledge = knowledgeDao.load(Knowledge.class, knowledgeId);
            knowledgeMap.put(knowledgeId, knowledge);
        }

        Knowledge knowledge = knowledgeMap.get(knowledgeId);
        return knowledge;
    }

    @Override
    public void completePlan(Integer planId) {
        //训练计划结束
        improvementPlanDao.updateStatus(planId, 2);
    }
}
