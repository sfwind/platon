package com.iquanwai.platon.biz.po;

import com.iquanwai.platon.biz.domain.fragmentation.plan.Section;
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
    @Deprecated
    private Integer warmupComplete; //巩固练习完成数量
    @Deprecated
    private Integer applicationComplete; //应用练习完成数量
    @Deprecated
    private Integer total; //任务总数
    @Deprecated
    private Integer keycnt; //钥匙数量
    private Integer currentSeries; //当前访问的节数
    private Integer completeSeries; //已完成的节数
    private Integer totalSeries; //总节数
    private Boolean riseMember; // 是否是会员
    private Integer requestCommentCount; //求点赞次数

    // ----------------- 非db字段------------------
    private Problem problem; //非db字段 问题
    private List<Section> sections; //非db字段
    private Boolean openRise; //非db字段 是否打开过rise
    private Integer deadline; //非db字段 离截止日期天数
    private Boolean hasProblemScore; //是否已打分
    private Boolean doneAllIntegrated; //是否做完所有综合练习
    private Integer lockedStatus = -1; //-1 之前必做练习未完成,-2 非会员未解锁,-3 小课已过期

    public final static int RUNNING = 1;
    public final static int COMPLETE = 2;
    public final static int CLOSE = 3;
}
