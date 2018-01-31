package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by justin on 16/12/8.
 */
public interface ProblemService {

    Integer MAX_RECOMMENDATION_SIZE = 6;

    /**
     * 获取所有课程
     */
    List<Problem> loadProblems();

    /**
     * 根据课程id获取问题
     *
     * @param problemId 课程id
     */
    Problem getProblem(Integer problemId);

    /**
     * 根据练习id获取课程
     *
     * @param practicePlanId 练习id
     */
    Problem getProblemForSchedule(Integer practicePlanId, Integer profileId);

    /**
     * 获得课程的目录分类
     */
    List<ProblemCatalog> getProblemCatalogs();

    /**
     * 获得课程的分类
     *
     * @param catalogId 分类
     */
    ProblemCatalog getProblemCatalog(Integer catalogId);

    /**
     * 获得课程的子分类
     *
     * @param subCatalogId 子分类
     */
    ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId);

    /**
     * 给问题打分
     *
     * @param profileId        用户id
     * @param problemId     课程id
     * @param problemScores 课程打分
     */
    void gradeProblem(Integer problemId, Integer profileId, List<ProblemScore> problemScores);

    /**
     * 更新 ProblemExtension 数据
     */
    Integer insertProblemExtension(ProblemExtension problemExtension);

    /**
     * 提交 ProblemActivity 数据
     */
    Integer insertProblemActivity(ProblemActivity problemActivity);

    /**
     * 根据 ProblemId 获取 ProblemExtension 信息
     *
     * @param problemId
     * @return
     */
    ProblemExtension loadProblemExtensionByProblemId(Integer problemId);

    /**
     * 根据 ProblemId 获取 ProblemActivity 列表
     *
     * @param problemId
     * @return
     */
    List<ProblemActivity> loadProblemActivitiesByProblemId(Integer problemId);

    /**
     * 根据 planId 获取卡包页面数据
     */
    Pair<Problem, List<EssenceCard>> loadProblemCards(Integer planId);

    /**
     * 获取精华卡图
     */
    String loadEssenceCardImg(Integer profileId, Integer problemId, Integer chapterId);

    boolean hasCollectedProblem(Integer profileId, Integer problemId);

    int collectProblem(Integer profileId, Integer problemId);

    int disCollectProblem(Integer profileId, Integer problemId);

    List<Problem> loadProblemCollections(Integer profileId);

    List<Problem> loadHotProblems(List<Integer> problemIds);

    List<ExploreBanner> loadExploreBanner();
}
