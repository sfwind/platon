package com.iquanwai.platon.biz.domain.fragmentation.message;

import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/2/27.
 *  */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private NotifyMessageDao notifyMessageDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    //TODO:改造成消息队列
    public void sendMessage(String message, String toUser, String fromUser, String url) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setFromUser(fromUser);
        notifyMessage.setToUser(toUser);
        notifyMessage.setMessage(message);
        notifyMessage.setIsRead(false);
        notifyMessage.setOld(false);
        notifyMessage.setSendTime(DateUtils.parseDateTimeToString(new Date()));
        notifyMessage.setUrl(url);

        notifyMessageDao.insert(notifyMessage);
    }

    @Override
    public List<NotifyMessage> getNotifyMessage(Integer profileId, Integer deviceType, Page page) {
        List<NotifyMessage> notifyMessages = notifyMessageDao.getMyMessages(profileId, page);
        int total = notifyMessageDao.getMyMessagesCount(profileId);
        page.setTotal(total);

        List<Integer> profileIds = notifyMessages.stream()
                .filter(notifyMessage1 -> !notifyMessage1.getFromUser().equals(SYSTEM_MESSAGE))
                .map(notifyMessage1 -> Integer.valueOf(notifyMessage1.getFromUser()))
                .collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(profileIds);
        //更新头像和昵称
        notifyMessages.forEach(notifyMessage -> {
            if (notifyMessage.getFromUser().equals(SYSTEM_MESSAGE)) {
                notifyMessage.setFromUserAvatar(Profile.DEFAULT_AVATAR);
                notifyMessage.setFromUserName(SYSTEM_MESSAGE_NAME);
            } else {
                profiles.forEach(profile -> {
                    if (notifyMessage.getFromUser().equals(String.valueOf(profile.getId()))) {
                        notifyMessage.setFromUserAvatar(profile.getHeadimgurl());
                        notifyMessage.setFromUserName(profile.getNickname());
                    }
                });
            }
            //修改发送时间
            notifyMessage.setSendTime(notifyMessage.getSendTime().substring(0, 10));
            //清空openid
            notifyMessage.setToUser(null);
            notifyMessage.setFromUser(null);
            // 根据 PC 和移动端修改跳转 URL
            if(deviceType == Constants.Device.PC) {
                String url = notifyMessage.getUrl();
                if(url.contains("/rise/static/message/warmup/reply")) {
                    url = url.replace("/rise/static/message/warmup/reply", "/fragment/message/warmup/reply");
                } else if(url.contains("/rise/static/message/knowledge/reply")) {
                    url = url.replace("/rise/static/message/knowledge/reply", "/fragment/message/knowledge/reply");
                } else if(url.contains("/rise/static/message/comment/reply")) {
                    url = url.replace("/rise/static/message/comment/reply", "/fragment/message/comment/reply");
                } else {
                    url = "/fragment/message";
                }
                notifyMessage.setUrl(url);
            }
        });
        return notifyMessages;
    }

    @Override
    public Integer unreadCount(Integer profileId) {
        return notifyMessageDao.loadOldCount(profileId);
    }

    @Override
    public void readMessage(int id) {
        notifyMessageDao.read(id);
    }


    @Override
    public void mark(Integer profileId) {
        notifyMessageDao.markOld(profileId);
    }

    @Override
    public ApplicationPractice loadAppPracticeByCommentId(Integer id) {
        Comment comment = commentDao.load(Comment.class, id);
        if(comment != null){
            ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, comment.getReferencedId());
            if(applicationSubmit != null) {
                ApplicationPractice applicationPractice = applicationPracticeDao.load(ApplicationPractice.class, applicationSubmit.getApplicationId());
                applicationPractice.setPlanId(applicationSubmit.getPlanId());
                return applicationPractice;
            }
        }
        return null;
    }

    public SubjectArticle loadSubjectArticleByCommentId(Integer id) {
        SubjectArticle subjectArticle = new SubjectArticle();
        Comment comment = commentDao.load(Comment.class, id);
        if(comment != null) {
             subjectArticle = subjectArticleDao.load(SubjectArticle.class, comment.getReferencedId());
        }
        return subjectArticle;
    }

    @Setter
    @Getter
    class VoteMessage{
        private int referenceId;
        private int type;
        private HomeworkVote lastVote;
        private int count = 1;

        public VoteMessage(int referenceId, int type) {
            this.referenceId = referenceId;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VoteMessage)) return false;

            VoteMessage that = (VoteMessage) o;

            return referenceId == that.referenceId && type == that.type;

        }

        @Override
        public int hashCode() {
            int result = referenceId;
            result = 31 * result + type;
            return result;
        }

        public void increment(){
            this.count++;
        }
    }
}
