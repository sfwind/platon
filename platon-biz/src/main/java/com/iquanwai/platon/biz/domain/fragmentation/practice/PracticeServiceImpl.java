package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
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
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<WarmupPractice> getWarmupPractice(Integer problemId, Integer practicePlanId){
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
                Knowledge knowledge = getKnowledge(problemId, warmupPractice.getKnowledgeId());
                warmupPractice.setKnowledge(knowledge);
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
        warmupResult.setTotal(warmupPracticeList.size());
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
        pointRepo.riseCustomerPoint(openid, point);


        return warmupResult;
    }

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer planId, boolean create) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = challengePracticeDao.load(ChallengePractice.class, id);
        // 查询该用户是否提交
        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, openid);
        if(submit==null && create){
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.CHALLENGE, submitId);
        }
        challengePractice.setContent(submit==null?null:submit.getContent());
        challengePractice.setSubmitId(submit==null?null:submit.getId());
        challengePractice.setSubmitUpdateTime(submit==null?null:DateUtils.parseDateToString(submit.getUpdateTime()));
        challengePractice.setPlanId(submit==null?planId:submit.getPlanId());
        return challengePractice;
    }

    @Override
    public ApplicationPractice getApplicationPractice(Integer id, String openid, Integer planId, boolean create) {
        Assert.notNull(openid, "openid不能为空");
        // 查询该应用练习
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(id, planId, openid);
        if (submit == null && create) {
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openid);
            submit.setPlanId(planId);
            submit.setApplicationId(id);
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.APPLICATION, submitId);
        }
        applicationPractice.setContent(submit==null?null:submit.getContent());
        applicationPractice.setSubmitId(submit==null?null:submit.getId());
        applicationPractice.setSubmitUpdateTime(submit==null?null:DateUtils.parseDateToString(submit.getUpdateTime()));
        applicationPractice.setPlanId(submit==null?planId:submit.getPlanId());

        // 查询点赞数
        applicationPractice.setVoteCount(votedCount(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId()));
        // 查询评论数
        applicationPractice.setCommentCount(commentCount(Constants.CommentModule.APPLICATION, applicationPractice.getSubmitId()));
        // 查询我对它的点赞状态
        HomeworkVote myVote = loadVoteRecord(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId(), openid);
        if (myVote != null && myVote.getDel() == 0) {
            // 点赞中
            applicationPractice.setVoteStatus(1);
        } else {
            applicationPractice.setVoteStatus(0);
        }
        //查询求点赞数
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if(plan!=null && plan.getRequestCommentCount()>0){
            applicationPractice.setRequestComment(true);
        }else{
            applicationPractice.setRequestComment(false);
        }
        return applicationPractice;
    }

    @Override
    public Boolean applicationSubmit(Integer id, String content) {
        Integer type;
        ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, id);
        if (submit == null) {
            logger.error("submitId {} is not existed", id);
            return false;
        }
        int applicationId = submit.getApplicationId();
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);
        if(Knowledge.isReview(applicationPractice.getKnowledgeId())){
            type = PracticePlan.APPLICATION_REVIEW;
        }else{
            type = PracticePlan.APPLICATION;
        }
        boolean result;
        int length = CommonUtils.removeHTMLTag(content).length();
        if(submit.getContent() == null){
            result = applicationSubmitDao.firstAnswer(id, content, length);
        } else {
            result = applicationSubmitDao.answer(id, content, length);
        }
        if (result && submit.getPointStatus() == 0) {
            // 修改应用任务记录
            ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, submit.getPlanId());
            if (plan != null) {
                improvementPlanDao.updateApplicationComplete(plan.getId());
            } else {
                logger.error("ImprovementPlan is not existed,planId:{}", submit.getPlanId());
            }
            logger.info("应用练习加分:{}", id);
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                    submit.getApplicationId(), type);
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
        return result;
    }

    @Override
    public Boolean challengeSubmit(Integer id, String content) {
        ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class, id);
        if (submit == null) {
            logger.error("submitId {} is not existed", id);
            return false;
        }
        boolean result;
        int length = CommonUtils.removeHTMLTag(content).length();
        if (submit.getContent() == null) {
            result = challengeSubmitDao.firstAnswer(id, content, length);
        } else {
            result = challengeSubmitDao.answer(id, content, length);
        }
        if (result && submit.getPointStatus() == 0) {
            // 修改小课任务记录
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

        return result;
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer warmupId) {
        return cacheService.getWarmupPractice(warmupId);
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
                // 小课论坛点赞
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
    public List<Comment> loadComments(Integer moduleId, Integer submitId, Page page){
        page.setTotal(commentDao.commentCount(moduleId, submitId));
        return commentDao.loadComments(moduleId,submitId,page);
    }

    @Override
    public Pair<Boolean,String> comment(Integer moduleId, Integer referId, String openId, String content) {
        boolean isAsst = false;
        Profile profile = accountService.getProfile(openId, false);
        //是否是助教评论
        if(profile!=null){
            isAsst = Role.isAsst(profile.getRole());
        }

        if (moduleId == Constants.CommentModule.CHALLENGE) {
            ChallengeSubmit load = challengeSubmitDao.load(ChallengeSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //自己给自己评论不提醒
            if(load.getOpenid()!=null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/challenge?id=" + load.getChallengeId();
                messageService.sendMessage("评论了我的小目标", load.getOpenid(), openId, url);
            }
        } else if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //更新助教评论状态
            if(isAsst){
                applicationSubmitDao.asstFeedback(load.getId());
                Integer planId = load.getPlanId();
                ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
                if(plan!=null){
                    asstCoachComment(load.getOpenid(), plan.getProblemId());
                }
            }
            //自己给自己评论不提醒
            if(load.getOpenid()!=null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/practice/application?id=" + load.getApplicationId();
                messageService.sendMessage("评论了我的应用练习", load.getOpenid(), openId, url);
            }
        } else if(moduleId == Constants.CommentModule.SUBJECT){
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class,referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(false, "没有该文章");
            }
            //更新助教评论状态
            if(isAsst){
                subjectArticleDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getOpenid() != null && !load.getOpenid().equals(openId)) {
                String url = "/rise/static/message/subject/reply?submitId=" + referId;
                messageService.sendMessage("评论了我的小课分享", load.getOpenid(), openId, url);
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

    private void asstCoachComment(String openId, Integer problemId) {
        AsstCoachComment asstCoachComment =asstCoachCommentDao.loadAsstCoachComment(problemId, openId);
        if(asstCoachComment==null){
            asstCoachComment = new AsstCoachComment();
            asstCoachComment.setCount(1);
            asstCoachComment.setOpenid(openId);
            asstCoachComment.setProblemId(problemId);
            asstCoachCommentDao.insert(asstCoachComment);
        }else{
            asstCoachComment.setCount(asstCoachComment.getCount()+1);
            asstCoachCommentDao.updateCount(asstCoachComment);
        }
    }

    @Override
    public Integer riseArticleViewCount(Integer module, Integer id,Integer type) {
        return fragmentAnalysisDataDao.riseArticleViewCount(module, id, type);
    }

    @Override
    public Integer submitSubjectArticle(SubjectArticle subjectArticle){
        String content = CommonUtils.removeHTMLTag(subjectArticle.getContent());
        subjectArticle.setLength(content.length());
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
    public List<ArticleLabel> updateLabels(Integer moduleId, Integer articleId, List<ArticleLabel> labels){
        List<ArticleLabel> oldLabels = articleLabelDao.loadArticleLabels(moduleId, articleId);
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
        userChoose.forEach(item -> articleLabelDao.insertArticleLabel(moduleId, articleId, item));
        shouldDels.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 1));
        shouldReAdds.forEach(item -> articleLabelDao.updateDelStatus(item.getId(), 0));
        return articleLabelDao.loadArticleActiveLabels(moduleId,articleId);
    }

    @Override
    public List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId){
        return articleLabelDao.loadArticleActiveLabels(moduleId, articleId);
    }

    @Override
    public List<Knowledge> loadKnowledges(Integer practicePlanId, Integer problemId) {
        List<Knowledge> knowledges = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);

        String practiceId = practicePlan.getPracticeId();
        String[] knowledgeIds = practiceId.split(",");
        for(String knowledgeId:knowledgeIds){
            Knowledge knowledge = getKnowledge(problemId, Integer.valueOf(knowledgeId));
            knowledges.add(knowledge);
        }
        return knowledges;
    }

    private Knowledge getKnowledge(Integer problemId, Integer knowledgeId) {
        Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
        WarmupPractice warmupPractice = warmupPracticeDao.loadExample(knowledge.getId(), problemId);
        if(warmupPractice!=null) {
            knowledge.setExample(cacheService.getWarmupPractice(warmupPractice.getId()));
        }
        return knowledge;
    }

    @Override
    public void learnKnowledge(Integer practicePlanId) {
        practicePlanDao.complete(practicePlanId);
    }

    @Override
    public boolean requestComment(Integer submitId, Integer moduleId) {
        if(moduleId.equals(Constants.Module.APPLICATION)){
            ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, submitId);
            if(applicationSubmit.getRequestFeedback()){
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }
            Integer planId = applicationSubmit.getPlanId();
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            if(improvementPlan!=null && improvementPlan.getRequestCommentCount()>0){
                //更新求点评次数
                improvementPlanDao.updateRequestComment(planId, improvementPlan.getRequestCommentCount()-1);
                //求点评
                applicationSubmitDao.requestComment(applicationSubmit.getId());
                return true;
            }
        }else if(moduleId.equals(Constants.Module.SUBJECT)){
            SubjectArticle subjectArticle = subjectArticleDao.load(SubjectArticle.class, submitId);
            if(subjectArticle.getRequestFeedback()){
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }

            Integer problemId = subjectArticle.getProblemId();
            String openid = subjectArticle.getOpenid();
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(openid, problemId);
            if(improvementPlan!=null && improvementPlan.getRequestCommentCount()>0){
                //更新求点评次数
                improvementPlanDao.updateRequestComment(improvementPlan.getId(), improvementPlan.getRequestCommentCount()-1);
                //求点评
                subjectArticleDao.requestComment(subjectArticle.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRequestComment(Integer problemId, String openid) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(openid, problemId);
        if(improvementPlan!=null && improvementPlan.getRequestCommentCount()>0){
            return true;
        }
        return false;
    }

}
