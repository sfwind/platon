package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.PracticePlanStatusManager;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointManager;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointManagerImpl;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.biz.po.common.UserRole;
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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeServiceImpl implements PracticeService {
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private ApplicationSubmitDraftDao applicationSubmitDraftDao;
    @Autowired
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private PointManager poinManager;
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
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private CommentEvaluationDao commentEvaluationDao;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private PracticePlanStatusManager practicePlanStatusManager;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<WarmupPractice> getWarmupPractices(Integer practicePlanId) {
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan != null) {
            String[] practiceIds = practicePlan.getPracticeId().split(",");
            for (String practiceId : practiceIds) {
                //设置分值
                WarmupPractice warmupPractice = cacheService.getWarmupPractice(Integer.parseInt(practiceId));
                if (warmupPractice.getDifficulty() == 1) {
                    warmupPractice.setScore(PointManager.EASY_SCORE);
                } else if (warmupPractice.getDifficulty() == 2) {
                    warmupPractice.setScore(PointManager.NORMAL_SCORE);
                } else if (warmupPractice.getDifficulty() == 3) {
                    warmupPractice.setScore(PointManager.HARD_SCORE);
                }
                warmupPractices.add(warmupPractice);
            }
        }
        return warmupPractices;
    }

    @Override
    public List<WarmupSubmit> getWarmupSubmit(Integer practicePlanId, List<Integer> questionIds) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            logger.error("{} not existed", practicePlanId);
            return Lists.newArrayList();
        }

        return warmupSubmitDao.getWarmupSubmit(practicePlan.getPlanId(), questionIds);
    }

    @Override
    public WarmupSubmit getWarmupSubmit(Integer profileId, Integer questionId) {
        return warmupSubmitDao.getWarmupSubmit(profileId, questionId);
    }

    @Override
    public WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                             String openid, Integer profileId) throws AnswerException {
        WarmupResult warmupResult = new WarmupResult();
        Integer rightNumber = 0;
        Integer point = 0;
        warmupResult.setTotal(warmupPracticeList.size());
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        Integer planId = practicePlan.getPlanId();
        for (WarmupPractice userAnswer : warmupPracticeList) {
            List<Integer> userChoice = userAnswer.getChoice();
            WarmupPractice practice = cacheService.getWarmupPractice(userAnswer.getId());
            if (practice == null) {
                logger.error("practice {} is not existed", userAnswer.getId());
                continue;
            }
            Pair<Integer, Boolean> ret = poinManager.warmupScore(practice, userChoice);
            Integer score = ret.getLeft();
            Boolean accurate = ret.getRight();
            if (accurate) {
                rightNumber++;
            }
            point += score;
            WarmupSubmit warmupSubmit = warmupSubmitDao.getWarmupSubmit(planId, practice.getId(), profileId);
            if (warmupSubmit != null) {
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
            warmupSubmit.setProfileId(profileId);
            warmupSubmitDao.insert(warmupSubmit);
        }
        if (practicePlan.getStatus() == 0) {
            // practicePlanDao.complete(practicePlan.getId());
            practicePlanStatusManager.completePracticePlan(profileId, practicePlanId);
            certificateService.generateSingleFullAttendanceCoupon(practicePlanId);
        }
        improvementPlanDao.updateWarmupComplete(planId);
        poinManager.risePoint(planId, point);
        warmupResult.setRightNumber(rightNumber);
        warmupResult.setPoint(point);

        return warmupResult;
    }

    @Override
    public ChallengePractice getChallengePractice(Integer id, String openid, Integer profileId, Integer planId, boolean create) {
        Assert.notNull(openid, "openid不能为空");
        ChallengePractice challengePractice = new ChallengePractice(id);
        // 查询该用户是否提交
        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, profileId);
        if (submit == null && create) {
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setOpenid(openid);
            submit.setProfileId(profileId);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.CHALLENGE, submitId);
        }
        if (submit != null && submit.getContent() != null) {
            String content = CommonUtils.replaceHttpsDomainName(submit.getContent());
            if (!content.equals(submit.getContent())) {
                submit.setContent(content);
                challengeSubmitDao.updateContent(submit.getId(), content);
            }
        }
        challengePractice.setContent(submit == null ? null : submit.getContent());
        challengePractice.setSubmitId(submit == null ? null : submit.getId());
        challengePractice.setSubmitUpdateTime(submit == null ? null : DateUtils.parseDateToString(submit.getUpdateTime()));
        challengePractice.setPlanId(submit == null ? planId : submit.getPlanId());
        return challengePractice;
    }

    @Override
    public Pair<ApplicationPractice, Boolean> getApplicationPractice(Integer id, String openid, Integer profileId, Integer planId, boolean create) {
        Assert.notNull(openid, "openid不能为空");
        Boolean isNewApplication = false; // 该 ApplicationPractice 是否是新生成的
        // 查询该应用练习
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(id, planId, profileId);
        if (submit == null && create) {
            isNewApplication = true; // 该 ApplicationPractice 为新创建
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setOpenid(openid);
            submit.setProfileId(profileId);
            submit.setPlanId(planId);
            submit.setApplicationId(id);
            submit.setProblemId(applicationPractice.getProblemId());
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
            fragmentAnalysisDataDao.insertArticleViewInfo(Constants.ViewInfo.Module.APPLICATION, submitId);
        }

        Map<Integer, Integer> scoreMap = ConfigUtils.getWorkScoreMap();
        // 设置每道题的得分
        applicationPractice.setApplicationScore(scoreMap.get(applicationPractice.getDifficulty()));

        // 未提交过内容，查询草稿表 ApplicationSubmitDraft
        ApplicationSubmitDraft applicationSubmitDraft = applicationSubmitDraftDao.loadApplicationSubmitDraft(profileId, id, planId);
        if (submit == null) {
            if (applicationSubmitDraft != null) {
                applicationPractice.setDraftId(applicationSubmitDraft.getId());
                applicationPractice.setDraft(applicationSubmitDraft.getContent());
            }
            // 用户还未提交，必然未同步
            applicationPractice.setIsSynchronized(false);
        } else {
            if (applicationSubmitDraft != null) {
                applicationPractice.setDraftId(applicationSubmitDraft.getId());
                applicationPractice.setDraft(applicationSubmitDraft.getContent());
                if (submit.getContent() == null) {
                    applicationPractice.setIsSynchronized(false);
                } else if (submit.getLastModifiedTime() == null) {
                    applicationPractice.setIsSynchronized(true);
                } else {
                    applicationPractice.setIsSynchronized(submit.getContent().equals(applicationSubmitDraft.getContent())
                            || submit.getLastModifiedTime().compareTo(applicationSubmitDraft.getUpdateTime()) >= 0);
                }
                applicationPractice.setOverrideLocalStorage(applicationSubmitDraft.getPriority() > 0);
            } else {
                applicationPractice.setIsSynchronized(true);
            }
        }

        if (submit != null) {
            applicationPractice.setContent(submit.getContent());
            applicationPractice.setSubmitId(submit.getId());
            applicationPractice.setSubmitUpdateTime(DateUtils.parseDateToString(submit.getPublishTime()));
            applicationPractice.setFeedback(submit.getFeedback());

            planId = submit.getPlanId();
        }
        applicationPractice.setPlanId(planId);

        // 查询点赞数
        applicationPractice.setVoteCount(votedCount(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId()));
        // 查询评论数
        applicationPractice.setCommentCount(commentCount(Constants.CommentModule.APPLICATION, applicationPractice.getSubmitId()));
        // 查询我对它的点赞状态
        HomeworkVote myVote = loadVoteRecord(Constants.VoteType.APPLICATION, applicationPractice.getSubmitId(), profileId);
        if (myVote != null && myVote.getDel() == 0) {
            // 点赞中
            applicationPractice.setVoteStatus(1);
        } else {
            applicationPractice.setVoteStatus(0);
        }
        //查询求点赞数
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (submit != null) {
            applicationPractice.setRequest(submit.getRequestFeedback());
        }
        if (plan != null && plan.getRequestCommentCount() > 0) {
            applicationPractice.setRequestCommentCount(plan.getRequestCommentCount());
        } else {
            RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
            if (riseMember != null) {
                if (riseMember.getMemberTypeId().equals(RiseMember.ELITE) || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                    applicationPractice.setRequestCommentCount(0);
                }
            }
        }

        // 检查该道题是否是简单应用题还是复杂应用题
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        PracticePlan targetPracticePlan = practicePlans.stream()
                .filter(planItem -> planItem.getPracticeId().equals(id.toString())).findAny().orElse(null);
        applicationPractice.setIsBaseApplication(targetPracticePlan.getSequence() == 3);

        return new MutablePair<>(applicationPractice, isNewApplication);
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
        if (Knowledge.isReview(applicationPractice.getKnowledgeId())) {
            type = PracticePlan.APPLICATION_REVIEW;
        } else {
            type = PracticePlan.APPLICATION;
        }
        boolean result;
        int length = CommonUtils.removeHTMLTag(content).length();
        if (submit.getContent() == null) {
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
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(), submit.getApplicationId(), type);
            if (practicePlan != null) {
                practicePlanStatusManager.completePracticePlan(submit.getProfileId(), practicePlan.getId());
                certificateService.generateSingleFullAttendanceCoupon(practicePlan.getId());
                Integer point = PointManagerImpl.score.get(applicationPracticeDao.load(ApplicationPractice.class, submit.getApplicationId()).getDifficulty());
                // 查看难度，加分
                poinManager.risePoint(submit.getPlanId(), point);
                // 修改status
                applicationSubmitDao.updatePointStatus(id);
            }
        }
        return result;
    }

    @Override
    public Integer insertApplicationSubmitDraft(Integer profileId, Integer applicationId, Integer planId, String content) {
        ApplicationSubmitDraft submitDraft = applicationSubmitDraftDao.loadApplicationSubmitDraft(applicationId, planId);
        if (submitDraft == null) {
            // 用户第一次提交，或者历史数据，没有草稿存储，新建 draft，并且初始化数据
            ApplicationSubmitDraft tempDraft = new ApplicationSubmitDraft();
            Profile profile = accountService.getProfile(profileId);
            tempDraft.setOpenid(profile.getOpenid());
            tempDraft.setProfileId(profileId);
            tempDraft.setApplicationId(applicationId);
            tempDraft.setPlanId(planId);
            tempDraft.setContent(content);
            tempDraft.setLength(content != null ? content.length() : null);
            return applicationSubmitDraftDao.insertSubmitDraft(tempDraft);
        } else {
            // 用户存在历史草稿，直接更新
            return applicationSubmitDraftDao.updateApplicationSubmitDraft(submitDraft.getId(), content);
        }
        // ApplicationSubmitDraft applicationSubmitDraft = applicationSubmitDraftDao.loadApplicationSubmitDraft(profileId, applicationId, planId);
        // if (applicationSubmitDraft != null) {
        //     return applicationSubmitDraft.getId();
        // } else {
        //     ApplicationSubmitDraft draft = new ApplicationSubmitDraft();
        //     draft.setOpenid(openId);
        //     draft.setProfileId(profileId);
        //     draft.setApplicationId(applicationId);
        //     draft.setPlanId(planId);
        //     return applicationSubmitDraftDao.insertSubmitDraft(draft);
        // }
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
                practicePlanStatusManager.completePracticePlan(submit.getProfileId(), practicePlan.getId());
                // 加分
                poinManager.risePoint(submit.getPlanId(), ConfigUtils.getChallengeScore());
                // 修改status
                challengeSubmitDao.updatePointStatus(id);
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
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, Integer profileId) {
        return homeworkVoteDao.loadVoteRecord(type, referId, profileId);
    }

    @Override
    public boolean vote(Integer type, Integer referencedId, Integer profileId, String openid, Integer device) {
        if (device == null) {
            device = Constants.Device.MOBILE;
        }
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(type, referencedId, profileId);
        if (vote == null) {
            Integer planId = null;
            String submitOpenId = null;
            Integer submitProfileId = null;
            if (type == Constants.VoteType.CHALLENGE) {
                // 挑战任务点赞
                ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class, referencedId);
                if (submit == null) {
                    return false;
                }
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
                submitProfileId = submit.getProfileId();
            } else if (type == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, referencedId);
                if (submit == null) {
                    return false;
                }
                planId = submit.getPlanId();
                submitOpenId = submit.getOpenid();
                submitProfileId = submit.getProfileId();
            } else if (type == Constants.VoteType.SUBJECT) {
                // 小课论坛点赞
                SubjectArticle submit = subjectArticleDao.load(SubjectArticle.class, referencedId);
                if (submit == null) {
                    return false;
                }
                submitOpenId = submit.getOpenid();
                submitProfileId = submit.getProfileId();
                List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
                for (ImprovementPlan plan : improvementPlans) {
                    if (plan.getProblemId().equals(submit.getProblemId())) {
                        planId = plan.getId();
                    }
                }
            }
            HomeworkVote homeworkVote = new HomeworkVote();
            homeworkVote.setReferencedId(referencedId);
            homeworkVote.setVoteOpenId(openid);
            homeworkVote.setVoteProfileId(profileId);
            homeworkVote.setType(type);
            homeworkVote.setVotedOpenid(submitOpenId);
            homeworkVote.setVotedProfileId(submitProfileId);
            homeworkVote.setDevice(device);
            homeworkVoteDao.vote(homeworkVote);
            poinManager.risePoint(planId, ConfigUtils.getVoteScore());
        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
        return true;
    }

    @Override
    public List<ApplicationSubmit> loadApplicationSubmits(Integer applicationId) {
        return applicationSubmitDao.load(applicationId).stream().map(applicationSubmit -> {
            String content = CommonUtils.replaceHttpsDomainName(applicationSubmit.getContent());
            if (!content.equals(applicationSubmit.getContent())) {
                applicationSubmitDao.updateContent(applicationSubmit.getId(), content);
                applicationSubmit.setContent(content);
            }
            return applicationSubmit;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationSubmit> loadAllOtherApplicationSubmits(Integer applicationId) {
        List<ApplicationSubmit> submits = loadApplicationSubmits(applicationId); // 所有应用练习
        List<Integer> submitsIdList = submits.stream().map(ApplicationSubmit::getId).collect(Collectors.toList());
        // applicationSubmit Id 序列 -> votes
        List<HomeworkVote> votes = homeworkVoteDao.getHomeworkVotesByIds(submitsIdList); // 所有应用练习的点赞
        List<Integer> referenceIds = votes.stream().map(HomeworkVote::getReferencedId).collect(Collectors.toList()); // vote 的 referenceId 集合
        List<Comment> comments = commentDao.loadAllCommentsByReferenceIds(submitsIdList); // 所有评论
        List<UserRole> userRoles = userRoleDao.loadAll(UserRole.class); // 所有用户角色信息
        // 已被点评
        List<ApplicationSubmit> feedbackSubmits = Lists.newArrayList();
        // 未被点评，有点赞
        List<ApplicationSubmit> votedSubmits = Lists.newArrayList();
        // 未被点评、无点赞
        List<ApplicationSubmit> restSubmits = Lists.newArrayList();
        submits.forEach(submit -> {
            if (submit.getFeedback()) {
                feedbackSubmits.add(submit);
            } else if (referenceIds.contains(submit.getId())) {
                votedSubmits.add(submit);
            } else {
                restSubmits.add(submit);
            }
        });
        // 已被点评作业内部排序：最新被点评到最旧被点评，点评时间越新该条作业越靠前
        feedbackSubmits.sort((left, right) -> {
            Date leftFeedbackDate = new Date(0);
            Date rightFeedbackDate = new Date(0);
            for (Comment comment : comments) {
                Integer commentProfileId = comment.getCommentProfileId();
                Date commentAddTime = comment.getAddTime();
                if (left.getId() == comment.getReferencedId()) {
                    for (UserRole userRole : userRoles) {
                        if (userRole.getProfileId().equals(commentProfileId) && Role.isAsst(userRole.getRoleId())) {
                            leftFeedbackDate = commentAddTime.compareTo(leftFeedbackDate) > 0 ? comment.getAddTime() : leftFeedbackDate;
                        }
                    }
                } else if (right.getId() == comment.getReferencedId()) {
                    for (UserRole userRole : userRoles) {
                        if (userRole.getProfileId().equals(commentProfileId) && Role.isAsst(userRole.getRoleId())) {
                            rightFeedbackDate = commentAddTime.compareTo(rightFeedbackDate) > 0 ? comment.getAddTime() : leftFeedbackDate;
                        }
                    }
                }
            }
            return rightFeedbackDate.compareTo(leftFeedbackDate);
        });
        // 有点赞数作业内部排序：1. 根据点评数由多至少排序 2. 点评数一样，根据作业提交日期逆序排列，提交日期越新，越靠前。
        votedSubmits.sort((left, right) -> {
            int leftVoteCnt = 0;
            int rightVoteCnt = 0;
            for (Integer id : referenceIds) {
                if (id.equals(left.getId())) {
                    leftVoteCnt++;
                } else if (id.equals(right.getId())) {
                    rightVoteCnt++;
                }
            }
            if (leftVoteCnt == rightVoteCnt) {
                return right.getPublishTime().compareTo(left.getPublishTime());
            } else {
                return rightVoteCnt - leftVoteCnt;
            }
        });
        // 剩余无教练点评，无点赞作业内部排序：根据作业提交日期逆序排列，提交日期越新，越靠前。
        restSubmits.sort(Comparator.comparing(ApplicationSubmit::getPublishTime).reversed());
        List<ApplicationSubmit> applicationSubmits = Lists.newArrayList();
        applicationSubmits.addAll(feedbackSubmits);
        applicationSubmits.addAll(votedSubmits);
        applicationSubmits.addAll(restSubmits);
        return applicationSubmits;
    }

    @Override
    public List<Comment> loadComments(Integer moduleId, Integer submitId, Page page) {
        page.setTotal(commentDao.commentCount(moduleId, submitId));
        return commentDao.loadComments(moduleId, submitId, page);
    }

    @Override
    public Comment loadApplicationReplyComment(Integer commentId) {
        return commentDao.load(Comment.class, commentId);
    }

    @Override
    public Pair<Integer, String> replyComment(Integer moduleId, Integer referId, Integer profileId,
                                              String openId, String content, Integer repliedId, Integer device) {
        if (device == null) {
            device = Constants.Device.MOBILE;
        }
        // 查看该评论是否为助教回复
        boolean isAsst = false;
        Profile profile = accountService.getProfile(profileId);
        if (profile != null) {
            isAsst = Role.isAsst(profile.getRole());
        }

        if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            // 是助教评论
            if (isAsst) {
                // 将此条评论所对应的 ApplicationSubmit 置为已被助教评论
                applicationSubmitDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProfileId(), load.getProblemId());
            }
        } else if (moduleId == Constants.CommentModule.SUBJECT) {
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            // 是助教评论
            if (isAsst) {
                // 将此条评论所对应的 SubjectArticle 置为已被助教评论
                subjectArticleDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProfileId(), load.getProblemId());
            }
        }

        //被回复的评论
        Comment repliedComment = commentDao.load(Comment.class, repliedId);
        if (repliedComment == null) {
            return new MutablePair<>(-1, "评论失败");
        }

        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentProfileId(profileId);
        comment.setCommentOpenId(openId);
        comment.setRepliedProfileId(repliedComment.getCommentProfileId());
        comment.setRepliedComment(repliedComment.getContent());
        comment.setRepliedDel(0);
        comment.setRepliedOpenId(repliedComment.getCommentOpenId());
        comment.setRepliedId(repliedId);
        comment.setDevice(device);
        int id = commentDao.insert(comment);
        //评论自己的评论,不发通知
        if (!repliedComment.getCommentProfileId().equals(profileId)) {
            String msg = "";
            StringBuilder url = new StringBuilder("/rise/static/message/comment/reply");
            if (moduleId == 2) {
                msg = "评论了我的应用题";
            } else if (moduleId == 3) {
                msg = "评论了我的小课分享";
            }
            url = url.append("?moduleId=").append(moduleId).append("&submitId=").append(referId).append("&commentId=").append(id);
            messageService.sendMessage(msg, repliedComment.getCommentProfileId().toString(), profileId.toString(), url.toString());
        }
        return new MutablePair<>(id, "评论成功");
    }

    @Override
    public Pair<Integer, String> comment(Integer moduleId, Integer referId, Integer profileId, String openId, String content, Integer device) {
        if (device == null) {
            device = Constants.Device.MOBILE;
        }
        //先插入评论
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
        comment.setCommentOpenId(openId);
        comment.setCommentProfileId(profileId);
        comment.setDevice(device);
        int id = commentDao.insert(comment);

        boolean isAsst = false;
        Profile profile = accountService.getProfile(profileId);
        //是否是助教评论
        if (profile != null) {
            isAsst = Role.isAsst(profile.getRole());
        }
        if (moduleId == Constants.CommentModule.APPLICATION) {
            ApplicationSubmit load = applicationSubmitDao.load(ApplicationSubmit.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //更新助教评论状态
            if (isAsst) {
                applicationSubmitDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProfileId(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getProfileId() != null && !load.getProfileId().equals(profileId)) {
                String url = "/rise/static/message/application/reply?submitId=" + referId + "&commentId=" + id;
                messageService.sendMessage("评论了我的应用题", load.getProfileId().toString(), profileId.toString(), url);
            }
        } else if (moduleId == Constants.CommentModule.SUBJECT) {
            SubjectArticle load = subjectArticleDao.load(SubjectArticle.class, referId);
            if (load == null) {
                logger.error("评论模块:{} 失败，没有文章id:{}，评论内容:{}", moduleId, referId, content);
                return new MutablePair<>(-1, "没有该文章");
            }
            //更新助教评论状态
            if (isAsst) {
                subjectArticleDao.asstFeedback(load.getId());
                asstCoachComment(load.getOpenid(), load.getProfileId(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getProfileId() != null && !load.getProfileId().equals(profileId)) {
                String url = "/rise/static/message/subject/reply?submitId=" + referId;
                messageService.sendMessage("评论了我的小课分享", load.getProfileId().toString(), profileId.toString(), url);
            }
        }
        return new MutablePair<>(id, "评论成功");
    }

    private void asstCoachComment(String openid, Integer profileId, Integer problemId) {
        AsstCoachComment asstCoachComment = asstCoachCommentDao.loadAsstCoachComment(problemId, profileId);
        if (asstCoachComment == null) {
            asstCoachComment = new AsstCoachComment();
            asstCoachComment.setCount(1);
            asstCoachComment.setOpenid(openid);
            asstCoachComment.setProblemId(problemId);
            asstCoachComment.setProfileId(profileId);
            asstCoachCommentDao.insert(asstCoachComment);
        } else {
            asstCoachComment.setCount(asstCoachComment.getCount() + 1);
            asstCoachCommentDao.updateCount(asstCoachComment);
        }
    }

    @Override
    public void initCommentEvaluation(Integer submitId, Integer commentId) {
        Comment comment = commentDao.load(Comment.class, commentId);
        if (comment != null && comment.getCommentProfileId() != null) {
            // 对于一道应用题，只有一次评价
            List<Comment> comments = commentDao.loadCommentsByProfileId(submitId, comment.getCommentProfileId());
            if (comments.size() == 1) {
                commentEvaluationDao.initCommentEvaluation(submitId, commentId);
            }
        }
    }

    @Override
    public Boolean loadEvaluated(Integer commentId) {
        CommentEvaluation evaluation = commentEvaluationDao.loadByCommentId(commentId);
        // 数据库如果没有这条评论记录，默认为已评
        if (evaluation == null) {
            return true;
        }
        Integer evaluated = evaluation.getEvaluated();
        if (evaluated != null) {
            switch (evaluated) {
                case 0:
                    return false;
                case 1:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void updateEvaluation(Integer commentId, Integer useful, String reason) {
        if (StringUtils.isEmpty(reason)) {
            commentEvaluationDao.updateEvaluation(commentId, useful);
        } else {
            commentEvaluationDao.updateEvaluation(commentId, useful, reason);
        }
    }

    @Override
    public List<CommentEvaluation> loadUnEvaluatedCommentEvaluationBySubmitId(Integer profileId, Integer submitId) {
        ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, submitId);
        if (applicationSubmit == null || !profileId.equals(applicationSubmit.getProfileId())) {
            return Lists.newArrayList();
        }

        List<CommentEvaluation> evaluations = commentEvaluationDao.loadUnEvaluatedCommentEvaluationBySubmitId(submitId);
        List<Integer> commentIds = evaluations.stream().map(CommentEvaluation::getCommentId).collect(Collectors.toList());
        List<Comment> comments = commentDao.loadAllCommentsByIds(commentIds);
        Map<Integer, Comment> commentMap = comments.stream().collect(Collectors.toMap(Comment::getId, comment -> comment));
        List<Integer> profileIds = comments.stream().map(Comment::getCommentProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        for (CommentEvaluation evaluation : evaluations) {
            Comment comment = commentMap.get(evaluation.getCommentId());
            if (comment == null) {
                continue;
            }
            Profile profile = profileMap.get(comment.getCommentProfileId());
            if (profile == null) {
                continue;
            }
            evaluation.setNickName(profile.getNickname());
        }
        return evaluations;
    }

    @Override
    public List<CommentEvaluation> loadUnEvaluatedCommentEvaluationByCommentId(Integer commentId) {
        List<CommentEvaluation> commentEvaluations = Lists.newArrayList();
        CommentEvaluation evaluation = commentEvaluationDao.loadByCommentId(commentId);
        if (evaluation != null && evaluation.getEvaluated() == 0) {
            Comment comment = commentDao.loadByCommentId(evaluation.getCommentId());
            if (comment != null && comment.getCommentProfileId() != null) {
                Profile profile = accountService.getProfile(comment.getCommentProfileId());
                if (profile != null) {
                    evaluation.setNickName(profile.getNickname());
                }
            }
            commentEvaluations.add(evaluation);
        } else {
            return commentEvaluations;
        }
        return commentEvaluations;
    }

    @Override
    public Integer riseArticleViewCount(Integer module, Integer id, Integer type) {
        return fragmentAnalysisDataDao.riseArticleViewCount(module, id, type);
    }

    @Override
    public Integer submitSubjectArticle(SubjectArticle subjectArticle) {
        String content = CommonUtils.removeHTMLTag(subjectArticle.getContent());
        subjectArticle.setLength(content.length());
        Integer submitId = subjectArticle.getId();
        if (subjectArticle.getId() == null) {
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
    public List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page) {
        page.setTotal(subjectArticleDao.count(problemId));
        return subjectArticleDao.loadArticles(problemId, page).stream().map(subjectArticle -> {
            String content = CommonUtils.replaceHttpsDomainName(subjectArticle.getContent());
            if (!content.equals(subjectArticle.getContent())) {
                subjectArticleDao.updateContent(subjectArticle.getId(), content);
                subjectArticle.setContent(content);
            }
            return subjectArticle;
        }).collect(Collectors.toList());
    }

    @Override
    public SubjectArticle loadSubjectArticle(Integer submitId) {
        return subjectArticleDao.load(SubjectArticle.class, submitId);
    }

    @Override
    public List<LabelConfig> loadProblemLabels(Integer problemId) {
        return labelConfigDao.loadLabelConfigs(problemId);
    }

    @Override
    public List<ArticleLabel> updateLabels(Integer moduleId, Integer articleId, List<ArticleLabel> labels) {
        List<ArticleLabel> oldLabels = articleLabelDao.loadArticleLabels(moduleId, articleId);
        List<ArticleLabel> shouldDels = Lists.newArrayList();
        List<ArticleLabel> shouldReAdds = Lists.newArrayList();
        labels = labels == null ? Lists.newArrayList() : labels;
        List<Integer> userChoose = labels.stream().map(ArticleLabel::getLabelId).collect(Collectors.toList());
        oldLabels.forEach(item -> {
            if (userChoose.contains(item.getLabelId())) {
                if (item.getDel()) {
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
        return articleLabelDao.loadArticleActiveLabels(moduleId, articleId);
    }

    @Override
    public List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId) {
        return articleLabelDao.loadArticleActiveLabels(moduleId, articleId);
    }

    @Override
    public List<Knowledge> loadKnowledges(Integer practicePlanId) {
        List<Knowledge> knowledges = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);

        String practiceId = practicePlan.getPracticeId();
        String[] knowledgeIds = practiceId.split(",");
        for (String knowledgeId : knowledgeIds) {
            Knowledge knowledge = getKnowledge(Integer.valueOf(knowledgeId));
            knowledges.add(knowledge);
        }
        return knowledges;
    }

    @Override
    public Knowledge loadKnowledge(Integer knowledgeId) {
        return cacheService.getKnowledge(knowledgeId);
    }

    private Knowledge getKnowledge(Integer knowledgeId) {
        Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
        WarmupPractice warmupPractice = warmupPracticeDao.loadExample(knowledge.getId());
        if (warmupPractice != null) {
            knowledge.setExample(cacheService.getWarmupPractice(warmupPractice.getId()));
        }
        return knowledge;
    }

    @Override
    public void learnKnowledge(Integer profileId, Integer practicePlanId) {
        practicePlanStatusManager.completePracticePlan(profileId, practicePlanId);
        certificateService.generateSingleFullAttendanceCoupon(practicePlanId);
    }

    @Override
    public boolean requestComment(Integer submitId, Integer moduleId, Integer profileId) {
        if (moduleId.equals(Constants.Module.APPLICATION)) {
            ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, submitId);
            if (applicationSubmit.getRequestFeedback()) {
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }
            Integer planId = applicationSubmit.getPlanId();
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            if (improvementPlan != null && improvementPlan.getRequestCommentCount() > 0) {
                //更新求点评次数
                improvementPlanDao.updateRequestComment(planId, improvementPlan.getRequestCommentCount() - 1);
                //求点评
                applicationSubmitDao.requestComment(applicationSubmit.getId());
                return true;
            }
        } else if (moduleId.equals(Constants.Module.SUBJECT)) {
            SubjectArticle subjectArticle = subjectArticleDao.load(SubjectArticle.class, submitId);
            if (subjectArticle.getRequestFeedback()) {
                logger.warn("{} 已经是求点评状态", submitId);
                return true;
            }

            Integer problemId = subjectArticle.getProblemId();
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
            if (improvementPlan != null && improvementPlan.getRequestCommentCount() > 0) {
                //更新求点评次数
                improvementPlanDao.updateRequestComment(improvementPlan.getId(), improvementPlan.getRequestCommentCount() - 1);
                //求点评
                subjectArticleDao.requestComment(subjectArticle.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer hasRequestComment(Integer problemId, Integer profileId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        if (improvementPlan != null && improvementPlan.getRequestCommentCount() > 0) {
            return improvementPlan.getRequestCommentCount();
        }
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember != null) {
            if (riseMember.getMemberTypeId().equals(RiseMember.ELITE)
                    || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                return 0;
            }
        }
        return null;
    }

    @Override
    public void deleteComment(Integer commentId) {
        commentDao.deleteComment(commentId);
    }

    @Override
    public ApplicationSubmit loadApplicationSubmitByApplicationId(Integer applicationId, Integer profileId) {
        return applicationSubmitDao.load(applicationId, profileId);
    }

    @Override
    public ApplicationSubmit loadApplocationSubmitById(Integer applicationSubmitId) {
        return applicationSubmitDao.load(ApplicationSubmit.class, applicationSubmitId);
    }

    @Override
    public ApplicationSubmit getApplicationSubmit(Integer id, Integer readProfileId) {
        ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, id);
        if (applicationSubmit != null) {
            Integer applicationId = applicationSubmit.getApplicationId();
            ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationId);
            applicationSubmit.setTopic(applicationPractice.getTopic());
            //点赞状态
            applicationSubmit.setVoteCount(homeworkVoteDao.votedCount(Constants.CommentModule.APPLICATION, id));
            applicationSubmit.setVoteStatus(homeworkVoteDao.loadVoteRecord(Constants.CommentModule.APPLICATION, id,
                    readProfileId) != null);
        }
        return applicationSubmit;
    }

    @Override
    public Comment loadComment(Integer commentId) {
        return commentDao.load(Comment.class, commentId);
    }

    @Override
    public Boolean isModifiedAfterFeedback(Integer submitId, Integer commentProfileId, Date commentAddDate) {
        UserRole userRole = accountService.getUserRole(commentProfileId);
        if (userRole != null && Role.isAsst(userRole.getRoleId())) {
            ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, submitId);
            Date lastModifiedTime = applicationSubmit.getLastModifiedTime();
            if (lastModifiedTime == null) {
                return applicationSubmit.getPublishTime().compareTo(commentAddDate) > 0;
            } else {
                return lastModifiedTime.compareTo(commentAddDate) > 0;
            }
        }
        return false;
    }

    @Override
    public Integer loadCompletedApplicationCnt(Integer planId) {
        List<PracticePlan> practicePlans = practicePlanDao.loadApplicationPracticeByPlanId(planId);
        Long completedCnt = practicePlans.stream().filter(practicePlan ->
                (practicePlan.getType() == 11 || practicePlan.getType() == 12) && practicePlan.getStatus() == 1
        ).count();
        return completedCnt.intValue();
    }

}
