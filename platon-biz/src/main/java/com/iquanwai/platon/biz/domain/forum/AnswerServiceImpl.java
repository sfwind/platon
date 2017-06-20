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
    public void approveAnswer(Integer profileId,Integer answerId){
        AnswerApproval answerApproval = new AnswerApproval();
        answerApproval.setProfileId(profileId);
        answerApproval.setAnswerId(answerId);
        answerApproval.setDel(false);
        answerApprovalDao.insert(answerApproval);
        forumAnswerDao.approve(answerId);
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
                });
            }
            answer.setComments(comments);
        }
        return answer;
    }
}
