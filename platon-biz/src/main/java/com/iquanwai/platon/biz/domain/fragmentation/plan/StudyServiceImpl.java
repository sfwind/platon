package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.CustomerStatusDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDefaultDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.PracticePlanStatusManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.manager.Section;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
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
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;

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

        boolean major = isMajorCourse(problem.getId(), improvementPlan.getProfileId());
        studyLine.setProblemType(major ? "major" : "minor");

        List<PracticePlan> preview = practicePlans.stream()
                .filter(practicePlan -> practicePlan.getSeries() == 0)
                .map(practicePlan -> {
                    //设置过期状态
                    if (close && !practicePlan.getUnlocked()) {
                        practicePlan.setStatus(-3);
                    }
                    return practicePlan;
                }).collect(Collectors.toList());

        studyLine.setPreview(preview);

        List<Chapter> chapters = problemScheduleManager.loadRoadMap(planId);
        chapters.forEach(chapter -> {
            List<Section> sections = chapter.getSections();
            sections.forEach(section -> {
                int status = practicePlanStatusManager.calculateSectionStatus(practicePlans, section.getSeries());
                if (close) {
                    //设置过期状态
                    section.setStatus(-3);
                } else {
                    section.setStatus(status);
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


    private boolean isMajorCourse(Integer problemId, Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        List<CourseScheduleDefault> courseSchedules;
        if (riseMember.getMemberTypeId() == RiseMember.ELITE ||
                riseMember.getMemberTypeId() == RiseMember.HALF_ELITE) {

            //老学员用老课表
            if (customerStatusDao.load(profileId, CustomerStatus.OLD_SCHEDULE) != null) {
                courseSchedules = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(
                        CourseScheduleDefault.CategoryType.OLD_STUDENT);
            } else {
                //新学员用新课表
                courseSchedules = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(
                        CourseScheduleDefault.CategoryType.NEW_STUDENT);
            }
        } else {
            //非商学院用户,使用新课表
            courseSchedules = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(
                    CourseScheduleDefault.CategoryType.NEW_STUDENT);
        }

        return courseSchedules.stream().anyMatch(courseScheduleDefault ->
                courseScheduleDefault.getProblemId().equals(problemId));
    }
}
