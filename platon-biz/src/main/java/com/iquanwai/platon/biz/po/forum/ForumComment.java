package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumComment {
    private int id;
    private Integer answerId; //回答id
    private Integer commentProfileId; //评论者id
    private Boolean del; //是否删除（0-未删除，1-已删除）
    private Integer repliedId; //回复评论id
    private Integer repliedProfileId; //被评论者id
    private Boolean repliedDel; //被评论是否删除（0-有效，1-无效）
}
