package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.PracticePlanStatusManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Section;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
                if (close && status<0) {
                    //设置过期状态
                    section.setStatus(-3);
                } else {
                    section.setStatus(status);
                }
                //拿到最后一个解锁的练习
                PracticePlan practicePlan = practicePlans.stream()
                        .filter(plan -> plan.getSeries().equals(section.getSeries()) && plan.getUnlocked())
                        .reduce((o1, o2) -> o1.getSequence() < o2.getSequence() ? o2 : o1).orElse(null);
                if (practicePlan != null && practicePlan.getStatus() == PracticePlan.STATUS.UNCOMPLETED) {
                    section.setType(practicePlan.getType());
                    section.setPracticePlanId(practicePlan.getId());
                    section.setPracticeId(practicePlan.getPracticeId());
                } else {
                    //如果所有练习都完成,返回第一个练习
                    practicePlan = practicePlans.stream()
                            .filter(plan -> plan.getSeries().equals(section.getSeries()) && plan.getSequence() == 1)
                            .findAny().orElse(null);
                    section.setType(practicePlan.getType());
                    section.setPracticePlanId(practicePlan.getId());
                    section.setPracticeId(practicePlan.getPracticeId());
                }
            });
        });
        studyLine.setChapters(chapters);

        studyLine.setReview(buildReviewPractice(practicePlans, close));

        return studyLine;
    }

    private List<ReviewPractice> buildReviewPractice(List<PracticePlan> practicePlans, boolean close) {
        boolean unlocked = practicePlanStatusManager.calculateProblemUnlocked(practicePlans);
        //设置解锁状态
        int status = 0;
        if (close) {
            status = -3;
        } else {
            if (!unlocked) {
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

}
