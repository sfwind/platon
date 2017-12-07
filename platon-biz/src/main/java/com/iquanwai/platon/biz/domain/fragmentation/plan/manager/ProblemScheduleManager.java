package com.iquanwai.platon.biz.domain.fragmentation.plan.manager;

import java.util.List;

/**
 * Created by justin on 2017/11/11.
 */
public interface ProblemScheduleManager {
    /**
     * 获取章节地图
     * @param planId 小课计划id
     * */
    List<Chapter> loadRoadMap(Integer planId);

    /**
     * 获取章节地图
     * @param problemId 小课id
     * */
    List<Chapter> loadDefaultRoadMap(Integer problemId);
}
