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
    int publish(Integer profileId, String topic, String description, List<Integer> tagIds);


    /**
     * 根据标签id获取问题
     * @param tagId 标签id
     * */
    List<ForumQuestion> loadQuestions(Integer tagId, Page page);

    List<ForumQuestion> loadQuestions(Page page,Integer loadProfileId);

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
