package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/4/5.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanLoadDto {
    private ImprovementPlan improvementPlan;
    private Boolean riseMember;
}
