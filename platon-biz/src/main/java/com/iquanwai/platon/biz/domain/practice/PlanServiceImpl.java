package com.iquanwai.platon.biz.domain.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.practice.*;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.DateUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Random;

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
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ProblemKnowledgeMapDao problemKnowledgeMapDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int EASY = 1;
    private static final int NORMAL = 2;
    private static final int HARD = 3;


    @Override
    public void generatePlan(String openid, Integer problemId) {
        Assert.notNull(openid, "openid不能为空");
        Problem problem = problemDao.load(Problem.class, problemId);
        if(problem == null){
            logger.error("problemId {} is invalid", problemId);
        }
        //生成训练计划
        int planId = createPlan(problem, openid);

        List<PracticePlan> practicePlans = Lists.newArrayList();
        //生成热身训练
        practicePlans.addAll(createWarmupPractice(problem, planId));
        //生成应用训练
        practicePlans.addAll(createApplicationPractice(problem, planId));
        //生成挑战训练
        practicePlans.addAll(createChallengePractice(problem, planId));
        //插入数据库
        practicePlanDao.batchInsert(practicePlans);

    }

    private List<PracticePlan> createChallengePractice(Problem problem, int planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selected = Lists.newArrayList();
        int challengeCount = problem.getChallengeCount();
        List<ChallengePractice> practices = challengePracticeDao.loadPractice(problem.getId());

        List<ChallengePractice> challengePractices = selectChallenge(practices, challengeCount);
        challengePractices.stream().forEach(practice->{
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setLock(true);
            practicePlan.setPlanId(planId);
            practicePlan.setType(21);
            practicePlan.setPracticeId(practice.getId());
            practicePlan.setStatus(0);
            selected.add(practicePlan);
        });

        return selected;
    }

    private List<ChallengePractice> selectChallenge(List<ChallengePractice> practices, Integer count) {
        List<ChallengePractice> challengePractices = Lists.newArrayList();

        challengePractices.addAll(randomSelect(practices, Math.round(count)));

        return challengePractices;
    }

    private List<PracticePlan> createApplicationPractice(Problem problem, int planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();
        int applicationCount = problem.getApplicationCount();
        List<KnowledgeVolume> knowledgeVolumes = assignVolume(problem.getId(), applicationCount);
        List<ApplicationPractice> applicationPractices = Lists.newArrayList();

        knowledgeVolumes.stream().forEach(knowledgeVolume -> {
            List<ApplicationPractice> practices = applicationPracticeDao.loadPractice(knowledgeVolume.getKnowledgeId());

            List<ApplicationPractice> selected = selectApplication(practices, knowledgeVolume.getCount());
            applicationPractices.addAll(selected);
        });

        applicationPractices.stream().forEach(practice->{
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setLock(true);
            practicePlan.setPlanId(planId);
            practicePlan.setType(11);
            practicePlan.setPracticeId(practice.getId());
            practicePlan.setStatus(0);
            selectedPractice.add(practicePlan);
        });

        return selectedPractice;
    }

    private List<ApplicationPractice> selectApplication(List<ApplicationPractice> practices, Integer count) {
        List<ApplicationPractice> applicationPractices = Lists.newArrayList();
        List<ApplicationPractice> easyPractice = Lists.newArrayList();
        List<ApplicationPractice> normalPractice = Lists.newArrayList();
        List<ApplicationPractice> hardPractice = Lists.newArrayList();

        //按难度拆分题库
        practices.stream().forEach(practice->{
            if(practice.getDifficulty()==EASY){
                easyPractice.add(practice);
            }else if(practice.getDifficulty()==NORMAL){
                normalPractice.add(practice);
            }else if(practice.getDifficulty()==HARD){
                hardPractice.add(practice);
            }
        });

        //easy题目
        applicationPractices.addAll(randomSelect(easyPractice, Math.round(count)));
        //normal题目
        applicationPractices.addAll(randomSelect(normalPractice, Math.round(count)));
        //hard题目
        applicationPractices.addAll(randomSelect(hardPractice, Math.round(count)));

        return applicationPractices;
    }

    private List<PracticePlan> createWarmupPractice(Problem problem, Integer planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();
        int warmupCount = problem.getWarmupCount();
        List<KnowledgeVolume> knowledgeVolumes = assignVolume(problem.getId(), warmupCount);
        List<WarmupPractice> warmupPractices = Lists.newArrayList();

        knowledgeVolumes.stream().forEach(knowledgeVolume -> {
            List<WarmupPractice> practices = warmupPracticeDao.loadPractice(knowledgeVolume.getKnowledgeId());

            List<WarmupPractice> selected = selectWarmup(practices, knowledgeVolume.getCount());
            warmupPractices.addAll(selected);
        });

        warmupPractices.stream().forEach(practice->{
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setLock(true);
            practicePlan.setPlanId(planId);
            practicePlan.setType(practice.getType());
            practicePlan.setPracticeId(practice.getId());
            practicePlan.setStatus(0);
            selectedPractice.add(practicePlan);
        });

        return selectedPractice;
    }

    private List<WarmupPractice> selectWarmup(List<WarmupPractice> practices, Integer count) {
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        List<WarmupPractice> easyPractice = Lists.newArrayList();
        List<WarmupPractice> normalPractice = Lists.newArrayList();
        List<WarmupPractice> hardPractice = Lists.newArrayList();

        //按难度拆分题库
        practices.stream().forEach(practice->{
                if(practice.getDifficulty()==EASY){
                    easyPractice.add(practice);
                }else if(practice.getDifficulty()==NORMAL){
                    normalPractice.add(practice);
                }else if(practice.getDifficulty()==HARD){
                    hardPractice.add(practice);
                }
        });

        //easy题目
        warmupPractices.addAll(randomSelect(easyPractice, Math.round(count)));
        //normal题目
        warmupPractices.addAll(randomSelect(normalPractice, Math.round(count)));
        //hard题目
        warmupPractices.addAll(randomSelect(hardPractice, Math.round(count)));

        return warmupPractices;
    }

    private List randomSelect(List list, int count) {
        List selected = Lists.newArrayList();
        for(int i=0;i<count;i++) {
            int id;
            do{
                id=new Random().nextInt(list.size());
            }while (selected.contains(list.get(id)));

            selected.add(list.get(id));
        }
        return selected;
    }

    private List<KnowledgeVolume> assignVolume(Integer problemId, int count) {
        int left = count;
        List<ProblemKnowledgeMap> maps = problemKnowledgeMapDao.loadKnowledges(problemId);
        List<KnowledgeVolume> knowledgeVolumes = Lists.newArrayList();

        //分配n个知识点的题目数,前n-1个根据权重*总题量后四舍五入,最后一个取余数
        for(int i=0;i<maps.size();i++){
            if(i!=maps.size()-1) {
                int weight = maps.get(i).getWeight();
                int cnt = (int)Math.round((double)(count*weight/100));
                knowledgeVolumes.add(new KnowledgeVolume().
                        knowledgeId(maps.get(i).getKnowledgeId()).count(cnt));
                //剩余题目数
                left -=count;
            }else{
                knowledgeVolumes.add(new KnowledgeVolume().
                        knowledgeId(maps.get(i).getKnowledgeId()).count(left));
            }
        }

        return knowledgeVolumes;
    }

    private int createPlan(Problem problem, String openid) {
        Assert.notNull(problem, "problem不能为空");
        int length = problem.getLength();
        ImprovementPlan improvementPlan = new ImprovementPlan();
        improvementPlan.setOpenid(openid);
        improvementPlan.setComplete(0);
        improvementPlan.setReadWizard(false);
        improvementPlan.setProblemId(problem.getId());
        improvementPlan.setScore(0);
        improvementPlan.setStatus(0);
        improvementPlan.setStartDate(new Date());
        improvementPlan.setEndDate(DateUtils.afterDays(new Date(), length));
        improvementPlan.setTotal(problem.getWarmupCount()+
                problem.getApplicationCount()+
                problem.getChallengeCount());

        improvementPlanDao.insert(improvementPlan);

        return 0;
    }

    @Data
    class KnowledgeVolume{
        private Integer knowledgeId;
        private Integer count;

        public KnowledgeVolume knowledgeId(Integer knowledgeId){
            this.knowledgeId = knowledgeId;
            return this;
        }

        public KnowledgeVolume count(Integer count){
            this.count = count;
            return this;
        }
    }
}
