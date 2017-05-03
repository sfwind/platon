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
     * 获取巩固练习列表
     * @param problemId 小课id
     * @param practicePlanId 训练组编号
     * */
    List<WarmupPractice> getWarmupPractice(Integer problemId, Integer practicePlanId);

    /**
     * 获取巩固练习解析
     * @param planId 训练id
     * @param questionIds 练习编号
     * */
    List<WarmupSubmit> getWarmupSubmit(Integer planId, List<Integer> questionIds);
    /**
     * 回答巩固练习问题
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
     * 获取应用练习
     * @param id 应用练习id
     * @param openid openid
     * @param planId 训练id
     * */
    ApplicationPractice getApplicationPractice(Integer id, String openid, Integer planId, boolean create);

    /**
     * 提交应用训练
     * @param id 提交id
     * @param content 提交内容
     * */
    Boolean applicationSubmit(Integer id, String content);

    /**
     * 提交小目标
     * @param id 提交id
     * @param content 提交内容
     * */
    Boolean challengeSubmit(Integer id, String content);

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
     * 查询应用练习提交记录
     * @param applicationId 应用练习id
     */
    List<ApplicationSubmit> loadApplicationSubmits(Integer applicationId);

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
     * 获取巩固练习
     * @param warmupId 训练id
     * */
    WarmupPractice getWarmupPractice(Integer warmupId);

    /**
     * 提交小课分享文章
     */
    Integer submitSubjectArticle(SubjectArticle subjectArticle);

    /**
     * 加载小课分享区文章
     * @param problemId 小课id
     * @param page 分页对象
     */
    List<SubjectArticle> loadSubjectArticles(Integer problemId, Page page);

    /**
     * 加载分享区文章
     * @param submitId 提交id
     */
    SubjectArticle loadSubjectArticle(Integer submitId);

    /**
     * 获取小课所有标签
     * @param problemId 小课id
     * */
    List<LabelConfig> loadProblemLabels(Integer problemId);

    /**
     * 更新小课分享的标签
     * @param moduleId 模块
     * @param articleId 文章id
     * @param labels 标签
     * */
    List<ArticleLabel> updateLabels(Integer moduleId, Integer articleId, List<ArticleLabel> labels);

    /**
     * 获取小课分享的标签
     * @param moduleId 模块
     * @param articleId 文章id
     * */
    List<ArticleLabel> loadArticleActiveLabels(Integer moduleId, Integer articleId);

    /**
     * 根据训练id获取知识
     * @param practicePlanId 训练id
     * @param problemId 小课id
     */
    List<Knowledge> loadKnowledges(Integer practicePlanId, Integer problemId);

    /**
     * 学习知识点
     * @param practicePlanId 练习id
     */
    void learnKnowledge(Integer practicePlanId);

    /**
     * 求点评
     * @param submitId 文章提交id
     * @param moduleId 模块id（2-应用练习,3-小课分享）
     */
    boolean requestComment(Integer submitId, Integer moduleId);


    /**
     * 求点评次数
     * @param problemId 小课id
     * @param openid 学员id
     */
    boolean hasRequestComment(Integer problemId, String openid);
}
