package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;

import java.util.List;

/**
 * Created by justin on 2017/11/3.
 */
public interface BusinessPlanService {

    PersonalSchedulePlan getPersonalSchedulePlans(Integer profileId);

    /**
     * 获取个人与月份相关的学习计划安排
     *
     * @param profileId 个人 id
     */
    List<List<CourseSchedule>> loadPersonalCourseSchedule(Integer profileId);

    /**
     * 获取用户的课程进度
     *
     * @param profileId 用户 id
     */
    SchedulePlan getSchedulePlan(Integer profileId);

    /**
     * 个人计划中课程对应学习年月的更改
     *
     * @param profileId   个人 id
     * @param problemId   课程 id
     * @param targetYear  目标年份
     * @param targetMonth 目标月份
     */
    boolean modifyProblemSchedule(Integer profileId, Integer problemId, Integer targetYear, Integer targetMonth);

    /**
     * 更新学习计划中一门课程的选择状态
     * @param courseScheduleId 该门课程，在 CourseSchedule 中的记录 id
     * @param selected 是否选择 0-未选择 1-选择
     */
    boolean updateProblemScheduleSelected(Integer courseScheduleId, Boolean selected);

    void batchModifyCourseSchedule(Integer year, Integer month, List<CourseSchedule> courseSchedules);
    /**
     * 根据选择题答案初始化课表
     * <p>具体流程：</p>
     * <ol>
     * <li>存储选择题id</li>
     * <li>获取对应类型的默认课表</li>
     * <li>按月份生成所有课程表</li>
     * </ol>
     *
     * @param profileId         用户 id
     * @param scheduleQuestions 格式：题目id-答案id
     */
    void initCourseSchedule(Integer profileId, List<ScheduleQuestion> scheduleQuestions);

    /**
     * 获取课程表选择题内容
     *
     * @return 课程表选择题
     */
    List<ScheduleQuestion> loadScheduleQuestions(Integer profileId);

    void initCourseSchedule(Integer profileId, Integer memberTypeId);
}
