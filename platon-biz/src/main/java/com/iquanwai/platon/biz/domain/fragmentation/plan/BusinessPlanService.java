package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.CourseSchedule;

import java.util.List;
import java.util.Map;

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
     * 获取个人与月份相关的学习计划安排
     * @param profileId 个人 id
     */
    Map<Integer, List<CourseSchedule>> getPersonalCourseSchedule(Integer profileId);

    /**
     * 查看某门小课的计划能否更改
     * @param profileId 用户 id
     * @param problemId 小课 id
     */
    boolean checkProblemModifyAccess(Integer profileId, Integer problemId);

    /**
     * 获取用户的课程进度
     * @param profileId 用户 id
     * */
    SchedulePlan getSchedulePlan(Integer profileId);
}
