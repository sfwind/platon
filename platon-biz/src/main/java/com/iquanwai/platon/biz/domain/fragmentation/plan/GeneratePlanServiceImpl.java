package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
 * Created by justin on 16/12/13.
 */
@Service
public class GeneratePlanServiceImpl implements GeneratePlanService {
    @Autowired
    private ProblemPlanDao problemPlanDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Integer generatePlan(String openid, Integer problemId) {
        Assert.notNull(openid, "openid不能为空");
        Problem problem = cacheService.getProblem(problemId);
        if(problem == null){
            logger.error("problemId {} is invalid", problemId);
        }
        //生成训练计划
        int planId = createPlan(problem, openid);

        List<PracticePlan> practicePlans = Lists.newArrayList();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        //按照日期排序
        Map<Integer, List<ProblemSchedule>> problemScheduleMap = Maps.newLinkedHashMap();
        problemSchedules.stream().forEach(problemSchedule -> {
            if(problemScheduleMap.get(problemSchedule.getDay())==null){
                problemScheduleMap.put(problemSchedule.getDay(), Lists.newArrayList());
            }
            List<ProblemSchedule> problemScheduleList = problemScheduleMap.get(problemSchedule.getDay());
            problemScheduleList.add(problemSchedule);
        });
        //生成知识点
        practicePlans.addAll(createKnowledge(planId, problemScheduleMap));
        //生成理解训练
        practicePlans.addAll(createWarmupPractice(planId, problemScheduleMap));
        //生成应用训练
        practicePlans.addAll(createApplicationPractice(problem, planId, problemScheduleMap));
        //生成小目标
        practicePlans.addAll(createChallengePractice(problem, planId));
        //插入数据库
        practicePlanDao.batchInsert(practicePlans);
        //更新问题状态
        problemPlanDao.updateStatus(openid, problemId, 1);
        //发送欢迎通知
        sendWelcomeMsg(openid, problem);

        return planId;
    }

    private List<PracticePlan> createKnowledge(int planId, Map<Integer, List<ProblemSchedule>> problemScheduleMap) {
        List<PracticePlan> selected = Lists.newArrayList();

        problemScheduleMap.keySet().stream().forEach(day->{
            PracticePlan practicePlan = new PracticePlan();
            //第一天内容自动解锁
            if (day == 1) {
                practicePlan.setUnlocked(true);
            } else {
                practicePlan.setUnlocked(false);
            }
            boolean review = getReview(problemScheduleMap.get(day));
            if(!review) {
                practicePlan.setType(PracticePlan.KNOWLEDGE);
            }else{
                practicePlan.setType(PracticePlan.KNOWLEDGE_REVIEW);
            }
            practicePlan.setPlanId(planId);
            List<Integer> knowledgeId = problemScheduleMap.get(day).stream().
                    map(ProblemSchedule::getKnowledgeId).collect(Collectors.toList());
            practicePlan.setPracticeId(StringUtils.join(knowledgeId, ","));
            practicePlan.setStatus(0);
            practicePlan.setSequence(KNOWLEDGE_SEQUENCE);
            practicePlan.setSeries(day);
//            practicePlan.setSummary(false);
            selected.add(practicePlan);
        });

        return selected;
    }

    private void sendWelcomeMsg(String openid, Problem problem) {
        Assert.notNull(openid, "openid不能为空");
        Assert.notNull(problem, "problem不能为空");
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.courseStartMsg());
        Profile profile = accountService.getProfile(openid, false);
        String first;
        if(profile!=null){
            first = "Hi，"+profile.getNickname()+"，你刚才选择了RISE的专题：\n";
        }else{
            first = "Hi，你刚才选择了RISE的专题：\n";
        }
        int length = problem.getLength();
        String closeDate = DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), length + 6));
        data.put("first",new TemplateMessage.Keyword(first));
        data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2",new TemplateMessage.Keyword("今天——"+closeDate));
        data.put("remark",new TemplateMessage.Keyword("\n这个专题一共"+length+"组训练，记得每天完成1组吧\n" +
                "\n如有疑问请在下方留言，小Q会尽快给你回复的"));
        templateMessageService.sendMessage(templateMessage);
    }

    private List<PracticePlan> createChallengePractice(Problem problem, int planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selected = Lists.newArrayList();
        List<ChallengePractice> practices = challengePracticeDao.loadPractice(problem.getId());

        List<ChallengePractice> challengePractices = selectChallenge(practices);
        challengePractices.stream().forEach(practice->{
            PracticePlan practicePlan = new PracticePlan();
            practicePlan.setUnlocked(true);
            practicePlan.setPlanId(planId);
            practicePlan.setType(PracticePlan.CHALLENGE);
            practicePlan.setPracticeId(practice.getId()+"");
            practicePlan.setStatus(0);
            practicePlan.setSequence(WARMUP_SEQUENCE+APPLICATION_TASK_NUMBER+1);
            practicePlan.setSeries(0);
//            practicePlan.setSummary(false);
            selected.add(practicePlan);
        });

        return selected;
    }

    private List<ChallengePractice> selectChallenge(List<ChallengePractice> practices) {
        Assert.notNull(practices, "小目标不能为空");
        List<ChallengePractice> challengePractices = Lists.newArrayList();

        challengePractices.add(practices.get(0));

        return challengePractices;
    }

    private List<PracticePlan> createApplicationPractice(Problem problem, int planId,
                                                         Map<Integer, List<ProblemSchedule>> problemScheduleMap) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();
        // 问题涉及的知识点
        List<ApplicationPractice> applicationPractices = applicationPracticeDao.loadPractice(problem.getId());
        //去掉删除的题目,按照sequence排序
        applicationPractices = applicationPractices.stream().filter(o1 -> !o1.getDel())
                .sorted((o1, o2) -> o1.getSequence() - o2.getSequence()).collect(Collectors.toList());

        int index = 0;
        for(Integer day:problemScheduleMap.keySet()){
            //当天是否是综合训练
            boolean review = getReview(problemScheduleMap.get(day));
            for(int i=1;i<=APPLICATION_TASK_NUMBER;i++){
                PracticePlan practicePlan = new PracticePlan();
                //第一天内容自动解锁
                if(day==1) {
                    practicePlan.setUnlocked(true);
                }else{
                    practicePlan.setUnlocked(false);
                }
                practicePlan.setPlanId(planId);
                if(!review) {
                    practicePlan.setType(PracticePlan.APPLICATION);
                }else{
                    practicePlan.setType(PracticePlan.APPLICATION_REVIEW);
                }
                practicePlan.setSequence(WARMUP_SEQUENCE + i);
                practicePlan.setSeries(day);
                practicePlan.setStatus(0);
//            practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
//            practicePlan.setSummary(false);
                practicePlan.setPracticeId(applicationPractices.get(index).getId()+"");
                index++;
                selectedPractice.add(practicePlan);
                //综合训练组只有一个应用训练
                if(review){
                    break;
                }
            }

        }

        return selectedPractice;
    }


    private List<PracticePlan> createWarmupPractice(Integer planId,
                                                    Map<Integer, List<ProblemSchedule>> problemScheduleMap) {
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        //构建选择题
        problemScheduleMap.keySet().stream().forEach(day->{
            List<ProblemSchedule> problemSchedules = problemScheduleMap.get(day);
            //当天是否是综合训练
            boolean review = getReview(problemSchedules);
            PracticePlan practicePlan = new PracticePlan();
            //第一天内容自动解锁
            if(day==1) {
                practicePlan.setUnlocked(true);
            }else{
                practicePlan.setUnlocked(false);
            }
            practicePlan.setPlanId(planId);
            if(!review) {
                practicePlan.setType(PracticePlan.WARM_UP);
            }else{
                practicePlan.setType(PracticePlan.WARM_UP_REVIEW);
            }
            practicePlan.setSequence(WARMUP_SEQUENCE);
            practicePlan.setSeries(day);
            practicePlan.setStatus(0);
//            practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
//            practicePlan.setSummary(false);
            List<Integer> practiceIds = Lists.newArrayList();
            for(ProblemSchedule problemSchedule:problemSchedules){
                int knowledgeId = problemSchedule.getKnowledgeId();
                int problemId = problemSchedule.getProblemId();
                List<WarmupPractice> practices = warmupPracticeDao.loadPractice(knowledgeId, problemId);
                practiceIds.addAll(practices.stream()
                        .filter(warmupPractice -> !warmupPractice.getExample() && !warmupPractice.getDel())
                        .map(WarmupPractice::getId)
                        .collect(Collectors.toList()));
            }

            practicePlan.setPracticeId(StringUtils.join(practiceIds, ","));
            selectedPractice.add(practicePlan);
        });

        return selectedPractice;
    }

    private Boolean getReview(List<ProblemSchedule> problemSchedules) {
        if(CollectionUtils.isEmpty(problemSchedules)){
            return false;
        }
        ProblemSchedule problemSchedule = problemSchedules.get(0);

        return Knowledge.isReview(problemSchedule.getKnowledgeId());
    }


    private int createPlan(Problem problem, String openid) {
        Assert.notNull(problem, "problem不能为空");
        Assert.notNull(openid, "openid不能为空");
        int length = problem.getLength();
        ImprovementPlan improvementPlan = new ImprovementPlan();
        improvementPlan.setOpenid(openid);
        improvementPlan.setWarmupComplete(0);
        improvementPlan.setApplicationComplete(0);
        improvementPlan.setProblemId(problem.getId());
        improvementPlan.setPoint(0);
        //初始化状态进行中
        improvementPlan.setStatus(ImprovementPlan.RUNNING);
        //初始化时有一把钥匙
        improvementPlan.setKeycnt(0);
        //总题组=难度训练总天数
        improvementPlan.setTotalSeries(length);
        improvementPlan.setCurrentSeries(1);
        improvementPlan.setStartDate(new Date());
        improvementPlan.setEndDate(DateUtils.afterDays(new Date(), length));
        //最长开放30天
        improvementPlan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
        //总训练数=理解训练+应用训练
//        improvementPlan.setTotal(problem.getWarmupCount()+
//                problem.getApplicationCount());

        return improvementPlanDao.insert(improvementPlan);

    }
}
