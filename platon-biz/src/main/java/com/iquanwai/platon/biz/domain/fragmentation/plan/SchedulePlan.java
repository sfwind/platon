package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/11/4.
 */
@Data
public class SchedulePlan {
    private int minorPercent; //辅修进度
    private int majorPercent; //主修进度
    private List<ImprovementPlan> runningProblem; // 辅修课程
    private List<ImprovementPlan> completeProblem; // 辅修课程
    private String topic; //本月主题
    private int month; //本月
    private String today; //今天日期

}
