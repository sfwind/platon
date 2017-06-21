package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.AnswerApprovalDao;
import com.iquanwai.platon.biz.dao.forum.ForumAnswerDao;
import com.iquanwai.platon.biz.dao.forum.ForumQuestionDao;
import com.iquanwai.platon.biz.dao.forum.ForumTagDao;
import com.iquanwai.platon.biz.dao.forum.QuestionFollowDao;
import com.iquanwai.platon.biz.dao.forum.QuestionTagDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.po.forum.ForumTag;
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
    @Autowired
    private AnswerApprovalDao answerApprovalDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int publish(Integer questionId,Integer profileId, String topic, String description, List<Integer> tagIds) {
        int id;
        if (questionId == null) {
            // 新问题提交
            ForumQuestion forumQuestion = new ForumQuestion();
            forumQuestion.setProfileId(profileId);
            forumQuestion.setTopic(topic);
            forumQuestion.setDescription(description);
            id = forumQuestionDao.insert(forumQuestion);
        } else {
            // 老问题修改
            id = questionId;
            forumQuestionDao.update(description, topic, id);
        }
        // 处理tag
        List<QuestionTag> existTagIds = questionTagDao.getAllQuestionTagsByQuestionId(id);
        chooseQuestionTags(existTagIds, tagIds, questionId);
        return id;
    }

    @Override
    public List<ForumQuestion> loadQuestionsByTags(Integer tagId, Page page) {
        List<QuestionTag> questionTags = questionTagDao.getQuestionTagsByTagId(tagId);
        List<Integer> questionIds = questionTags.stream().map(QuestionTag::getQuestionId).collect(Collectors.toList());
        Integer total = questionTagDao.getQuestionTagsCountByQuestionId(tagId);
        page.setTotal(total);
        List<ForumQuestion> result = forumQuestionDao.getQuestionsById(questionIds, page);
        result.forEach(item->{
            // 去掉profileId
            item.setProfileId(null);
        });
        return result;
    }


    @Override
    public List<ForumQuestion> loadQuestions(Integer loadProfileId, Page page){
        List<ForumQuestion> questions = forumQuestionDao.getQuestions(page);
        // 查询有多少条
        Long total = forumQuestionDao.count(ForumQuestion.class);
        page.setTotal(total.intValue());
        // 填充数据
        questions.forEach(item -> initQuestionList(item, loadProfileId));
        return questions;
    }

    @Override
    public List<ForumQuestion> loadSelfQuestions(Integer profileId, Page page) {
        List<ForumQuestion> forumQuestions = forumQuestionDao.getQuestions(profileId, page);
        // 查询有多少条
        Integer total = forumQuestionDao.getQuestionsCount(profileId);
        page.setTotal(total);
        // 填充数据
        forumQuestions.forEach(item -> initQuestionList(item, profileId));
        return forumQuestions;
    }

    @Override
    public List<ForumTag> loadTags() {
        return forumTagDao.loadAll(ForumTag.class);
    }

    @Override
    public ForumQuestion loadQuestion(Integer questionId,Integer loadProfileId) {
        ForumQuestion forumQuestion = forumQuestionDao.load(ForumQuestion.class, questionId);
        if(forumQuestion!=null){
            QuestionFollow load = questionFollowDao.load(questionId, loadProfileId);
            forumQuestion.setFollow(load != null && !load.getDel());

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
                item.setApproval(answerApprovalDao.load(item.getId(), loadProfileId) != null);
                item.setMine(loadProfileId.equals(item.getProfileId()));
                // 去掉profileId
                item.setProfileId(null);
            });
            forumQuestion.setAnswerList(answerList);
            // 问题添加时间
            forumQuestion.setAddTimeStr(DateUtils.parseDateToString(forumQuestion.getAddTime()));
            // 问题发布人信息
            Profile profile = accountService.getProfile(forumQuestion.getProfileId());
            forumQuestion.setAuthorHeadPic(profile.getHeadimgurl());
            forumQuestion.setAuthorUserName(profile.getNickname());
            List<QuestionTag> questionTagList = questionTagDao.getQuestionTagsByQuestionId(questionId);
            forumQuestion.setQuestionTagList(questionTagList);
        }
        // 去掉profileId,判断是否是自己的
        if (forumQuestion != null) {
            forumQuestion.setMine(loadProfileId.equals(forumQuestion.getProfileId()));
            forumQuestion.setProfileId(null);
        }
        return forumQuestion;
    }


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

    /**
     * 初始化问题列表
     * @param item 问题
     * @param loadProfileId 执行加载操作的人
     */
    private void initQuestionList(ForumQuestion item,Integer loadProfileId){
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
            List<Integer> distinctUsers = answers.stream().map(ForumAnswer::getProfileId).distinct().collect(Collectors.toList());
            List<String> answerNames = distinctUsers.stream().limit(2).map(profileId -> {
                Profile temp = accountService.getProfile(profileId);
                return temp.getNickname();
            }).collect(Collectors.toList());

            if (distinctUsers.size() > 1) {
                answerTips = "查看" + StringUtils.join(answerNames, "、") + "等" + distinctUsers.size() + "人的回答";
            } else {
                answerTips = "查看" + answerNames.get(0) + "的回答";
            }
        } else {
            answerTips = "成为第一个回答者";
        }
        item.setAnswerTips(answerTips);
        // 初始化添加时间
        item.setAddTimeStr(DateUtils.parseDateToString(item.getAddTime()));
        QuestionFollow load = questionFollowDao.load(item.getId(), loadProfileId);
        item.setFollow(load != null && !load.getDel());

        item.setMine(loadProfileId.equals(item.getProfileId()));
        // 去掉profileId
        item.setProfileId(null);
    }

    /**
     * 选择问题tag
     * @param exitTags 已经存在的tag，包括del的
     * @param tagIds    用户选择的tag
     * @param questionId 问题id
     */
    private void chooseQuestionTags(List<QuestionTag> exitTags,List<Integer> tagIds,Integer questionId){
        if (CollectionUtils.isNotEmpty(exitTags)) {
            // 存在tag，先处理老tag
            try {
                exitTags.forEach(tag -> {
                    if (tagIds.contains(tag.getTagId())) {
                        if (tag.getDel()) {
                            // 已删除但这次已选择，需要恢复
                            questionTagDao.reChooseTag(tag.getId());
                        }
                        // 已选择，之前也未删除，不作处理，只删掉就好了
                        tagIds.remove(tag.getTagId());
                    } else {
                        // 这次选择的不包含已有的
                        questionTagDao.deleteQuestionTag(tag.getId());
                    }
                });
            } catch (Exception e) {
                logger.error("插入问题标签失败", e);
            }
        }
        // 是否还有待处理tag
        if (CollectionUtils.isNotEmpty(tagIds)) {
            try {
                tagIds.forEach(tagId -> {
                    QuestionTag questionTag = new QuestionTag();
                    questionTag.setTagId(tagId);
                    questionTag.setQuestionId(questionId);
                    questionTag.setDel(false);
                    questionTagDao.insert(questionTag);
                });
            } catch (Exception e) {
                logger.error("插入问题标签失败", e);
            }
        }
    }
}
