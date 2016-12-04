package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ImprovementPlan {
    private int id;
    private String openid; //openid
    private Integer problemId; //问题id
    private Date startDate; //开始日期
    private Date endDate; //结束日期
    private Integer status; //执行状态（0-未开始，1-正在进行，2-已结束）
    private Integer score; //积分
    private Integer complete; //完成训练个数
    private Integer total; //任务总数
    private Boolean readWizard; //是否已阅读玩法
}
