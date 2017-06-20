package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.po.forum.ForumAnswer;

/**
 * Created by justin on 17/6/19.
 */
public interface AnswerService {
    void approveAnswer(Integer profileId, Integer answerId);

    ForumAnswer submitAnswer(Integer answerId,Integer profileId,String answer,Integer questionId);

    ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId);
}
