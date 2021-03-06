package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.*;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/12/7.
 */
@Service
public class StudyServiceImpl implements StudyService {
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PracticePlanStatusManager practicePlanStatusManager;
    @Autowired
    private ProblemScheduleManager problemScheduleManager;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public StudyLine loadStudyLine(Integer planId) {
        StudyLine studyLine = new StudyLine();
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);

        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (improvementPlan == null) {
            logger.error("{} is not existed", planId);
            return null;
        }

        boolean close = improvementPlan.getStatus() == ImprovementPlan.CLOSE;

        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        studyLine.setProblemId(problem.getId());
        studyLine.setProblemName(problem.getProblem());
        studyLine.setProblemType(problemScheduleManager.getProblemType(problem.getId(), improvementPlan.getProfileId()));

        List<PracticePlan> preview = practicePlans.stream()
                .filter(practicePlan -> practicePlan.getSeries() == 0)
                .map(practicePlan -> {
                    //设置过期状态
                    if (!practicePlan.getUnlocked()) {
                        if (close) {
                            practicePlan.setStatus(-3);
                        } else {
                            practicePlan.setStatus(-1);
                        }
                    }
                    return practicePlan;
                }).collect(Collectors.toList());

        studyLine.setPreview(preview);

        List<Chapter> chapters = problemScheduleManager.loadRoadMap(planId);
        chapters.forEach(chapter -> {
            List<Section> sections = chapter.getSections();
            sections.forEach(section -> {
                int status = practicePlanStatusManager.calculateSectionStatus(practicePlans, section.getSeries());
                if (close && status < 0) {
                    //设置过期状态
                    section.setStatus(-3);
                } else {
                    section.setStatus(status);
                }
                // 该小节总共练习数
                section.setTotalPractices((int) practicePlans.stream()
                        .filter(plan -> plan.getSeries().equals(section.getSeries()))
                        .count());
                // 该小节已经完成练习数
                section.setCompletePractices((int) practicePlans.stream()
                        .filter(plan -> plan.getSeries().equals(section.getSeries()))
                        .filter(plan -> plan.getStatus() == PracticePlan.STATUS.COMPLETED)
                        .count());
                //拿到第一个未完成的练习
                PracticePlan practicePlan = practicePlans.stream()
                        .filter(plan -> plan.getSeries().equals(section.getSeries()))
                        .filter(PracticePlan::getUnlocked)
                        .filter(plan -> plan.getStatus() == PracticePlan.STATUS.UNCOMPLETED)
                        .reduce((o1, o2) -> o1.getSequence() < o2.getSequence() ? o1 : o2).orElse(null);
                if (practicePlan != null) {
                    section.setType(practicePlan.getType());
                    section.setPracticePlanId(practicePlan.getId());
                    section.setPracticeId(practicePlan.getPracticeId());
                } else {
                    //如果所有练习都完成,返回第一个练习
                    practicePlan = practicePlans.stream()
                            .filter(plan -> plan.getSeries().equals(section.getSeries()) && plan.getSequence() == 1)
                            .findAny().orElse(null);
                    if(practicePlan!=null) {
                        section.setType(practicePlan.getType());
                        section.setPracticePlanId(practicePlan.getId());
                        section.setPracticeId(practicePlan.getPracticeId());
                    }
                }
            });
        });
        studyLine.setChapters(chapters);

        studyLine.setReview(buildReviewPractice(practicePlans, close));

        studyLine.setStatus(improvementPlan.getStatus());

        return studyLine;
    }

    private List<ReviewPractice> buildReviewPractice(List<PracticePlan> practicePlans, boolean close) {
        boolean unlocked = practicePlanStatusManager.calculateProblemUnlocked(practicePlans);
        //设置解锁状态
        int status = 0;
        if (!unlocked) {
            if (close) {
                status = -3;
            } else {
                status = -1;
            }
        }

        List<ReviewPractice> reviewPractices = Lists.newArrayList();
        ReviewPractice studyReport = new ReviewPractice();
        studyReport.setType(ReviewPractice.STUDY_REPORT);
        studyReport.setStatus(status);
        reviewPractices.add(studyReport);

        ReviewPractice studyExtension = new ReviewPractice();
        studyExtension.setType(ReviewPractice.STUDY_EXTENSION);
        studyExtension.setStatus(status);
        reviewPractices.add(studyExtension);

        return reviewPractices;
    }

    @Override
    public CountDownElement loadLatestCountDownElement(Integer profileId) {
        List<RiseMember> validRiseMembers = riseMemberManager.member(profileId);
        RiseMember latestRiseMember = validRiseMembers.stream()
                .filter(riseMember -> riseMember.getOpenDate().compareTo(new Date()) > 0)
                .sorted(Comparator.comparing(RiseMember::getOpenDate))
                .findFirst().orElse(null);

        List<MemberType> memberTypes = cacheService.loadMemberTypes();
        Map<Integer, MemberType> memberTypeMap = memberTypes.stream().collect(Collectors.toMap(MemberType::getId, memberType -> memberType, (key1, key2) -> key1));

        if (latestRiseMember != null) {
            CountDownElement countDownElement = new CountDownElement();
            int memberTypeId = latestRiseMember.getMemberTypeId();
            countDownElement.setMemberTypeId(memberTypeId);
            int remainCountNumber = DateUtils.interval(latestRiseMember.getOpenDate());
            countDownElement.setRemainCount(String.format("%02d", remainCountNumber));
            MemberType memberType;
            switch (memberTypeId) {
                case RiseMember.ELITE:
                    // 核心能力项
                    memberType = memberTypeMap.getOrDefault(RiseMember.ELITE, null);
                    break;
                case RiseMember.CAMP:
                    // 专项课
                    memberType = memberTypeMap.getOrDefault(RiseMember.CAMP, null);
                    break;
                case RiseMember.BUSINESS_THOUGHT:
                    // 商业进阶
                    memberType = memberTypeMap.getOrDefault(RiseMember.BUSINESS_THOUGHT, null);
                    break;
                default:
                    return null;
            }
            if (memberType != null) {
                countDownElement.setDescription(memberType.getDescription());
            }
            return countDownElement;
        } else {
            return null;
        }
    }

}
