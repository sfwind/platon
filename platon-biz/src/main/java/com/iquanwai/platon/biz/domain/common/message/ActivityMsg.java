package com.iquanwai.platon.biz.domain.common.message;

import lombok.Data;

/**
 * Created by justin on 17/6/30.
 */
@Data
public class ActivityMsg {
    private String startTime; //开始通知时间
    private String endTime;  //结束通知时间
    private String message;  //通知消息
    private String url;      //点击消息跳转url
    private String eventKey;  //消息key,用于定向推送
}
