package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ChallengePracticeDao challengePracticeDao;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private KnowledgePlanDao knowledgePlanDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String submitUrlPrefix = "/home";

    public List<WarmupPractice> getWarmupPractice(Integer planId, Integer practicePlanId){
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if(practicePlan!=null) {
            String[] practiceIds = practicePlan.getPracticeId().split(",");
            for(String practiceId:practiceIds){
                //设置分值
                WarmupPractice warmupPractice = cacheService.getWarmupPractice(Integer.parseInt(practiceId));
                if(warmupPractice.getDifficulty()==1){
                    warmupPractice.setScore(PointRepo.EASY_SCORE);
                }else if(warmupPractice.getDifficulty()==2){
                    warmupPractice.setScore(PointRepo.NORMAL_SCORE);
                }else if(warmupPractice.getDifficulty()==3){
                    warmupPractice.setScore(PointRepo.HARD_SCORE);
                }
                warmupPractices.add(warmupPractice);
            }
        }
        return warmupPractices;
    }

    @Override
    public List<WarmupSubmit> getWarmupSubmit(Integer planId, List<Integer> questionIds) {
        return warmupSubmitDao.getWarmupSubmit(planId, questionIds);
    }

    @Override
    public WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                             Integer planId, String openid) throws AnswerException{
        WarmupResult warmupResult = new WarmupResult();
        Integer rightNumber = 0;
        Integer point = 0;
        warmupResult.setTotal(GeneratePlanService.WARMUP_TASK_PRACTICE_NUMBER);
        for(WarmupPractice userAnswer:warmupPracticeList){
            List<Integer> userChoice = userAnswer.getChoice();
            WarmupPractice practice = cacheService.getWarmupPractice(userAnswer.getId());
            if(practice==null){
                logger.error("practice {} is not existed", userAnswer.getId());
                continue;
            }
            Pair<Integer,Boolean> ret = pointRepo.warmupScore(practice, userChoice);
            Integer score = ret.getLeft();
            Boolean accurate = ret.getRight();
            if(accurate){
                rightNumber++;
            }
            point +=score;
            WarmupSubmit warmupSubmit = warmupSubmitDao.getWarmupSubmit(planId, practice.getId());
            if(warmupSubmit!=null){
                logger.error("{} has answered practice {}", openid, practice.getId());
                throw new AnswerException();
            }
            //生成提交记录
            warmupSubmit = new WarmupSubmit();
            warmupSubmit.setContent(StringUtils.join(userChoice, ","));
            warmupSubmit.setPlanId(planId);
            warmupSubmit.setQuestionId(practice.getId());
            warmupSubmit.setIsRight(accurate);
            warmupSubmit.setScore(score);
            warmupSubmit.setOpenid(openid);
            warmupSubmitDao.insert(warmupSubmit);
        }
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if(practicePlan!=null && practicePlan.getStatus() == 0) {
            practicePlanDao.complete(practicePlan.getId());
        }
        improvementPlanDao.updateWarmupComplete(planId);
        pointRepo.risePoint(planId, point);
        warmupResult.setRightNumber(rightNumber);
        warmupResult.setPoint(point);

        return warmupResult;
    }

    @Override
    public ApplicationPractice getApplicationPractice(Integer id, Integer planId) {
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        //打开即完成
        PracticePlan practicePlan = practicePlanDao.loadPracticePlan(planId, applicationPractice.getId()+"", PracticePlan.APPLICATION);
        if(practicePlan!=null && practicePlan.getStatus() == 0){
            practicePlanDao.complete(practicePlan.getId());
            improvementPlanDao.updateApplicationComplete(planId);
        }
        return applicationPractice;
    }

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);

//        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        String url = submitUrlPrefix;
        challengePractice.setPcurl(ConfigUtils.pcDomainName()+url);
//        //生成挑战训练提交记录
//        if(submit==null){
//            submit = new ChallengeSubmit();
//            submit.setOpenid(openid);
//            submit.setPlanId(planId);
//            submit.setSubmitUrl(url);
//            submit.setChallengeId(id);
//            challengeSubmitDao.insert(submit);
//        }

        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        //获取挑战训练的文案
        String description = getChallengePracticeContent(improvementPlan);
        challengePractice.setDescription(description);
        return challengePractice;
    }

    private String getChallengePracticeContent(ImprovementPlan improvementPlan) {
        //第一天文案
        if(DateUtils.parseDateToString(improvementPlan.getStartDate()).equals(
                DateUtils.parseDateToString(new Date()))){
            return "今天是训练第1天，给自己定个小目标，或者写下跟本次训练相关的困扰你的难题吧。小目标帮你更积极地学习，也带给你更多成就感！";
        }
        //TODO:最后一天
        else if(DateUtils.parseDateToString(improvementPlan.getCloseDate()).equals(
                DateUtils.parseDateToString(new Date()))){
            String message =  "经过{0}天的学习，你学了{1}个知识点，做了{2}道题，那么当初的小目标实现了吗？难题也有答案了吗？把你的感悟、心得、经历写下来，跟大家交流、共同进步吧！";

            int date = DateUtils.interval(improvementPlan.getStartDate(), improvementPlan.getCloseDate());
            int knowledgeCount = knowledgePlanDao.getKnowledgePlanByPlanId(improvementPlan.getId()).size();
            int practiceNumbers = improvementPlan.getWarmupComplete()*GeneratePlanService.WARMUP_TASK_PRACTICE_NUMBER;
            return MessageFormat.format(message, date, knowledgeCount, practiceNumbers);
        }else{
            return "如果今天的知识点，对你的目标/难题有所启发，不妨写几个字吧，慢慢接近小目标！";
        }
    }
}
