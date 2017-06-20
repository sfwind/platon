package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumComment;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
public interface AnswerService {
    void approveAnswer(Integer profileId, Integer answerId);

    ForumAnswer submitAnswer(Integer answerId,Integer profileId,String answer,Integer questionId);

    ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId);

    ForumComment commentAnswer(Integer answerId, Integer repliedCommentId, Integer profileId, String comment);

    ForumComment loadComment(Integer commentId);

    Boolean deleteComment(Integer commentId);

    List<ForumAnswer> loadSelfAnswers(Integer profileId,Page page);
}
