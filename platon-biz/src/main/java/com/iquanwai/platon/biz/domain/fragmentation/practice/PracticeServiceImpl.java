package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String submitUrlPrefix = "/community";

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
        pointRepo.riseCustomerPoint(openid,point);


        return warmupResult;
    }

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);
        // 查询该用户是否提交
        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if(submit==null){
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
        }
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        return challengePractice;
    }

    @Override
    public ApplicationPractice getApplicationPractice(Integer id, String openid, Integer planId) {
        Assert.notNull(openid, "openid不能为空");
        // 查询该应用训练
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(id, planId, openid);
        if (submit == null) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setApplicationId(id);
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
        }
        applicationPractice.setContent(submit.getContent());
        applicationPractice.setSubmitId(submit.getId());
        return applicationPractice;
    }

    @Override
    public Boolean submit(Integer id, String content, Integer type) {
        Assert.notNull(type, "提交类型不能为空");
        boolean result = false;
        if(type.equals(PracticePlan.APPLICATION)) {
            ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, id);
            if (submit == null) {
                logger.error("submitId {} is not existed", id);
                return false;
            }
            result = applicationSubmitDao.answer(id, content);
            if (result && submit.getPointStatus() == 0) {
                // 修改应用任务记录
                ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, submit.getPlanId());
                if (plan != null) {
                    improvementPlanDao.updateApplicationComplete(plan.getId());
                } else {
                    logger.error("ImprovementPlan is not existed,planId:{}", submit.getPlanId());
                }
                logger.info("应用训练加分:{}", id);
                PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                        submit.getApplicationId(), PracticePlan.APPLICATION);
                if (practicePlan != null) {
                    practicePlanDao.complete(practicePlan.getId());
                    Integer point = PointRepoImpl.score.get(applicationPracticeDao.load(ApplicationPractice.class, submit.getApplicationId()).getDifficulty());
                    // 查看难度，加分
                    pointRepo.risePoint(submit.getPlanId(), point);
                    // 修改status
                    applicationSubmitDao.updatePointStatus(id);
                    // 加总分
                    pointRepo.riseCustomerPoint(submit.getOpenid(), point);
                }
            }
        }else if(type.equals(PracticePlan.CHALLENGE)){
            ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class, id);
            if (submit == null) {
                logger.error("submitId {} is not existed", id);
                return false;
            }
            result = challengeSubmitDao.answer(id, content);
            if (result && submit.getPointStatus() == 0) {
                // 修改专题任务记录
                logger.info("专题训练加分:{}", id);
                PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                        submit.getChallengeId(), PracticePlan.CHALLENGE);
                if (practicePlan != null) {
                    practicePlanDao.complete(practicePlan.getId());
                    // 加分
                    pointRepo.risePoint(submit.getPlanId(), ConfigUtils.getChallengeScore());
                    // 修改status
                    challengeSubmitDao.updatePointStatus(id);
                    // 加总分
                    pointRepo.riseCustomerPoint(submit.getOpenid(),ConfigUtils.getChallengeScore());
                }

            }
        }
        return result;
    }
}
