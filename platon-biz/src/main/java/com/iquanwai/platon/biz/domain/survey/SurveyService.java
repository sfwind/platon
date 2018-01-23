package com.iquanwai.platon.biz.domain.survey;

import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.po.survey.SurveySubmitVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author nethunder
 */
@Service
public interface SurveyService {
    /**
     * 根据category加载题目信息
     *
     * @param category 问卷种类
     * @return 题目信息
     */
    List<SurveyQuestion> loadQuestionsByCategory(String category);

    /**
     * 答题
     *
     * @param submits 提交的信息
     * @return 提交结果
     */
    Integer submitQuestions(String openId,String category,Integer referId, List<SurveySubmitVo> submits);

    /**
     * 获取提交记录
     * @param openId
     * @param category
     * @return
     */
    SurveyResult loadSubmit(String openId, String category);
}
