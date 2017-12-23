package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplySubmitDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationOrderDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplicationOrder;
import com.iquanwai.platon.biz.po.common.Profile;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private AccountService accountService;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private BusinessApplySubmitDao businessApplySubmitDao;
    @Autowired
    private BusinessSchoolApplicationOrderDao businessSchoolApplicationOrderDao;


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

    @Override
    public BusinessSchoolApplicationOrder loadUnAppliedOrder(Integer profileId) {
        return businessSchoolApplicationOrderDao.loadUnAppliedOrder(profileId);
    }

    @Override
    public void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, String orderId) {
        Profile profile = accountService.getProfile(profileId);
        //获取上次审核的结果
        BusinessSchoolApplication lastBussinessApplication = businessSchoolApplicationDao.getLastVerifiedByProfileId(profileId);

        BusinessSchoolApplication application = new BusinessSchoolApplication();
        application.setProfileId(profileId);
        application.setSubmitTime(new Date());
        application.setOpenid(profile.getOpenid());
        application.setStatus(BusinessSchoolApplication.APPLYING);

        application.setIsDuplicate(false);

        application.setDeal(false);
        application.setOrderId(orderId);

        if(lastBussinessApplication!=null){
            application.setLastVerified(lastBussinessApplication.getStatus());
        }
        else{
            application.setLastVerified(0);
        }

        Optional<RiseMember> optional = riseMemberDao.loadRiseMembersByProfileId(profileId).stream().sorted(((o1, o2) -> o2.getId() - o1.getId())).findFirst();
        optional.ifPresent(riseMember -> application.setOriginMemberType(riseMember.getMemberTypeId()));

        Integer applyId = businessSchoolApplicationDao.insert(application);
        businessSchoolApplicationOrderDao.applied(orderId);
        userApplySubmits.forEach(item -> {
            item.setApplyId(applyId);
            if (item.getChoiceId() != null) {
                BusinessApplyChoice choice = businessApplyChoiceDao.load(BusinessApplyChoice.class, item.getChoiceId());
                item.setChoiceText(choice.getSubject() == null ? "异常数据" : choice.getSubject());
            }
        });
        businessApplySubmitDao.batchInsertApplySubmit(userApplySubmits);
    }

}
