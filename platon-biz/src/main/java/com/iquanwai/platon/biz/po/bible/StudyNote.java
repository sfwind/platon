package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/14.
 */
@Data
public class StudyNote {
    private Integer id;
    private Integer profileId;
    private String name;
    private Integer page;
    private String url;
    private String note;
    private Integer Minute;
    private Date publishTime;
    private Date lastModifiedTime;
    private Boolean del;

    // 非DB字段
    private List<Integer> tagIds;
    private List<SubscribeArticleTag> tags;
}
