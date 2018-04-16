package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.PracticePlanStatusManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointManager;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
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
    private CommentDao commentDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private AsstCoachCommentDao asstCoachCommentDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private CommentEvaluationDao commentEvaluationDao;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private PracticePlanStatusManager practicePlanStatusManager;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private ProblemPreviewDao problemPreviewDao;


    // 商业思维项目字数下限50字
    private static final int BUSINESS_THOUGHT_PROJECT_WORD_AT_LEAST = 50;
    // 核心能力项目字数下限50字
    private static final int CORE_PROJECT_WORD_AT_LEAST = 10;

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
                    warmupPractice.setScore(PointManager.WARMUP_EASY_SCORE);
                } else if (warmupPractice.getDifficulty() == 2) {
                    warmupPractice.setScore(PointManager.WARMUP_NORMAL_SCORE);
                } else if (warmupPractice.getDifficulty() == 3) {
                    warmupPractice.setScore(PointManager.WARMUP_HARD_SCORE);
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
                                             Integer profileId) throws AnswerException {
        WarmupResult warmupResult = new WarmupResult();
        Integer rightNumber = 0;
        Integer point = 0;
        warmupResult.setTotal(warmupPracticeList.size());
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        Integer planId = practicePlan.getPlanId();
        for (int i = 0; i < warmupPracticeList.size(); i++) {
            WarmupPractice userAnswer = warmupPracticeList.get(i);
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
                logger.error("{} has answered practice {}", profileId, practice.getId());
                throw new AnswerException();
            }
            //生成提交记录
            warmupSubmit = new WarmupSubmit();
            warmupSubmit.setContent(StringUtils.join(userChoice, ","));
            warmupSubmit.setPlanId(planId);
            warmupSubmit.setQuestionId(practice.getId());
            warmupSubmit.setIsRight(accurate);
            warmupSubmit.setScore(score);
            warmupSubmit.setProfileId(profileId);
            warmupSubmitDao.insert(warmupSubmit);

            operationLogService.trace(profileId, "submitWarmup", () -> {
                OperationLogService.Prop prop = OperationLogService.props();
                prop.add("warmupId", practice.getId());
                prop.add("problemId", practice.getProblemId());
                prop.add("series", practicePlan.getSeries());
                prop.add("sequence", practice.getSequence());
                prop.add("isRight", accurate);
                return prop;
            });
        }
        if (PracticePlan.STATUS.UNCOMPLETED == practicePlan.getStatus()) {
            practicePlanStatusManager.completePracticePlan(profileId, practicePlan);
            improvementPlanDao.updateWarmupComplete(planId);
            poinManager.risePoint(planId, point);
        }
        warmupResult.setRightNumber(rightNumber);
        warmupResult.setPoint(point);
        return warmupResult;
    }

    @Override
    public ChallengePractice getChallengePractice(Integer id, Integer profileId, Integer planId, boolean create) {
        ChallengePractice challengePractice = new ChallengePractice(id);
        // 查询该用户是否提交
        ChallengeSubmit submit = challengeSubmitDao.load(id, planId, profileId);
        if (submit == null && create) {
            // 没有提交，生成
            submit = new ChallengeSubmit();
            submit.setProfileId(profileId);
            submit.setPlanId(planId);
            submit.setChallengeId(id);
            int submitId = challengeSubmitDao.insert(submit);
            submit.setId(submitId);
            submit.setUpdateTime(new Date());
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
    public Pair<ApplicationPractice, Boolean> getApplicationPractice(Integer id, Integer profileId, Integer planId, boolean create) {
        Boolean isNewApplication = false; // 该 ApplicationPractice 是否是新生成的
        // 查询该应用练习
        ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, id);
        Assert.notNull(applicationPractice, "应用练习" + id + "不存在");
        if (planId == null) {
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, applicationPractice.getProblemId());
            Assert.notNull(improvementPlan, profileId + "没有开课" + applicationPractice.getProblemId());
            planId = improvementPlan.getId();
        }

        // 查询该用户是否提交
        ApplicationSubmit submit = applicationSubmitDao.load(id, planId, profileId);
        if (submit == null && create) {
            isNewApplication = true; // 该 ApplicationPractice 为新创建
            // 没有提交，生成
            submit = new ApplicationSubmit();
            submit.setProfileId(profileId);
            submit.setPlanId(planId);
            submit.setApplicationId(id);
            submit.setProblemId(applicationPractice.getProblemId());
            int submitId = applicationSubmitDao.insert(submit);
            submit.setId(submitId);
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
        // 应用题名字
        applicationPractice.setName(applicationName(applicationPractice.getType()));

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
        }

        // 检查该道题是否是简单应用题还是复杂应用题
        List<PracticePlan> practicePlans = practicePlanDao.loadApplicationPracticeByPlanId(planId);
        practicePlans
                .stream()
                .filter(planItem -> planItem.getPracticeId().equals(id.toString())).findAny()
                .ifPresent(targetPracticePlan -> {
                    // 找到目标练习
                    practicePlans.stream()
                            // 同组最大者
                            .filter(planItem -> planItem.getSeries().equals(targetPracticePlan.getSeries()))
                            .max(Comparator.comparingInt(PracticePlan::getSequence))
                            // 判断下这道题是不是最后一道
                            .ifPresent(item -> applicationPractice.setIsLastApplication(item.getPracticeId().equals(id.toString())));
                    // 是不是base
                    applicationPractice.setIsBaseApplication(targetPracticePlan.getSequence() == 3);
                });
        return new MutablePair<>(applicationPractice, isNewApplication);
    }

    @Override
    public Integer applicationSubmit(Integer id, String content) {
        ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, id);
        if (submit == null) {
            logger.error("submitId {} is not existed", id);
            return null;
        }
        boolean result;
        boolean hasImage = content.contains("<img");
        int length = CommonUtils.removeHTMLTag(content).length();
        if (submit.getContent() == null) {
            result = applicationSubmitDao.firstAnswer(id, content, length, hasImage);
        } else {
            result = applicationSubmitDao.answer(id, content, length, hasImage);
        }

        PracticePlan practicePlan = practicePlanDao.loadApplicationPractice(submit.getPlanId(), submit.getApplicationId());
        if (practicePlan != null) {
            if (result) {
                // 完成练习
                if (PracticePlan.STATUS.UNCOMPLETED == practicePlan.getStatus()) {
                    practicePlanStatusManager.completePracticePlan(submit.getProfileId(), practicePlan);
                    improvementPlanDao.updateApplicationComplete(submit.getPlanId());
                    certificateService.generatePersonalFullAttendance(practicePlan.getId());
                }
                // 修改应用任务记录
                ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, submit.getPlanId());
                if (plan != null) {
                    // 至少达到一定字数才能加分
                    if (submit.getPointStatus() == 0) {
                        Problem problem = cacheService.getProblem(plan.getProblemId());
                        if (problem.getProject() == Constants.Project.BUSINESS_THOUGHT_PROJECT &&
                                (length >= BUSINESS_THOUGHT_PROJECT_WORD_AT_LEAST || hasImage)) {
                            applicationAddPoint(practicePlan, id, submit);
                        } else if (length >= CORE_PROJECT_WORD_AT_LEAST || hasImage) {
                            applicationAddPoint(practicePlan, id, submit);
                        }
                    }
                } else {
                    logger.error("ImprovementPlan is not existed, planId:{}", submit.getPlanId());
                }
            } else {
                logger.error("应用练习{}提交失败", id);
            }
            return practicePlan.getId();
        }

        return null;
    }

    private void applicationAddPoint(PracticePlan practicePlan, Integer id, ApplicationSubmit submit) {
        if (practicePlan != null) {
            logger.info("应用练习加分:{}", id);

            Integer point = poinManager.calcApplicationScore(applicationPracticeDao.load(ApplicationPractice.class,
                    submit.getApplicationId()).getDifficulty());
            // 查看难度，加分
            poinManager.risePoint(submit.getPlanId(), point);
            // 修改status
            applicationSubmitDao.updatePointStatus(id);
        }
    }

    @Override
    public Integer insertApplicationSubmitDraft(Integer profileId, Integer applicationId, Integer planId, String content) {
        ApplicationSubmitDraft submitDraft = applicationSubmitDraftDao.loadApplicationSubmitDraft(profileId, applicationId, planId);
        if (submitDraft == null) {
            // 用户第一次提交，或者历史数据，没有草稿存储，新建 draft，并且初始化数据
            ApplicationSubmitDraft tempDraft = new ApplicationSubmitDraft();
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
            // 修改课程任务记录
            PracticePlan practicePlan = practicePlanDao.loadPracticePlan(submit.getPlanId(),
                    submit.getChallengeId(), PracticePlan.CHALLENGE);
            if (practicePlan != null) {
                practicePlanStatusManager.completePracticePlan(submit.getProfileId(), practicePlan);
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
        return homeworkVoteDao.votedCount(referencedId);
    }

    @Override
    public Map<Integer, List<HomeworkVote>> getHomeworkVotes(List<ApplicationSubmit> applicationSubmits) {
        List<Integer> submitIds = applicationSubmits.stream().map(ApplicationSubmit::getId).collect(Collectors.toList());

        List<HomeworkVote> homeworkVotes = homeworkVoteDao.getHomeworkVotesByReferenceIds(submitIds);
        Map<Integer, List<HomeworkVote>> homeworkOriginMap = homeworkVotes.stream()
                .collect(Collectors.groupingBy(HomeworkVote::getReferencedId));

        Map<Integer, List<HomeworkVote>> homeworkVoteMap = Maps.newHashMap();
        for (Integer submitId : submitIds) {
            homeworkVoteMap.put(submitId, homeworkOriginMap.getOrDefault(submitId, Lists.newArrayList()));
        }

        return homeworkVoteMap;
    }

    @Override
    public Integer commentCount(Integer moduleId, Integer referId) {
        return commentDao.commentCount(referId);
    }

    @Override
    public Map<Integer, Integer> commentCount(List<ApplicationSubmit> applicationSubmits) {
        List<Integer> submitIds = applicationSubmits.stream().map(ApplicationSubmit::getId).collect(Collectors.toList());
        List<Comment> comments = commentDao.loadAllCommentsByReferenceIds(submitIds);
        Map<Integer, Integer> commentMap = Maps.newHashMap();

        comments.forEach(comment -> {
            Integer count = commentMap.getOrDefault(comment.getReferencedId(), 0);
            commentMap.put(comment.getReferencedId(), count + 1);
        });
        return commentMap;
    }

    @Override
    public HomeworkVote loadVoteRecord(Integer type, Integer referId, Integer profileId) {
        return homeworkVoteDao.loadVoteRecord(referId, profileId);
    }

    @Override
    public boolean vote(Integer type, Integer referencedId, Integer profileId, Integer device) {
        HomeworkVote vote = homeworkVoteDao.loadVoteRecord(referencedId, profileId);
        if (vote == null) {
            Integer planId = null;
            Integer submitProfileId;
            if (type == Constants.VoteType.CHALLENGE) {
                // 挑战任务点赞
                ChallengeSubmit submit = challengeSubmitDao.load(ChallengeSubmit.class, referencedId);
                if (submit == null) {
                    return false;
                }
                planId = submit.getPlanId();
                submitProfileId = submit.getProfileId();
            } else if (type == Constants.VoteType.APPLICATION) {
                // 应用任务点赞
                ApplicationSubmit submit = applicationSubmitDao.load(ApplicationSubmit.class, referencedId);
                if (submit == null) {
                    return false;
                }
                planId = submit.getPlanId();
                submitProfileId = submit.getProfileId();
                operationLogService.trace(profileId, "voteApplication", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    Profile profile = accountService.getProfile(submitProfileId);
                    Problem problem = problemDao.load(Problem.class, submit.getProblemId());
                    List<RiseMember> riseMembers = riseMemberManager.member(submitProfileId);
                    if (riseMembers.isEmpty()) {
                        prop.add("votedRolenames", Lists.newArrayList("0"));
                    } else {
                        prop.add("votedRolenames", riseMembers.stream().map(RiseMember::getMemberTypeId).map(Object::toString).distinct().collect(Collectors.toList()));
                    }
                    prop.add("applicationId", submit.getApplicationId());
                    prop.add("votedRiseId", profile.getRiseId());
                    prop.add("problemId", problem.getId());
                    prop.add("deviceType", device);
                    return prop;
                });
            } else {
                submitProfileId = null;
            }
            HomeworkVote homeworkVote = new HomeworkVote();
            homeworkVote.setReferencedId(referencedId);
            homeworkVote.setVoteProfileId(profileId);
            homeworkVote.setType(type);
            homeworkVote.setVotedProfileId(submitProfileId);
            homeworkVote.setDevice(device);
            homeworkVoteDao.vote(homeworkVote);
            poinManager.risePoint(planId, ConfigUtils.getVoteScore());


        } else {
            homeworkVoteDao.reVote(vote.getId());
        }
        return true;
    }


    private List<ApplicationSubmit> loadApplicationSubmits(Integer applicationId, Page page) {
        return applicationSubmitDao.loadSubmits(applicationId, page).stream().map(applicationSubmit -> {
            String content = CommonUtils.replaceHttpsDomainName(applicationSubmit.getContent());
            if (!content.equals(applicationSubmit.getContent())) {
                applicationSubmitDao.updateContent(applicationSubmit.getId(), content);
                applicationSubmit.setContent(content);
            }
            return applicationSubmit;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationSubmit> loadAllOtherApplicationSubmits(Integer applicationId, Page page) {
        // 加载时间
        List<ApplicationSubmit> submits = loadApplicationSubmits(applicationId, page);
        int count = applicationSubmitDao.count(applicationId);
        page.setTotal(count);

        return submits;
    }

    @Override
    public List<Comment> loadComments(Integer moduleId, Integer submitId, Page page) {
        page.setTotal(commentDao.commentCount(submitId));
        return commentDao.loadComments(submitId, page);
    }

    @Override
    public Comment loadApplicationReplyComment(Integer commentId) {
        return commentDao.load(Comment.class, commentId);
    }

    @Override
    public Pair<Integer, String> replyComment(Integer moduleId, Integer referId, Integer profileId,
                                              String content, Integer repliedId, Integer device) {
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
                asstCoachComment(load.getProfileId(), load.getProblemId());
            }
            operationLogService.trace(profileId, "replyCommentApplication", () -> {
                List<RiseMember> riseMembers = riseMemberManager.member(load.getProfileId());
                RiseClassMember riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(load.getProfileId());
                Profile repliesProfile = accountService.getProfile(load.getProfileId());
                OperationLogService.Prop prop = OperationLogService.props();
                prop.add("repliedRiseId", repliesProfile.getRiseId());
                prop.add("applicationId", load.getApplicationId());
                prop.add("problemId", load.getProblemId());
                if (riseMembers.isEmpty()) {
                    prop.add("repliedRolenames", Lists.newArrayList("0"));
                } else {
                    prop.add("repliedRolenames", riseMembers.stream().map(RiseMember::getMemberTypeId).map(Object::toString).distinct().collect(Collectors.toList()));
                }
                if (riseClassMember != null) {
                    if (riseClassMember.getClassName() != null) {
                        prop.add("repliedClassname", riseClassMember.getClassName());
                    }
                    if (riseClassMember.getGroupId() != null) {
                        prop.add("repliedGroupid", riseClassMember.getGroupId());
                    }
                }
                return prop;
            });
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
        comment.setRepliedProfileId(repliedComment.getCommentProfileId());
        comment.setRepliedComment(repliedComment.getContent());
        comment.setRepliedDel(0);
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
                msg = "评论了我的课程分享";
            }
            url = url.append("?moduleId=").append(moduleId).append("&submitId=").append(referId).append("&commentId=").append(id);
            messageService.sendMessage(msg, repliedComment.getCommentProfileId().toString(), profileId.toString(), url.toString());
        }
        return new MutablePair<>(id, "评论成功");
    }

    @Override
    public Pair<Integer, String> comment(Integer moduleId, Integer referId, Integer profileId, String content, Integer device) {
        if (device == null) {
            device = Constants.Device.MOBILE;
        }
        //先插入评论
        Comment comment = new Comment();
        comment.setModuleId(moduleId);
        comment.setReferencedId(referId);
        comment.setType(Constants.CommentType.STUDENT);
        comment.setContent(content);
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
                //记录首次点评时间
                if (load.getFeedBackTime() == null) {
                    applicationSubmitDao.asstFeedBackAndTime(load.getId());
                } else {
                    applicationSubmitDao.asstFeedback(load.getId());
                }
                asstCoachComment(load.getProfileId(), load.getProblemId());
            }
            //自己给自己评论不提醒
            if (load.getProfileId() != null && !load.getProfileId().equals(profileId)) {
                String url = "/rise/static/message/application/reply?submitId=" + referId + "&commentId=" + id;
                messageService.sendMessage("评论了我的应用题", load.getProfileId().toString(), profileId.toString(), url);
            }
            operationLogService.trace(profileId, "commentApplication", () -> {
                OperationLogService.Prop prop = OperationLogService.props();
                Profile discussedProfile = accountService.getProfile(load.getProfileId());
                List<RiseMember> riseMembers = riseMemberManager.member(load.getProfileId());
                prop.add("applicationId", load.getApplicationId());
                if (riseMembers.isEmpty()) {
                    prop.add("discussedRolenames", Lists.newArrayList("0"));
                } else {
                    prop.add("discussedRolenames", riseMembers.stream().map(RiseMember::getMemberTypeId).map(Object::toString).distinct().collect(Collectors.toList()));
                }
                prop.add("discussedRiseId", discussedProfile.getRiseId());
                prop.add("problemId", load.getProblemId());
                return prop;
            });
        }
        return new MutablePair<>(id, "评论成功");
    }

    private void asstCoachComment(Integer profileId, Integer problemId) {
        AsstCoachComment asstCoachComment = asstCoachCommentDao.loadAsstCoachComment(problemId, profileId);
        if (asstCoachComment == null) {
            asstCoachComment = new AsstCoachComment();
            asstCoachComment.setCount(1);
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
        Map<Integer, Comment> commentMap = comments.stream().collect(Collectors.toMap(Comment::getId, comment -> comment, (key1, key2) -> key2));
        List<Integer> profileIds = comments.stream().map(Comment::getCommentProfileId).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key2));

        evaluations = evaluations.stream()
                .filter(commentEvaluation -> {
                    Comment comment = commentMap.get(commentEvaluation.getCommentId());
                    if (comment != null) {
                        Profile profile = profileMap.get(comment.getCommentProfileId());
                        if (profile != null) {
                            return true;
                        }
                    }
                    return false;
                })
                .peek(commentEvaluation -> {
                    Comment comment = commentMap.get(commentEvaluation.getCommentId());
                    Profile profile = profileMap.get(comment.getCommentProfileId());
                    commentEvaluation.setNickName(profile.getNickname());
                })
                .collect(Collectors.toList());
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
    public SubjectArticle loadSubjectArticle(Integer submitId) {
        return subjectArticleDao.load(SubjectArticle.class, submitId);
    }

    @Override
    public List<Knowledge> loadKnowledges(Integer practicePlanId) {
        List<Knowledge> knowledges = Lists.newArrayList();
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            logger.error("{} is not existed", practicePlanId);
            return Lists.newArrayList();
        }
        String practiceId = practicePlan.getPracticeId();
        String[] knowledgeIds = practiceId.split(",");
        for (String knowledgeId : knowledgeIds) {
            Knowledge knowledge = getKnowledge(Integer.valueOf(knowledgeId));
            knowledges.add(knowledge);
        }
        return knowledges;
    }

    @Override
    public ProblemPreview loadProblemPreview(Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (practicePlan == null) {
            logger.error("{} is not existed", practicePlanId);
            return null;
        }
        String practiceId = practicePlan.getPracticeId();

        return problemPreviewDao.load(ProblemPreview.class, Integer.valueOf(practiceId));
    }

    @Override
    public void learnPracticePlan(Integer profileId, Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        practicePlanStatusManager.completePracticePlan(profileId, practicePlan);
    }

    @Override
    public Knowledge loadKnowledge(Integer knowledgeId) {
        //小目标的knowledgeId=null
        if (knowledgeId == null) {
            Knowledge knowledge = new Knowledge();
            //文案写死
            knowledge.setKnowledge("让你的训练更有效");
            return knowledge;
        }
        Knowledge knowledge = cacheService.getKnowledge(knowledgeId);
        WarmupPractice warmupPractice = warmupPracticeDao.loadExample(knowledgeId);
        if (warmupPractice != null) {
            warmupPractice = cacheService.getWarmupPractice(warmupPractice.getId());
            knowledge.setExample(warmupPractice);
        }
        return knowledge;
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

                operationLogService.trace(profileId, "requestComment", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    prop.add("problemId", improvementPlan.getProblemId());
                    prop.add("applicationId", applicationSubmit.getApplicationId());
                    return prop;
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteComment(Integer commentId) {
        commentDao.deleteComment(commentId);
    }

    @Override
    public ApplicationSubmit loadApplicationSubmitById(Integer applicationSubmitId) {
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
            applicationSubmit.setVoteCount(homeworkVoteDao.votedCount(id));
            applicationSubmit.setVoteStatus(homeworkVoteDao.loadVoteRecord(id, readProfileId) != null);
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
        Long completedCnt = practicePlans.stream()
                .filter(practicePlan -> PracticePlan.isApplicationPractice(practicePlan.getType()) &&
                        PracticePlan.STATUS.COMPLETED == practicePlan.getStatus())
                .count();
        return completedCnt.intValue();
    }

    @Override
    public PracticePlan getPractice(Integer practicePlanId) {
        return practicePlanDao.load(PracticePlan.class, practicePlanId);
    }

    private String applicationName(Integer type) {
        return "今日应用";
    }

}
