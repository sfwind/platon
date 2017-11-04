package com.iquanwai.platon.biz.domain.fragmentation.plan;

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
    private List<Problem> majorProblem; // 主修课程
    private List<Problem> minorProblem; // 辅修课程
    private List<Problem> completeProblem; // 辅修课程
    private List<Problem> trialProblem; // 试听课程
    private String topic; //本月主题
    private int month; //本月
    private String today; //今天日期

}
