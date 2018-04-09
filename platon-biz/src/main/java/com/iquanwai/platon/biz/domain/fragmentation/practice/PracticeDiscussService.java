package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/8.
 */
public interface PracticeDiscussService {
    /**
     * 讨论某个巩固练习
     * @param profileId 发表讨论的用户id
     * @param warmupPracticeId 巩固练习id
     * @param comment 讨论内容
     * @param repliedId 回复的讨论id
     */
    void discuss(Integer profileId, Integer warmupPracticeId, String comment, Integer repliedId);

    /**
     * 讨论某个知识点
     * @param profileId 发表讨论的用户id
     * @param knowledgeId 知识点id
     * @param comment 讨论内容
     * @param repliedId 回复的讨论id
     */
    void discussKnowledge(Integer profileId, Integer knowledgeId, String comment, Integer repliedId);

    /**
     * 获取讨论内容
     * @param warmupPracticeId 巩固练习id
     * @param page 分页信息
     */
    List<WarmupComment> loadDiscuss(Integer profileId, Integer warmupPracticeId, Page page);

    /**
     * 获取多个问题的讨论内容
     * @param warmupPracticeIds 巩固练习id列表
     * @param page 分页信息
     */
    Map<Integer, List<WarmupComment>> loadDiscuss(Integer profileId, List<Integer> warmupPracticeIds, Page page);

    /**
     * 获取讨论内容
     * @param discussId 讨论内容id
     */
    WarmupPracticeDiscuss loadDiscuss(Integer discussId);

    /**
     * 删除评论
     * @param discussId 讨论id
     */
    void deleteComment(Integer discussId);

    /**
     * 获取知识点讨论
     * @param discussId 讨论id
     */
    KnowledgeDiscuss loadKnowledgeDiscuss(Integer discussId);

    /**
     * 获取知识点讨论
     * @param knowledgeId 知识点id
     * @param page 分页信息
     */
    List<KnowledgeDiscuss> loadKnowledgeDiscusses(Integer knowledgeId, Page page);

    /**
     * 获取知识点的个人评论数据
     */
    List<PersonalDiscuss> loadPersonalKnowledgeDiscussList(Integer profileId, Integer knowledgeId);

    /**
     * 获取知识点的相关评论
     */
    List<DiscussElementsPair> loadPriorityKnowledgeDiscuss(Integer knowledgeId);

    /**
     * 删除知识点评论
     * @param discussId 讨论id
     */
    Integer deleteKnowledgeDiscussById(Integer discussId);

    @SuppressWarnings("unchecked")
    Map<Integer, WarmupDiscussDistrict> loadWarmUpDiscuss(Integer profileId, List<Integer> warmUpPracticeIds);

    // 获取精华区的作业和讨论

    /**
     * 根据选择题 id 获取对应的所有评论内容
     * @param profileId 用户 id
     * @param warmUpPracticeId 巩固练习的 id
     */
    WarmupDiscussDistrict loadSingleWarmUpDiscuss(Integer profileId, Integer warmUpPracticeId);

    /**
     * 获取个人应用题提交和评论
     */
    List<PersonalDiscuss> loadPersonalApplicationSubmitDiscussList(Integer profileId, Integer applicationId, Integer planId);

    /**
     * 获取精华应用题和对应的评论信息
     */
    List<DiscussElementsPair> loadPriorityApplicationSubmitDiscussList(Integer profileId, Integer applicationId);
}
