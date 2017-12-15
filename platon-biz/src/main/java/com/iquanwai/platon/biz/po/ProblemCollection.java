package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

@Data
public class ProblemCollection {

    private Integer Id;
    private Integer profileId; // 用户 id
    private Integer problemId; // 课程 id
    private Date collectTime; // 最新收藏时间
    private Integer del; // 是否删除收藏，0-未删除 1-已删除

}
