package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.fragmentation.plan.PersonalSchedulePlan;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class PersonalCoursePlanDto {

    private List<PersonalSchedulePlan.SchedulePlan> runningPlans;
    private List<PersonalSchedulePlan.SchedulePlan> completePlans;
    private int loginCount;
    private int joinDays;
    private int totalPoint;
    private String announce;

}
