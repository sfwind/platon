package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author nethunder
 * @version 2017-11-22
 */
@Service
public class ApplyServiceImpl implements ApplyService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BusinessApplyQuestionDao businessApplyQuestionDao;
    @Autowired
    private BusinessApplyChoiceDao businessApplyChoiceDao;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;

    @Override
    public List<BusinessApplyQuestion> loadBusinessApplyQuestions(Integer profileId) {
        Integer category;
        if (ConfigUtils.getPayApplyFlag()) {
            category = BusinessApplyQuestion.PAY_CATEGORY;
        } else {
            // 非付费
            category = BusinessApplyQuestion.NO_PAY_CATEGORY;
        }
        List<BusinessApplyQuestion> questions = businessApplyQuestionDao.loadAll(BusinessApplyQuestion.class)
                .stream()
                .filter(item -> !item.getDel())
                .filter(item -> category.equals(item.getCategory()))
                .collect(Collectors.toList());
        List<BusinessApplyChoice> choices = businessApplyChoiceDao.loadAll(BusinessApplyChoice.class)
                .stream()
                .filter(item -> !item.getDel())
                .collect(Collectors.toList());

        Map<Integer, List<BusinessApplyChoice>> choiceQuestionMap = choices.stream().collect(Collectors.groupingBy(BusinessApplyChoice::getQuestionId));
        questions.sort((o1, o2) -> {
            int o1Sequence = o1.getSequence() == null ? 0 : o1.getSequence();
            int o2Sequence = o2.getSequence() == null ? 0 : o2.getSequence();
            return o1Sequence - o2Sequence;
        });
        questions.forEach(item -> {
            List<BusinessApplyChoice> itemChoices = choiceQuestionMap.get(item.getId());
            if (CollectionUtils.isNotEmpty(itemChoices)) {
                itemChoices.sort(((o1, o2) -> {
                    int o1Sequence = o1.getSequence() == null ? 0 : o1.getSequence();
                    int o2Sequence = o2.getSequence() == null ? 0 : o2.getSequence();
                    return o1Sequence - o2Sequence;
                }));
            }
            item.setChoices(itemChoices);
        });
        return questions;
    }


    @Override
    public List<BusinessSchoolApplication> loadApplyList(Integer profileId) {
        return businessSchoolApplicationDao.loadApplyList(profileId);
    }


    @Override
    public boolean hasAvailableApply(Integer profileId, Integer project) {
        return this.hasAvailableApply(businessSchoolApplicationDao.loadApplyList(profileId), project);
    }

    @Override
    public boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer project) {
        return applyList
                .stream()
                .filter(item -> Objects.equals(item.getProject(), project))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.APPROVE)
                .filter(BusinessSchoolApplication::getDeal)
                .anyMatch(item -> DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) > 0);
    }
}
