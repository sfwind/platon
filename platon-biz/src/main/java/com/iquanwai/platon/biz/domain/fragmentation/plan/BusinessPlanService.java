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
     * 获取个人与月份相关的学习计划安排
     * @param profileId 个人 id
     */
    List<List<CourseSchedule>> loadPersonalCourseSchedule(Integer profileId);

    /**
     * 获取默认小课计划安排表
     */
    List<List<CourseSchedule>> loadDefaultCourseSchedule();

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

    /**
     * 个人计划中小课对应学习年月的更改
     * @param profileId 个人 id
     * @param problemId 小课 id
     * @param targetYear 目标年份
     * @param targetMonth 目标月份
     */
    boolean modifyProblemSchedule(Integer profileId, Integer problemId, Integer targetYear, Integer targetMonth);

    /**
     * 更新学习计划中一门小课的选择状态
     * @param courseScheduleId 该门小课，在 CourseSchedule 中的记录 id
     * @param selected 是否选择 0-未选择 1-选择
     */
    boolean updateProblemScheduleSelected(Integer courseScheduleId, Boolean selected);

    void batchModifyCourseSchedule(Integer year, Integer month, List<CourseSchedule> courseSchedules);
}
