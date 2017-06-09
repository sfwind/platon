package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemScore;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;

import java.util.List;

/**
 * Created by justin on 16/12/8.
 */
public interface ProblemService {
    /**
    * 获取所有工作中的问题
    * */
    List<Problem> loadProblems();

    /**
     * 根据问题id获取问题
     * @param problemId 问题id
     * */
    Problem getProblem(Integer problemId);

    /**
     * 获得问题的目录分类
     */
    List<ProblemCatalog> getProblemCatalogs();

    /**
     * 获得问题的分类
     * @param catalogId 分类
     */
    ProblemCatalog getProblemCatalog(Integer catalogId);

    /**
     * 获得问题的子分类
     * @param subCatalogId 子分类
     */
    ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId);

    /**
     * 给问题打分
     * @param openId 用户id
     * @param problemId 小课id
     * @param problemScores 小课打分
     */
    void gradeProblem(Integer problemId, String openId, Integer profileId, List<ProblemScore> problemScores);

    /**
     * 用户是否已对问题评分
     * @param profileId 用户id
     * @param problemId 小课id
     */
    boolean hasProblemScore(Integer profileId, Integer problemId);

}
