package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumQuestion {
    private int id;
    private String topic; //标题
    private String description; //问题详情
    private Integer profileId; //提问者id
    private Integer followCount; //关注数
    private Integer openCount; //打开数
    private Integer weight; //排序权重
}
