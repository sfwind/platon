package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanSeriesStatus;
import lombok.Data;

import java.util.List;

@Data
public class SectionProgressDto {

    private String planSeriesTitle; // 当前小节名称
    private List<PlanSeriesStatus> planSeriesStatuses; // 当前小节各个类型题目的完成状态

}
