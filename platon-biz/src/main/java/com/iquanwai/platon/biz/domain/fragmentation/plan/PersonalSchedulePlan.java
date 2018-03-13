package com.iquanwai.platon.biz.domain.fragmentation.plan;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class PersonalSchedulePlan {

    private List<SchedulePlan> runningPlans;
    private List<SchedulePlan> completePlans;

    @Data
    public static class SchedulePlan {
        private int problemId;
        private String name; // 课程名字
        private String description; // 描述信息，如9月主修
        // private int month;
        private int type; // 课程类型
        private Boolean isLearning; // 是否正在学习
        // private int point;
        private int completeSeries; // 完成节数
        private int totalSeries; // 总节数
        private int remainDaysCount; // 剩余时间
        private String completeTime; // 课程完成时间
    }

}
