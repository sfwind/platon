package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by xfduan on 2017/5/12.
 * 应用训练评论回复
 */
@Data
public class ApplicationCommentDto {
    private Integer id;
    private String name;
    private String avatar;
    private String discussTime;
    private Integer priority;
    private String comment;
    private String repliedComment;
    private String repliedName;
    private Integer warmupPracticdId;
    private Integer role;
    private String signature;
    private Boolean isMine;
    private Integer repliedDel;
    private Integer repliedId;
}
