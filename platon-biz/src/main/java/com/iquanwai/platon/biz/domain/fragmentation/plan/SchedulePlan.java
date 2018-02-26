package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/11/4.
 */
@Data
public class SchedulePlan {
    private int majorTotal; // 主修总章节数
    private int majorComplete; // 主修完成章节数
    private int minorTotal; //辅修总章节数
    private int minorComplete; // 辅修完成章节数
    private List<ImprovementPlan> runningProblem; // 辅修课程
    private List<ImprovementPlan> completeProblem; // 辅修课程
    private String topic; //本月主题
    private int month; //本月
    private String today; //今天日期
    private boolean minorSelected; // 当月是否选了辅修课

}
