package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文 on 2017/10/17
 */
@Data
public class CustomerMessageLog {

    private Integer id;
    private String openId;
    private Date publishTime;
    private String comment;
    private String contentHash; // 发送内容的 hash 值
    private Integer forwardlyPush; // 用户无触发推送
    private Integer validPush; // 有效的推送

}
