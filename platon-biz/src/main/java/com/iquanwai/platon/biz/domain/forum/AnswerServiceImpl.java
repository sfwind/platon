package com.iquanwai.platon.biz.domain.forum;

import com.iquanwai.platon.biz.dao.forum.*;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.forum.*;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
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
public class AnswerServiceImpl implements AnswerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AnswerApprovalDao answerApprovalDao;
    @Autowired
    private QuestionFollowDao questionFollowDao;
    @Autowired
    private ForumAnswerDao forumAnswerDao;
    @Autowired
    private ForumQuestionDao forumQuestionDao;
    @Autowired
    private ForumCommentDao forumCommentDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MessageService messageService;

    private static final String ANSWER_URL = "/rise/static/message/question/answer";

    private static final String COMMENT_URL = "/rise/static/message/answer/comment";

    @Override
    public Boolean approveAnswer(Integer profileId, Integer answerId) {
        AnswerApproval exist = answerApprovalDao.loadUserAnswerApproval(answerId, profileId);
        if (exist != null) {
            if (exist.getDel()) {
                // 恢复
                Integer update = answerApprovalDao.update(profileId, answerId, 0);
                if (update != -1) {
                    // 恢复支持，加1
                    forumAnswerDao.approve(answerId);
                    return true;
                } else {
                    // sql报错，返回false
                    return false;
                }
            } else {
                logger.error("用户:{},已关注这个问题:{},无法再次关注!", profileId, answerId);
                // 已关注了，可能多次点击，继续返回true就可以
                return true;
            }
        } else {
            ForumAnswer forumAnswer = forumAnswerDao.load(ForumAnswer.class, answerId);
            if (forumAnswer == null) {
                return false;
            }
            // 新增
            AnswerApproval answerApproval = new AnswerApproval();
            answerApproval.setProfileId(profileId);
            answerApproval.setAnswerId(answerId);
            answerApproval.setQuestionId(forumAnswer.getQuestionId());
            answerApproval.setAnswerProfileId(forumAnswer.getProfileId());
            answerApproval.setDel(false);
            int id = answerApprovalDao.insert(answerApproval);
            if (id != -1) {
                forumAnswerDao.approve(answerId);
                return true;
            } else {
                // sql报错，返回false
                return false;
            }
        }
    }

    @Override
    public Boolean cancelApproveAnswer(Integer profileId, Integer answerId) {
        AnswerApproval exist = answerApprovalDao.loadUserAnswerApproval(answerId, profileId);
        if (exist != null) {
            // 存在
            if (exist.getDel()) {
                // 已经删除，返回true就可以
                return true;
            } else {
                // 未删除，进行操作
                Integer delete = answerApprovalDao.update(profileId, answerId, 1);
                if (delete != -1) {
                    forumAnswerDao.cancelApprove(answerId);
                    return true;
                } else {
                    // sql报错，返回false
                    return false;
                }
            }
        } else {
            // 不存在
            logger.error("用户:{}取关 {} 失败，不存在该支持.", profileId, answerId);
            return false;
        }
    }

    @Override
    public ForumAnswer submitAnswer(Integer answerId, Integer profileId, String answer, Integer questionId) {
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
                    answerQuestionMsg(questionId, insert, profileId, question.getProfileId());
                    forumAnswer.setPublishTimeStr(DateUtils.parseDateToString(new Date()));
                    // 新增，肯定是0
                    forumAnswer.setCommentCount(0);
                    forumAnswer.setId(insert);
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
                    forumAnswer.setPublishTimeStr(DateUtils.parseDateToString(forumAnswer.getPublishTime()));
                    forumAnswer.setCommentCount(forumCommentDao.getCommentCount(answerId));
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

    private void answerQuestionMsg(Integer questionId, Integer answerId, Integer answerProfileId, Integer questionProfileId) {
        //发送给提问者
        String answerUrl = ANSWER_URL + "?questionId=" + questionId + "&answerId=" + answerId;
        Profile profile = accountService.getProfile(answerProfileId);
        messageService.sendMessage(profile.getNickname() + "回答了我的论坛提问", questionProfileId.toString(),
                answerProfileId.toString(), answerUrl);
        //发送给关注者
        List<QuestionFollow> questionFollows = questionFollowDao.load(questionId);
        if (CollectionUtils.isNotEmpty(questionFollows)) {
            questionFollows.forEach(questionFollow -> {
                //自己关注自己的问题,不发消息提醒 自己回答自己关注的问题,不发消息提醒
                if (!questionProfileId.equals(questionFollow.getProfileId()) &&
                        !answerProfileId.equals(questionFollow.getProfileId())) {
                    messageService.sendMessage("你关注的问题有新回答", questionFollow.getProfileId().toString(),
                            MessageService.SYSTEM_MESSAGE, answerUrl);
                }
            });
        }
    }

    private void commentMsg(Integer answerId, Integer commentId, Integer answerProfileId, Integer commentProfileId) {
        //发送给回答者,自己给自己评论,不发消息提醒
        if (!answerProfileId.equals(commentProfileId)) {
            String commentUrl = COMMENT_URL + "?answerId=" + answerId + "&commentId=" + commentId;
            Profile profile = accountService.getProfile(commentProfileId);
            messageService.sendMessage(profile.getNickname() + "回复了我的论坛答案", answerProfileId.toString(),
                    commentProfileId.toString(), commentUrl);
        }
    }

    private void replyMsg(Integer answerId, Integer commentId, Integer commentedProfileId, Integer commentProfileId) {
        //发送给回答者,自己给自己回复,不发消息提醒
        if (!commentedProfileId.equals(commentProfileId)) {
            String commentUrl = COMMENT_URL + "?answerId=" + answerId + "&commentId=" + commentId;
            Profile profile = accountService.getProfile(commentProfileId);
            messageService.sendMessage(profile.getNickname() + "回复了我的论坛评论", commentedProfileId.toString(),
                    commentProfileId.toString(), commentUrl);
        }
    }

    @Override
    public ForumAnswer loadAnswer(Integer answerId, Integer loadProfileId) {
        ForumAnswer answer = forumAnswerDao.load(ForumAnswer.class, answerId);
        if (answer != null) {
            // 加载评论
            List<ForumComment> comments = forumCommentDao.getComments(answerId);
            if (CollectionUtils.isNotEmpty(comments)) {
                // 对评论信息作处理，添加逻辑字段
                comments.forEach(comment -> {
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
                            comment.setRepliedComment("该评论已删除");
                        } else {
                            ForumComment repliedComment = forumCommentDao.load(ForumComment.class, comment.getRepliedId());
                            comment.setRepliedComment(repliedComment.getComment());
                        }
                    }
                    comment.setMine(comment.getCommentProfileId().equals(loadProfileId));
                    comment.setCommentProfileId(null);
                    comment.setRepliedProfileId(null);
                });
            }
            answer.setCommentCount(forumCommentDao.getCommentCount(answerId));

            ForumQuestion question = forumQuestionDao.load(ForumQuestion.class, answer.getQuestionId());
            answer.setTopic(question.getTopic());
            // 是否是自己的
            answer.setMine(answer.getProfileId().equals(loadProfileId));
            Profile profile = accountService.getProfile(answer.getProfileId());
            answer.setAuthorHeadPic(profile.getHeadimgurl());
            answer.setAuthorUserName(profile.getNickname());
            // 发布时间
            answer.setPublishTimeStr(DateUtils.parseDateToString(answer.getPublishTime()));
            // 评论
            answer.setComments(comments);
            // 是否支持
            answer.setApproval(answerApprovalDao.load(answerId, loadProfileId) != null);
            // 隐藏profileId
            answer.setProfileId(null);

        }
        return answer;
    }

    @Override
    public ForumComment commentAnswer(Integer answerId, Integer repliedCommentId, Integer profileId, String comment) {
        ForumComment forumComment = new ForumComment();
        forumComment.setComment(comment);
        forumComment.setCommentProfileId(profileId);
        forumComment.setDel(false);
        forumComment.setAnswerId(answerId);
        ForumComment repliedComment = null;
        if (repliedCommentId != null) {
            repliedComment = forumCommentDao.load(ForumComment.class, repliedCommentId);
            if (repliedComment == null) {
                logger.error("回复评论失败，没有该评论");
                return null;
            } else {
                forumComment.setRepliedId(repliedCommentId);
                forumComment.setRepliedProfileId(repliedComment.getCommentProfileId());
                forumComment.setRepliedDel(false);
                Profile profile = accountService.getProfile(repliedComment.getCommentProfileId());
                forumComment.setRepliedName(profile.getNickname());
                forumComment.setRepliedComment(repliedComment.getComment());
            }
        }
        int id = forumCommentDao.comment(forumComment);
        if (repliedCommentId != null) {
            //发回复消息
            replyMsg(answerId, id, repliedComment.getCommentProfileId(), profileId);
        } else {
            ForumAnswer forumAnswer = forumAnswerDao.load(ForumAnswer.class, answerId);
            if (forumAnswer != null) {
                //发评论消息
                commentMsg(answerId, id, forumAnswer.getProfileId(), profileId);
            }
        }
        forumComment.setPublishTimeStr(DateUtils.parseDateToString(new Date()));
        // 是自己的
        forumComment.setMine(true);
        forumComment.setId(id);
        return forumComment;
    }

    @Override
    public ForumComment loadComment(Integer commentId) {
        return forumCommentDao.load(ForumComment.class, commentId);
    }

    @Override
    public Boolean deleteComment(Integer commentId) {
        Integer count = forumCommentDao.deleteComment(commentId);
        if (count != -1) {
            // 更新其他状态
            forumCommentDao.updateRepliedDel(commentId);
            return true;
        }
        logger.error("删除评失败,commentId:{}", commentId);
        return false;
    }

    @Override
    public List<ForumAnswer> loadSelfAnswers(Integer profileId, Page page) {
        List<ForumAnswer> forumAnswers = forumAnswerDao.loadUserAnswers(profileId, page);
        page.setTotal(forumAnswerDao.loadUserAnswersCount(profileId));
        forumAnswers.forEach(item -> {
            ForumQuestion question = forumQuestionDao.load(ForumQuestion.class, item.getQuestionId());
            if (question != null) {
                item.setTopic(question.getTopic());
            }
            // set null
            item.setProfileId(null);
        });
        return forumAnswers;
    }

    @Override
    public List<ForumAnswer> loadUserQuestionAnswers(Integer questionId, Integer profileId) {
        return forumAnswerDao.loadUserQuestionAnswers(questionId, profileId);
    }
}
