package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplySubmitDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationOrderDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.ApplyException;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplicationOrder;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    public List<BusinessSchoolApplication> loadApplyList(Integer profileId) {
        return businessSchoolApplicationDao.loadApplyList(profileId);
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

        if (lastBussinessApplication != null) {
            application.setLastVerified(lastBussinessApplication.getStatus());
        } else {
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

    @Override
    public void checkApplyPrivilege(Integer profileId) throws ApplyException {
        // 已经是商学院用户
        RiseMember riseMember = accountService.getValidRiseMember(profileId);
        if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE ||
                riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
            throw new ApplyException("您已经是商学院用户,无需重复申请");
        }

        List<BusinessSchoolApplication> applyList = this.loadApplyList(profileId);
        // 已有报名权限
        Boolean applyPass = accountService.hasStatusId(profileId, CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
        if (applyPass) {
            throw new ApplyException("您已经有报名权限,无需重复申请");
        }

        // 检查是否有申请中订单
        Boolean checking = applyList.stream().anyMatch(item -> !item.getDeal());
        if (checking) {
            throw new ApplyException("您的申请正在审核中哦");
        }

        // 一个月之内被拒绝过
        List<BusinessSchoolApplication> rejectLists = applyList
                .stream()
                .filter(item -> item.getStatus() == BusinessSchoolApplication.REJECT &&
                        new DateTime(item.getSubmitTime()).plusMonths(1).isAfterNow()).collect(Collectors.toList());
        if (rejectLists.size() > 0) {
            Integer maxWaitDays = rejectLists
                    .stream()
                    .map(item -> DateUtils.interval(item.getSubmitTime()))
                    .max((Comparator.comparingInt(o -> o))).orElse(1);
            throw new ApplyException("还有 " + maxWaitDays + " 天才能再次申请哦");
        }
    }

}
