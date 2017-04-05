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
    private Integer warmupComplete; //理解训练完成数量
    private Integer applicationComplete; //应用训练完成数量
    @Deprecated
    private Integer total; //任务总数
    @Deprecated
    private Integer keycnt; //钥匙数量
    private Integer currentSeries; //已解锁的题组
    private Integer totalSeries; //总题组
    private Problem problem; //非db字段 问题
    private List<Practice> practice; //非db字段
    private Integer length; //非db字段 总时长
    private Integer deadline; //非db字段 离截止日期天数
    private Boolean summary; //非db字段 是否显示任务总结
    private Integer series; //非db字段 当前组号
    private Boolean openRise; //非db字段 是否打开过rise
    private Boolean newMessage; //非db字段 是否有新消息
    private Boolean doneAllPractice; //非db字段 是否完成当日练习
    private Boolean doneAllApplication; //非db字段 是否完成所有应用训练
    private Boolean doneCurSerialApplication; //非db字段 是否完成当日所有应用训练
    private Integer completeSeries; // 非db字段 已完成的题组

    public final static int RUNNING = 1;
    public final static int COMPLETE = 2;
    public final static int CLOSE = 3;
}
