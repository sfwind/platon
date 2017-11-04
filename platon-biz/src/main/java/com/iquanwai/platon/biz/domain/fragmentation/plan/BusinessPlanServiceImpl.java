package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/11/3.
 */
@Service
public class BusinessPlanServiceImpl implements BusinessPlanService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    @Override
    public List<CourseSchedule> getPlan(Integer profileId) {
        return courseScheduleDao.getAllScheduleByProfileId(profileId);
    }

    @Override
    public List<CourseSchedule> getOpeningPlan(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        courseSchedules = courseSchedules.stream().filter(courseSchedule -> {
            int month = DateUtils.getMonth(date);
            int year = DateUtils.getYear(date);

            return courseSchedule.getYear() <= year && courseSchedule.getMonth() <= month;
        }).collect(Collectors.toList());
        return courseSchedules;
    }

    @Override
    public Map<Integer, List<CourseSchedule>> getPersonalCourseSchedule(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);
        courseSchedules.forEach(this::buildProblemData);
        return courseSchedules.stream().collect(Collectors.groupingBy(CourseSchedule::getMonth));
    }

    @Override
    public boolean checkProblemModifyAccess(Integer profileId, Integer problemId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        // 用户没有开启过这门小课，这门小课的计划才能修改
        return improvementPlan == null;
    }


    // 将 problem 的数据放入 CourseSchedule 之中
    private CourseSchedule buildProblemData(CourseSchedule courseSchedule) {
        if (courseSchedule == null || courseSchedule.getProblemId() == null) {
            return null;
        }
        Problem problem = cacheService.getProblem(courseSchedule.getProblemId());
        courseSchedule.setProblem(problem);
        return courseSchedule;
    }

}
