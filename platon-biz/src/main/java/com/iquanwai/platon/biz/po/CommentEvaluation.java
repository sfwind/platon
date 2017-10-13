package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by xfduan on 2017/8/2.
 */
@Data
public class CommentEvaluation {

    // db 字段
    private Integer id;
    private Integer submitId;
    private Integer commentId;
    private Integer useful;
    private String reason;
    private Integer evaluated;

    // 非 db 字段
    private String nickName; // 该条评论记录的评论人昵称

}
