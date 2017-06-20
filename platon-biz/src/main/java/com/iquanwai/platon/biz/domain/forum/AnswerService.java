package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumComment;

/**
 * Created by justin on 17/6/19.
 */
public interface AnswerService {
    void approveAnswer(Integer profileId, Integer answerId);

    ForumAnswer submitAnswer(Integer answerId,Integer profileId,String answer,Integer questionId);

    ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId);

    ForumComment commentAnswer(Integer answerId, Integer repliedCommentId, Integer profileId, String comment);
}
