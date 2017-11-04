package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDefaultDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.CourseScheduleDefault;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.modelmapper.ModelMapper;
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
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
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

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        int month = DateUtils.getMonth(date);
        schedulePlan.setMonth(month);
        schedulePlan.setToday(DateUtils.parseDateToFormat5(new Date()));
        schedulePlan.setTopic(cacheService.loadMonthTopic().get(month));
        return schedulePlan;
    }

    @Override
    public List<List<CourseSchedule>> loadPersonalCourseSchedule(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);
        courseSchedules.forEach(this::buildProblemData);
        List<List<CourseSchedule>> courseScheduleLists = Lists.newArrayList();
        Map<Integer, List<CourseSchedule>> courseScheduleMap = courseSchedules.stream().collect(Collectors.groupingBy(CourseSchedule::getMonth));
        courseScheduleMap.forEach((k, v) -> courseScheduleLists.add(v));
        return courseScheduleLists;
    }

    @Override
    public List<List<CourseSchedule>> loadDefaultCourseSchedule() {
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadDefaultCourseSchedule();
        List<CourseSchedule> courseSchedules = courseScheduleDefaults.stream().map(courseScheduleDefault -> {
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(courseScheduleDefault, CourseSchedule.class);
        }).collect(Collectors.toList());
        courseSchedules.forEach(this::buildProblemData);
        List<List<CourseSchedule>> courseScheduleLists = Lists.newArrayList();
        Map<Integer, List<CourseSchedule>> courseScheduleMap = courseSchedules.stream().collect(Collectors.groupingBy(CourseSchedule::getMonth));
        courseScheduleMap.forEach((k, v) -> courseScheduleLists.add(v));
        return courseScheduleLists;
    }

    @Override
    public boolean checkProblemModifyAccess(Integer profileId, Integer problemId) {
        CourseSchedule courseSchedule = courseScheduleDao.loadSingleCourseSchedule(profileId, problemId);
        if (courseSchedule != null && courseSchedule.getType() == CourseScheduleDefault.Type.MINOR) {
            ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
            return improvementPlan == null;
        }
        return false;
    }

    @Override
    public boolean modifyProblemSchedule(Integer profileId, Integer problemId, Integer targetYear, Integer targetMonth) {
        CourseSchedule courseSchedule = courseScheduleDao.loadSingleCourseSchedule(profileId, problemId);
        if (courseSchedule != null) {
            return courseScheduleDao.updateProblemSchedule(profileId, problemId, targetYear, targetMonth) > 0;
        } else {
            courseSchedule = new CourseSchedule();
            courseSchedule.setProfileId(profileId);
            courseSchedule.setProblemId(problemId);
            courseSchedule.setYear(targetYear);
            courseSchedule.setMonth(targetMonth);
            courseSchedule.setType(CourseScheduleDefault.Type.MINOR);
            return courseScheduleDao.insertCourseSchedule(courseSchedule) > 0;
        }
    }

    // 将 problem 的数据放入 CourseSchedule 之中
    private CourseSchedule buildProblemData(CourseSchedule courseSchedule) {
        if (courseSchedule == null || courseSchedule.getProblemId() == null) {
            return null;
        }
        Problem problem = cacheService.getProblem(courseSchedule.getProblemId());
        courseSchedule.setProblem(problem.simple());

        Map<Integer, String> monthTopic = cacheService.loadMonthTopic();
        if (monthTopic != null) {
            courseSchedule.setTopic(monthTopic.get(courseSchedule.getMonth()));
        }
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
    private List<Problem> getListProblem(List<ImprovementPlan> improvementPlans, List<Integer> problemIds, List<Integer> currentMonthProblemIds) {

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

    private boolean containsProblemId(List<Problem> problems, Integer problemId) {
        for (Problem problem : problems) {
            if (problem.getId() == problemId) {
                return true;
            }
        }
        return false;
    }
}
