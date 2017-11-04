package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
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
    public SchedulePlan getSchedulePlan(Integer profileId) {
        SchedulePlan schedulePlan = new SchedulePlan();
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);

        //用户的课程计划
        List<CourseSchedule> courseSchedules = getPlan(profileId);
        //用户的本月计划
        List<CourseSchedule> currentMonthCourseSchedules = getCurrentMonthSchedule(courseSchedules);
        //已完成的小课
        List<Problem> completeProblem = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.CLOSE)
                .map(improvementPlan -> {
                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    return problem.simple();
                }).collect(Collectors.toList());
        schedulePlan.setCompleteProblem(completeProblem);

        //试听小课
        List<Problem> trialProblem = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> improvementPlan.getProblemId().equals(ConfigUtils.getTrialProblemId()))
                .map(improvementPlan -> {
                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    return problem.simple();
                }).collect(Collectors.toList());
        schedulePlan.setTrialProblem(trialProblem);

        //主修小课id
        List<Integer> majorProblemIds = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MAJOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //本月主修小课id
        List<Integer> currentMonthMajorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MAJOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //主修小课列表
        schedulePlan.setMajorProblem(getListProblem(improvementPlans, majorProblemIds, currentMonthMajorProblemIds));

        //本月主修进度
        schedulePlan.setMajorPercent(completePercent(improvementPlans, currentMonthMajorProblemIds));


        //辅修小课id
        List<Integer> minorProblemIds = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MINOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //本月辅修小课id
        List<Integer> currentMonthMinorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MINOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //辅修小课列表
        schedulePlan.setMinorProblem(getListProblem(improvementPlans, minorProblemIds, currentMonthMinorProblemIds));

        //本月主修进度
        schedulePlan.setMinorPercent(completePercent(improvementPlans, currentMonthMinorProblemIds));

        return schedulePlan;
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

    public boolean modifyProblemScheduleMonth(Integer profileId, Integer problemId, Integer targetYear, Integer targetMonth) {

        return false;
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

    private List<CourseSchedule> getCurrentMonthSchedule(List<CourseSchedule> courseSchedules) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        int month = DateUtils.getMonth(date);
        int year = DateUtils.getYear(date);

        return courseSchedules.stream().filter(courseSchedule -> courseSchedule.getMonth() == month &&
                courseSchedule.getYear() == year).collect(Collectors.toList());

    }


    private boolean containsProblemId(List<Problem> problems, Integer problemId) {
        for (Problem problem : problems) {
            if (problem.getId() == problemId) {
                return true;
            }
        }

        return false;
    }

    //计算主修或辅修小课进度
    private int completePercent(List<ImprovementPlan> improvementPlans, List<Integer> currentMonthProblemIds) {
        int majorTotalSeries = improvementPlans.stream().filter(improvementPlan -> currentMonthProblemIds.contains(improvementPlan.getProblemId()))
                .collect(Collectors.summingInt(ImprovementPlan::getTotalSeries));

        int majorCompleteSeries = improvementPlans.stream().filter(improvementPlan -> currentMonthProblemIds.contains(improvementPlan.getProblemId()))
                .collect(Collectors.summingInt(ImprovementPlan::getCompleteSeries));

        if (majorCompleteSeries == 0) {
            return 0;
        }

        return majorTotalSeries * 100 / majorCompleteSeries;

    }

    //小课列表 = 进行中小课+本月计划小课
    private List<Problem> getListProblem(List<ImprovementPlan> improvementPlans,
                                         List<Integer> problemIds, List<Integer> currentMonthProblemIds){

        // 选出进行中的小课
        List<Problem> problems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> problemIds.contains(improvementPlan.getProblemId()))
                .map(improvementPlan -> {
                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    return problem.simple();
                }).collect(Collectors.toList());

        //如果本月小课没有开始,加到推荐列表
        currentMonthProblemIds.forEach(currentMonthProblemId -> {
            boolean in = containsProblemId(problems, currentMonthProblemId);
            if (!in) {
                problems.add(cacheService.getProblem(currentMonthProblemId).simple());
            }
        });

        return problems;
    }
}
