package com.iquanwai.platon.biz.domain.fragmentation.message;

import com.iquanwai.platon.biz.po.HomeworkVote;
import com.iquanwai.platon.biz.po.NotifyMessage;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;

/**
 * Created by justin on 17/2/27.
 */
public interface MessageService {
    /**
     * 评论区回复消息
     * @param message 消息
     * @param fromUser 发送者
     * @param toUser 接收者
     * @param url 打开后跳转的链接
     * */
    void sendMessage(String message, String toUser, String fromUser, String url);

    /**
     * 获取所有的通知消息
     * @param openid 接收者
     * @param page 分页信息
     * */
    List<NotifyMessage> getNotifyMessage(String openid, Page page);

    Integer loadOldCount(String openid);

    /**
     * 阅读消息
     * @param id 消息id
     * */
    void readMessage(int id);

    /**
     * 消息标记为非最新
     * @param openid 接收者
     * */
    void mark(String openid);

    /**
     * 发送每日点赞通知
     * @param homeworkVotes 每日点赞
     * */
    void sendLikeMessage(List<HomeworkVote> homeworkVotes);

    /**
     * 发送试用版消息
     * @param openId openId
     */
    void sendRiseTrialMessage(String openId);


}
