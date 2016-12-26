package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.DateUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by justin on 16/12/13.
 */
@Service
public class GeneratePlanServiceImpl implements GeneratePlanService {
    @Autowired
    private ProblemListDao problemListDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private ProblemKnowledgeMapDao problemKnowledgeMapDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    private static final int EASY = 1;
    private static final int NORMAL = 2;
    private static final int HARD = 3;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Integer generatePlan(String openid, Integer problemId) {
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
        //更新问题状态
        problemListDao.updateStatus(openid, problemId, 1);

        return planId;
    }

    private List<PracticePlan> createChallengePractice(Problem problem, int planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selected = Lists.newArrayList();
        int challengeCount = problem.getChallengeCount();
        List<ChallengePractice> practices = challengePracticeDao.loadPractice(problem.getId());

        List<ChallengePractice> challengePractices = selectChallenge(practices, challengeCount);
        challengePractices.stream().forEach(practice->{
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setUnlocked(true);
            practicePlan.setPlanId(planId);
            practicePlan.setType(PracticePlan.CHALLENGE);
            practicePlan.setPracticeId(practice.getId()+"");
            practicePlan.setStatus(0);
            practicePlan.setSequence(WARMUP_TASK_NUMBER+2);
            practicePlan.setSeries(0);
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
        // 问题涉及的知识点
        List<ProblemKnowledgeMap> maps = problemKnowledgeMapDao.loadKnowledges(problem.getId());
        List<KnowledgeVolume> knowledgeVolumes = assignVolume(applicationCount, maps);
        List<ApplicationPractice> applicationPractices = Lists.newArrayList();

        knowledgeVolumes.stream().forEach(knowledgeVolume -> {
            List<ApplicationPractice> practices = applicationPracticeDao.loadPractice(knowledgeVolume.getKnowledgeId());

            List<ApplicationPractice> selected = selectApplication(practices, knowledgeVolume.getCount());
            applicationPractices.addAll(selected);
        });

        int knowledgeCursor = 0;
        for(int i=0;i<problem.getLength();i++){
            ApplicationPractice practice = null;
            //防死循环控制
            int retry = 1000;
            //当某一知识点的题目用完后,继续挑选下一知识点的题
            while(practice==null && retry-->0) {
                if(knowledgeCursor>=maps.size()){
                    knowledgeCursor = 0;
                }
                Integer knowledgeId = maps.get(knowledgeCursor).getKnowledgeId();
                practice = getApplicationPracticeByKnowledge(applicationPractices, knowledgeId);
                if(practice==null){
                    knowledgeCursor++;
                }
            }
            if(practice!=null) {
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                practicePlan.setType(PracticePlan.APPLICATION);
                practicePlan.setPracticeId(practice.getId()+"");
                practicePlan.setKnowledgeId(practice.getKnowledgeId());
                practicePlan.setStatus(0);
                practicePlan.setSequence(WARMUP_TASK_NUMBER + 1);
                practicePlan.setSeries(i + 1);
                selectedPractice.add(practicePlan);
            }
            knowledgeCursor++;
        }

        return selectedPractice;
    }

    private List<ApplicationPractice> selectApplication(List<ApplicationPractice> practices, Integer count) {
//        List<ApplicationPractice> applicationPractices = Lists.newArrayList();
//        List<ApplicationPractice> easyPractice = Lists.newArrayList();
//        List<ApplicationPractice> normalPractice = Lists.newArrayList();
//        List<ApplicationPractice> hardPractice = Lists.newArrayList();
//
//        float cnt = count/3;
//        int easyCount = Math.round(cnt);
//        int normalCount = Math.round(cnt);
//        int hardCount = count-easyCount-normalCount;
//        //按难度拆分题库
//        practices.stream().forEach(practice->{
//            if(practice.getDifficulty()==EASY){
//                easyPractice.add(practice);
//            }else if(practice.getDifficulty()==NORMAL){
//                normalPractice.add(practice);
//            }else if(practice.getDifficulty()==HARD){
//                hardPractice.add(practice);
//            }
//        });

        //easy题目
//        applicationPractices.addAll(randomSelect(easyPractice, easyCount));
        //normal题目
//        applicationPractices.addAll(randomSelect(normalPractice, normalCount));
        //hard题目
//        applicationPractices.addAll(randomSelect(hardPractice, hardCount));

        return randomSelect(practices, count);
    }

    private List<PracticePlan> createWarmupPractice(Problem problem, Integer planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();
        int warmupCount = problem.getWarmupCount();
        List<ProblemKnowledgeMap> maps = problemKnowledgeMapDao.loadKnowledges(problem.getId());
        List<KnowledgeVolume> knowledgeVolumes = assignVolume(warmupCount, maps);
        List<WarmupPractice> warmupPractices = Lists.newArrayList();

        knowledgeVolumes.stream().forEach(knowledgeVolume -> {
            List<WarmupPractice> practices = warmupPracticeDao.loadPractice(knowledgeVolume.getKnowledgeId());

            List<WarmupPractice> selected = selectWarmup(practices, knowledgeVolume.getCount() *
                    WARMUP_TASK_PRACTICE_NUMBER);
            warmupPractices.addAll(selected);
        });

        int knowledgeCursor=0;
        // i——第i组练习, j——组中第j套练习, k——套中第k题
        for(int i=0;i<problem.getLength();i++){
            for(int j=0;j<WARMUP_TASK_NUMBER;j++) {
                // 保证每套题的知识点是同一个,知识点依次出现
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                practicePlan.setType(1);
                practicePlan.setSequence(j + 1);
                practicePlan.setSeries(i + 1);
                practicePlan.setStatus(0);
                selectedPractice.add(practicePlan);
                for(int k=0;k<WARMUP_TASK_PRACTICE_NUMBER;k++) {
                    WarmupPractice practice = null;
                    //防死循环控制
                    int retry = 1000;
                    //当某一知识点的题目用完后,继续挑选下一知识点的题
                    while(practice==null && retry-->0) {
                        if(knowledgeCursor>=maps.size()){
                            knowledgeCursor = 0;
                        }
                        Integer knowledgeId = maps.get(knowledgeCursor).getKnowledgeId();
                        practice = getWarmupPracticeByKnowledge(warmupPractices, knowledgeId);
                        if(practice==null){
                            knowledgeCursor++;
                        }
                    }
                    if(practice!=null){
                        //practiceId用逗号隔开
                        practicePlan.setKnowledgeId(practice.getKnowledgeId());
                        if(practicePlan.getPracticeId()==null) {
                            practicePlan.setPracticeId(practice.getId()+"");
                        }else{
                            practicePlan.setPracticeId(practicePlan.getPracticeId()+","+practice.getId());
                        }
                    }
                }
                knowledgeCursor++;
            }
        }

        return selectedPractice;
    }

    private WarmupPractice getWarmupPracticeByKnowledge(List<WarmupPractice> warmupPractices, Integer knowledgeId) {
        for(Iterator<WarmupPractice> it = warmupPractices.iterator();it.hasNext();){
            WarmupPractice warmupPractice = it.next();
            if(warmupPractice.getKnowledgeId().equals(knowledgeId)){
                it.remove();
                return warmupPractice;
            }
        }

        return null;
    }

    private ApplicationPractice getApplicationPracticeByKnowledge(List<ApplicationPractice> applicationPractices, Integer knowledgeId) {
        for(Iterator<ApplicationPractice> it = applicationPractices.iterator();it.hasNext();){
            ApplicationPractice applicationPractice = it.next();
            if(applicationPractice.getKnowledgeId().equals(knowledgeId)){
                it.remove();
                return applicationPractice;
            }
        }

        return null;
    }

    //根据难度分题目
    private List<WarmupPractice> selectWarmup(List<WarmupPractice> practices, Integer count) {
//        List<WarmupPractice> warmupPractices = Lists.newArrayList();
//        List<WarmupPractice> easyPractice = Lists.newArrayList();
//        List<WarmupPractice> normalPractice = Lists.newArrayList();
//        List<WarmupPractice> hardPractice = Lists.newArrayList();
//
//        float cnt = count/3;
//        int easyCount = Math.round(cnt);
//        int normalCount = Math.round(cnt);
//        //按难度拆分题库
//        practices.stream().forEach(practice -> {
//            if (practice.getDifficulty() == EASY) {
//                easyPractice.add(practice);
//            } else if (practice.getDifficulty() == NORMAL) {
//                normalPractice.add(practice);
//            } else if (practice.getDifficulty() == HARD) {
//                hardPractice.add(practice);
//            }
//        });

        //easy题目
//        List easyList = randomSelect(easyPractice, easyCount);
//        warmupPractices.addAll(easyList);
        //normal题目
//        List normalList = randomSelect(easyPractice, normalCount);
//        warmupPractices.addAll(normalList);
        //hard题目
//        List hardList = randomSelect(easyPractice, count-normalList.size()-easyList.size());
//        warmupPractices.addAll(hardList);
        // TODO:先不分难度
        return randomSelect(practices, count);
    }

    private List randomSelect(List list, int count) {
        if(list.size()<=count){
            return list;
        }
        List selected = Lists.newArrayList();
        List<Integer> selectedIds = Lists.newArrayList();
        for(int i=0;i<count;i++) {
            int id;
            do{
                id=new Random().nextInt(list.size());
            }while (selectedIds.contains(id));

            selected.add(list.get(id));
            selectedIds.add(id);
        }
        return selected;
    }

    private List<KnowledgeVolume> assignVolume(int count, List<ProblemKnowledgeMap> maps) {
        int left = count;
        List<KnowledgeVolume> knowledgeVolumes = Lists.newArrayList();

        //分配n个知识点的题目数,前n-1个根据权重*总题量后四舍五入,最后一个取余数
        for(int i=0;i<maps.size();i++){
            if(i!=maps.size()-1) {
                int weight = maps.get(i).getWeight();
                int cnt = Math.round((float)(count*weight/100));
                knowledgeVolumes.add(new KnowledgeVolume().
                        knowledgeId(maps.get(i).getKnowledgeId()).count(cnt));
                //剩余题目数
                left -=cnt;
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
        improvementPlan.setProblemId(problem.getId());
        improvementPlan.setPoint(0);
        improvementPlan.setStatus(1);
        //初始化时有一把钥匙
        improvementPlan.setKeycnt(1);
        improvementPlan.setStartDate(new Date());
        improvementPlan.setEndDate(DateUtils.afterDays(new Date(), length));
        //结束时期后再开放7天
        improvementPlan.setCloseDate(DateUtils.afterDays(new Date(), length + 7));
        improvementPlan.setTotal(problem.getWarmupCount()+
                problem.getApplicationCount()+
                problem.getChallengeCount());

        return improvementPlanDao.insert(improvementPlan);

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
