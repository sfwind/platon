package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.List;
import java.util.Map;
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
    private ProblemListDao problemListDao;
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
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KnowledgePlanDao knowledgePlanDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();

    private static final int EASY = 1;
    private static final int NORMAL = 2;
    private static final int HARD = 3;


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
    }

    private List<Practice> createPractice(List<PracticePlan> runningPractice) {
        List<Practice> practiceList = Lists.newArrayList();
        Practice warmUpPractice = new Practice();
        warmUpPractice.setPracticeIdList(Lists.newArrayList());
        practiceList.add(warmUpPractice);
        Practice applicationPractice = new Practice();
        applicationPractice.setPracticeIdList(Lists.newArrayList());
        practiceList.add(applicationPractice);
        Practice challengePractice = new Practice();
        challengePractice.setPracticeIdList(Lists.newArrayList());
        practiceList.add(challengePractice);
        for(PracticePlan practicePlan:runningPractice){
            if(practicePlan.getType()<10){
                buildPractice(warmUpPractice, practicePlan);
            }else if(practicePlan.getType()==PracticePlan.APPLICATION){
                buildPractice(applicationPractice, practicePlan);
            }else if(practicePlan.getType()==PracticePlan.CHALLENGE){
                buildPractice(challengePractice, practicePlan);
            }
        }

        return practiceList;
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
        int sequenceCursor = 0;
        List<PracticePlan> runningPractice = Lists.newArrayList();
        List<PracticePlan> tempPractice = Lists.newArrayList();
        for(PracticePlan practicePlan:practicePlans){
            //TODO:挑战只能一个!!
            if(practicePlan.getType() == PracticePlan.CHALLENGE){
                runningPractice.add(practicePlan);
                continue;
            }
            if(practicePlan.getSequence()!=sequenceCursor){
                //找到正在进行的训练组
                if(running){
                    break;
                }
                sequenceCursor = practicePlan.getSequence();
                tempPractice.clear();
            }
            tempPractice.add(practicePlan);
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
                    practicePlanDao.unlock(practicePlan.getPlanId(), practicePlan.getPracticeId());
                }
            }
        }
        if(unlock) {
            improvementPlanDao.updateKey(improvementPlan.getId(), improvementPlan.getKeycnt() - 1);
        }
        runningPractice.addAll(tempPractice);
        return runningPractice;
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
            practicePlan.setPracticeId(practice.getId());
            practicePlan.setStatus(0);
            practicePlan.setSequence(0);
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

        for(int i=0;i<applicationPractices.size();i++){
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setUnlocked(false);
            practicePlan.setPlanId(planId);
            practicePlan.setType(PracticePlan.APPLICATION);
            practicePlan.setPracticeId(applicationPractices.get(i).getId());
            practicePlan.setKnowledgeId(applicationPractices.get(i).getKnowledgeId());
            practicePlan.setStatus(0);
            practicePlan.setSequence(i+1);
            selectedPractice.add(practicePlan);
        }

        return selectedPractice;
    }

    private List<ApplicationPractice> selectApplication(List<ApplicationPractice> practices, Integer count) {
        List<ApplicationPractice> applicationPractices = Lists.newArrayList();
        List<ApplicationPractice> easyPractice = Lists.newArrayList();
        List<ApplicationPractice> normalPractice = Lists.newArrayList();
        List<ApplicationPractice> hardPractice = Lists.newArrayList();

        float cnt = count/3;
        int easyCount = Math.round(cnt);
        int normalCount = Math.round(cnt);
        int hardCount = count-easyCount-normalCount;
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
        applicationPractices.addAll(randomSelect(easyPractice, easyCount));
        //normal题目
        applicationPractices.addAll(randomSelect(normalPractice, normalCount));
        //hard题目
        applicationPractices.addAll(randomSelect(hardPractice, hardCount));

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

            List<WarmupPractice> selected = selectWarmup(practices, knowledgeVolume.getCount()* WARMUP_TASK_PRACTICE_NUMBER);
            warmupPractices.addAll(selected);
        });

        for(int i=0;i<warmupPractices.size()/WARMUP_TASK_PRACTICE_NUMBER;i++){
            for(int j=0;j<WARMUP_TASK_PRACTICE_NUMBER;j++){
                WarmupPractice practice = warmupPractices.get(WARMUP_TASK_PRACTICE_NUMBER * i + j);
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                practicePlan.setType(practice.getType());
                practicePlan.setPracticeId(practice.getId());
                practicePlan.setKnowledgeId(practice.getKnowledgeId());
                practicePlan.setSequence(j+1);
                practicePlan.setStatus(0);
                selectedPractice.add(practicePlan);
            }
        }

        return selectedPractice;
    }

    private List<WarmupPractice> selectWarmup(List<WarmupPractice> practices, Integer count) {
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        List<WarmupPractice> easyPractice = Lists.newArrayList();
        List<WarmupPractice> normalPractice = Lists.newArrayList();
        List<WarmupPractice> hardPractice = Lists.newArrayList();

        float cnt = count/3;
        int easyCount = Math.round(cnt);
        int normalCount = Math.round(cnt);
        int hardCount = count-easyCount-normalCount;
        //按难度拆分题库
        practices.stream().forEach(practice -> {
            if (practice.getDifficulty() == EASY) {
                easyPractice.add(practice);
            } else if (practice.getDifficulty() == NORMAL) {
                normalPractice.add(practice);
            } else if (practice.getDifficulty() == HARD) {
                hardPractice.add(practice);
            }
        });

        //easy题目
        warmupPractices.addAll(randomSelect(easyPractice, easyCount));
        //normal题目
        warmupPractices.addAll(randomSelect(normalPractice, normalCount));
        //hard题目
        warmupPractices.addAll(randomSelect(hardPractice, hardCount));

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
        improvementPlan.setScore(0);
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

//        return improvementPlanDao.insert(improvementPlan);

        return 1;
    }

    private Knowledge getKnowledge(Integer knowledgeId, Integer planId){
        Knowledge knowledge = getKnowledge(knowledgeId);
        KnowledgePlan knowledgePlan = knowledgePlanDao.getKnowledgePlan(planId, knowledgeId);
        if(knowledgePlan==null){
            knowledgePlan.setAppear(true);
            knowledgePlan.setKnowledgeId(knowledgeId);
            knowledgePlan.setPlanId(planId);
            knowledgePlanDao.insert(knowledgePlan);
        }
        knowledge.setAppear(knowledgePlan.getAppear());
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
