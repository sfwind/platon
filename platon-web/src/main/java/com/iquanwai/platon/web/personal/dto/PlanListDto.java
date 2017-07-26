package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.common.MemberType;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
public class PlanListDto {
    private String riseId;
    private List<PlanDto> runningPlans;
    private List<PlanDto> donePlans;
//    private Integer riseMember;
    private MemberType memberType;
    private Integer point;
}
