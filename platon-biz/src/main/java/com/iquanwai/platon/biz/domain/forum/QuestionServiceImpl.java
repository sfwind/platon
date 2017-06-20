package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.ForumAnswerDao;
import com.iquanwai.platon.biz.dao.forum.ForumQuestionDao;
import com.iquanwai.platon.biz.dao.forum.ForumTagDao;
import com.iquanwai.platon.biz.dao.forum.QuestionFollowDao;
import com.iquanwai.platon.biz.dao.forum.QuestionTagDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.QuestionFollow;
import com.iquanwai.platon.biz.po.forum.QuestionTag;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    private AccountService accountService;

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
    public List<ForumQuestion> loadQuestions(Page page){
        List<ForumQuestion> questions = forumQuestionDao.getQuestions(page);
        questions.forEach(item->{
            Profile profile = accountService.getProfile(item.getProfileId());
            // 设置昵称
            item.setAuthorUserName(profile.getNickname());
            // 设置头像
            item.setAuthorHeadPic(profile.getHeadimgurl());
            // 查询多少人回答
            List<ForumAnswer> answers = forumAnswerDao.load(item.getId());
            // 初始化回答提示
            String answerTips = "";
            if (CollectionUtils.isNotEmpty(answers)) {
                List<Integer> answerProfiles = answers.stream().limit(2).map(ForumAnswer::getProfileId).collect(Collectors.toList());
                List<String> answerNames = answerProfiles.stream().map(profileId -> {
                    Profile temp = accountService.getProfile(profileId);
                    return temp.getNickname();
                }).collect(Collectors.toList());
                if (answers.size() > 1) {
                    answerTips = "查看" + StringUtils.join(answerNames, "、") + "等" + answers.size() + "人的回答";
                } else {
                    answerTips = "查看" + answerNames.get(0) + "的回答";
                }
            } else {
                answerTips = "成为第一个回答者";
            }
            item.setAnswerTips(answerTips);
            // 初始化添加时间
            item.setAddTimeStr(DateUtils.parseDateToString(item.getAddTime()));
        });
        return questions;
    }

    @Override
    public List<QuestionTag> loadTags() {
        return forumTagDao.loadAll(QuestionTag.class);
    }

    @Override
    public ForumQuestion loadQuestion(Integer questionId) {
        ForumQuestion forumQuestion = forumQuestionDao.load(ForumQuestion.class, questionId);
        if(forumQuestion!=null){
            Integer point = ConfigUtils.getForumQuestionOpenPoint();
            // 加权重
            forumQuestionDao.open(questionId, point);
            // 设置答案列表
            List<ForumAnswer> answerList = forumAnswerDao.load(questionId);
            answerList.forEach(item->{
                Profile profile = accountService.getProfile(item.getProfileId());
                item.setAuthorUserName(profile.getNickname());
                item.setAuthorHeadPic(profile.getHeadimgurl());
                item.setPublishTimeStr(DateUtils.parseDateToString(item.getPublishTime()));
            });
            forumQuestion.setAnswerList(answerList);
            // 问题添加时间
            forumQuestion.setAddTimeStr(DateUtils.parseDateToString(forumQuestion.getAddTime()));
            // 问题发布人信息
            Profile profile = accountService.getProfile(forumQuestion.getProfileId());
            forumQuestion.setAuthorHeadPic(profile.getHeadimgurl());
            forumQuestion.setAuthorUserName(profile.getNickname());
        }
        return forumQuestion;
    }

//    @Override
//    public Boolean loadQuestion(Integer profileId,Integer questionId){
//
//    }

    @Override
    public void followQuestion(Integer profileId, Integer questionId) {
        QuestionFollow questionFollow = questionFollowDao.load(questionId, profileId);
        Integer followPoint = ConfigUtils.getForumQuestionFollowPoint();
        if(questionFollow==null){
            questionFollow = new QuestionFollow();
            questionFollow.setProfileId(profileId);
            questionFollow.setQuestionId(questionId);
            questionFollowDao.insert(questionFollow);
            forumQuestionDao.follow(questionId,followPoint);
        }else{
            if(questionFollow.getDel()){
                questionFollowDao.updateDel(questionFollow.getId(), 0);
                forumQuestionDao.follow(questionId, followPoint);
            }
        }
    }

    @Override
    public void unfollowQuestion(Integer profileId, Integer questionId) {
        QuestionFollow questionFollow = questionFollowDao.load(questionId, profileId);
        if(questionFollow!=null){
            if(!questionFollow.getDel()){
                Integer followPoint = ConfigUtils.getForumQuestionFollowPoint();
                questionFollowDao.updateDel(questionFollow.getId(), 1);
                forumQuestionDao.unfollow(questionId, followPoint);
            }
        }
    }
}
