package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/7/7.
 */
@Data
public class PlanListDto {
    private List<PlanDto> runningPlans;
    private List<PlanDto> completedPlans;
    private List<PlanDto> trialClosedPlans;
    private List<Problem> recommendations;
    private Boolean riseMember;
    private Boolean openNavigator;
    private Boolean openWelcome;
}
