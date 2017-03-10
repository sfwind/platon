package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemPlan;

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
     * 保存学员希望解决的问题
     * @param problemIds 问题id列表
     * @param openid 学员id
     * */
    void saveProblems(List<Integer> problemIds, String openid);

    /**
     * 获取学员的问题
     * @param openid 学员id
     * */
    List<ProblemPlan> loadProblems(String openid);

    /**
     * 根据问题id获取问题内容
     * @param problemId 问题id
     * */
    String getProblemContent(Integer problemId);

    /**
     * 根据问题id获取问题
     * @param problemId 问题id
     * */
    Problem getProblem(Integer problemId);

    List<ProblemCatalog> getProblemCatalogs();
}
