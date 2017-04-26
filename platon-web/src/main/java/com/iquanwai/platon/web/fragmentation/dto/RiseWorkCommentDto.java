package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
public class RiseWorkCommentDto {
    private Integer id;
    private String content;
    private String upName;
    private String upTime;
    private String headPic;
    private String signature;
    private Integer role;
}
