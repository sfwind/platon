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
public class StudyServiceImpl implements StudyService{
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
        if(improvementPlan == null){
            logger.error("{} is not existed", planId);
            return null;
        }

        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        studyLine.setProblemId(problem.getId());
        studyLine.setProblemName(problem.getProblem());

        List<PracticePlan> preview = practicePlans.stream().filter(practicePlan -> practicePlan.getSeries() == 0).collect(Collectors.toList());
        studyLine.setPreview(preview);

        List<Chapter> chapters = problemScheduleManager.loadRoadMap(planId);
        chapters.forEach(chapter -> {
            List<Section> sections = chapter.getSections();
            sections.forEach(section -> {
                int status = practicePlanStatusManager.calculateSectionStatus(practicePlans, section.getSeries());
                section.setStatus(status);
            });
        });
        studyLine.setChapters(chapters);

        studyLine.setReview(buildReviewPractice(practicePlans));

        return studyLine;
    }

    private List<ReviewPractice> buildReviewPractice(List<PracticePlan> practicePlans){
        boolean unlocked = practicePlanStatusManager.calculateProblemUnlocked(practicePlans);

        List<ReviewPractice> reviewPractices = Lists.newArrayList();
        ReviewPractice studyReport = new ReviewPractice();
        studyReport.setType(ReviewPractice.STUDY_REPORT);
        studyReport.setUnlocked(unlocked);
        reviewPractices.add(studyReport);

        ReviewPractice studyExtension = new ReviewPractice();
        studyExtension.setType(ReviewPractice.STUDY_EXTENSION);
        studyExtension.setUnlocked(unlocked);
        reviewPractices.add(studyExtension);

        return reviewPractices;
    }
}
