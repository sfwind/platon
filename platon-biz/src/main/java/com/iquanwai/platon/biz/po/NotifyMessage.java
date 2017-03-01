package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/2/27.
 */
@Data
public class NotifyMessage {
    private int id;
    private String message;  //通知消息
    private String fromUser; //发送者openid
    private String toUser;  //接收者openid
    private Boolean isRead; //是否阅读(0-未读，1-已读)
    private Boolean old; //是否老消息(0-否，1-是)
    private Date readTime;  //阅读时间
    private String sendTime;  //发送时间
    private String url;  //跳转URL
    private String fromUserName;  //发送者名称 非db字段
    private String fromUserAvatar; //发送者头像 非db字段

}
