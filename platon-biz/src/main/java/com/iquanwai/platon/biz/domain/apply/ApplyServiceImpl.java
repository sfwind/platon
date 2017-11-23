package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
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
        List<BusinessApplyQuestion> questions = businessApplyQuestionDao.loadAll(BusinessApplyQuestion.class).stream().filter(item -> !item.getDel()).collect(Collectors.toList());
        List<BusinessApplyChoice> choices = businessApplyChoiceDao.loadAll(BusinessApplyChoice.class).stream().filter(item -> !item.getDel()).collect(Collectors.toList());

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
    public BusinessSchoolApplication loadCheckingApply(Integer profileId) {
        return businessSchoolApplicationDao.loadCheckingApplication(profileId);
    }

}
