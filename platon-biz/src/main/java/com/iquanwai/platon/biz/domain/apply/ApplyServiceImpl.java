package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.domain.common.member.RiseMemberTypeRepo;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;

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
    public boolean hasAvailableApply(Integer profileId, Integer memberTypeId) {
        return this.hasAvailableApply(businessSchoolApplicationDao.loadApplyList(profileId), memberTypeId);
    }

    @Override
    public boolean hasAvailableApply(List<BusinessSchoolApplication> applyList, Integer memberTypeId) {
        return applyList
                .stream()
                .filter(item -> Objects.equals(item.getMemberTypeId(), memberTypeId))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.APPROVE)
                .filter(BusinessSchoolApplication::getDeal)
                .filter(item -> !item.getExpired())
                .peek(item -> {
                    if (DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) <= 0) {
                        // 已经过期
                        item.setExpired(true);
                        businessSchoolApplicationDao.expiredApply(item.getId());
                    }
                })
                .filter(item -> !item.getEntry())
                .anyMatch(item -> !item.getExpired());
    }

    @Override
    public Pair<Long, Integer> loadRemainTimeMemberTypeId(Integer profileId) {
        List<BusinessSchoolApplication> applies = this.loadApplyList(profileId);
        List<MemberType> memberTypes = riseMemberTypeRepo.loadAll().stream().filter(item -> RiseMember.isMember(item.getId())).collect(Collectors.toList());
        Integer finalMemberType = memberTypes.stream().map(MemberType::getId).filter(item -> hasAvailableApply(applies, item)).findAny().orElse(null);
        if (finalMemberType != null) {
            // 可以付款这个,拿出id最大的
            BusinessSchoolApplication apply = applies.stream().filter(item -> item.getMemberTypeId().equals(finalMemberType)).sorted(((o1, o2) -> o2.getId() - o1.getId())).findFirst().orElse(null);
            if (apply != null) {
                Long intervalLong = DateUtils.afterDays(apply.getDealTime(), 1).getTime() -
                        System.currentTimeMillis();
                return Pair.of(intervalLong, finalMemberType);
            }
        }
        return Pair.of(0L, null);
    }
}
