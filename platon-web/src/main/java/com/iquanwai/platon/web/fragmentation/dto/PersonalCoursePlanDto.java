package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.fragmentation.plan.CountDownElement;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PersonalSchedulePlan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("个人课表")
public class PersonalCoursePlanDto {

    @ApiModelProperty("进行中的学习计划")
    private List<PersonalSchedulePlan.SchedulePlan> runningPlans;
    @ApiModelProperty("已完成学习计划")
    private List<PersonalSchedulePlan.SchedulePlan> completePlans;
    @ApiModelProperty("连续登录天数")
    private int loginCount;
    @ApiModelProperty("加入圈外天数")
    private int joinDays;
    @ApiModelProperty("总积分")
    private int totalPoint;
    @ApiModelProperty("通知")
    private String announce;
    @ApiModelProperty("是否制定学习计划")
    private Boolean hasCourseSchedule;
    @ApiModelProperty("倒计时信息")
    private CountDownElement countDownElement;

}
