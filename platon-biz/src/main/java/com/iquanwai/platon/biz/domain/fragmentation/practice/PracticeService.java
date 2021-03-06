package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {
    /**
     * 获取巩固练习列表
     *
     * @param practicePlanId 训练组编号
     */
    List<WarmupPractice> getWarmupPractices(Integer practicePlanId);

    /**
     * 获取巩固练习解析
     *
     * @param practicePlanId 练习id
     * @param questionIds    练习编号
     */
    List<WarmupSubmit> getWarmupSubmit(Integer practicePlanId, List<Integer> questionIds);

    /**
     * 获取巩固练习解析
     *
     * @param profileId  学员id
     * @param questionId 问题id
     */
    WarmupSubmit getWarmupSubmit(Integer profileId, Integer questionId);

    /**
     * 回答巩固练习问题
     *
     * @param warmupPracticeList 练习答案
     * @param practicePlanId     练习id
     */
    WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                      Integer profileId) throws AnswerException;


    /**
     * 获取小目标
     *
     * @param id     小目标id
     * @param profileId 学员id
     */
    ChallengePractice getChallengePractice(Integer id, Integer profileId, Integer planId, boolean create);

    /**
     * 获取应用练习
     *
     * @param id     应用练习id
     * @param profileId 用户id
     * @param planId 训练id
     */
    Pair<ApplicationPractice, Boolean> getApplicationPractice(Integer id, Integer profileId, Integer planId, boolean create);

    /**
     * 提交应用训练
     *
     * @param id      提交id
     * @param content 提交内容
     */
    Integer applicationSubmit(Integer id, String content);

    /**
     * 提交应用训练草稿
     *
     */
    Integer insertApplicationSubmitDraft(Integer profileId, Integer applicationId, Integer planId, String content);

    /**
     * 提交小目标
     *
     * @param id      提交id
     * @param content 提交内容
     */
    Boolean challengeSubmit(Integer id, String content);

    void initCommentEvaluation(Integer submitId, Integer commentId);

    void updateEvaluation(Integer commentId, Integer useful, String reason);

    /**
     * 获取当前应用题训练尚未被评价的教练评论记录
     */
    List<CommentEvaluation> loadUnEvaluatedCommentEvaluationBySubmitId(Integer profileId, Integer submitId);

    /**
     * 消息中心回复页面根据 commentId 来查询教练评论
     */
    List<CommentEvaluation> loadUnEvaluatedCommentEvaluationByCommentId(Integer commentId);


    /**
     * 获取点赞数量
     * @param type 点赞的类型
     * @param referencedId 被赞的id
     * @return 点赞数
     */
    Integer votedCount(Integer type, Integer referencedId);

    /**
     * 批量获取作业点赞
     */
    Map<Integer, List<HomeworkVote>> getHomeworkVotes(List<ApplicationSubmit> applicationSubmits);


    /**
     * 获取评论数
     *
     * @param moduleId 评论模块
     * @param referId  被评论的id
     * @return 评论数
     */
    Integer commentCount(Integer moduleId, Integer referId);

    /**
     * 批量获取作业点评数
     */
    Map<Integer, Integer> commentCount(List<ApplicationSubmit> applicationSubmits);

    /**
     * 获取点赞记录
     *
     * @param type      点赞类型
     * @param referId   被点赞的id
     * @param profileId 点赞的profileId
     * @return 点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, Integer profileId);

    /**
     * 点赞
     *
     * @param type         点赞类型
     * @param referencedId 被点赞的id
     * @param profileId    点赞的人
     */
    boolean vote(Integer type, Integer referencedId, Integer profileId, Integer device);

    /**
     * 查询同学的应用练习提交记录
     *
     * @param applicationId 应用练习id
     */
    List<ApplicationSubmit> loadAllOtherApplicationSubmits(Integer applicationId, Page page);

    /**
     * 查询评论
     *
     * @param moduleId 模块id
     * @param submitId 提交id
     * @param page     分页对象
     */
    List<Comment> loadComments(Integer moduleId, Integer submitId, Page page);


    /**
     * 提交评论
     *
     * @param moduleId 模块id
     * @param referId  关联id
     * @param content  评论内容
     * @param device 设备，默认是移动提交
     */
    Pair<Integer, String> comment(Integer moduleId, Integer referId, Integer profileId, String content,Integer device);

    /**
     * 获取回复应用练习的评论
     */
    Comment loadApplicationReplyComment(Integer commentId);

    /**
     * 回复评论
     *
     * @param moduleId  模块id
     * @param referId   关联id
     * @param content   评论内容
     * @param repliedId 被回复评论id
     * @param device 提交设备
     */
    Pair<Integer, String> replyComment(Integer moduleId, Integer referId, Integer profileId,
                                       String content, Integer repliedId,Integer device);

    /*
     * 获取巩固练习
     * @param warmupId 训练id
     * */
    WarmupPractice getWarmupPractice(Integer warmupId);

    /**
     * 加载分享区文章
     *
     * @param submitId 提交id
     */
    SubjectArticle loadSubjectArticle(Integer submitId);

    /**
     * 根据训练id获取知识
     *
     * @param practicePlanId 训练id
     */
    List<Knowledge> loadKnowledges(Integer practicePlanId);

    /**
     * 根据训练id获取课前思考
     *
     * @param practicePlanId 训练id
     */
    ProblemPreview loadProblemPreview(Integer practicePlanId);

    /**
     * 完成练习
     *
     * @param practicePlanId 练习id
     */
    void learnPracticePlan(Integer profileId, Integer practicePlanId);

    /**
     * 根据知识点id获取知识
     *
     * @param knowledgeId 知识点id
     */
    Knowledge loadKnowledge(Integer knowledgeId);

    /**
     * 求点评
     *
     * @param submitId 文章提交id
     * @param moduleId 模块id（2-应用练习,3-课程分享）
     */
    boolean requestComment(Integer submitId, Integer moduleId, Integer profileId);

    /**
     * 删除评论
     *
     * @param commentId 评论id
     */
    void deleteComment(Integer commentId);

    /**
     * 根据 ApplicationSubmit 的 id 获取 ApplicationSubmit
     */
    ApplicationSubmit loadApplicationSubmitById(Integer applicationSubmitId);

    /**
     * 根据Id获取ApplicationSubmit对象
     *
     * @param id 主键
     * @param readProfileId 阅读者id
     */
    ApplicationSubmit getApplicationSubmit(Integer id, Integer readProfileId);

    /**
     * 根据Id获取评论
     *
     * @param commentId 评论id
     */
    Comment loadComment(Integer commentId);

    /**
     * 根据 ApplicationSubmit 中的 id 和对应评论的 profileid 以及 commentAddDate 来判断学员是否在助教评论之后更改过答案
     */
    Boolean isModifiedAfterFeedback(Integer submitId, Integer commentProfileId, Date commentAddDate);

    /**
     * 获取完成的应用题数字
     */
    Integer loadCompletedApplicationCnt(Integer planId);

    /**
     * 获取练习
     *
     * @param practicePlanId 训练组编号
     */
    PracticePlan getPractice(Integer practicePlanId);

}
