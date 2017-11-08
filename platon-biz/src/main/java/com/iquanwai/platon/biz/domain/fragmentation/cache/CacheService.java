package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.iquanwai.platon.biz.domain.fragmentation.plan.Chapter;
import com.iquanwai.platon.biz.po.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/1/1.
 */
public interface CacheService {
    /** 获取所有的小课 */
    List<Problem> getProblems();

    /** 获取某个小课 */
    Problem getProblem(Integer problemId);

    /** 获取某个知识点 */
    Knowledge getKnowledge(Integer knowledgeId);

    /** 获取某个巩固练习 */
    WarmupPractice getWarmupPractice(Integer practiceId);

    List<Chapter> loadRoadMap(Integer problemId);

    /** 获取某个问题的主分类 **/
    ProblemCatalog getProblemCatalog(Integer catalogId);

    /** 获取某个问题的子分类 **/
    ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId);

    /** 获取所有问题主分类 **/
    List<ProblemCatalog> loadProblemCatalogs();

    MonthlyCampConfig loadMonthlyCampConfig();

    /**
     * 获取商学院计划每月主题
     */
    Map<Integer, String> loadMonthTopic(Integer category, Integer year);

    /** 更新缓存 */
    void reload();

    void reloadMonthlyCampConfig();
}
