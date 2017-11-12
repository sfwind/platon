package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;

import java.util.List;

/**
 * Created by justin on 2017/11/3.
 */
public interface BusinessPlanService {
    /**
     * 获取用户的课程计划
     *
     * @param profileId 用户id
     */
    List<CourseSchedule> getPlan(Integer profileId);

    /**
     * 获取个人与月份相关的学习计划安排
     *
     * @param profileId 个人 id
     */
    List<List<CourseSchedule>> loadPersonalCourseSchedule(Integer profileId);

    /**
     * 获取默认小课计划安排表
     *
     * @param profileId 个人 id
     */
    List<List<CourseSchedule>> loadDefaultCourseSchedule(Integer profileId);

    /**
     * 查看某门小课的计划能否更改
     *
     * @param profileId 用户 id
     * @param problemId 小课 id
     */
    boolean checkProblemModifyAccess(Integer profileId, Integer problemId);

    /**
     * 获取用户的课程进度
     *
     * @param profileId 用户 id
     */
    SchedulePlan getSchedulePlan(Integer profileId);

    /**
     * 个人计划中小课对应学习年月的更改
     *
     * @param profileId   个人 id
     * @param problemId   小课 id
     * @param targetYear  目标年份
     * @param targetMonth 目标月份
     */
    boolean modifyProblemSchedule(Integer profileId, Integer problemId, Integer targetYear, Integer targetMonth);

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
    List<ScheduleQuestion> loadScheduleQuestions();
}