package com.iquanwai.platon.biz.domain.survey;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.survey.SurveyChoiceDao;
import com.iquanwai.platon.biz.dao.survey.SurveyDefineDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionResultDao;
import com.iquanwai.platon.biz.dao.survey.SurveyQuestionTypeDao;
import com.iquanwai.platon.biz.dao.survey.SurveyReportCharacterDao;
import com.iquanwai.platon.biz.dao.survey.SurveyReportSuggestDao;
import com.iquanwai.platon.biz.dao.survey.SurveyResultDao;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
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
import com.iquanwai.platon.biz.po.survey.report.SurveyReportCharacter;
import com.iquanwai.platon.biz.po.survey.report.SurveyVariableInfo;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
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
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private SurveyReportCharacterDao surveyReportCharacterDao;

    private static Integer MAX_VALID_OTHER_SURVEY_COUNT = 10;

    private static Map<Integer, Integer> CATEGORY_SEQUENCE = Maps.newHashMap();

    static {
        // 思维智识>人际交往>心理品质>工作效率
        CATEGORY_SEQUENCE.put(3, 10);
        CATEGORY_SEQUENCE.put(1, 7);
        CATEGORY_SEQUENCE.put(4, 4);
        CATEGORY_SEQUENCE.put(2, 2);
    }

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
    public Integer submitQuestions(Integer profileId, String openId, String category, Integer referId, List<SurveySubmitVo> submits) {
        SurveyQuestion surveyQuestion = surveyQuestionDao.loadOneQuestion(category);
        SurveyResult surveyResult = new SurveyResult();
        surveyResult.setCategory(category);
        surveyResult.setOpenid(openId);
        surveyResult.setProfileId(profileId);
        surveyResult.setReferSurveyId(referId);
        Integer version;
        if (surveyQuestion != null) {
            version = surveyQuestion.getVersion();
        } else {
            version = null;
        }
        Boolean reportValid;
        if (referId != null) {
            // 他评
            if (surveyResultDao.loadReportValidByReferId(referId).size() >= MAX_VALID_OTHER_SURVEY_COUNT) {
                reportValid = false;
            } else {
                reportValid = true;
            }
        } else {
            reportValid = false;
        }
        surveyResult.setReportValid(reportValid);


        surveyResult.setVersion(version);

        // 查看他是第几层
        List<Integer> refers = surveyResultDao.loadByOpenIdAndCategory(openId).stream().filter(item -> item.getReferSurveyId() != null).map(SurveyResult::getReferSurveyId).collect(Collectors.toList());
        Integer level = surveyResultDao.loadByIdsAndCategory(refers, category).stream().mapToInt(SurveyResult::getLevel).max().orElse(0) + 1;
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

        operationLogService.trace(profileId, "submitSurvey", () -> {
            OperationLogService.Prop prop = OperationLogService
                    .props()
                    .add("surveyLevel", level)
                    .add("surveyCategory", category);
            if (referId != null) {
                prop.add("surveyReferId", referId);
            }
            if (version != null) {
                prop.add("surveyVersion", version);
            }
            return prop;
        });


        if (referId != null && SurveyQuestion.EVALUATION_OTHER.equals(category)) {
            // 其他人提交的
            SurveyResult refer = this.loadSubmit(referId);
            if (refer != null) {
                Profile selfSurveyProfile = accountService.getProfile(refer.getOpenid());
                if (profile != null) {
                    // 价值观测试，需要发消息
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(refer.getOpenid());
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    templateMessage.setTemplate_id(ConfigUtils.getMessageReplyCode());
                    data.put("first", new TemplateMessage.Keyword("Hi " + selfSurveyProfile.getNickname() + "，你的职业发展核心能力和心理品质量表，有新的他评问卷完成，请知晓。\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(selfSurveyProfile.getNickname()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword("职业发展核心能力和心理品质量表-他评"));
                    templateMessageService.sendMessage(templateMessage);
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
    public Integer generateReport(Integer submitId) {
        return surveyResultDao.generateReport(submitId);
    }


    @Override
    public SurveyReport loadSurveyReport(Integer submitId) {
        // 获取有效的他评
        List<SurveyResult> otherSurveyResults = surveyResultDao.loadReportValidByReferId(submitId).stream().limit(MAX_VALID_OTHER_SURVEY_COUNT).collect(Collectors.toList());
        // 计算自评分数
        List<SurveyQuestionType> selfs = this.calculatePoint(submitId);
        SurveyReport report = new SurveyReport();
        // 收集到多少分他评
        report.setOtherSurveyCount(otherSurveyResults.size());
        // 是否显示完整报告
        report.setShowComplete(otherSurveyResults.size() >= 3);
        // 合并计算
        mergeSurveyInfo(selfs, otherSurveyResults);
        // 设置reportInfo
        report.setCategoryInfos(convertToSurveyReport(selfs));
        // 获取名字头像
        report.setNamePicPair(otherSurveyResults.stream().map(result -> {
            Profile profile = accountService.getProfile(result.getOpenid());
            if (profile == null) {
                return Pair.of("圈柚", "https://www.iqycamp.com/images/fragment/logo2x.jpg");
            } else {
                return Pair.of(profile.getNickname(), profile.getHeadimgurl());
            }
        }).collect(Collectors.toList()));
        // 获取角色
        report.setCharacter(getCharacter(selfs));
        return report;
    }

    private String getCharacter(List<SurveyQuestionType> types) {
        Map<Integer, Double> typePoint = Maps.newHashMap();
        List<Pair<Integer, Double>> categoryPoint = types
                .stream()
                .collect(Collectors.groupingBy(SurveyQuestionType::getCategory))
                .entrySet()
                .stream()
                .map(item -> {
                    Integer categoryId = item.getKey();
                    Double point = item.getValue().stream().mapToDouble(SurveyQuestionType::getPoint).average().orElse(0);
                    return Pair.of(categoryId, point);
                })
                .sorted(((o1, o2) -> {
                    // 思维智识>人际交往>心理品质>工作效率
                    Double point1 = o1.getValue();
                    Double point2 = o2.getValue();
                    if (Objects.equals(point2, point1)) {
                        Integer s2 = CATEGORY_SEQUENCE.get(o2.getKey()) == null ? o2.getKey() : CATEGORY_SEQUENCE.get(o2.getKey());
                        Integer s1 = CATEGORY_SEQUENCE.get(o1.getKey()) == null ? o1.getKey() : CATEGORY_SEQUENCE.get(o1.getKey());
                        return s2 - s1;
                    } else {
                        return point2 > point1 ? 1 : -1;
                    }
                }))
                .collect(Collectors.toList());
        if (categoryPoint.size() >= 2) {
            Integer highestId = categoryPoint.get(1).getKey();
            Integer lowestId = categoryPoint.get(categoryPoint.size() - 1).getKey();
            SurveyReportCharacter character = surveyReportCharacterDao.loadCharacterByHightAndLow(highestId, lowestId);
            if (character != null) {
                return character.getReportCharacter();
            }
        }
        return null;
    }

    // 计算分数
    private List<SurveyQuestionType> calculatePoint(Integer submitId) {
        // 获取提交了什么题目
        List<SurveyQuestionResult> results = surveyQuestionSubmitDao.loadSubmitQuestions(submitId);
        // 用题目获取题目分类，只有这些题目才会积分
        List<SurveyQuestionType> validQuestionTypes = surveyQuestionTypeDao.loadQuestionTypes(results.stream().map(SurveyQuestionResult::getQuestionCode).collect(Collectors.toList()));
        // 根据题目获取所有选项id
        List<Integer> userChoiceIds = results
                .stream()
                // 只查看积分的选项
                .filter(item -> validQuestionTypes
                        .stream()
                        .anyMatch(type -> type.getQuestionCode().equals(item.getQuestionCode())))
                .map(SurveyQuestionResult::getChoiceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 获取所有用户选择的选项数据
        List<SurveyChoice> userChoiceList = surveyChoiceDao.loadChoicesByIds(userChoiceIds);
        /*
        计算分数，返回
        [{category,variable,point},{category,variable,point}]
         */
        return validQuestionTypes.stream()
                .peek(type -> {
                    // 算分 计算这个题目的分数
                    double point = results
                            .stream()
                            .filter(surveyQuestionResult -> surveyQuestionResult.getQuestionCode().equals(type.getQuestionCode()))
                            .mapToDouble(surveyQuestionResult -> {
                                // 查到这个题目的选项
                                SurveyChoice userChoice = userChoiceList.stream().filter(choice -> choice.getId().equals(surveyQuestionResult.getChoiceId())).findFirst().orElse(null);
                                if (userChoice == null) {
                                    return 0D;
                                } else {
                                    // 转换成百分制
                                    if (type.getReverse()) {
                                        return ((Double.valueOf(type.getMaxChoice()) - userChoice.getSequence()) / type.getMaxChoice()) * 100;
                                    } else {
                                        return (Double.valueOf(userChoice.getSequence()) / type.getMaxChoice()) * 100;
                                    }
                                }
                            }).average().orElse(0);
                    type.setPoint(point);
                })
                .collect(Collectors.groupingBy(SurveyQuestionType::getVariable))
                .values()
                .stream()
                .map(list -> {
                    // 聚合
                    SurveyQuestionType type = list.stream().findFirst().orElse(null);
                    if (type == null) {
                        return null;
                    } else {
                        double totalPoint = list.stream().mapToDouble(SurveyQuestionType::getPoint).average().orElse(0);
                        type.setPoint(totalPoint);
                    }
                    return type;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
    private List<SurveyCategoryInfo> convertToSurveyReport(List<SurveyQuestionType> selfs) {
        // 获取未删除的维度分类信息
        List<SurveyDefine> defines = surveyDefineDao.loadAllWithoutDel(SurveyDefine.class);
        // 获取建议
        List<SurveyReportSuggest> suggests = surveyReportSuggestDao.loadAllWithoutDel(SurveyReportSuggest.class);
        return selfs.stream()
                // 去掉测谎题
                .filter(item -> !item.getLiar())
                // 根据大维度分类
                .collect(Collectors.groupingBy(SurveyQuestionType::getCategory))
                .entrySet()
                .stream().map(entry -> {
                    // 返回大维度信息
                    SurveyCategoryInfo categoryInfo = new SurveyCategoryInfo();
                    Integer category = entry.getKey();
                    List<SurveyQuestionType> types = entry.getValue();
                    // 设置维度名字
                    defines.stream()
                            .filter(define -> define.getDefineId().equals(category) && define.getType().equals(SurveyDefine.CATEGORY))
                            .findFirst().ifPresent(define -> categoryInfo.setLegend(define.getName()));
                    // 设置维度内小维度信息
                    categoryInfo.setDetail(types.stream().map(item -> {
                        SurveyVariableInfo info = new SurveyVariableInfo();
                        // 设置小维度名字
                        defines.stream()
                                .filter(define -> define.getDefineId().equals(item.getVariable()) && define.getType().equals(SurveyDefine.VARIABLE))
                                .findFirst().ifPresent(define -> info.setCategory(define.getName()));
                        // 设置小维度分数
                        info.setMax(100);
                        info.setValue(item.getPoint());
                        // 设置建议
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
