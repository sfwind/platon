package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDefaultDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleChoiceDao;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleChoiceSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleQuestionDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.CourseScheduleDefault;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.MonthlyCampConfig;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoice;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoiceSubmit;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author justin
 * @version 2017/11/3
 */
@Service
public class BusinessPlanServiceImpl implements BusinessPlanService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Integer NO_MINOR = 24;
    private static Integer DEFAULT_MINOR = 25;
    private static Integer ALL_MINOR = 26;

    @Autowired
    private CacheService cacheService;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ScheduleQuestionDao scheduleQuestionDao;
    @Autowired
    private ScheduleChoiceDao scheduleChoiceDao;
    @Autowired
    private ScheduleChoiceSubmitDao scheduleChoiceSubmitDao;
    @Autowired
    private AccountService accountService;

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

        Integer category = accountService.loadUserScheduleCategory(profileId);
        int month = DateUtils.getMonth(date);
        schedulePlan.setMonth(month);
        schedulePlan.setToday(DateUtils.parseDateToFormat5(new Date()));
        //TODO 一定要改为学习的年份!!!!!
        schedulePlan.setTopic(cacheService.loadMonthTopic(category).get(month));
        return schedulePlan;
    }

    @Override
    public List<List<CourseSchedule>> loadPersonalCourseSchedule(Integer profileId) {
        List<CourseSchedule> courseSchedules = courseScheduleDao.getAllScheduleByProfileId(profileId);
        courseSchedules.forEach((item) -> this.buildProblemData(item, profileId));

        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        List<Integer> planProblemIds = improvementPlans.stream().map(ImprovementPlan::getProblemId).collect(Collectors.toList());
        courseSchedules.forEach((item) -> {
            if (planProblemIds.contains(item.getProblemId())) {
                item.setAdjustable(false);
            }
        });

        List<List<CourseSchedule>> courseScheduleLists = Lists.newArrayList();
        Map<Integer, List<CourseSchedule>> courseScheduleMap = courseSchedules.stream().collect(Collectors.groupingBy(CourseSchedule::getMonth));
        courseScheduleMap.forEach((k, v) -> courseScheduleLists.add(v));
        return courseScheduleLists;
    }

    @Override
    public List<List<CourseSchedule>> loadDefaultCourseSchedule(Integer profileId) {
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadDefaultCourseSchedule();
        List<CourseSchedule> courseSchedules = courseScheduleDefaults.stream().map(courseScheduleDefault -> {
            ModelMapper modelMapper = new ModelMapper();
            return modelMapper.map(courseScheduleDefault, CourseSchedule.class);
        }).collect(Collectors.toList());
        courseSchedules.forEach((item) -> this.buildProblemData(item, profileId));
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

    @Override
    public void initCourseSchedule(Integer profileId, List<ScheduleQuestion> scheduleQuestions) {
        // 用户类型id
        Integer categoryId = accountService.loadUserScheduleCategory(profileId);
        // 默认课表
        List<CourseScheduleDefault> defaults = courseScheduleDefaultDao.loadDefaultCourseScheduleByCategory(categoryId);
        // 用户课表
        List<CourseSchedule> userSchedule = courseScheduleDao.getAllScheduleByProfileId(profileId);
        // 用户选择的选项id
        List<Integer> choices = Lists.newArrayList();
        scheduleQuestions.forEach(question -> question.getScheduleChoices().forEach(item -> choices.add(item.getId())));
        if (CollectionUtils.isEmpty(userSchedule)) {
            // 插入用户选择
            scheduleChoiceSubmitDao.batchInsert(choices.stream().map(item -> {
                ScheduleChoiceSubmit submit = new ScheduleChoiceSubmit();
                submit.setProfileId(profileId);
                submit.setChoiceId(item);
                return submit;
            }).collect(Collectors.toList()));
            // 用户还没有课表,生成课表
            List<CourseSchedule> waitInserts = defaults.stream()
                    .map(defaultCourse -> this.buildSchedule(defaultCourse, profileId, choices))
                    .collect(Collectors.toList());
            // 插入数据库
            courseScheduleDao.batchInsertCourseSchedule(waitInserts);
        } else {
            logger.error("用户：{}，再次生成课表", profileId);
        }
    }

    private CourseSchedule buildSchedule(CourseScheduleDefault defaultSchedule, Integer profileId, List<Integer> choices) {
        CourseSchedule schedule = new CourseSchedule();
        schedule.setMonth(defaultSchedule.getMonth());
        schedule.setProfileId(profileId);
        schedule.setProblemId(defaultSchedule.getProblemId());
        schedule.setType(defaultSchedule.getType());
        schedule.setCategory(defaultSchedule.getCategory());
        Boolean recommend = false;
        if (defaultSchedule.getType() == CourseScheduleDefault.Type.MINOR) {
            List<Integer> initChoices = Lists.newArrayList(defaultSchedule.getInitChoice().split(",")).stream().map(Integer::valueOf).collect(Collectors.toList());
            recommend = choices.stream().anyMatch(initChoices::contains);
        }

        if (choices.contains(NO_MINOR)) {
            // 默认不选
            recommend = false;
        } else if (choices.contains(ALL_MINOR)) {
            // 默认全选
            recommend = true;
        }
        schedule.setRecommend(recommend);
        // 默认选中
        schedule.setSelected(recommend);
        return schedule;
    }

    @Override
    public List<ScheduleQuestion> loadScheduleQuestions() {
        List<ScheduleQuestion> questions = scheduleQuestionDao.loadAll(ScheduleQuestion.class);
        List<ScheduleChoice> choices = scheduleChoiceDao.loadAll(ScheduleChoice.class);
        Map<Integer, List<ScheduleChoice>> mapChoices = choices.stream().
                filter(item -> !item.getDel()).
                collect(Collectors.groupingBy(ScheduleChoice::getQuestionId));
        return questions.stream().
                filter(item -> !item.getDel()).
                peek(item -> item.setScheduleChoices(mapChoices.get(item.getId()))).
                collect(Collectors.toList());
    }

    @Override
    public boolean updateProblemScheduleSelected(Integer courseScheduleId, Boolean selected) {
        return courseScheduleDao.updateSelected(courseScheduleId, selected ? 1 : 0) > 0;
    }

    @Override
    public void batchModifyCourseSchedule(Integer year, Integer month, List<CourseSchedule> courseSchedules) {
        courseSchedules.forEach(schedule -> {
            Integer id = schedule.getId();
            Boolean selected = schedule.getSelected();
            courseScheduleDao.modifyScheduleYearMonth(id, year, month, selected ? 1 : 0);
        });
    }

    // 将 problem 的数据放入 CourseSchedule 之中
    private CourseSchedule buildProblemData(CourseSchedule courseSchedule, Integer profileId) {
        if (courseSchedule == null || courseSchedule.getProblemId() == null) {
            return null;
        }
        Problem problem = cacheService.getProblem(courseSchedule.getProblemId());
        courseSchedule.setProblem(problem.simple());
        Integer category = accountService.loadUserScheduleCategory(profileId);
        Map<Integer, String> monthTopicMap = cacheService.loadMonthTopic(category);
        if (monthTopicMap != null) {
            courseSchedule.setTopic(monthTopicMap.get(courseSchedule.getMonth()));
        }
        courseSchedule.setAdjustable(problem.getPublish());
        return courseSchedule;
    }

    private List<CourseSchedule> getCurrentMonthSchedule(List<CourseSchedule> courseSchedules) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();
        Optional<CourseSchedule> first = courseSchedules.stream().findFirst();
        Integer category;
        if (first.isPresent()) {
            category = accountService.loadUserScheduleCategory(first.get().getProfileId());
        } else {
            category = null;
        }

        int month = DateUtils.getMonth(date);
        int year = DateUtils.getYear(date);

        return courseSchedules.stream().filter(courseSchedule -> courseSchedule.getMonth() == month &&
                courseSchedule.getCategory().equals(category)).collect(Collectors.toList());
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
