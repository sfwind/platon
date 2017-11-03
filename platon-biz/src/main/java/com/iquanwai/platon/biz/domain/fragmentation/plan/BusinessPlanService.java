package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.CourseSchedule;

import java.util.List;

/**
 * Created by justin on 2017/11/3.
 */
public interface BusinessPlanService {
    /**
     * 获取用户的课程计划
     * @param profileId 用户id
     * */
    List<CourseSchedule> getPlan(Integer profileId);

    /**
     * 获取用户已经开始的课程计划
     * @param profileId 用户id
     * */
    List<CourseSchedule> getOpeningPlan(Integer profileId);
}
