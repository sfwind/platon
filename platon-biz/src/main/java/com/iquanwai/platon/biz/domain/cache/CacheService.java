package com.iquanwai.platon.biz.domain.cache;

import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.MemberType;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/1/1.
 */
public interface CacheService {
    /** 获取所有的课程 */
    List<Problem> getProblems();

    /** 获取某个课程 */
    Problem getProblem(Integer problemId);

    /** 获取某个知识点 */
    Knowledge getKnowledge(Integer knowledgeId);

    /** 获取某个巩固练习 */
    WarmupPractice getWarmupPractice(Integer practiceId);

    /** 获取某个问题的主分类 **/
    ProblemCatalog getProblemCatalog(Integer catalogId);

    /** 获取某个问题的子分类 **/
    ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId);

    /** 获取所有问题主分类 **/
    List<ProblemCatalog> loadProblemCatalogs();

    /**
     * 获取当月专项课配置
     * */
    MonthlyCampConfig loadMonthlyCampConfig();

    /**
     * 获取商学院计划每月主题
     */
    Map<Integer, String> loadMonthTopic(Integer category);

    /**
     * 获取售卖商品说明
     * @return
     */
    List<MemberType> loadMemberTypes();

    /** 更新缓存 */
    void reload();

    void reloadMonthlyCampConfig();
}
