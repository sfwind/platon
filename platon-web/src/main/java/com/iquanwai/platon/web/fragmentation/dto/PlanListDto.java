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
    private List<PlanDto> currentCampPlans; // 正在进行的专项课课程
    private List<PlanDto> runningPlans;
    private List<PlanDto> completedPlans;
    private List<Problem> recommendations;
    private Integer riseMember;

    // 专项课宣传 banner
    private String campBanner;
}
