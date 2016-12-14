package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
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
    private ChoiceDao choiceDao;
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, WarmupPractice> warmupPracticeMap = Maps.newConcurrentMap();

    private final static String shortUrlService = "http://tinyurl.com/api-create.php?url=";

    private final static String submitUrlPrefix = "/fragment/c?id=";

    @PostConstruct
    public void initQuestions(){
        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadAll(WarmupPractice.class);
        warmupPractices.stream().forEach(warmupPractice -> {
            warmupPractice.setChoiceList(Lists.newArrayList());
            warmupPracticeMap.put(warmupPractice.getId(), warmupPractice);
        });
        List<Choice> choices = choiceDao.loadAll(Choice.class);
        choices.stream().forEach(choice -> {
            Integer questionId = choice.getQuestionId();
            WarmupPractice warmupPractice = warmupPracticeMap.get(questionId);
            if(warmupPractice!=null){
                warmupPractice.getChoiceList().add(choice);
            }
        });
        logger.info("warmup practice init complete");
    }

    public List<WarmupPractice> getWarmupPractice(List<Integer> idList){
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        idList.forEach(id -> warmupPractices.add(warmupPracticeMap.get(id)));
        return warmupPractices;
    }

    @Override
    public WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer planId, String openid) {
        WarmupResult warmupResult = new WarmupResult();
        Integer rightNumber = 0;
        Integer point = 0;
        for(WarmupPractice userAnswer:warmupPracticeList){
            List<Integer> userChoice = userAnswer.getChoice();
            WarmupPractice practice = warmupPracticeMap.get(userAnswer.getId());
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
            //生成提交记录
            WarmupSubmit warmupSubmit = new WarmupSubmit();
            warmupSubmit.setContent(StringUtils.join(userChoice, ","));
            warmupSubmit.setPlanId(planId);
            warmupSubmit.setQuestionId(practice.getId());
            warmupSubmit.setIsRight(accurate);
            warmupSubmit.setScore(score);
            warmupSubmit.setOpenid(openid);
            warmupSubmitDao.insert(warmupSubmit);

            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(planId, practice.getId(), practice.getType());
            if(practicePlan!=null && practicePlan.getStatus() == 0) {
                practicePlanDao.complete(practicePlan.getId());
            }
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
        PracticePlan practicePlan = practicePlanDao.loadPracticePlan(planId, applicationPractice.getId(), PracticePlan.APPLICATION);
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

        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if(submit==null || submit.getContent()==null) {
            challengePractice.setSubmitted(false);
        }else{
            challengePractice.setSubmitted(true);
        }
        //生成挑战训练提交记录
        if(submit==null){
            String url = submitUrlPrefix+ CommonUtils.randomString(6);
            String shortUrl = generateShortUrl(ConfigUtils.domainName()+url);
            challengePractice.setPcurl(shortUrl);
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setSubmitUrl(url);
            submit.setChallengeId(id);
            if(shortUrl.equals(ConfigUtils.domainName()+url)){
                challengeSubmitDao.insert(submit);
            }else {
                submit.setShortUrl(shortUrl);
                challengeSubmitDao.insert(submit);
            }
        }else{
            if(submit.getSubmitUrl()!=null){
                challengePractice.setPcurl(submit.getShortUrl());
            }
            challengePractice.setContent(submit.getContent());
        }
        return challengePractice;
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
                    challengeSubmit.getChallengeId(), PracticePlan.CHALLENGE);
            if(practicePlan!=null && practicePlan.getStatus() == 0){
                practicePlanDao.complete(practicePlan.getId());
                improvementPlanDao.updateComplete(challengeSubmit.getPlanId());
                pointRepo.risePoint(challengeSubmit.getPlanId(), PointRepo.CHALLENGE_PRACTICE_SCORE);
            }
        }
        return result;
    }


}
