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
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoice;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoiceSubmit;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
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
    private static Integer ONE_MINOR = 25;
    private static Integer TWO_MINOR = 26;
    private static Integer ALL_MINOR = 37;

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
        return courseScheduleDao.getAllScheduleByProfileId(profileId).stream()
                .filter(CourseSchedule::getSelected).collect(Collectors.toList());
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
        List<ImprovementPlan> completeProblem = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.CLOSE)
                .map(improvementPlan -> {
                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    improvementPlan.setProblem(problem.simple());
                    return improvementPlan;
                })
                .collect(Collectors.toList());
        schedulePlan.setCompleteProblem(completeProblem);

        //试听小课
        List<ImprovementPlan> trialProblem = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> improvementPlan.getProblemId().equals(ConfigUtils.getTrialProblemId()))
                .map(improvementPlan -> {
                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    improvementPlan.setProblem(problem.simple());
                    return improvementPlan;
                }).collect(Collectors.toList());
        schedulePlan.setTrialProblem(trialProblem);

        //主修小课id
        List<CourseSchedule> majorSchedule = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MAJOR)
                .collect(Collectors.toList());

        //本月主修小课id
        List<Integer> currentMonthMajorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MAJOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //主修小课列表
        schedulePlan.setMajorProblem(getListProblem(improvementPlans, majorSchedule, currentMonthMajorProblemIds));

        //本月主修进度
        schedulePlan.setMajorPercent(completePercent(improvementPlans, currentMonthMajorProblemIds));


        //辅修小课id
        List<CourseSchedule> minorSchedule = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MINOR)
                .collect(Collectors.toList());

        //本月辅修小课id
        List<Integer> currentMonthMinorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == Constants.ProblemType.MINOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //辅修小课列表
        schedulePlan.setMinorProblem(getListProblem(improvementPlans, minorSchedule, currentMonthMinorProblemIds));

        //本月主修进度
        schedulePlan.setMinorPercent(completePercent(improvementPlans, currentMonthMinorProblemIds));

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();

        //本月
        int month = monthlyCampConfig.getLearningMonth();
        Integer category = accountService.loadUserScheduleCategory(profileId);
        schedulePlan.setMonth(month);
        schedulePlan.setToday(DateUtils.parseDateToFormat5(new Date()));

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

        int courseMonth;
        for (int i = 0; i < 12; i++) {
            if (i == 0) {
                courseMonth = cacheService.loadMonthlyCampConfig().getLearningMonth();
                courseScheduleLists.add(courseScheduleMap.get(courseMonth));
            } else {
                courseMonth = cacheService.loadMonthlyCampConfig().getLearningMonth() + i;
                if (courseMonth > 12) {
                    courseMonth = courseMonth % 12;
                }
                courseScheduleLists.add(courseScheduleMap.get(courseMonth));
            }
        }

        return courseScheduleLists;
    }

    public static void main(String[] args) {
        DateTime dateTime = new DateTime(new Date());
        dateTime.plusMonths(1);
        System.out.println(dateTime.getMonthOfYear());
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
            // 是辅修课
            if (choices.contains(NO_MINOR)) {
                // 不选辅修课 ignore
            } else if (choices.contains(ONE_MINOR)) {
                // 一门辅修课
                if (defaultSchedule.getDefaultSelected()) {
                    // 默认选择，不用看用户选了什么
                    recommend = true;
                } else {
                    // 查看是否选了对应的题
                    if (defaultSchedule.getInitChoice() == null) {
                        recommend = false;
                    } else {
                        List<Integer> initChoices = Lists.newArrayList(defaultSchedule.getInitChoice().split(",")).stream().map(Integer::valueOf).collect(Collectors.toList());
                        recommend = choices.stream().anyMatch(initChoices::contains);
                    }
                }
            } else if (choices.contains(TWO_MINOR)) {
                if (defaultSchedule.getDefaultSelected()) {
                    recommend = true;
                } else {
                    // 两门辅修课
                    String initChoices;
                    if (defaultSchedule.getTwoMinorInitChoice() != null) {
                        initChoices = defaultSchedule.getTwoMinorInitChoice();
                    } else {
                        initChoices = defaultSchedule.getInitChoice();
                    }
                    List<Integer> initChoicesList = Lists.newArrayList(initChoices.split(",")).stream().map(Integer::valueOf).collect(Collectors.toList());
                    recommend = choices.stream().anyMatch(initChoicesList::contains);
                }
            } else if (choices.contains(ALL_MINOR)) {
                // 全部辅修课
                recommend = true;
            }
        } else {
            // 主修
            recommend = true;
        }

        schedule.setRecommend(recommend);
        schedule.setSelected(recommend);
        return schedule;
    }

    @Override
    public List<ScheduleQuestion> loadScheduleQuestions(Integer profileId) {
        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<ScheduleQuestion> questions = scheduleQuestionDao.loadAll(ScheduleQuestion.class)
                .stream()
                .filter(item -> Lists.newArrayList(item.getCategoryGroup().split(",")).contains(category.toString()))
                .collect(Collectors.toList());
        List<ScheduleChoice> choices = scheduleChoiceDao.loadAll(ScheduleChoice.class);
        Map<Integer, List<ScheduleChoice>> mapChoices = choices.stream().
                filter(item -> !item.getDel()).
                collect(Collectors.groupingBy(ScheduleChoice::getQuestionId));
        return questions.stream().
                filter(item -> !item.getDel()).
                sorted(((o1, o2) -> {
                    int o1Sequence = o1.getSequence() == null ? 0 : o1.getSequence();
                    int o2Sequence = o2.getSequence() == null ? 0 : o2.getSequence();
                    return o1Sequence - o2Sequence;
                })).
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
            // TODO 是否更新年份 商榷
            courseScheduleDao.modifyScheduleYearMonth(id, month, selected ? 1 : 0);
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


    private boolean containsProblemId(List<ImprovementPlan> plans, Integer problemId) {
        return plans.stream().anyMatch(improvementPlan -> improvementPlan.getProblem().getId() == problemId);
    }

    //计算主修或辅修小课进度
    private int completePercent(List<ImprovementPlan> improvementPlans, List<Integer> currentMonthProblemIds) {
        int totalSeries = improvementPlans.stream().filter(improvementPlan -> currentMonthProblemIds.contains(improvementPlan.getProblemId()))
                .collect(Collectors.summingInt(ImprovementPlan::getTotalSeries));

        int completeSeries = improvementPlans.stream().filter(improvementPlan -> currentMonthProblemIds.contains(improvementPlan.getProblemId()))
                .collect(Collectors.summingInt(ImprovementPlan::getCompleteSeries));

        if (completeSeries == 0) {
            return 0;
        }

        return completeSeries * 100 / totalSeries;

    }

    //小课列表 = 进行中小课+本月计划小课
    private List<ImprovementPlan> getListProblem(List<ImprovementPlan> improvementPlans,
                                                 List<CourseSchedule> courseSchedules,
                                                 List<Integer> currentMonthProblemIds) {
        List<Integer> problemIds = courseSchedules.stream().map(CourseSchedule::getProblemId)
                .collect(Collectors.toList());

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        int month = DateUtils.getMonth(date);
        // 选出进行中的小课
        List<ImprovementPlan> problems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> problemIds.contains(improvementPlan.getProblemId()))
                .map(improvementPlan -> {
                    CourseSchedule courseSchedule = courseSchedules.stream()
                            .filter(courseSchedule1 -> courseSchedule1.getProblemId().equals(improvementPlan.getProblemId()))
                            .findAny().orElse(null);
                    if (courseSchedule != null) {
                        improvementPlan.setMonth(courseSchedule.getMonth());
                    }

                    Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                    improvementPlan.setProblem(problem.simple());
                    return improvementPlan;
                }).collect(Collectors.toList());

        // 选出已完成的小课
        List<ImprovementPlan> closeProblems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.CLOSE)
                .collect(Collectors.toList());

        //如果本月小课没有开始,加到推荐列表
        currentMonthProblemIds.forEach(currentMonthProblemId -> {
            boolean inRunning = containsProblemId(problems, currentMonthProblemId);
            boolean inClose = containsProblemId(closeProblems, currentMonthProblemId);
            if (!inRunning && !inClose) {
                ImprovementPlan improvementPlan = new ImprovementPlan();
                improvementPlan.setMonth(month);
                improvementPlan.setProblem(cacheService.getProblem(currentMonthProblemId).simple());
                problems.add(improvementPlan);
            }
        });

        return problems;
    }
}
