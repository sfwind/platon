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
    private List<PlanDto> currentCampPlans; // 正在进行的训练营课程
    private List<PlanDto> runningPlans;
    private List<PlanDto> completedPlans;
    private List<Problem> recommendations;
    private List<PlanDto> auditions;
    private Integer riseMember;

    // 训练营宣传 banner
    private String campBanner;
}
