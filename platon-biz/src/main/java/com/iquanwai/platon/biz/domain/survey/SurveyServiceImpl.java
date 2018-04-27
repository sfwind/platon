package com.iquanwai.platon.biz.domain.survey;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.survey.SurveyChoiceDao;
import com.iquanwai.platon.biz.dao.survey.SurveyDefineDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionResultDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionTypeDao;
import com.iquanwai.platon.biz.dao.survey.SurveyReportSuggestDao;
import com.iquanwai.platon.biz.dao.survey.SurveyResultDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.survey.SurveyChoice;
import com.iquanwai.platon.biz.po.survey.SurveyDefine;
import com.iquanwai.platon.biz.po.survey.SurveyQuestion;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionResult;
import com.iquanwai.platon.biz.po.survey.SurveyQuestionType;
import com.iquanwai.platon.biz.po.survey.SurveyReportSuggest;
import com.iquanwai.platon.biz.po.survey.SurveyResult;
import com.iquanwai.platon.biz.po.survey.SurveySubmitVo;
import com.iquanwai.platon.biz.po.survey.report.SurveyCategoryInfo;
import com.iquanwai.platon.biz.po.survey.report.SurveyReport;
import com.iquanwai.platon.biz.po.survey.report.SurveyVariableInfo;
import com.iquanwai.platon.biz.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
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
    @Autowired
    private SurveyDefineDao surveyDefineDao;
    @Autowired
    private SurveyReportSuggestDao surveyReportSuggestDao;


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
        // 查看他是第几层
        Integer level = surveyResultDao.loadByOpenIdAndCategory(openId, category).stream().mapToInt(SurveyResult::getLevel).max().orElse(0) + 1;
        surveyResult.setLevel(level);
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


    @Override
    public SurveyReport loadSurveyReport(Integer submitId) {
        List<SurveyResult> otherSurveyResults = surveyResultDao.loadByReferId(submitId);
        List<SurveyQuestionType> selfs = this.calculatePoint(submitId);
        SurveyReport report = new SurveyReport();
        // 收集到多少分他评
        report.setOtherSurveyCount(otherSurveyResults.size());
        // 是否显示完整报告
        report.setShowComplete(otherSurveyResults.size() >= 3);
        // 合并计算
        mergeSurveyInfo(selfs, otherSurveyResults);
        // 设置reportInfo
        report.setCategoryInfos(convertToSurvyeReport(selfs));
        return report;
    }

    // 计算分数
    private List<SurveyQuestionType> calculatePoint(Integer submitId) {
        List<SurveyQuestionResult> results = surveyQuestionSubmitDao.loadSubmitQuestions(submitId);
        // 这些type才是积分type
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
                            }).average().orElse(0);
                    type.setPoint(point);
                })
                .collect(Collectors.groupingBy(SurveyQuestionType::getVariable))
                .values().stream().map(list -> {
                    // 聚合
                    SurveyQuestionType type = list.stream().findFirst().orElse(null);
                    if (type == null) {
                        return null;
                    } else {
                        double totalPoint = list.stream().mapToDouble(SurveyQuestionType::getPoint).average().orElse(0);
                        type.setPoint(totalPoint);
                    }
                    return type;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * 合并计算他评和自评
     *
     * @param selfs              自评
     * @param otherSurveyResults 他评
     */
    private void mergeSurveyInfo(List<SurveyQuestionType> selfs, List<SurveyResult> otherSurveyResults) {
        if (otherSurveyResults.size() < 3) {
            return;
        }
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
                self.setPoint(self.getPoint() * selfPercent + otherPoint * otherPercent / otherSurveyResults.size());
            });
        }
    }

    /**
     * 转换自评类型到报告信息
     *
     * @param selfs 自评维度信息
     * @return 报告信息
     */
    private List<SurveyCategoryInfo> convertToSurvyeReport(List<SurveyQuestionType> selfs) {
        List<SurveyDefine> defines = surveyDefineDao.loadAllWithoutDel(SurveyDefine.class);
        List<SurveyReportSuggest> suggests = surveyReportSuggestDao.loadAllWithoutDel(SurveyReportSuggest.class);
        return selfs.stream()
                .filter(item -> !item.getLiar())
                .collect(Collectors.groupingBy(SurveyQuestionType::getCategory))
                .entrySet()
                .stream().map(entry -> {
                    SurveyCategoryInfo categoryInfo = new SurveyCategoryInfo();
                    Integer category = entry.getKey();
                    List<SurveyQuestionType> types = entry.getValue();
                    defines.stream()
                            .filter(define -> define.getDefineId().equals(category) && define.getType().equals(SurveyDefine.CATEGORY))
                            .findFirst().ifPresent(define -> categoryInfo.setLegend(define.getName()));
                    categoryInfo.setDetail(types.stream().map(item -> {
                        SurveyVariableInfo info = new SurveyVariableInfo();
                        defines.stream()
                                .filter(define -> define.getDefineId().equals(item.getVariable()) && define.getType().equals(SurveyDefine.VARIABLE))
                                .findFirst().ifPresent(define -> info.setCategory(define.getName()));
                        info.setMax(6);
                        info.setValue(item.getPoint());
                        suggests.stream()
                                .filter(suggest -> {
                                    try {
                                        if (!suggest.getVariableId().equals(item.getVariable())) {
                                            return false;
                                        }
                                        if (!suggest.getCategoryId().equals(item.getCategory())) {
                                            return false;
                                        }
                                        List<Double> range = Lists.newArrayList(suggest.getPointRange().split(",")).stream().map(Double::parseDouble).collect(Collectors.toList());
                                        if (CollectionUtils.isEmpty(range) || range.size() < 2) {
                                            return false;
                                        }
                                        Double low = range.get(0);
                                        Double hight = range.get(1);
                                        return CommonUtils.isBetween(Math.ceil(item.getPoint()), low, hight);
                                    } catch (Exception e) {
                                        logger.error(e.getLocalizedMessage(), e);
                                        return false;
                                    }
                                })
                                .findFirst()
                                .ifPresent(suggest -> info.setSuggest(suggest.getSuggest()));
                        return info;
                    }).collect(Collectors.toList()));
                    return categoryInfo;
                }).collect(Collectors.toList());
    }


}
