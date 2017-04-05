package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.KnowledgePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.NotifyMessageDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.KnowledgePlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.Set;
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
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private TemplateMessageService templateMessageService;


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
                //理解训练未完成时,返回-2
                if(practicePlan.getType()==PracticePlan.WARM_UP && practicePlan.getStatus()==0){
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isDoneApplication(List<PracticePlan> runningPractices){
        if(CollectionUtils.isNotEmpty(runningPractices)){
            for(PracticePlan practicePlan:runningPractices){
                // 应用训练是否完成
                if (practicePlan.getType() == PracticePlan.APPLICATION && practicePlan.getStatus() != null && practicePlan.getStatus() == 1) {
                    return true;
                }
            }
            // 没有已完成的应用训练
            return false;
        } else {
            // 当前组没有应用训练，默认返回true
            return true;
        }
    }

    private Integer completeSeriesCount(List<PracticePlan> practicePlans) {
        Set<Integer> disCompleteSeries = Sets.newHashSet();
        if(CollectionUtils.isNotEmpty(practicePlans)){
            for (PracticePlan practicePlan : practicePlans) {
                // 当前第几组
                if(practicePlan.getType()==PracticePlan.WARM_UP && practicePlan.getStatus() == 0){
                    disCompleteSeries.add(practicePlan.getSeries());
                }
            }
        }
        Optional<Integer> min = disCompleteSeries.stream().min(Integer::compareTo);
        Optional<Integer> max = practicePlans.stream().map(PracticePlan::getSeries).max(Integer::compareTo);
        return min.orElse(max.orElse(null));
    }

    @Override
    public Integer buildSeriesPlanDetail(ImprovementPlan improvementPlan, Integer series, Boolean riseMember) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Problem problem = problemDao.load(Problem.class, improvementPlan.getProblemId());
        improvementPlan.setProblem(problem);

        //选择当前组的练习
        List<PracticePlan> runningPractice = pickPracticeBySeries(improvementPlan, series);
        //已经到最后一组解锁训练,返回false
        if(CollectionUtils.isEmpty(runningPractice)){
            return -1;
        }
        PracticePlan firstPractice = runningPractice.get(0);
        //未解锁返回false
        if (!firstPractice.getUnlocked()) {
            // 判断是否是付费用户 || 获取前一组训练
            if(riseMember || series <= ConfigUtils.preStudySerials()) {
                List<PracticePlan> prePracticePlans = pickPracticeBySeries(improvementPlan, series - 1);
                if (isDone(prePracticePlans)) {
                    unlock(runningPractice, improvementPlan);
                }
            }
        }
        //创建练习对象
        List<Practice> practices = createPractice(runningPractice);
        improvementPlan.setPractice(practices);
        //写入非db字段
        setLogicParam(improvementPlan, runningPractice);
        // 不是会员并且是第四组，则提示一下
        if(!riseMember && series == 4){
            return -3;
        }
        return 0;
    }

    private void setLogicParam(ImprovementPlan improvementPlan, List<PracticePlan> runningPractice) {
        improvementPlan.setSummary(false);
        improvementPlan.setLength(DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getEndDate()));
        improvementPlan.setDeadline(DateUtils.interval(improvementPlan.getCloseDate())+1);
        improvementPlan.setSeries(getSeries(runningPractice));
        improvementPlan.setDoneAllPractice(isDone(runningPractice));
        int messageNumber = notifyMessageDao.newMessageCount(improvementPlan.getOpenid());
        improvementPlan.setNewMessage(messageNumber>0);
        // 所有的应用训练是否完成
//        List<PracticePlan> runningPractices = practicePlanDao.loadApplicationPracticeByPlanId(improvementPlan.getId());
//        improvementPlan.setDoneAllApplication(isDoneApplication(runningPractices));
        // 当前组的应用训练是否有完成的
        improvementPlan.setDoneCurSerialApplication(isDoneApplication(runningPractice));

        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        improvementPlan.setCompleteSeries(completeSeriesCount(practicePlans));
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
        improvementPlanDao.updateProgress(improvementPlan.getId(), improvementPlan.getKeycnt() - 1, progress);
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
        //设置选做标签,理解训练和知识点是必做,其他为选做
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
            Knowledge knowledge = getKnowledge(practicePlan.getKnowledgeId(), practicePlan.getPlanId());
            practice.setKnowledge(knowledge);
        }
        return practice;
    }

    private boolean isOptional(Integer type) {
        return type==PracticePlan.CHALLENGE || type==PracticePlan.APPLICATION_REVIEW || type==PracticePlan.APPLICATION;
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
        //第一天增加小目标,其余时间不显示小目标
        if(series==1) {
            runningPractice.add(practicePlanDao.loadChallengePractice(improvementPlan.getId()));
        }
        return runningPractice;
    }

    private List<PracticePlan> pickRunningPractice(List<PracticePlan> practicePlans, ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        Assert.notNull(practicePlans, "练习计划不能为空");
        List<PracticePlan> runningPractice = Lists.newArrayList();
        PracticePlan challengePractice = practicePlanDao.loadChallengePractice(improvementPlan.getId());
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
            //第一天增加小目标,其余时间不显示小目标
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
        //小目标的knowledgeId=null
        if(knowledgeId==null){
            Knowledge knowledge = new Knowledge();
            //文案写死
            knowledge.setKnowledge("让你的训练更有效");
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
        //解锁所有应用训练
        practicePlanDao.unlockApplicationPractice(planId);
        //更新待完成的专题状态
        problemPlanDao.updateStatus(plan.getOpenid(), plan.getProblemId(), 2);
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
        Problem problem = problemDao.load(Problem.class, plan.getProblemId());
        templateMessage.setData(data);

        data.put("first",new TemplateMessage.Keyword("太棒了！你已完成以下专题，并获得了"+plan.getPoint()+"积分\n"));
        data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2",new TemplateMessage.Keyword(DateUtils.parseDateToStringByCommon(new Date())));
//        data.put("remark",new TemplateMessage.Keyword("\nP. S. 使用中有不爽的地方？我们已经想了几个优化的点子，点击进来看看，" +
//                "是不是想到一起了→→→ （跳转调查链接）"));
        data.put("remark",new TemplateMessage.Keyword("\n应用训练/专题分享PC端永久开放，完成仍然加积分：www.iquanwai.com/community"));


        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public Pair<Boolean, Integer> completeCheck(ImprovementPlan improvementPlan) {
        Assert.notNull(improvementPlan, "训练计划不能为空");
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
        for(PracticePlan practicePlan:practicePlans){
            //理解训练必须完成,才算完成整个训练计划
            if(practicePlan.getType()==PracticePlan.WARM_UP && practicePlan.getStatus()==0){
                return new ImmutablePair<>(false, -1);
            }
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
    public Practice nextPractice(Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        Integer series = practicePlan.getSeries();
        Integer sequence = practicePlan.getSequence();
        Integer planId = practicePlan.getPlanId();
        PracticePlan nextPractice = practicePlanDao.loadBySeriesAndSequence(planId, series, sequence + 1);
        if(nextPractice==null){
            nextPractice = practicePlanDao.loadChallengePractice(planId);
        }
        return buildPractice(nextPractice);
    }

    @Override
    public WarmupPractice getExample(Integer knowledgeId, Integer problemId) {
        List<WarmupPractice> warmupPracticeList = warmupPracticeDao.loadExample(knowledgeId, problemId);
        if(CollectionUtils.isEmpty(warmupPracticeList)){
            return null;
        }else{
            Integer practiceId = warmupPracticeList.get(0).getId();

            return cacheService.getWarmupPractice(practiceId);
        }
    }

    @Override
    public Integer checkPractice(Integer series, ImprovementPlan improvementPlan) {
        //当前第一组返回0
        if (series == 1) {
            return 0;
        }
        //获取前一组训练
        List<PracticePlan> prePracticePlans = pickPracticeBySeries(improvementPlan, series - 1);
        if (isDone(prePracticePlans)) {
            List<PracticePlan> practicePlans = pickPracticeBySeries(improvementPlan, series);
            //未解锁返回-1
            for (PracticePlan practicePlan : practicePlans) {
                if (!practicePlan.getUnlocked()) {
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
        Problem load = problemDao.load(Problem.class, problemId);
        return load.getSubjectDesc();
    }
}
