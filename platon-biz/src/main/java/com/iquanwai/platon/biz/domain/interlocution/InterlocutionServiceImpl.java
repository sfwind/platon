package com.iquanwai.platon.biz.domain.interlocution;

import com.iquanwai.platon.biz.dao.interlocution.InterlocutionAnswerDao;
import com.iquanwai.platon.biz.dao.interlocution.InterlocutionDateDao;
import com.iquanwai.platon.biz.dao.interlocution.InterlocutionFollowDao;
import com.iquanwai.platon.biz.dao.interlocution.InterlocutionQuestionDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionAnswer;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionDate;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionFollow;
import com.iquanwai.platon.biz.po.interlocution.InterlocutionQuestion;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Service
public class InterlocutionServiceImpl implements InterlocutionService {
    @Autowired
    private InterlocutionQuestionDao interlocutionQuestionDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private InterlocutionFollowDao interlocutionFollowDao;
    @Autowired
    private InterlocutionDateDao interlocutionDateDao;
    @Autowired
    private InterlocutionAnswerDao interlocutionAnswerDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int publish(Integer questionId, Integer profileId, String topic, Date interlocutionDate) {
        int id;
        if (questionId == null) {
            // 新问题提交
            InterlocutionQuestion interlocutionQuestion = new InterlocutionQuestion();
            interlocutionQuestion.setProfileId(profileId);
            interlocutionQuestion.setTopic(topic);
            interlocutionQuestion.setInterlocutionDate(interlocutionDate);
            id = interlocutionQuestionDao.insert(interlocutionQuestion);
        } else {
            // 老问题修改
            id = questionId;
            interlocutionQuestionDao.update(topic, id);
        }
        return id;
    }


    @Override
    public List<InterlocutionQuestion> loadQuestions(String loadOpenid, String date, Page page) {
        List<InterlocutionQuestion> questions = interlocutionQuestionDao.getQuestions(date, page);
        // 查询有多少条
        Integer total = interlocutionQuestionDao.count(date);
        page.setTotal(total);
        // 填充数据
        questions.forEach(item -> initQuestionList(item, loadOpenid));
        return questions;
    }


    @Override
    public void followQuestion(String openid, Integer questionId) {
        InterlocutionFollow questionFollow = interlocutionFollowDao.load(questionId, openid);
        Integer followPoint = ConfigUtils.getForumQuestionFollowPoint();
        if (questionFollow == null) {
            questionFollow = new InterlocutionFollow();
            questionFollow.setOpenid(openid);
            questionFollow.setQuestionId(questionId);
            interlocutionFollowDao.insert(questionFollow);
            interlocutionQuestionDao.follow(questionId, followPoint);
        } else {
            if (questionFollow.getDel()) {
                interlocutionFollowDao.updateDel(questionFollow.getId(), 0);
                interlocutionQuestionDao.follow(questionId, followPoint);
            }
        }
    }

    @Override
    public void unfollowQuestion(String openid, Integer questionId) {
        InterlocutionFollow questionFollow = interlocutionFollowDao.load(questionId, openid);
        if (questionFollow != null) {
            if (!questionFollow.getDel()) {
                Integer followPoint = ConfigUtils.getForumQuestionFollowPoint();
                interlocutionFollowDao.updateDel(questionFollow.getId(), 1);
                interlocutionQuestionDao.unfollow(questionId, followPoint);
            }
        }
    }

    /**
     * 初始化问题列表
     *
     * @param item          问题
     * @param loadOpenid 执行加载操作的人
     */
    private void initQuestionList(InterlocutionQuestion item, String loadOpenid) {
        Profile profile = accountService.getProfile(item.getProfileId());
        if (profile == null) {
            logger.error("用户 {} 不存在", item.getProfileId());
            return;
        }
        // 设置昵称
        item.setAuthorUserName(profile.getNickname());
        // 设置头像
        item.setAuthorHeadPic(profile.getHeadimgurl());
        // 初始化添加时间
        item.setAddTimeStr(DateUtils.parseDateToString(item.getAddTime()));
        InterlocutionFollow load = interlocutionFollowDao.load(item.getId(), loadOpenid);
        item.setFollow(load != null && !load.getDel());
        // 去掉profileId
        item.setProfileId(null);
    }

    @Override
    public InterlocutionDate loadInterlocutionDateInfo(Date date) {
        if (date == null) {
            return null;
        }
        return interlocutionDateDao.loadDate(date);
    }

    @Override
    public InterlocutionQuestion loadQuanQuanAnswer(Date date) {
        InterlocutionAnswer answer = interlocutionAnswerDao.load(date);
        if (answer == null) {
            return null;
        }
        Integer questionId = answer.getQuestionId();
        InterlocutionQuestion question = interlocutionQuestionDao.load(InterlocutionQuestion.class, questionId);
        if (question == null) {
            return null;
        }
        question.setAnswer(answer);
        InterlocutionDate dateInfo = interlocutionDateDao.loadDate(date);
        InterlocutionDate nextDate = interlocutionDateDao.loadNextDate(date);
        question.setNextDate(nextDate);
        question.setDateInfo(dateInfo);
        return question;
    }

}
