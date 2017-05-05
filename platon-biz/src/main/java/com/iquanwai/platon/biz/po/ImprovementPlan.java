package com.iquanwai.platon.biz.po;

import com.iquanwai.platon.biz.domain.fragmentation.plan.Practice;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class ImprovementPlan {
    private int id;
    private String openid; //openid
    private Integer problemId; //问题id
    private Date startDate; //开始日期
    private Date endDate; //结束日期(当日开始复习)
    private Date closeDate; //课程关闭时间（课程关闭日期）
    private Date completeTime; // 完成时间
    private Integer status; //执行状态（1-正在进行, 2-已结束, 3-已过期）
    private Integer point; //积分
    private Integer warmupComplete; //巩固练习完成数量
    private Integer applicationComplete; //应用练习完成数量
    @Deprecated
    private Integer total; //任务总数
    @Deprecated
    private Integer keycnt; //钥匙数量
    private Integer currentSeries; //当前访问的节数
    private Integer completeSeries; //已完成的节数
    private Integer totalSeries; //总节数
    private Boolean riseMember; // 是否是会员

    // ----------------- 非db字段------------------
    private Problem problem; //非db字段 问题
    private List<Practice> practice; //非db字段
    private Integer length; //非db字段 总时长
    private Integer deadline; //非db字段 离截止日期天数
    private Boolean summary; //非db字段 是否显示任务总结
    private Integer series; //非db字段 当前节号
    private Boolean openRise; //非db字段 是否打开过rise
    private Boolean newMessage; //非db字段 是否有新消息
    private String chapter; // 非db字段 章名称
    private String section; // 非db字段 节名称
    // 首页弹窗信息
    private Boolean doneAllIntegrated; //非db字段 是否完成所有综合练习
    private Boolean doneCurSeriesApplication; //非db字段 是否完成当日所有应用练习
    private String alertMsg; //非db字段 弹窗内容


    public final static int RUNNING = 1;
    public final static int COMPLETE = 2;
    public final static int CLOSE = 3;
}
