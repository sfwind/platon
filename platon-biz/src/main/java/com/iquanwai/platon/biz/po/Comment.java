package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by nethunder on 2017/1/20.
 */
@Data
public class Comment {
    private Integer id;
    private Integer type;
    private Integer moduleId;
    private Integer referencedId;
    private String commentOpenId;
    private String content;
    private Integer del;
    private Integer device;
    private Date AddTime;
}
