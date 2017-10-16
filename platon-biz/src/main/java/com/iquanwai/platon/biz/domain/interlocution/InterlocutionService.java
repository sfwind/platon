package com.iquanwai.platon.biz.domain.interlocution;

import com.iquanwai.platon.biz.po.interlocution.InterlocutionDate;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionQuestion;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
public interface InterlocutionService {
    /**
     * 发布问题
     *
     * @param profileId 发布id
     * @param topic     标题
     */
    int publish(Integer questionId, Integer profileId, String topic,Date interlocutionDate);

    /**
     * d
     * /**
     * 加载问题，不分类
     *
     * @param loadProfileId 进行加载操作的人
     * @param page          分页参数
     * @return 问题列表
     */
    List<InterlocutionQuestion> loadQuestions(Integer loadProfileId, String date, Page page);


    /**
     * 关注问题
     *
     * @param questionId 问题id
     * @param profileId  关注者id
     */
    void followQuestion(Integer profileId, Integer questionId);

    /**
     * 不再关注问题
     *
     * @param questionId 问题id
     * @param profileId  关注者id
     */
    void unfollowQuestion(Integer profileId, Integer questionId);

    InterlocutionDate loadInterlocutionDateInfo(Date date);

    InterlocutionQuestion loadQuanQuanAnswer(Date date);
}
