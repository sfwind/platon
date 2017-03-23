package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
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
        Problem problem = problemDao.load(Problem.class, problemId);
        if(problem == null){
            logger.error("problemId {} is invalid", problemId);
        }
        //生成训练计划
        int planId = createPlan(problem, openid);

        List<PracticePlan> practicePlans = Lists.newArrayList();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        //按照知识点排序
        Map<Integer, List<ProblemSchedule>> problemList = Maps.newHashMap();
        problemSchedules.stream().forEach(problemSchedule -> {
            if(problemList.get(problemSchedule.getKnowledgeId())==null){
                problemList.put(problemSchedule.getKnowledgeId(), Lists.newArrayList());
            }
            List<ProblemSchedule> problemScheduleList = problemList.get(problemSchedule.getKnowledgeId());
            problemScheduleList.add(problemSchedule);
        });
        //生成理解训练
        practicePlans.addAll(createWarmupPractice(problem, planId, problemList));
        //生成应用训练
        practicePlans.addAll(createApplicationPractice(problem, planId, problemList));
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

    public void sendWelcomeMsg(String openid, Problem problem) {
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.courseStartMsg());
        Profile profile = accountService.getProfile(openid, false);
        String first;
        if(profile!=null){
            first = "Hi，"+profile.getNickname()+"，你刚才选择了RISE的专题：";
        }else{
            first = "Hi，你刚才选择了RISE的专题：";
        }
        int length = problem.getLength();
        String closeDate = DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), length + 6));
        data.put("first",new TemplateMessage.Keyword(first));
        data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2",new TemplateMessage.Keyword("今天——"+closeDate));
        data.put("remark",new TemplateMessage.Keyword("这个专题一共"+length+"组训练，记得每天完成1组吧\n" +
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
            practicePlan.setSequence(WARMUP_TASK_NUMBER+APPLICATION_TASK_NUMBER+1);
            practicePlan.setSeries(0);
            practicePlan.setSummary(false);
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
        problemScheduleMap.keySet().stream().forEach(knowledgeId ->{
            List<ProblemSchedule> problemSchedules = problemScheduleMap.get(knowledgeId);
            List<ApplicationPractice> applicationPractices = applicationPracticeDao.loadPractice(knowledgeId, problem.getId());
            //去掉删除的题目,按照sequence排序
            applicationPractices = applicationPractices.stream().filter(o1 -> !o1.getDel())
                    .sorted((o1, o2) -> o1.getSequence() - o2.getSequence()).collect(Collectors.toList());
            //知识点第几次出现
            int index = 0;
            //构建选择题
            for(ProblemSchedule problemSchedule:problemSchedules){
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                practicePlan.setType(PracticePlan.APPLICATION);
                practicePlan.setSequence(problemSchedule.getSequence()+WARMUP_TASK_NUMBER);
                practicePlan.setSeries(problemSchedule.getDay());
                practicePlan.setStatus(0);
                practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
                practicePlan.setSummary(false);
                practicePlan.setPracticeId(applicationPractices.get(index).getId()+"");
                index++;
                selectedPractice.add(practicePlan);
            }
        });

        return selectedPractice;
    }


    private List<PracticePlan> createWarmupPractice(Problem problem, Integer planId,
                                                    Map<Integer, List<ProblemSchedule>> problemScheduleMap) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        problemScheduleMap.keySet().stream().forEach(knowledgeId ->{
            List<ProblemSchedule> problemSchedules = problemScheduleMap.get(knowledgeId);
            List<WarmupPractice> warmupPractices = warmupPracticeDao.loadPractice(knowledgeId, problem.getId());
            //去掉删除的题目,按照sequence排序
            warmupPractices = warmupPractices.stream().filter(o1 -> !o1.getDel()).filter(o1 -> !o1.getExample())
                    .sorted((o1, o2) -> o1.getSequence() - o2.getSequence()).collect(Collectors.toList());
            //知识点第几次出现
            int index = 0;
            //构建选择题
            for(ProblemSchedule problemSchedule:problemSchedules){
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                practicePlan.setType(PracticePlan.WARM_UP);
                practicePlan.setSequence(problemSchedule.getSequence());
                practicePlan.setSeries(problemSchedule.getDay());
                practicePlan.setStatus(0);
                practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
                practicePlan.setSummary(false);
                practicePlan.setPracticeId(getPracticeId(index, warmupPractices));
                index = index + WARMUP_TASK_PRACTICE_NUMBER;
                selectedPractice.add(practicePlan);
            }
        });

        return selectedPractice;
    }

    private String getPracticeId(int index, List<WarmupPractice> warmupPractices) {
        List<Integer> practiceIdList = Lists.newArrayList();
        for(int i=0;i<WARMUP_TASK_PRACTICE_NUMBER; i++){
            practiceIdList.add(warmupPractices.get(index+i).getId());
        }

        return StringUtils.join(practiceIdList, ",");
    }


    private int createPlan(Problem problem, String openid) {
        Assert.notNull(problem, "problem不能为空");
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
        improvementPlan.setKeycnt(1);
        //总题组=难度训练总天数
        improvementPlan.setTotalSeries(length);
        improvementPlan.setCurrentSeries(1);
        improvementPlan.setStartDate(new Date());
        improvementPlan.setEndDate(DateUtils.afterDays(new Date(), length));
        //结束时期后再开放7天
        improvementPlan.setCloseDate(DateUtils.afterDays(new Date(), length + 7));
        //总训练数=理解训练+应用训练
        improvementPlan.setTotal(problem.getWarmupCount()+
                problem.getApplicationCount());

        return improvementPlanDao.insert(improvementPlan);

    }
}
