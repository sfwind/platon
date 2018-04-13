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

    /**
     * 获取当前学习月份主修课 ProblemId
     * @param profileId 用户 id
     * @param year 年份
     * @param month 月份
     */
    Integer getMajorProblemIdByYearAndMonth(Integer profileId, Integer year, Integer month);

    /**
     * 获取当前年月的主修课程
     * @param profileId 学员 id
     * @param memberTypeId 身份 id
     * @param year 证书年份
     * @param month 证书月份
     * @return 主修课程 id 集合
     */
    List<Integer> getMajorProblemIds(Integer profileId, Integer memberTypeId, Integer year, Integer month);

    /**
     * 获取个人对应年月的所有主修课 id
     * @param profileId 用户 id
     * @param year 学习对应年份
     * @param month 学习对应月份
     * @return 所有主修课课程 id
     */
    List<Integer> getMajorProblemIds(Integer profileId, Integer year, Integer month);
}
