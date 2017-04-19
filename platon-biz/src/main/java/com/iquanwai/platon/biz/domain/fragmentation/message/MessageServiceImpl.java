package com.iquanwai.platon.biz.domain.fragmentation.message;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.ChallengeSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.NotifyMessageDao;
import com.iquanwai.platon.biz.dao.fragmentation.SubjectArticleDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.ApplicationSubmit;
import com.iquanwai.platon.biz.po.ChallengeSubmit;
import com.iquanwai.platon.biz.po.HomeworkVote;
import com.iquanwai.platon.biz.po.NotifyMessage;
import com.iquanwai.platon.biz.po.SubjectArticle;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private ChallengeSubmitDao challengeSubmitDao;
    @Autowired
    private SubjectArticleDao subjectArticleDao;
    @Autowired
    private TemplateMessageService templateMessageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SYSTEM_MESSAGE ="AUTO";
    private static final String SYSTEM_MESSAGE_NAME ="系统消息";

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
    public List<NotifyMessage> getNotifyMessage(String openid, Page page) {
        List<NotifyMessage> notifyMessages = notifyMessageDao.getMyMessages(openid, page);
        int total = notifyMessageDao.getMyMessagesCount(openid);
        page.setTotal(total);

        List<String> openids = notifyMessages.stream().map(NotifyMessage::getFromUser).collect(Collectors.toList());
        List<Profile> profiles = accountService.getProfiles(openids);
        //更新头像和昵称
        notifyMessages.stream().forEach(notifyMessage -> {
            if(notifyMessage.getFromUser().equals(SYSTEM_MESSAGE)){
                notifyMessage.setFromUserAvatar(Profile.DEFAULT_AVATAR);
                notifyMessage.setFromUserName(SYSTEM_MESSAGE_NAME);
            }else {
                profiles.stream().forEach(profile -> {
                    if (notifyMessage.getFromUser().equals(profile.getOpenid())) {
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
        });

        return notifyMessages;
    }

    @Override
    public void readMessage(int id) {
        notifyMessageDao.read(id);
    }

    @Override
    public void mark(String openid) {
        notifyMessageDao.markOld(openid);
    }

    @Override
    public void sendLikeMessage(List<HomeworkVote> homeworkVotes) {
        List<VoteMessage> voteMessageList = Lists.newArrayList();

        //自己给自己点赞不提醒
        homeworkVotes.stream().filter(h1->!h1.getVoteOpenId().equals(h1.getVotedOpenid())).forEach(homeworkVote -> {
            VoteMessage voteMessage = new VoteMessage(homeworkVote.getReferencedId(),
                    homeworkVote.getType());

            //如果已经有了记录,点赞数+1
            if(voteMessageList.contains(voteMessage)){
                voteMessageList.forEach(voteMessageInList -> {
                    if(voteMessageInList.equals(voteMessage)){
                        voteMessageInList.increment();
                    }
                });
            }else{
                //如果已经没有记录,添加记录
                voteMessageList.add(voteMessage);
            }
            voteMessage.setLastVote(homeworkVote);
        });

        //发送消息
        voteMessageList.stream().forEach(voteMessage -> {
            HomeworkVote homeworkVote = voteMessage.getLastVote();
            String openid = homeworkVote.getVoteOpenId();
            Profile profile = accountService.getProfile(openid, false);
            //没查到点赞人,不发消息
            if(profile==null){
                logger.error("{} is not existed", openid);
                return;
            }
            String message = getLikeMessage(voteMessage, profile);
            String toUser = homeworkVote.getVotedOpenid();
            if(toUser==null){
                return;
            }
            String url = "";
            if(voteMessage.getType()==Constants.VoteType.CHALLENGE){
                ChallengeSubmit challengeSubmit = challengeSubmitDao.load(ChallengeSubmit.class, homeworkVote.getReferencedId());
                if(challengeSubmit==null){
                    return;
                }
                url = "/rise/static/practice/challenge?id=" + challengeSubmit.getChallengeId();
            }else if(voteMessage.getType()==Constants.VoteType.APPLICATION){
                ApplicationSubmit applicationSubmit = applicationSubmitDao.load(ApplicationSubmit.class, homeworkVote.getReferencedId());
                if(applicationSubmit==null){
                    return;
                }
                url = "/rise/static/practice/application?id=" + applicationSubmit.getApplicationId();
            }else if(voteMessage.getType()== Constants.VoteType.SUBJECT){
                SubjectArticle subjectArticle = subjectArticleDao.load(SubjectArticle.class, homeworkVote.getReferencedId());
                if (subjectArticle == null) {
                    return;
                }
                url = "/rise/static/message/subject/reply?submitId=" + subjectArticle.getId();
            }
            sendMessage(message, toUser, SYSTEM_MESSAGE, url);
        });
    }

    @Override
    public void sendRiseTrialMessage(String openId) {
        Profile profile = accountService.getProfile(openId, false);
        Assert.notNull(openId, "openid不能为空");
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.productTrailMsg());
        String first;
        if(profile!=null){
            first = "Hi，"+profile.getNickname()+"，欢迎试用RISE小课！\n\n";
        }else{
            first = "Hi，欢迎试用RISE小课！\n\n";
        }
        first += "试用版可体验一门小课的前3节内容。在小课页面点击“升级正式版”，学习更多内容和小课哦\n";
        data.put("first",new TemplateMessage.Keyword(first));
        data.put("keyword1",new TemplateMessage.Keyword("RISE小课"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n有疑问请在下方留言给小Q哦"));
        templateMessageService.sendMessage(templateMessage);
    }


    private String getLikeMessage(VoteMessage voteMessage, Profile profile) {
        String message = "";
        if(voteMessage.getCount()==1){
            if(voteMessage.getType()==Constants.VoteType.CHALLENGE){
                message = profile.getNickname()+"赞了我的小目标";
            }else if(voteMessage.getType()==Constants.VoteType.APPLICATION){
                message = profile.getNickname()+"赞了我的应用练习";
            } else if(voteMessage.getType()==Constants.VoteType.SUBJECT){
                message = profile.getNickname()+"赞了我的小课分享";
            }
        }else{
            if(voteMessage.getType()==Constants.VoteType.CHALLENGE){
                message = profile.getNickname()+"等"+voteMessage.getCount()+"人赞了我的小目标";
            }else if(voteMessage.getType()==Constants.VoteType.APPLICATION){
                message = profile.getNickname()+"等"+voteMessage.getCount()+"人赞了我的应用练习";
            }else if(voteMessage.getType()==Constants.VoteType.SUBJECT){
                message = profile.getNickname()+"等"+voteMessage.getCount()+"人赞了我的小课分享";
            }
        }
        return message;
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
