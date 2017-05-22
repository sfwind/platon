package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;
import com.iquanwai.platon.biz.po.WarmupPractice;

import java.util.List;

/**
 * Created by justin on 17/1/1.
 */
public interface CacheService {
    /** 获取所有的小课*/
    List<Problem> getProblems();
    /** 获取某个小课*/
    Problem getProblem(Integer problemId);
    /** 获取某个知识点*/
    Knowledge getKnowledge(Integer knowledgeId);
    /** 获取某个巩固练习*/
    WarmupPractice getWarmupPractice(Integer practiceId);
    /** 获取某个问题的主分类 **/
    ProblemCatalog getProblemCatalog(Integer catalogId);
    /** 获取某个问题的子分类 **/
    ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId);
    /** 获取所有问题主分类 **/
    List<ProblemCatalog> loadProblemCatalogs();

    /** 更新缓存*/
    void reload();
}
