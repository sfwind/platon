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
     * 获取热身训练列表
     * @param planId 训练id
     * @param practicePlanId 训练组编号
     * */
    List<WarmupPractice> getWarmupPractice(Integer planId, Integer practicePlanId);

    /**
     * 获取热身训练解析
     * @param planId 训练id
     * @param questionIds 练习编号
     * */
    List<WarmupSubmit> getWarmupSubmit(Integer planId, List<Integer> questionIds);
    /**
     * 回答热身训练问题
     * @param warmupPracticeList 练习答案
     * @param planId 训练id
     * */
    WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                      Integer planId, String openid) throws AnswerException;

    /**
     * 获取专题训练
     * @param id 专题训练id
     * @param openid 学员id
     * @param planId 训练计划id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId);

    /**
     * 获取应用训练
     * @param id 应用训练id
     * @param openId openid
     * @param planId 训练计划id
     * */
    ApplicationPractice getApplicationPractice(Integer id, String openId, Integer planId);

    /**
     * 获取专题训练
     * @param id 提交id
     * @param content 提交内容
     * @param type 11-应用训练,21-专题训练
     * */
    Boolean submit(Integer id, String content, Integer type);

    /**
     * 获取热身训练
     * @param warmupId 训练id
     * */
    WarmupPractice getWarmupPractice(Integer warmupId);

    /**
     * 获取前一天的点赞
     * */
    List<HomeworkVote> loadVoteYesterday();

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

    List<ChallengeSubmit> getChallengeSubmitList(Integer challengeId);

    List<Comment> loadComments(Integer moduleId, Integer submitId, Page page);

    Pair<Boolean,String> comment(Integer moduleId, Integer referId, String openId, String content);
}
