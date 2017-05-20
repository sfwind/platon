package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
public class RiseWorkCommentDto {
    private Integer id;
    private String comment;
    private String name;
    private String discussTime;
    private String avatar;
    private String signature;
    private Integer role;
    private Boolean isMine;

    private Integer repliedId; // 被回复的id
    private String repliedComment; // 被回复的评论内容
    private String repliedName; // 被回复人的名字
    private Integer repliedDel; //被回复人评论是否删除
}
