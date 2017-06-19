package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumAnswer {
    private int id;
    private String answer; //回答
    private Integer questionId; //回答id
    private Integer profileId; //回答者id
    private Integer approvalCount; //赞同数
    private Date publishTime; //发布时间
    private Date lastModifiedTime; //最后修改时间
}
