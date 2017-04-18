package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemPlan;
import com.iquanwai.platon.biz.po.ProblemScore;

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

    /**
     * 获得问题的目录分类
     */
    List<ProblemCatalog> getProblemCatalogs();

    /**
     * 给问题打分
     * @param openId 用户id
     * @param problemId 小课id
     * @param problemScores 小课打分
     */
    void gradeProblem(Integer problemId, String openId, List<ProblemScore> problemScores);

    /**
     * 用户是否已对问题评分
     * @param openId 用户id
     * @param problemId 小课id
     */
    boolean hasProblemScore(String openId,Integer problemId);
}
