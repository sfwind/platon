package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by xfduan on 2017/6/6.
 */
@Data
public class ApplicationSubmitDraft {

    private int id;
    private Integer profileId; // 用户 id
    private Integer applicationId; // 应用练习id
    private Integer planId; // 提升计划id
    private String content; // 提交内容
    private Integer length; // 字数
    private Integer priority; // 是否覆盖本地 localStorage
    private Date addTime; // 数据添加时间
    private Date updateTime; // 最后更新时间

}
