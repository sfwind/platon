package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.*;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.QuestionFollow;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
import com.iquanwai.platon.biz.util.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/6/19.
 */
@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private ForumQuestionDao forumQuestionDao;
    @Autowired
    private ForumAnswerDao forumAnswerDao;
    @Autowired
    private QuestionTagDao questionTagDao;
    @Autowired
    private ForumTagDao forumTagDao;
    @Autowired
    private QuestionFollowDao questionFollowDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int publish(Integer profileId, String topic, String description, List<Integer> tagIds) {
        ForumQuestion forumQuestion = new ForumQuestion();
        forumQuestion.setProfileId(profileId);
        forumQuestion.setTopic(topic);
        forumQuestion.setDescription(description);
        int id = forumQuestionDao.insert(forumQuestion);

        try {
            tagIds.forEach(tagId -> {
                QuestionTag questionTag = new QuestionTag();
                questionTag.setTagId(tagId);
                questionTag.setQuestionId(id);
                questionTag.setDel(false);
                questionTagDao.insert(questionTag);
            });
        } catch (Exception e) {
            logger.error("插入问题标签失败", e);
        }

        return id;
    }

    @Override
    public List<ForumQuestion> loadQuestions(Integer tagId, Page page) {
        List<QuestionTag> questionTags = questionTagDao.getQuestionTagsById(tagId);
        List<Integer> questionIds = questionTags.stream().map(QuestionTag::getQuestionId).collect(Collectors.toList());

        return forumQuestionDao.getQuestionsById(questionIds, page);
    }

    @Override
    public List<QuestionTag> loadTags() {
        return forumTagDao.loadAll(QuestionTag.class);
    }

    @Override
    public ForumQuestion loadQuestion(Integer questionId) {
        ForumQuestion forumQuestion = forumQuestionDao.load(ForumQuestion.class, questionId);
        if(forumQuestion!=null){
            forumQuestionDao.open(questionId);
            List<ForumAnswer> answerList = forumAnswerDao.load(questionId);
            forumQuestion.setAnswerList(answerList);
        }

        return forumQuestion;
    }

    @Override
    public void followQuestion(Integer profileId, Integer questionId) {
        QuestionFollow questionFollow = questionFollowDao.load(questionId, profileId);
        if(questionFollow==null){
            questionFollow = new QuestionFollow();
            questionFollow.setProfileId(profileId);
            questionFollow.setQuestionId(questionId);
            questionFollowDao.insert(questionFollow);
            forumQuestionDao.follow(questionId);
        }else{
            if(questionFollow.getDel()){
                questionFollowDao.updateDel(questionFollow.getId(), 0);
                forumQuestionDao.follow(questionId);
            }
        }
    }

    @Override
    public void unfollowQuestion(Integer profileId, Integer questionId) {
        QuestionFollow questionFollow = questionFollowDao.load(questionId, profileId);
        if(questionFollow!=null){
            if(!questionFollow.getDel()){
                questionFollowDao.updateDel(questionFollow.getId(), 1);
                forumQuestionDao.unfollow(questionId);
            }
        }
    }
}
