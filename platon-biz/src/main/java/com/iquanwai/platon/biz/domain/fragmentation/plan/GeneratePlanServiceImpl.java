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

        problemSchedules.sort((o1, o2) -> {
            if (!o1.getChapter().equals(o2.getChapter())) {
                return o1.getChapter() - o2.getChapter();
            }

            return o1.getSection() - o2.getSection();
        });
        //生成知识点
        practicePlans.addAll(createKnowledge(planId, problemSchedules));
        //生成巩固练习
        practicePlans.addAll(createWarmupPractice(planId, problemSchedules));
        //生成应用练习
        practicePlans.addAll(createApplicationPractice(problem, planId, problemSchedules));
        //生成小目标
        practicePlans.addAll(createChallengePractice(problem, planId));
        //插入数据库
        practicePlanDao.batchInsert(practicePlans);
        //发送欢迎通知
        sendWelcomeMsg(openid, problem);

        return planId;
    }

    private List<PracticePlan> createKnowledge(int planId, List<ProblemSchedule> problemScheduleList) {
        List<PracticePlan> selected = Lists.newArrayList();

        for(int sequence=1;sequence<=problemScheduleList.size();sequence++){
            PracticePlan practicePlan = new PracticePlan();
            Integer knowledgeId = problemScheduleList.get(sequence - 1).getKnowledgeId();
            //第一节内容自动解锁
            if (sequence == 1) {
                practicePlan.setUnlocked(true);
            } else {
                practicePlan.setUnlocked(false);
            }
            boolean review = Knowledge.isReview(knowledgeId);
            if(!review) {
                practicePlan.setType(PracticePlan.KNOWLEDGE);
            }else{
                practicePlan.setType(PracticePlan.KNOWLEDGE_REVIEW);
            }
            practicePlan.setPlanId(planId);

            practicePlan.setPracticeId(knowledgeId.toString());
            practicePlan.setStatus(0);
            practicePlan.setSequence(KNOWLEDGE_SEQUENCE);
            practicePlan.setSeries(sequence);
//            practicePlan.setSummary(false);
            selected.add(practicePlan);
        }

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
            first = "Hi，"+profile.getNickname()+"，你刚才选择了RISE小课：\n";
        }else{
            first = "Hi，你刚才选择了RISE小课：\n";
        }
        int length = problem.getLength();
        String closeDate = DateUtils.parseDateToStringByCommon(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH - 1));
        data.put("first",new TemplateMessage.Keyword(first));
        data.put("keyword1",new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2",new TemplateMessage.Keyword("今天——"+closeDate));
        data.put("remark",new TemplateMessage.Keyword("\n这个小课一共"+length+"节练习，推荐每天完成1节哦\n" +
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
                                                         List<ProblemSchedule> problemScheduleList) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        for(int sequence=1;sequence<=problemScheduleList.size();sequence++) {
            ProblemSchedule problemSchedule = problemScheduleList.get(sequence - 1);
            Integer knowledgeId = problemSchedule.getKnowledgeId();
            //该节是否是综合练习
            boolean review = Knowledge.isReview(knowledgeId);
//            practicePlan.setSummary(false);
            int problemId = problemSchedule.getProblemId();
            List<ApplicationPractice> practices = applicationPracticeDao.loadPractice(knowledgeId, problemId);
            practices = practices.stream().filter(applicationPractice -> !applicationPractice.getDel()).collect(Collectors.toList());
            //设置应用练习
            for(int i=0; i<practices.size();i++){
                PracticePlan practicePlan = new PracticePlan();
                //第一节内容自动解锁
                if (sequence == 1) {
                    practicePlan.setUnlocked(true);
                } else {
                    practicePlan.setUnlocked(false);
                }
                practicePlan.setPlanId(planId);
                if (!review) {
                    practicePlan.setType(PracticePlan.APPLICATION);
                } else {
                    practicePlan.setType(PracticePlan.APPLICATION_REVIEW);
                }
                practicePlan.setSequence(WARMUP_SEQUENCE+1+i);
                practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
                //设置节序号
                practicePlan.setSeries(sequence);
                practicePlan.setStatus(0);
                practicePlan.setPracticeId(practices.get(i).getId()+"");
                selectedPractice.add(practicePlan);
            }
        }

        return selectedPractice;
    }


    private List<PracticePlan> createWarmupPractice(Integer planId,
                                                    List<ProblemSchedule> problemScheduleList) {
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        //构建选择题
        for(int sequence=1;sequence<=problemScheduleList.size();sequence++) {
            PracticePlan practicePlan = new PracticePlan();
            ProblemSchedule problemSchedule = problemScheduleList.get(sequence - 1);
            Integer knowledgeId = problemSchedule.getKnowledgeId();
            //该节是否是综合练习
            boolean review = Knowledge.isReview(knowledgeId);
            //第一节内容自动解锁
            if (sequence == 1) {
                practicePlan.setUnlocked(true);
            } else {
                practicePlan.setUnlocked(false);
            }
            practicePlan.setPlanId(planId);
            if (!review) {
                practicePlan.setType(PracticePlan.WARM_UP);
            } else {
                practicePlan.setType(PracticePlan.WARM_UP_REVIEW);
            }
            practicePlan.setSequence(WARMUP_SEQUENCE);
            practicePlan.setSeries(sequence);
            practicePlan.setStatus(0);
            practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
//            practicePlan.setSummary(false);
            int problemId = problemSchedule.getProblemId();
            List<WarmupPractice> practices = warmupPracticeDao.loadPractice(knowledgeId, problemId);
            //设置巩固练习的id
            List<Integer> practiceIds = Lists.newArrayList();
            practiceIds.addAll(practices.stream()
                    .filter(warmupPractice -> !warmupPractice.getExample() && !warmupPractice.getDel())
                    .map(WarmupPractice::getId)
                    .collect(Collectors.toList()));

            practicePlan.setPracticeId(StringUtils.join(practiceIds, ","));
            selectedPractice.add(practicePlan);
        }

        return selectedPractice;
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
        //总节数
        improvementPlan.setTotalSeries(length);
        improvementPlan.setCurrentSeries(1);
        improvementPlan.setStartDate(new Date());
        improvementPlan.setEndDate(DateUtils.afterDays(new Date(), length));
        // 查询是否是riseMember
        Profile profile = accountService.getProfile(openid, false);
        improvementPlan.setRequestCommentCount(profile.getRequestCommentCount());
        if(profile.getRiseMember()){
            //最长开放30天
            improvementPlan.setCloseDate(DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
            improvementPlan.setRiseMember(true);
        } else {
            improvementPlan.setCloseDate(DateUtils.parseStringToDate("2099-1-1"));
            improvementPlan.setRiseMember(false);
        }

        return improvementPlanDao.insert(improvementPlan);

    }
}
