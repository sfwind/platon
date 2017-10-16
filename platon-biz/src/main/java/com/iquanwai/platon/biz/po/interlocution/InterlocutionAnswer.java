package com.iquanwai.platon.biz.po.interlocution;

import lombok.Data;

import java.util.Date;

@Data
public class InterlocutionAnswer {
    private int id;
    private String answer; //回答
    private Integer questionId; //回答id
    private Integer profileId; //回答者id
    private Integer approvalCount; //赞同数
    private Date publishTime; //发布时间
    private Date lastModifiedTime; //最后修改时间
}
