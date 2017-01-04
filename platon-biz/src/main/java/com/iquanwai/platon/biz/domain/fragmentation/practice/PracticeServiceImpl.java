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
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static String shortUrlService = "http://tinyurl.com/api-create.php?url=";

    private final static String submitUrlPrefix = "/home";

    public List<WarmupPractice> getWarmupPractice(Integer planId, Integer series, Integer sequence){
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.getPracticePlan(planId, series, sequence);
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
    public WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer planId, String openid) throws AnswerException{
        WarmupResult warmupResult = new WarmupResult();
        Integer rightNumber = 0;
        Integer point = 0;
        warmupResult.setTotal(GeneratePlanService.WARMUP_TASK_PRACTICE_NUMBER);
        List<Integer> practiceIds = Lists.newArrayList();
        for(WarmupPractice userAnswer:warmupPracticeList){
            practiceIds.add(userAnswer.getId());
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
        PracticePlan practicePlan = practicePlanDao.loadPracticePlan(planId,
                StringUtils.join(practiceIds, ","), 1);
        if(practicePlan!=null && practicePlan.getStatus() == 0) {
            practicePlanDao.complete(practicePlan.getId());
        }
        improvementPlanDao.updateComplete(planId);
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
            improvementPlanDao.updateComplete(planId);
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
            return "";
        }else{
            return "如果今天的知识点，对你的目标/难题有所启发，不妨写几个字吧，慢慢接近小目标！";
        }
    }

    @Override
    public ChallengePractice getChallengePractice(String code) {
        String submitUrl = submitUrlPrefix+code;
        ChallengeSubmit challengeSubmit = challengeSubmitDao.load(submitUrl);
        if(challengeSubmit==null){
            logger.error("code {} is not existed", submitUrl);
            return null;
        }
        return getChallengePractice(challengeSubmit.getChallengeId(), challengeSubmit.getOpenid(),
                challengeSubmit.getPlanId());
    }

    private String generateShortUrl(String url) {
        String requestUrl = shortUrlService;
        try {
            requestUrl = requestUrl + URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException ignored) {

        }
        String shortUrl = restfulHelper.getPlain(requestUrl);
        if(shortUrl.startsWith("http")){
            return shortUrl;
        }else{
            return url;
        }
    }

    @Override
    public Boolean submit(String code, String content){
        String submitUrl = submitUrlPrefix+code;
        ChallengeSubmit challengeSubmit = challengeSubmitDao.load(submitUrl);
        if(challengeSubmit==null){
            logger.error("code {} is not existed", submitUrl);
            return false;
        }
        boolean result = challengeSubmitDao.answer(challengeSubmit.getId(), content);
        if(result) {
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(challengeSubmit.getPlanId(),
                    challengeSubmit.getChallengeId()+"", PracticePlan.CHALLENGE);
            if(practicePlan!=null && practicePlan.getStatus() == 0){
                practicePlanDao.complete(practicePlan.getId());
                improvementPlanDao.updateComplete(challengeSubmit.getPlanId());
//                pointRepo.risePoint(challengeSubmit.getPlanId(), PointRepo.CHALLENGE_PRACTICE_SCORE);
            }
        }
        return result;
    }


}
