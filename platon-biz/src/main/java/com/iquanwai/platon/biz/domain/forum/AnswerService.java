package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumComment;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
public interface AnswerService {
    /**
     * 支持回答
     * @param profileId 进行支持的人
     * @param answerId  被支持的回答
     * @return 是否支持成功
     */
    Boolean approveAnswer(Integer profileId, Integer answerId);

    /**
     * 取消支持
     * @param profileId 取消支持的人
     * @param answerId  被取消支持的回答
     */
    Boolean cancelApproveAnswer(Integer profileId, Integer answerId);

    /**
     * 提交回答／修改回答
     * @param answerId 回答id
     * @param profileId 进行操作的profileId
     * @param answer    回答内容
     * @param questionId    问题id
     * @return 提交或修改后的回答pojo
     */
    ForumAnswer submitAnswer(Integer answerId, Integer profileId, String answer, Integer questionId);

    /**
     * 加载回答
     * @param answerId 回答id
     * @param loadProfileId 加载回答的人
     * @return 回答pojo
     */
    ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId);

    /**
     * 对回答进行评论
     * @param answerId 回答id
     * @param repliedCommentId 是否是回复评论的，如果null的话，代表直接评论回答
     * @param profileId 进行操作的人
     * @param comment 评论内容
     * @return 评论pojo
     */
    ForumComment commentAnswer(Integer answerId, Integer repliedCommentId, Integer profileId, String comment);

    /**
     * 加载评论内容
     * @param commentId 评论id
     * @return 评论pojo
     */
    ForumComment loadComment(Integer commentId);

    /**
     * 删除评论
     * @param commentId 评论id
     * @return 是否删除成功
     */
    Boolean deleteComment(Integer commentId);

    /**
     * 加载自己的回答
     * @param profileId profileId
     * @param page 分页
     * @return 回答列表
     */
    List<ForumAnswer> loadSelfAnswers(Integer profileId,Page page);
}
