package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.UserProblemScheduleDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import com.iquanwai.platon.biz.po.UserProblemSchedule;
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
public class ProblemScheduleRepositoryImpl implements ProblemScheduleRepository {
    @Autowired
    private UserProblemScheduleDao userProblemScheduleDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;

    @Override
    public List<Chapter> loadRoadMap(Integer planId) {
        List<UserProblemSchedule> problemSchedules = userProblemScheduleDao.loadUserProblemSchedule(planId);
        if(CollectionUtils.isEmpty(problemSchedules)){
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            List<ProblemSchedule> problemSchedules1 = problemScheduleDao.loadProblemSchedule(improvementPlan.getProblemId());

            problemSchedules = problemSchedules1.stream()
                    .map(problemSchedule -> getUserProblemSchedule(problemSchedule, improvementPlan.getId()))
                    .collect(Collectors.toList());
            userProblemScheduleDao.batchInsert(problemSchedules);
        }

        List<Chapter> chapterList = getChapters(problemSchedules);

        return chapterList;
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
            if (CollectionUtils.isNotEmpty(sectionList)) {
                chapter.setIntegrated(Knowledge.isReview(sectionList.get(0).getKnowledgeId()));
            }
            chapterList.add(chapter);
        });
        return chapterList;
    }

    @Override
    public List<Chapter> loadDefaultRoadMap(Integer problemId) {

        List<ProblemSchedule> problemSchedules1 = problemScheduleDao.loadProblemSchedule(problemId);

        List<UserProblemSchedule> problemSchedules = problemSchedules1.stream()
                .map(problemSchedule -> getUserProblemSchedule(problemSchedule, null))
                .collect(Collectors.toList());

        List<Chapter> chapterList = getChapters(problemSchedules);
        return chapterList;
    }

    private String chapterName(List<Section> sectionList) {
        if (CollectionUtils.isEmpty(sectionList)) {
            return "";
        }
        //步骤
        return sectionList.get(0).getChapterName();
    }

    @Override
    public void batchinsert() {
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAll(ImprovementPlan.class);
        improvementPlans = improvementPlans.stream().filter(improvementPlan -> !improvementPlan.getDel()).collect(Collectors.toList());

        improvementPlans.forEach(improvementPlan -> {
            List<UserProblemSchedule> problemSchedules = userProblemScheduleDao.loadUserProblemSchedule(improvementPlan.getId());
            if (CollectionUtils.isEmpty(problemSchedules)) {
                List<ProblemSchedule> problemSchedules1 = problemScheduleDao.loadProblemSchedule(improvementPlan.getProblemId());

                problemSchedules = problemSchedules1.stream()
                        .map(problemSchedule -> getUserProblemSchedule(problemSchedule, improvementPlan.getId()))
                        .collect(Collectors.toList());
                userProblemScheduleDao.batchInsert(problemSchedules);
            }
        });
    }

    private UserProblemSchedule getUserProblemSchedule(ProblemSchedule problemSchedule, Integer planId) {
        ModelMapper mapper = new ModelMapper();
        UserProblemSchedule userProblemSchedule = mapper.map(problemSchedule, UserProblemSchedule.class);
        userProblemSchedule.setPlanId(planId);
        return userProblemSchedule;
    }
}
