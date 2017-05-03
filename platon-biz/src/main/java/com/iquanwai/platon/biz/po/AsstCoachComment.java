package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 17/5/2.
 */
@Data
public class AsstCoachComment {
    private int id;
    private String openid; //被评论人openid
    private Integer count; //被评论次数
    private Integer problemId; //小课id
}
