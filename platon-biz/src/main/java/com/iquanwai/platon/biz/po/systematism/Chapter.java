package com.iquanwai.platon.biz.po.systematism;

import lombok.Data;

/**
 * Created by justin on 16/8/25.
 */
@Data
public class Chapter {
    private int id;
    private Integer courseId; //课程id
    private String name;  //章节名称
    private Integer type; //章节类型（1-挑战，2-作业，3-讨论，4-休息，5-毕业，6-任务挑战，7-任务作业）
    private Integer sequence; //课程内顺序
    private Integer week; //章节所在的周序号
    private Integer startDay; //开始于第几天
    private Integer endDay; //结束于第几天
    private boolean unlock; //是否解锁 非db字段
    private boolean complete; //是否完成 非db字段
    private String icon; //icon链接 非db字段
    private Integer pageSequence; //当前第几页 非db字段
    private Integer totalPage; //一共几页 非db字段
    private String comment; //点击反馈 非db字段
}
