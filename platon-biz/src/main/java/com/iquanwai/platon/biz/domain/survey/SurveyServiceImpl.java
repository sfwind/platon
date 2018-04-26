package com.iquanwai.platon.biz.domain.survey;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iquanwai.platon.biz.dao.survey.SurveyChoiceDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionResultDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionTypeDao;
import com.iquanwai.platon.biz.dao.survey.SurveyResultDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.survey.SurveyChoice;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionResult;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionType;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.po.survey.SurveySubmitVo;
import com.iquanwai.platon.biz.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    @Autowired
    private AccountService accountService;
    @Autowired
    private SurveyQuestionTypeDao surveyQuestionTypeDao;


    @Override
    public List<SurveyQuestion> loadQuestionsByCategory(String category) {
        List<SurveyQuestion> questions = surveyQuestionDao.loadQuestionByCategory(category);
        List<String> codes = questions.stream().map(SurveyQuestion::getQuestionCode).collect(Collectors.toList());
        List<SurveyChoice> choices = surveyChoiceDao.loadChoicesByQuestionCode(codes);

        Map<String, List<SurveyChoice>> choiceMap = choices.stream().collect(Collectors.groupingBy(SurveyChoice::getQuestionCode));
        questions.forEach(question -> {
            List<SurveyChoice> questionChoices = choiceMap.get(question.getQuestionCode());
            if (questionChoices != null) {
                questionChoices.sort(Comparator.comparingInt(SurveyChoice::getSequence));
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
            submit.setSubmitId(result);
            if (CollectionUtils.isNotEmpty(item.getChoiceIds())) {
                submit.setChoiceIds(String.join(",", item.getChoiceIds()));
            }
            return submit;
        }).collect(Collectors.toList());
        surveyQuestionSubmitDao.batchInsert(collect);
        Profile profile = accountService.getProfile(openId);
        if (profile != null) {
            for (SurveyQuestion question : surveyQuestionDao.loadByCategoryAndMemoNotNull(category)) {
                try {
                    JSONObject memo = JSON.parseObject(question.getMemo());
                    if (SurveyQuestion.MEMO_TYPE.PHONE.equals(memo.getString("type"))) {
                        // 电话 ignore validCode的时候会加进去
                    } else if (SurveyQuestion.MEMO_TYPE.WECHAT_CODE.equals(memo.getString("type"))) {
                        // 微信id
                        collect.stream()
                                .filter(item -> item.getQuestionCode().equals(question.getQuestionCode()))
                                .filter(item -> item.getUserValue() != null)
                                .findFirst()
                                .ifPresent(surveySubmit -> accountService.updateWeixinId(profile.getId(), surveySubmit.getUserValue()));
                    }
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }


    @Override
    public SurveyResult loadSubmitByReferId(String openId, Integer referId) {
        return surveyResultDao.loadByOpenidAndReferId(openId, referId);
    }

    @Override
    public SurveyResult loadSubmit(String openId, String category) {
        return surveyResultDao.loadByOpenid(openId, category);
    }

    @Override
    public SurveyResult loadSubmit(Integer id) {
        return surveyResultDao.load(SurveyResult.class, id);
    }


    public List<SurveyQuestionType> calculatePoint(Integer submitId) {
        List<SurveyQuestionResult> results = surveyQuestionSubmitDao.loadSubmitQuestions(submitId);
        List<SurveyQuestionType> types = surveyQuestionTypeDao.loadQuestionTypes(results.stream().map(SurveyQuestionResult::getQuestionCode).collect(Collectors.toList()));

        List<Integer> userChoiceIds = results
                .stream()
                .filter(item -> types
                        .stream()
                        .anyMatch(type -> type.getQuestionCode().equals(item.getQuestionCode())))
                .map(SurveyQuestionResult::getChoiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<SurveyChoice> choices = surveyChoiceDao.loadChoicesByIds(userChoiceIds);
        /*
        计算分数，返回
        [{category,variable,point},{category,variable,point}]
         */
        return types.stream()
                .peek(type -> {
                    // 算分
                    double point = results
                            .stream()
                            .filter(item -> item.getQuestionCode().equals(type.getQuestionCode()))
                            .mapToInt(item -> {
                                SurveyChoice userChoice = choices.stream().filter(choice -> choice.getId().equals(item.getChoiceId())).findFirst().orElse(null);
                                if (userChoice == null) {
                                    return 0;
                                } else {
                                    if (type.getReverse()) {
                                        return 7 - userChoice.getSequence();
                                    } else {
                                        return userChoice.getSequence();
                                    }
                                }
                            }).sum();
                    type.setPoint(point);
                })
                .collect(Collectors.groupingBy(SurveyQuestionType::getVariable))
                .values().stream().map(list -> {
                    // 聚合
                    SurveyQuestionType type = list.stream().findFirst().orElse(null);
                    if (type == null) {
                        return null;
                    } else {
                        double totalPoint = list.stream().mapToDouble(SurveyQuestionType::getPoint).sum();
                        type.setPoint(totalPoint);
                    }
                    return type;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Pair<List<String>, List<SurveyQuestionType>> getSurveyReport(Integer submitId) {
        List<SurveyResult> otherSurveyResults = surveyResultDao.loadByReferId(submitId);
        List<SurveyQuestionType> selfs = this.calculatePoint(submitId);

        if (otherSurveyResults.size() >= 3) {
            Optional<List<SurveyQuestionType>> reduce = otherSurveyResults.stream().map(item -> this.calculatePoint(item.getId())).reduce((finalList, list) -> {
                finalList.forEach(item ->
                        list.stream().filter(listItem -> item.getVariable().equals(listItem.getVariable())).forEach(listItem ->
                                item.setPoint(item.getPoint() + listItem.getPoint())));
                return finalList;
            });
            if (reduce.isPresent()) {
                // 有他评
                List<SurveyQuestionType> others = reduce.get();
                // 查看比重
                double selfPercent;
                double otherPercent;
                Double average = selfs.stream().filter(SurveyQuestionType::getLiar).mapToDouble(SurveyQuestionType::getPoint).average().orElse(3);
                if (CommonUtils.isBetween(average, 0, 2)) {
                    // 6:4
                    selfPercent = 0.6;
                    otherPercent = 0.4;
                } else if (CommonUtils.isBetween(average, 2, 4)) {
                    selfPercent = 0.5;
                    otherPercent = 0.5;
                } else {
                    selfPercent = 0.4;
                    otherPercent = 0.6;
                }
                // 开始计算
                selfs.forEach(self -> {
                    double otherPoint = others.stream().filter(item -> self.getVariable().equals(item.getVariable())).mapToDouble(SurveyQuestionType::getPoint).average().orElse(0);
                    self.setPoint(self.getPoint() * selfPercent + otherPoint * otherPercent);
                });
            }
        } else {
            // 没有他评
        }


        return null;
    }


}
