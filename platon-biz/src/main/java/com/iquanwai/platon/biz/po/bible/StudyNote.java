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
    private String source;
    private Integer catalogId;
    private String name;
    private Integer page;
    private String url;
    private String note;
    private Integer minute;
    private Date publishTime;
    private Date lastModifiedTime;
    private Boolean del;

    // 非DB字段
    private List<Integer> tagIds;
    private List<SubscribeArticleTag> tags;


    public static final int BOOK = 1;
    public static final int COURSE = 2;
    public static final int VIDEO = 3;
    public static final int AUDIO = 4;
    public static final int CHAIR = 5;
    public static final int PROJECT = 6;


}
