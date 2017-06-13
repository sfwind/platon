package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/5/2.
 */
@Data
public class AsstCoachComment {
    private int id;
    private String openid; //评论人openid
    private Integer profileId; //评论人id
    private Integer count; //评论次数
    private Integer problemId; //小课id
}
