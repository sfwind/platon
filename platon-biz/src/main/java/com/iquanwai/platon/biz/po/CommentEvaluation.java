package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by xfduan on 2017/8/2.
 */
@Data
public class CommentEvaluation {

    private Integer id;
    private Integer profileId;
    private Integer commentId;
    private Integer targetId;
    private Integer level;
    private String reason;
    private Integer evaluated;

}