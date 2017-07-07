package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by xfduan on 2017/7/5.
 */
@Data
public class ProblemActivity {

    /**
     * db 字段
     */
    private Integer id;
    private Integer problemId; // 小课 Id
    private String description; // 活动描述
    private String location; // 活动地址
    private String workshop; // 工作坊名称
    private String uri; // 资源链接地址
    private String password; // 密码
    private Integer type; // 活动类型 0-线上 1-线下
    private String addTime;
    private String updateTime;

    /**
     * Type 字段分别对应值
     */
    public static final Integer Online = 0;
    public static final Integer Offline = 1;

}
