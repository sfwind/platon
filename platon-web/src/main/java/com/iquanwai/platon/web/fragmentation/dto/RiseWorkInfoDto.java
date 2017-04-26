package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.ArticleLabel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
public class RiseWorkInfoDto {
    private String title;
    private String userName;
    private String submitUpdateTime;
    private String headImage;
    private String content;
    private Integer voteCount;
    private Integer commentCount;
    private Integer submitId;
    private Integer type;
    private Integer voteStatus;
    private Date publishTime;

    private Integer priority;
    private Boolean perfect;
    private Integer problemId;
    private Integer authorType;
    private Boolean isMine;
    private List<ArticleLabel> labelList;
    private List<String> picList;
    private Integer role;
    private String signature;
}
