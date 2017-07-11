package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Recommendation;

import java.util.List;

/**
 * Created by nethunder on 2017/6/8.
 */
public interface ReportService {

    ImprovementReport loadUserImprovementReport(ImprovementPlan plan);

    List<Recommendation> loadRecommendationByProblemId(Integer problemId);

}
