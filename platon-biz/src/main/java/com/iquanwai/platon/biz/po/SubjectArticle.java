package com.iquanwai.platon.biz.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by nethunder on 2017/3/8.
 */
@Data
@NoArgsConstructor
public class SubjectArticle {
    private Integer id;
    private String openid; //提交用户openid
    private Integer problemId; //难题id
    private Integer authorType; //发布者类型
    private Integer sequence; // 排序字段,非0时是精彩评论
    private String title; // 标题
    private String content; //提交内容
    private Integer pointStatus; //是否已加分（0-否，1-是）
    private Integer length; //字数
    private Date updateTime; //最后更新时间
    private Boolean requestFeedback; //是否求点评

    public SubjectArticle(Integer id,String openid,Integer problemId,Integer authorType,Integer sequence,String title,String content){
        this.id = id;
        this.openid = openid;
        this.problemId = problemId;
        this.authorType = authorType;
        this.sequence = sequence;
        this.title = title;
        this.content = content;
    }
}
