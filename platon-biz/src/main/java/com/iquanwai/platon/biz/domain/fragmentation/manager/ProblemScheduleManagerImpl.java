package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.CustomerStatusDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/11/11.
 */
@Service
public class ProblemScheduleManagerImpl implements ProblemScheduleManager {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private UserProblemScheduleDao userProblemScheduleDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private CourseScheduleDao courseScheduleDao;

    @Override
    public List<Chapter> loadRoadMap(Integer planId) {
        List<UserProblemSchedule> problemSchedules = userProblemScheduleDao.loadUserProblemSchedule(planId);
        if (CollectionUtils.isEmpty(problemSchedules)) {
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            List<ProblemSchedule> problemSchedules1 = problemScheduleDao.loadProblemSchedule(improvementPlan.getProblemId());

            problemSchedules = problemSchedules1.stream()
                    .map(problemSchedule -> getUserProblemSchedule(problemSchedule, improvementPlan.getId()))
                    .collect(Collectors.toList());
            userProblemScheduleDao.batchInsert(problemSchedules);
        }

        return getChapters(problemSchedules);
    }

    @Override
    public List<Chapter> loadDefaultRoadMap(Integer problemId) {

        List<ProblemSchedule> problemSchedules1 = problemScheduleDao.loadProblemSchedule(problemId);

        List<UserProblemSchedule> problemSchedules = problemSchedules1.stream()
                .map(problemSchedule -> getUserProblemSchedule(problemSchedule, null))
                .collect(Collectors.toList());

        return getChapters(problemSchedules);
    }

    @Override
    public Integer getProblemType(Integer problemId, Integer profileId) {
        //查询用户是否生成了课表
        CourseSchedule courseSchedule = courseScheduleDao.loadSingleCourseSchedule(profileId, problemId);
        if (courseSchedule != null) {
            return courseSchedule.getType();
        }

        // TODO: 2018年10月,老课表都废弃后需要调整
        RiseMember riseMember = riseMemberManager.coreBusinessSchoolUser(profileId);
        List<CourseScheduleDefault> courseSchedules;
        if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE ||
                riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {

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

        boolean major = courseSchedules.stream().anyMatch(courseScheduleDefault ->
                courseScheduleDefault.getProblemId().equals(problemId));

        return major ? CourseScheduleDefault.Type.MAJOR : CourseScheduleDefault.Type.MINOR;
    }

    @Override
    public List<Integer> getMajorProblemIds(Integer profileId, Integer memberTypeId, Integer year, Integer month) {
        List<CourseSchedule> majorCourseSchedules = courseScheduleDao.loadAllMajorScheduleByProfileId(profileId);
        List<Integer> majorProblemIds = majorCourseSchedules.stream()
                .filter(schedule -> memberTypeId.equals(schedule.getMemberTypeId()) && year.equals(schedule.getYear()) && month.equals(schedule.getMonth()))
                .map(CourseSchedule::getProblemId)
                .collect(Collectors.toList());
        return majorProblemIds;
    }


    @Override
    public List<Integer> getMajorProblemIds(Integer profileId, Integer year, Integer month) {
        List<CourseSchedule> majorCourseSchedules = courseScheduleDao.loadAllMajorScheduleByProfileId(profileId);
        List<Integer> majorProblemIds = majorCourseSchedules.stream()
                .filter(schedule -> year.equals(schedule.getYear()) && month.equals(schedule.getMonth()))
                .map(CourseSchedule::getProblemId)
                .collect(Collectors.toList());
        return majorProblemIds;
    }

    private List<Chapter> getChapters(List<UserProblemSchedule> problemSchedules) {
        Map<Integer, List<UserProblemSchedule>> problemScheduleMap = Maps.newLinkedHashMap();
        //按节组合成一组知识点
        problemSchedules.forEach(problemSchedule -> {
            List<UserProblemSchedule> problemScheduleList = problemScheduleMap.getOrDefault(
                    problemSchedule.getChapter(), Lists.newArrayList());
            problemScheduleList.add(problemSchedule);
            problemScheduleMap.put(problemSchedule.getChapter(), problemScheduleList);
        });

        List<Chapter> chapterList = Lists.newArrayList();

        //构建章节
        problemScheduleMap.keySet().forEach(chapterSequence -> {
            Chapter chapter = new Chapter();
            List<UserProblemSchedule> scheduleList = problemScheduleMap.get(chapterSequence);
            List<Section> sectionList = scheduleList.stream().sorted((o1, o2) -> o1.getSection() - o2.getSection())
                    .map(problemSchedule -> {
                        //构建小节
                        Section section = new Section();
                        Knowledge knowledge = cacheService.getKnowledge(problemSchedule.getKnowledgeId());
                        section.setKnowledgeId(knowledge.getId());
                        section.setName(knowledge.getKnowledge());
                        section.setSection(problemSchedule.getSection());
                        section.setSeries(problemSchedule.getSeries());
                        section.setIntegrated(Knowledge.isReview(problemSchedule.getKnowledgeId()));
                        section.setChapterName(knowledge.getStep());
                        section.setChapter(problemSchedule.getChapter());
                        return section;
                    })
                    .collect(Collectors.toList());
            chapter.setName(chapterName(sectionList));
            chapter.setSections(sectionList);
            chapter.setChapter(chapterSequence);
            chapterList.add(chapter);
        });

        chapterList.sort((o1, o2) -> o1.getChapter() - o2.getChapter());
        return chapterList;
    }

    private String chapterName(List<Section> sectionList) {
        if (CollectionUtils.isEmpty(sectionList)) {
            return "";
        }
        //步骤
        return sectionList.get(0).getChapterName();
    }

    private UserProblemSchedule getUserProblemSchedule(ProblemSchedule problemSchedule, Integer planId) {
        ModelMapper mapper = new ModelMapper();
        UserProblemSchedule userProblemSchedule = mapper.map(problemSchedule, UserProblemSchedule.class);
        userProblemSchedule.setPlanId(planId);
        return userProblemSchedule;
    }

}
