package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/7/7.
 */
@Data
public class PlanListDto {
    private List<ImprovementPlan> runningPlans;
    private List<ImprovementPlan> completedPlans;
    private Boolean riseMember;
}
