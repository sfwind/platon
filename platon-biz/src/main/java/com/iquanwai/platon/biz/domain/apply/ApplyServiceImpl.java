package com.iquanwai.platon.biz.domain.apply;


import com.iquanwai.platon.biz.dao.apply.BusinessApplyChoiceDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplyQuestionDao;
import com.iquanwai.platon.biz.dao.apply.BusinessApplySubmitDao;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.exception.ApplyException;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.apply.BusinessApplyChoice;
import com.iquanwai.platon.biz.po.apply.BusinessApplyQuestion;
import com.iquanwai.platon.biz.po.apply.BusinessApplySubmit;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
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
import java.util.Objects;
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
    private RiseMemberManager riseMemberManager;
    @Autowired
    private BusinessApplySubmitDao businessApplySubmitDao;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private RiseCertificateDao riseCertificateDao;

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
    public void submitBusinessApply(Integer profileId, List<BusinessApplySubmit> userApplySubmits, Boolean valid, Integer project) {
        //获取上次审核的结果
        BusinessSchoolApplication lastBusinessApplication = businessSchoolApplicationDao.getLastVerifiedByProfileId(profileId);

        BusinessSchoolApplication application = new BusinessSchoolApplication();
        application.setProfileId(profileId);
        application.setSubmitTime(new Date());
        application.setStatus(BusinessSchoolApplication.APPLYING);

        application.setIsDuplicate(false);
        application.setValid(valid);
        application.setDeal(false);
        application.setProject(project);

        if (lastBusinessApplication != null) {
            application.setLastVerified(lastBusinessApplication.getStatus());
        } else {
            application.setLastVerified(0);
        }

        // TODO:待验证
        Optional<RiseMember> optional = riseMemberManager.member(profileId).stream()
                .sorted(((o1, o2) -> o2.getId() - o1.getId())).findFirst();
        optional.ifPresent(riseMember -> application.setOriginMemberType(riseMember.getMemberTypeId()));

        Integer applyId = businessSchoolApplicationDao.insert(application);

        userApplySubmits.forEach(item -> {
            item.setApplyId(applyId);
            if (item.getChoiceId() != null) {
                BusinessApplyChoice choice = businessApplyChoiceDao.load(BusinessApplyChoice.class, item.getChoiceId());
                item.setChoiceText(choice.getSubject() == null ? "异常数据" : choice.getSubject());
            }
        });
        businessApplySubmitDao.batchInsertApplySubmit(userApplySubmits);

        operationLogService.trace(profileId, "submitApply");
    }

    private void checkApplyMiniMba(Integer profileId) throws ApplyException {
        RiseMember riseMember = riseMemberManager.businessThought(profileId);
        if (riseMember != null) {
            throw new ApplyException("您已经是商业进阶课用户，无需重复申请");
        }

        List<BusinessSchoolApplication> applyList = this.loadApplyList(profileId);

        if (this.hasAvailableApply(applyList, Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
            throw new ApplyException("您已经有报名权限,无需重复申请");
        }

        if (applyList.stream().anyMatch(item -> !item.getDeal() && item.getProject().equals(Constants.Project.BUSINESS_THOUGHT_PROJECT))) {
            throw new ApplyException("您的申请正在审核中哦");
        }

        if (applyList.stream().anyMatch(item -> !item.getDeal() && item.getProject().equals(Constants.Project.CORE_PROJECT))) {
            throw new ApplyException("您已申请核心能力项，可联系圈外更改申请新项目");
        }
    }


    private void checkApplyBusiness(Integer profileId) throws ApplyException {
        // 已经是商学院用户
        RiseMember riseMember = riseMemberManager.coreBusinessSchoolMember(profileId);
        if (riseMember != null) {
            throw new ApplyException("您已经是商学院用户,无需重复申请");
        }

        List<BusinessSchoolApplication> applyList = this.loadApplyList(profileId);
        // 已有报名权限，申请通过
        boolean applyPass = this.hasAvailableApply(applyList, Constants.Project.CORE_PROJECT);
        if (applyPass) {
            throw new ApplyException("您已经有报名权限,无需重复申请");
        }
        if (this.hasAvailableApply(applyList, Constants.Project.BUSINESS_THOUGHT_PROJECT)) {
            throw new ApplyException("您已经有商业进阶课报名权限,可联系圈外更改报名项目");
        }

        // 已有报名权限，优秀证书
        RiseCertificate riseCertificate = riseCertificateDao.loadGraduateByProfileId(profileId);
        applyPass = riseCertificate != null;
        if (applyPass) {
            throw new ApplyException("优秀学员有报名权限,无需重复申请");
        }

        // 检查是否有申请中订单
        if (applyList.stream().anyMatch(item -> !item.getDeal() && item.getProject().equals(Constants.Project.CORE_PROJECT))) {
            throw new ApplyException("您的申请正在审核中哦");
        }
        if (applyList.stream().anyMatch(item -> !item.getDeal() && item.getProject().equals(Constants.Project.BUSINESS_THOUGHT_PROJECT))) {
            throw new ApplyException("您已申请商业进阶课，可联系圈外更改申请新项目");
        }

        // 一个月之内被拒绝过
        List<BusinessSchoolApplication> rejectLists = applyList
                .stream()
                .filter(item -> item.getProject().equals(Constants.Project.CORE_PROJECT))
                .filter(item -> item.getStatus() == BusinessSchoolApplication.REJECT)
                .filter(item -> new DateTime(item.getDealTime()).withTimeAtStartOfDay().plusMonths(1).isAfter(new DateTime().withTimeAtStartOfDay()))
                .collect(Collectors.toList());
        if (rejectLists.size() > 0) {
            Integer maxWaitDays = rejectLists
                    .stream()
                    .map(item -> DateUtils.interval(new DateTime(item.getDealTime()).withTimeAtStartOfDay().plusMonths(1).toDate(), new DateTime().withTimeAtStartOfDay().toDate()))
                    .max((Comparator.comparingInt(o -> o)))
                    .orElse(0);
            throw new ApplyException("还有 " + maxWaitDays + " 天才能再次申请哦");
        }
    }

    @Override
    public void checkApplyPrivilege(Integer profileId, Integer project) throws ApplyException {
        switch (project) {
            case Constants.Project.CORE_PROJECT: {
                checkApplyBusiness(profileId);
                break;
            }
            case Constants.Project.BUSINESS_THOUGHT_PROJECT: {
                checkApplyMiniMba(profileId);
                break;
            }
            default: {
                throw new ApplyException("项目类型异常");
            }
        }
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
