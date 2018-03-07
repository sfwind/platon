package com.iquanwai.platon.biz.domain.fragmentation.manager;

import java.util.List;

/**
 * Created by justin on 2017/11/11.
 */
public interface ProblemScheduleManager {
    /**
     * 获取章节地图
     * @param planId 小课计划id
     */
    List<Chapter> loadRoadMap(Integer planId);

    /**
     * 获取默认章节地图
     * @param problemId 小课id
     */
    List<Chapter> loadDefaultRoadMap(Integer problemId);

    /**
     * 获取课程类型
     * @param problemId 小课id
     * @param profileId 用户id
     */
    Integer getProblemType(Integer problemId, Integer profileId);

    /**
     * 获取当前学习月份主修课 ProblemId
     * @param profileId 用户 id
     */
    Integer getLearningMajorProblemId(Integer profileId);

    Integer getMajorProblemIdByYearAndMonth(Integer profileId, Integer year, Integer month);
}
