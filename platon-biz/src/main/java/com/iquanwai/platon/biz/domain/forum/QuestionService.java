package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.ForumTag;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
public interface QuestionService {
    /**
     * 发布问题
     * @param profileId 发布id
     * @param topic     标题
     * @param description 描述
     * @param tagIds    问题标签
     * */
    int publish(Integer questionId, Integer profileId, String topic, String description, List<Integer> tagIds);

    /**
     * 根据标签id获取问题
     * @param tagId 标签id
     * */
    List<ForumQuestion> loadQuestionsByTags(Integer tagId, Page page);

    /**
     * 加载问题，不分类
     * @param loadProfileId 进行加载操作的人
     * @param page 分页参数
     * @return 问题列表
     */
    List<ForumQuestion> loadQuestions(Integer loadProfileId, Page page);

    /**
     * 搜索问题
     * @param loadProfileId 搜索的人
     * @param content 搜索内容
     * @param page 分页参数
     * @return 问题列表
     */
    List<ForumQuestion> searchQuestions(Integer loadProfileId, String content, Page page);

    /**
     * 加载自己的所有问题
     * @param profileId 执行加载操作的人
     * @param page 分页参数
     * @return 问题列表
     */
    List<ForumQuestion> loadSelfQuestions(Integer profileId, Page page);

    /**
     * 根据标签
     * */
    List<ForumTag> loadTags();

    /**
     * 根据id获取问题
     * @param questionId 问题id
     * @param loadProfileId 加载问题的人
     * */
    ForumQuestion loadQuestion(Integer questionId,Integer loadProfileId);


    /**
     * 关注问题
     * @param questionId 问题id
     * @param profileId 关注者id
     * */
    void followQuestion(Integer profileId, Integer questionId);

    /**
     * 不再关注问题
     * @param questionId 问题id
     * @param profileId 关注者id
     * */
    void unfollowQuestion(Integer profileId, Integer questionId);
}
