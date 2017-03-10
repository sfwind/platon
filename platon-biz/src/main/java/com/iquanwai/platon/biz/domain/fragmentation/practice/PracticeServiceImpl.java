package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.ArticleLabelDao;
import com.iquanwai.platon.biz.dao.fragmentation.ChallengePracticeDao;
import com.iquanwai.platon.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.CommentDao;
import com.iquanwai.platon.biz.dao.fragmentation.FragmentAnalysisDataDao;
import com.iquanwai.platon.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.LabelConfigDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.SubjectArticleDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupSubmitDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.ApplicationSubmit;
import com.iquanwai.platon.biz.po.ArticleLabel;
import com.iquanwai.platon.biz.po.ChallengePractice;
import com.iquanwai.platon.biz.po.ChallengeSubmit;
import com.iquanwai.platon.biz.po.Comment;
import com.iquanwai.platon.biz.po.HomeworkVote;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.LabelConfig;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.SubjectArticle;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private FragmentAnalysisDataDao fragmentAnalysisDataDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private LabelConfigDao labelConfigDao;
    @Autowired
    private ArticleLabelDao articleLabelDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

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
    public ChallengePractice getChallengePractice(Integer id, String openid) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);
        // 查询该用户是否提交
        List<ChallengeSubmit> submitList = challengeSubmitDao.load(id, openid);
        ChallengeSubmit submit;
        if(CollectionUtils.isEmpty(submitList)){
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setChallengeId(id);
            ImprovementPlan improvementPlan = improvementPlanDao.loadRunningPlan(openid);
            submit.setPlanId(improvementPlan.getId());
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.CHALLENGE, submitId);
        }else{
            submit = submitList.get(0);
        }
        challengePractice.setContent(submit.getContent());
        challengePractice.setSubmitId(submit.getId());
        challengePractice.setSubmitUpdateTime(DateUtils.parseDateToString(submit.getUpdateTime()));
        return challengePractice;
    }

    @Override
    public ApplicationPractice getApplicationPractice(Integer id, String openid) {
        Assert.notNull(openid, "openid不能为空");
        // 查询该应用训练
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        // 查询该用户是否提交
        List<ApplicationSubmit> submitList = applicationSubmitDao.load(id, openid);
        ApplicationSubmit submit;
        if (CollectionUtils.isEmpty(submitList)) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openid);
            submit.setApplicationId(id);
            ImprovementPlan improvementPlan = improvementPlanDao.loadRunningPlan(openid);
            submit.setPlanId(improvementPlan.getId());
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.APPLICATION, submitId);
        }else{
            submit = submitList.get(0);
        }
        applicationPractice.setContent(submit.getContent());
        applicationPractice.setSubmitId(submit.getId());
        applicationPractice.setSubmitUpdateTime(DateUtils.parseDateToString(submit.getUpdateTime()));
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
                logger.info("小目标加分:{}", id);
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

    @Override
    public WarmupPractice getWarmupPractice(Integer warmupId) {
        return cacheService.getWarmupPractice(warmupId);
    }

    @Override
    public List<HomeworkVote> loadVoteYesterday(){
        return homeworkVoteDao.loadVoteByDate(DateUtils.beforeDays(new Date(), 1));
    }

    @Override
    public Integer votedCount(Integer type, Integer referencedId) {
        return homeworkVoteDao.votedCount(type, referencedId);
    }

    @Override
    public Integer commentCount(Integer moduleId, Integer referId) {
        return commentDao.commentCount(moduleId, referId);
    }

    @Override
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId) {
        return homeworkVoteDao.loadVoteRecord(type, referId, openId);
    }

    @Override
    public boolean vote(Integer type, Integer referencedId, String openId) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, openId);
        if (vote == null) {
            Integer planId = null;
            String submitOpenId = null;
            if(type == Constants.VoteType.CHALLENGE){
                // 挑战任务点赞
                ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class,referencedId);
                if(submit==null){
                    return false;
                }
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if(type == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class,referencedId);
                if(submit==null){
                    return false;
                }
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
            } else if (type == Constants.VoteType.SUBJECT){
                // 专题区点赞
                SubjectArticle submit = subjectArticleDao.load(SubjectArticle.class,referencedId);
                if(submit==null){
                    return false;
                }
                submitOpenId = submit.getOpenid();
                List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(submitOpenId);
                for(ImprovementPlan plan:improvementPlans){
                    if (plan.getProblemId().equals(submit.getProblemId())) {
                        planId = plan.getId();
                    }
                }
            }
            HomeworkVote homeworkVote = new HomeworkVote();
            homeworkVote.setReferencedId(referencedId);
            homeworkVote.setVoteOpenId(openId);
            homeworkVote.setType(type);
            homeworkVote.setVotedOpenid(submitOpenId);
            homeworkVote.setDevice(Constants.Device.MOBILE);
            homeworkVoteDao.vote(homeworkVote);
            pointRepo.risePoint(planId,ConfigUtils.getVoteScore());
            pointRepo.riseCustomerPoint(submitOpenId,ConfigUtils.getVoteScore());
        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
        return true;
    }

    @Override
    public List<ApplicationSubmit> loadApplicationSubmits(Integer applicationId) {
        return applicationSubmitDao.load(applicationId);
    }

    @Override
    public List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId) {
        return challengeSubmitDao.load(challengeId);
    }

    @Override
    public List<Comment> loadComments(Integer moduleId, Integer submitId, Page page){
        page.setTotal(commentDao.commentCount(moduleId, submitId));
        return commentDao.loadComments(moduleId,submitId,page);
    }

    @Override
    public Pair<Boolean,String> comment(Integer moduleId, Integer referId, String openId, String content) {
        if (moduleId == Constants.CommentModule.CHALLENGE) {
            ChallengeSubmit load = challengeSubmitDao.load(ChallengeSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if(load.getOpenid()!=null && !load.getOpenid().equals(openId)) {
                Profile profile = accountService.getProfile(openId, false);
                if (profile != null) {
                    String url = "/rise/static/practice/challenge?id=" + load.getChallengeId();
                    messageService.sendMessage("评论了我的小目标", load.getOpenid(), openId, url);
                }
            }
        } else if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                Profile profile = accountService.getProfile(openId, false);
                if (profile != null) {
                    String url = "/rise/static/practice/application?id=" + load.getApplicationId();
                    messageService.sendMessage("评论了我的应用训练", load.getOpenid(), openId, url);
                }
            }
        } else if(moduleId == Constants.CommentModule.SUBJECT){
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class,referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                Profile profile = accountService.getProfile(openId, false);
                if (profile != null) {
                    String url = "/rise/static/message/subject/reply?submitId=" + referId;
                    messageService.sendMessage("评论了我的精华分享", load.getOpenid(), openId, url);
                }
            }
        }
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setDevice(Constants.Device.MOBILE);
        commentDao.insert(comment);
        return new MutablePair<>(true,"评论成功");
    }

    @Override
    public Integer riseArticleViewCount(Integer module, Integer id,Integer type) {
        return fragmentAnalysisDataDao.riseArticleViewCount(module, id, type);
    }

    @Override
    public Integer submitSubjectArticle(SubjectArticle subjectArticle){
        Integer submitId = subjectArticle.getId();
        if (subjectArticle.getId()==null){
            // 第一次提交
            submitId = subjectArticleDao.insert(subjectArticle);
            // 生成记录表
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.SUBJECT, submitId);
        } else {
            // 更新之前的
            subjectArticleDao.update(subjectArticle);
        }
        return submitId;
    }

    @Override
    public List<SubjectArticle> loadSubjectArticles(Integer problemId,Page page) {
        page.setTotal(subjectArticleDao.count(problemId));
        return subjectArticleDao.loadArticles(problemId, page);
    }

    @Override
    public SubjectArticle loadSubjectArticle(Integer submitId){
        return subjectArticleDao.load(SubjectArticle.class, submitId);
    }

    @Override
    public List<LabelConfig> loadProblemLabels(Integer problemId){
        return labelConfigDao.loadLabelConfigs(problemId);
    }

    @Override
    public List<ArticleLabel> updateLabels(Integer module, Integer articleId, List<ArticleLabel> labels){
        List<ArticleLabel> oldLabels = articleLabelDao.loadArticleLabels(module, articleId);
        List<ArticleLabel> shouldDels = Lists.newArrayList();
        List<ArticleLabel> shouldReAdds = Lists.newArrayList();
        labels = labels==null?Lists.newArrayList():labels;
        List<Integer> userChoose = labels.stream().map(ArticleLabel::getLabelId).collect(Collectors.toList());
        oldLabels.forEach(item->{
            if(userChoose.contains(item.getLabelId())){
                if(item.getDel()){
                    shouldReAdds.add(item);
                }
            } else {
                shouldDels.add(item);
            }
            userChoose.remove(item.getLabelId());
        });
        userChoose.forEach(item -> articleLabelDao.insertArticleLabel(module, articleId, item));
        shouldDels.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 1));
        shouldReAdds.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 0));
        return articleLabelDao.loadArticleActiveLabels(module,articleId);
    }

    @Override
    public List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId){
        return articleLabelDao.loadArticleActiveLabels(moduleId, articleId);
    }
}
