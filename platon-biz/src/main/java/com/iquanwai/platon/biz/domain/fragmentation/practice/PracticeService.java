package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {
    /**
     * 获取巩固训练列表
     * @param problemId 专题id
     * @param practicePlanId 训练组编号
     * */
    List<WarmupPractice> getWarmupPractice(Integer problemId, Integer practicePlanId);

    /**
     * 获取巩固训练解析
     * @param planId 训练id
     * @param questionIds 练习编号
     * */
    List<WarmupSubmit> getWarmupSubmit(Integer planId, List<Integer> questionIds);
    /**
     * 回答巩固训练问题
     * @param warmupPracticeList 练习答案
     * @param planId 训练id
     * */
    WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                      Integer planId, String openid) throws AnswerException;

    /**
     * 获取小目标
     * @param id 小目标id
     * @param openid 学员id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId, boolean create);

    /**
     * 获取应用训练
     * @param id 应用训练id
     * @param openid openid
     * @param planId 训练id
     * */
    ApplicationPractice getApplicationPractice(Integer id, String openid, Integer planId, boolean create);

    /**
     * 提交训练
     * @param id 提交id
     * @param content 提交内容
     * @param type 11-应用训练,21-小目标
     * */
    Boolean submit(Integer id, String content, Integer type);

    /**
     * 增加文章视图的记录数
     */
    Integer riseArticleViewCount(Integer module,Integer id,Integer type);

    /*
     * 获取点赞数量
     * @param type 点赞的类型
     * @param referencedId 被赞的id
     * @return 点赞数
     */
    Integer votedCount(Integer type, Integer referencedId);

    /**
     * 获取评论数
     * @param moduleId 评论模块
     * @param referId 被评论的id
     * @return 评论数
     */
    Integer commentCount(Integer moduleId,Integer referId);

    /**
     * 获取点赞记录
     * @param type 点赞类型
     * @param referId 被点赞的id
     * @param openId 点赞的openId
     * @return 点赞记录
     */
    HomeworkVote loadVoteRecord(Integer type, Integer referId, String openId);

    /**
     * 点赞
     * @param type 点赞类型
     * @param referencedId 被点赞的id
     * @param openId 点赞的人
     */
    boolean vote(Integer type, Integer referencedId, String openId);

    /**
     * 查询应用训练提交记录
     * @param applicationId 应用训练id
     */
    List<ApplicationSubmit> loadApplicationSubmits(Integer applicationId);

    /**
     * 查询训练提交列表
     * @param challengeId 小目标id
     */
    List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId);

    /**
     * 查询评论
     * @param moduleId 模块id
     * @param submitId 提交id
     * @param page 分页对象
     */
    List<Comment> loadComments(Integer moduleId, Integer submitId, Page page);

    /**
     * 提交评论
     * @param moduleId 模块id
     * @param referId 关联id
     * @param openId 评论人
     * @param content 评论内容
     */
    Pair<Boolean,String> comment(Integer moduleId, Integer referId, String openId, String content);

    /*
     * 获取巩固训练
     * @param warmupId 训练id
     * */
    WarmupPractice getWarmupPractice(Integer warmupId);

    /**
     * 获取前一天的点赞
     * */
    List<HomeworkVote> loadVoteYesterday();

    /**
     * 提交专题分享文章
     */
    Integer submitSubjectArticle(SubjectArticle subjectArticle);

    /**
     * 加载专题分享区文章
     * @param problemId 专题id
     * @param page 分页对象
     */
    List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page);

    /**
     * 加载分享区文章
     * @param submitId 提交id
     */
    SubjectArticle loadSubjectArticle(Integer submitId);

    /**
     * 获取专题所有标签
     * @param problemId 专题id
     * */
    List<LabelConfig> loadProblemLabels(Integer problemId);

    /**
     * 更新专题分享的标签
     * @param moduleId 模块
     * @param articleId 文章id
     * @param labels 标签
     * */
    List<ArticleLabel> updateLabels(Integer moduleId, Integer articleId, List<ArticleLabel> labels);

    /**
     * 获取专题分享的标签
     * @param moduleId 模块
     * @param articleId 文章id
     * */
    List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId);

    /**
     * 根据训练id获取知识
     * @param practicePlanId 训练id
     * @param problemId 专题id
     */
    List<Knowledge> loadKnowledges(Integer practicePlanId, Integer problemId);

    /**
     * 学习知识点
     * @param practicePlanId 练习id
     */
    void learnKnowledge(Integer practicePlanId);

}
