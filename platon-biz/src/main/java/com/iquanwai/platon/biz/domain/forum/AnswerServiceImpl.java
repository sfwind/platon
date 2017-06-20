package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.AnswerApprovalDao;
import com.iquanwai.platon.biz.dao.forum.ForumAnswerDao;
import com.iquanwai.platon.biz.dao.forum.ForumCommentDao;
import com.iquanwai.platon.biz.dao.forum.ForumQuestionDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.forum.AnswerApproval;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumComment;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Service
public class AnswerServiceImpl implements AnswerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AnswerApprovalDao answerApprovalDao;
    @Autowired
    private ForumAnswerDao forumAnswerDao;
    @Autowired
    private ForumQuestionDao forumQuestionDao;
    @Autowired
    private ForumCommentDao forumCommentDao;
    @Autowired
    private AccountService accountService;


    @Override
    public Boolean approveAnswer(Integer profileId,Integer answerId){
        AnswerApproval answerApproval = new AnswerApproval();
        answerApproval.setProfileId(profileId);
        answerApproval.setAnswerId(answerId);
        answerApproval.setDel(false);
        int id = answerApprovalDao.insert(answerApproval);
        if (id != -1) {
            forumAnswerDao.approve(answerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean cancelApproveAnswer(Integer profileId, Integer answerId){
        Integer delete = answerApprovalDao.delete(profileId, answerId);
        if (delete != -1) {
            forumAnswerDao.cancelApprove(answerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ForumAnswer submitAnswer(Integer answerId,Integer profileId,String answer,Integer questionId){
        if (answerId == null) {
            ForumAnswer forumAnswer = new ForumAnswer();
            forumAnswer.setQuestionId(questionId);
            forumAnswer.setProfileId(profileId);
            forumAnswer.setAnswer(answer);
            forumAnswer.setApprovalCount(0);
            ForumQuestion question = forumQuestionDao.load(ForumQuestion.class, forumAnswer.getQuestionId());
            if (question != null) {
                int insert = forumAnswerDao.insert(forumAnswer);
                if (insert != -1) {
                    // 增加回答数字
                    forumQuestionDao.answer(questionId);
                    return forumAnswer;
                }
                logger.error("插入答案失败,{}", forumAnswer);
            }
            logger.error("提交失败，该问题为空,{}", forumAnswer);
        } else {
            ForumAnswer forumAnswer = forumAnswerDao.load(ForumAnswer.class, answerId);
            if (forumAnswer != null) {
                if (profileId.equals(forumAnswer.getProfileId())) {
                    forumAnswerDao.update(answer, answerId);
                    // 设置新的answer并返回
                    forumAnswer.setAnswer(answer);
                    return forumAnswer;
                } else {
                    logger.error("更新答案失败，不是自己的答案");
                }
            } else {
                logger.error("更新答案失败");
            }
        }
        return null;
    }

    @Override
    public ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId){
        ForumAnswer answer = forumAnswerDao.load(ForumAnswer.class, answerId);
        if (answer != null) {
            // 加载评论
            List<ForumComment> comments = forumCommentDao.getComments(answerId);
            if (CollectionUtils.isNotEmpty(comments)) {
                comments.forEach(comment->{
                    Profile profile = accountService.getProfile(comment.getCommentProfileId());
                    comment.setAuthorUserName(profile.getNickname());
                    comment.setAuthorHeadPic(profile.getHeadimgurl());
                    comment.setPublishTimeStr(DateUtils.parseDateToString(comment.getAddTime()));
                    if (comment.getRepliedId() != null) {
                        if (comment.getRepliedProfileId() != null) {
                            Profile commentProfile = accountService.getProfile(comment.getRepliedProfileId());
                            comment.setRepliedName(commentProfile.getNickname());
                        }
                        // 被回复
                        if (comment.getRepliedDel()) {
                            // 回复的人已经删掉了
                            comment.setComment("该评论已删除");
                        } else {
                            ForumComment repliedComment = forumCommentDao.load(ForumComment.class, comment.getRepliedId());
                            comment.setRepliedComment(repliedComment.getComment());
                        }
                    }
                });
            }
            answer.setComments(comments);
        }
        return answer;
    }

    @Override
    public ForumComment commentAnswer(Integer answerId, Integer repliedCommentId, Integer profileId, String comment){
        ForumComment forumComment = new ForumComment();
        forumComment.setComment(comment);
        forumComment.setCommentProfileId(profileId);
        forumComment.setDel(false);
        forumComment.setAnswerId(answerId);
        if (repliedCommentId != null) {
            ForumComment repliedComment = forumCommentDao.load(ForumComment.class, repliedCommentId);
            if (repliedComment == null) {
                logger.error("回复评论失败，没有该评论");
                return null;
            } else {
                forumComment.setRepliedId(repliedCommentId);
                forumComment.setRepliedProfileId(repliedComment.getCommentProfileId());
                forumComment.setRepliedDel(false);
            }
        }
        forumCommentDao.comment(forumComment);
        return forumComment;
    }

    @Override
    public ForumComment loadComment(Integer commentId){
        return forumCommentDao.load(ForumComment.class, commentId);
    }

    @Override
    public Boolean deleteComment(Integer commentId){
        Integer count = forumCommentDao.deleteComment(commentId);
        if (count != -1) {
            // 更新其他状态
            forumCommentDao.updateRepliedDel(commentId);
            return true;
        }
        logger.error("删除评失败,commentId:{}",commentId);
        return false;
    }

    @Override
    public List<ForumAnswer> loadSelfAnswers(Integer profileId,Page page){
        List<ForumAnswer> forumAnswers = forumAnswerDao.loadUserAnswers(profileId, page);
        forumAnswers.forEach(item->{
            ForumQuestion question = forumQuestionDao.load(ForumQuestion.class, item.getQuestionId());
            item.setQuestion(question.getTopic());
            // set null
            item.setProfileId(null);
        });
        return forumAnswers;
    }
}
