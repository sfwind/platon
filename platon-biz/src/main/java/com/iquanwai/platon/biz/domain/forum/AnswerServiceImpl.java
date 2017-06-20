package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.AnswerApprovalDao;
import com.iquanwai.platon.biz.dao.forum.ForumAnswerDao;
import com.iquanwai.platon.biz.dao.forum.ForumQuestionDao;
import com.iquanwai.platon.biz.po.forum.AnswerApproval;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ForumAnswer submitAnswer(ForumAnswer forumAnswer){
        ForumQuestion question = forumQuestionDao.load(ForumQuestion.class, forumAnswer.getQuestionId());
        if (question != null) {
            int insert = forumAnswerDao.insert(forumAnswer);
            if (insert != -1) {
                return forumAnswer;
            }
            logger.error("插入答案失败,{}", forumAnswer);
        }
        logger.error("提交失败，该问题为空,{}", forumAnswer);
        return null;
    }
}
