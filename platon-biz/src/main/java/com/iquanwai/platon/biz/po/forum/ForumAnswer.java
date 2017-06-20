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

    /** 非DB字段，业务字段 **/
    private String publishTimeStr;
    private String authorUserName; // 作者名字
    private String authorHeadPic; // 作者头像
    private Boolean approval; // 是否已经赞同

}
