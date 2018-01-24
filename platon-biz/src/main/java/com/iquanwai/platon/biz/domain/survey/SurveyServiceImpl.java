package com.iquanwai.platon.biz.domain.survey;


import com.iquanwai.platon.biz.dao.survey.SurveyChoiceDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionResultDao;
import com.iquanwai.platon.biz.dao.survey.SurveyResultDao;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.survey.SurveyChoice;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionResult;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.po.survey.SurveySubmitVo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nethunder
 * 问卷servie
 */
@Service
public class SurveyServiceImpl implements SurveyService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SurveyChoiceDao surveyChoiceDao;
    @Autowired
    private SurveyQuestionDao surveyQuestionDao;
    @Autowired
    private SurveyQuestionResultDao surveyQuestionSubmitDao;
    @Autowired
    private SurveyResultDao surveyResultDao;

    @Override
    public List<SurveyQuestion> loadQuestionsByCategory(String category) {
        List<SurveyQuestion> questions = surveyQuestionDao.loadQuestionByCategory(category);
        List<String> codes = questions.stream().map(SurveyQuestion::getQuestionCode).collect(Collectors.toList());
        List<SurveyChoice> choices = surveyChoiceDao.loadChoicesByQuestionCode(codes);

        Map<String, List<SurveyChoice>> choiceMap = choices.stream().collect(Collectors.groupingBy(SurveyChoice::getQuestionCode));
        questions.forEach(question -> {
            List<SurveyChoice> questionChoices = choiceMap.get(question.getQuestionCode());
            if (questionChoices != null) {
                question.setChoices(questionChoices);
            }
        });

        return questions;
    }

    @Override
    public Integer submitQuestions(String openId, String category, Integer referId, List<SurveySubmitVo> submits) {
        SurveyQuestion surveyQuestion = surveyQuestionDao.loadOneQuestion(category);
        SurveyResult surveyResult = new SurveyResult();
        surveyResult.setCategory(category);
        surveyResult.setOpenid(openId);
        surveyResult.setReferSurveyId(referId);
        Integer version = null;
        if (surveyQuestion != null) {
            version = surveyQuestion.getVersion();
        }
        surveyResult.setVersion(version);

        Integer result = surveyResultDao.insert(surveyResult);
        if (result < 1) {
            logger.error("提交问卷失败,openId:{},category:{},version:{},referId:{}", openId, category, surveyQuestion, referId);
        }
        List<SurveyQuestionResult> collect = submits.stream().map(item -> {
            SurveyQuestionResult submit = new SurveyQuestionResult();
            submit.setQuestionCode(item.getQuestionCode());
            submit.setCategory(category);
            submit.setChoiceId(item.getChoiceId());
            submit.setUserValue(item.getUserValue());
            if (CollectionUtils.isNotEmpty(item.getChoiceIds())) {
                submit.setChoiceIds(String.join(",", item.getChoiceIds()));
            }
            return submit;
        }).collect(Collectors.toList());
        surveyQuestionSubmitDao.batchInsert(collect);
        return result;
    }



    @Override
    public SurveyResult loadSubmit(String openId, String category) {
        return surveyResultDao.loadByOpenid(openId, category);
    }

    @Override
    public SurveyResult loadSubmit(Integer id) {
        return surveyResultDao.load(SurveyResult.class, id);
    }

}
