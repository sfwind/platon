package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleChoiceDao;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleChoiceSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.schedule.ScheduleQuestionDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoice;
import com.iquanwai.platon.biz.po.schedule.ScheduleChoiceSubmit;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;
    @Autowired
    private RiseMemberDao riseMemberDao;

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

        //主修小课id
        List<CourseSchedule> majorSchedule = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == CourseScheduleDefault.Type.MAJOR)
                .collect(Collectors.toList());

        //本月主修小课id
        List<Integer> currentMonthMajorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == CourseScheduleDefault.Type.MAJOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //主修小课列表
        List<ImprovementPlan> majorProblem = getMajorListProblem(improvementPlans, majorSchedule, currentMonthMajorProblemIds);
        schedulePlan.setMajorProblem(majorProblem);

        //本月主修进度
        schedulePlan.setMajorPercent(completePercent(improvementPlans, currentMonthMajorProblemIds));

        //辅修小课id
        List<CourseSchedule> minorSchedule = courseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == CourseScheduleDefault.Type.MINOR)
                .collect(Collectors.toList());

        //本月辅修小课id
        List<Integer> currentMonthMinorProblemIds = currentMonthCourseSchedules.stream()
                .filter(courseSchedule -> courseSchedule.getType() == CourseScheduleDefault.Type.MINOR)
                .map(CourseSchedule::getProblemId).collect(Collectors.toList());

        //试听小课
        AuditionClassMember auditionClassMember = auditionClassMemberDao.loadByProfileId(profileId);
        Integer auditionProblemId = null;
        if (auditionClassMember != null) {
            List<ImprovementPlan> trialProblem = improvementPlans.stream()
                    .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                            || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                    .filter(improvementPlan -> improvementPlan.getProblemId().equals(auditionClassMember.getProblemId()))
                    .map(improvementPlan -> {
                        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                        improvementPlan.setProblem(problem.simple());
                        return improvementPlan;
                    }).collect(Collectors.toList());
            //如果试听课正在进行中,加入试听课列表
            if (CollectionUtils.isNotEmpty(trialProblem)) {
                schedulePlan.setTrialProblem(trialProblem);
                auditionProblemId = auditionClassMember.getProblemId();
            }

        }
        List<ImprovementPlan> minorProblem = getMinorListProblem(improvementPlans, minorSchedule, currentMonthMinorProblemIds);
        // 如果试听课正在进行中,则在辅修课中过滤试听课
        if (auditionProblemId != null) {
            int problemId = auditionProblemId;
            minorProblem = minorProblem.stream().filter(improvementPlan -> improvementPlan.getProblemId() != problemId)
                    .collect(Collectors.toList());
        }
        //辅修小课列表
        schedulePlan.setMinorProblem(minorProblem);
        schedulePlan.setMinorPercent(completePercent(improvementPlans, currentMonthMinorProblemIds));

        //本月辅修进度
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
            if (planProblemIds.contains(item.getProblemId()) || CourseScheduleDefault.Type.MAJOR == item.getType()) {
                item.setAdjustable(false);
            }
        });

        List<List<CourseSchedule>> courseScheduleLists = Lists.newArrayList();
        Map<Integer, List<CourseSchedule>> courseScheduleMap = courseSchedules.stream().collect(Collectors.groupingBy(CourseSchedule::getMonth));

        int startMonth;
        Integer category = accountService.loadUserScheduleCategory(profileId);
        if (CourseScheduleDefault.CategoryType.OLD_STUDENT == category) {
            startMonth = 8;
        } else {
            CourseSchedule oldestCourseSchedule = courseScheduleDao.loadOldestCourseSchedule(profileId);
            if (oldestCourseSchedule != null) {
                startMonth = oldestCourseSchedule.getMonth();
            } else {
                startMonth = 1;
            }
        }

        int month;
        for (int i = 0; i < 12; i++) {
            if (i == 0) {
                if (courseScheduleMap.get(startMonth) != null) {
                    courseScheduleLists.add(courseScheduleMap.get(startMonth));
                }
            } else {
                month = startMonth + i;
                if (month > 12) {
                    month = month % 12;
                }
                if (courseScheduleMap.get(month) != null) {
                    courseScheduleLists.add(courseScheduleMap.get(month));
                }
            }
        }

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
        // 用户购买记录
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);

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
                    .map(defaultCourse -> this.buildSchedule(defaultCourse, profileId, choices, riseMember.getOpenDate()))
                    .collect(Collectors.toList());
            // 插入数据库
            Map<Integer, List<CourseSchedule>> waitToReduce = waitInserts.stream()
                    .filter(item -> item.getType() == CourseScheduleDefault.Type.MINOR)
                    .filter(CourseSchedule::getRecommend)
                    .collect(Collectors.groupingBy(CourseSchedule::getMonth));
            if (choices.contains(ONE_MINOR)) {
                // 一门课
                waitToReduce.forEach((key, list) -> {
                    if (list.size() > 1) {
                        // 需要筛选
                        list.forEach(item -> {
                            item.setRecommend(false);
                            item.setSelected(false);
                        });
                        list.stream().sorted(this::scoreCompare).findFirst().ifPresent(item -> {
                            item.setSelected(true);
                            item.setRecommend(true);
                        });
                    }
                });
            } else if (choices.contains(TWO_MINOR)) {
                // 二门课
                waitToReduce.forEach((key, list) -> {
                    List<Integer> selectedIds = Lists.newArrayList(list.stream().mapToInt(CourseSchedule::getProblemId).iterator());

                    if (list.size() > 2) {
                        // 需要筛选
                        list.forEach(item -> {
                            item.setRecommend(false);
                            item.setSelected(false);
                        });
                        list.stream().sorted(this::scoreCompare).limit(2).forEach(item -> {
                            item.setRecommend(true);
                            item.setSelected(true);
                        });
                    } else {
                        // 不满足两门，要补足两门
                        List<Integer> collect = defaults.stream()
                                .filter(item -> Objects.equals(item.getMonth(), key))
                                .filter(item -> item.getType() == CourseScheduleDefault.Type.MINOR)
                                .filter(item -> !selectedIds.contains(item.getProblemId()))
                                .map(CourseScheduleDefault::getProblemId)
                                .sorted(((o1, o2) -> {
                                    Problem p1 = cacheService.getProblem(o1);
                                    Problem p2 = cacheService.getProblem(o2);
                                    // 默认四分
                                    Double useful1 = p1.getUsefulScore() == null ? 4 : p1.getUsefulScore();
                                    Double useful2 = p2.getUsefulScore() == null ? 4 : p2.getUsefulScore();
                                    return useful2.compareTo(useful1);
                                }))
                                .limit(2 - list.size())
                                .collect(Collectors.toList());
                        waitInserts.forEach(item -> {
                            if (collect.contains(item.getProblemId())) {
                                item.setSelected(true);
                                item.setRecommend(true);
                            }
                        });
                    }
                });
            }
            courseScheduleDao.batchInsertCourseSchedule(waitInserts);
        } else {
            logger.error("用户：{}，再次生成课表", profileId);
        }
    }

    private int scoreCompare(CourseSchedule o1, CourseSchedule o2) {
        Problem p1 = cacheService.getProblem(o1.getProblemId());
        Problem p2 = cacheService.getProblem(o2.getProblemId());
        Double useful1 = p1.getUsefulScore() == null ? 4 : p1.getUsefulScore();
        Double useful2 = p2.getUsefulScore() == null ? 4 : p2.getUsefulScore();
        return useful2.compareTo(useful1);
    }

    private CourseSchedule buildSchedule(CourseScheduleDefault defaultSchedule, Integer profileId,
                                         List<Integer> choices, Date openDate) {
        Integer year;
        Integer month;
        if (defaultSchedule.getCategory() == CourseScheduleDefault.CategoryType.NEW_STUDENT) {
            // 新学员，以开营日来计算
            month = DateUtils.getMonth(openDate);
            year = DateUtils.getYear(openDate);
        } else {
            // 老学员
            year = 2017;
            month = 8;
        }
        if (defaultSchedule.getMonth() < month) {
            year++;
        }
        CourseSchedule schedule = new CourseSchedule();
        schedule.setYear(year);
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
                peek(item -> {
                    List<ScheduleChoice> choicesGroup = mapChoices.get(item.getId());
                    choicesGroup.sort(((o1, o2) -> {
                        int o1Sequence = o1.getSequence() == null ? 0 : o1.getSequence();
                        int o2Sequence = o2.getSequence() == null ? 0 : o2.getSequence();
                        return o1Sequence - o2Sequence;
                    }));
                    item.setScheduleChoices(choicesGroup);
                }).
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

        return courseSchedules.stream().filter(courseSchedule -> courseSchedule.getMonth() == month &&
                courseSchedule.getCategory().equals(category)).collect(Collectors.toList());
    }


    private boolean containsProblemId(List<ImprovementPlan> plans, Integer problemId) {
        return plans.stream().anyMatch(improvementPlan -> {
                    if (improvementPlan.getProblem() == null) {
                        return false;
                    }
                    return improvementPlan.getProblem().getId() == problemId;
                }
        );
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

    //辅修小课列表 = 进行中辅修小课+本月计划辅修小课
    private List<ImprovementPlan> getMinorListProblem(List<ImprovementPlan> improvementPlans,
                                                 List<CourseSchedule> courseSchedules,
                                                 List<Integer> currentMonthProblemIds) {

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        int month = DateUtils.getMonth(date);
        // 选出进行中的辅修小课
        List<ImprovementPlan> problems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> {
                    CourseSchedule courseSchedule = courseSchedules.stream()
                            .filter(courseSchedule1 -> courseSchedule1.getType().equals(CourseScheduleDefault.Type.MINOR))
                            .filter(courseSchedule1 -> courseSchedule1.getProblemId().equals(improvementPlan.getProblemId()))
                            .findAny().orElse(null);
                    if (courseSchedule != null) {
                        improvementPlan.setMonth(courseSchedule.getMonth());
                        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                        improvementPlan.setProblem(problem.simple());
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());

        // 选出已完成的小课
        List<ImprovementPlan> closeProblems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.CLOSE)
                .collect(Collectors.toList());

        //如果本月辅修小课没有开始,加到推荐列表
        currentMonthProblemIds.forEach(currentMonthProblemId -> {
            boolean inRunning = containsProblemId(problems, currentMonthProblemId);
            boolean inClose = containsProblemId(closeProblems, currentMonthProblemId);
            if (!inRunning && !inClose) {
                ImprovementPlan improvementPlan = new ImprovementPlan();
                improvementPlan.setMonth(month);
                Problem problem = cacheService.getProblem(currentMonthProblemId).simple();
                improvementPlan.setProblem(problem);
                improvementPlan.setProblemId(problem.getId());
                improvementPlan.setTotalSeries(problem.getLength());
                improvementPlan.setCompleteSeries(0);
                problems.add(improvementPlan);
            }
        });

        return problems;
    }

    //主修小课列表 = 进行中主修小课+本月计划主修小课+往期未开主修小课
    private List<ImprovementPlan> getMajorListProblem(List<ImprovementPlan> improvementPlans,
                                                      List<CourseSchedule> courseSchedules,
                                                      List<Integer> currentMonthProblemIds) {

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        //拿到开营日
        Date date = monthlyCampConfig.getOpenDate();

        int month = DateUtils.getMonth(date);
        // 选出进行中的主修小课
        List<ImprovementPlan> problems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.RUNNING
                        || improvementPlan.getStatus() == ImprovementPlan.COMPLETE)
                .filter(improvementPlan -> {
                    CourseSchedule courseSchedule = courseSchedules.stream()
                            .filter(courseSchedule1 -> courseSchedule1.getType().equals(CourseScheduleDefault.Type.MAJOR))
                            .filter(courseSchedule1 -> courseSchedule1.getProblemId().equals(improvementPlan.getProblemId()))
                            .findAny().orElse(null);
                    if (courseSchedule != null) {
                        improvementPlan.setMonth(courseSchedule.getMonth());
                        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                        improvementPlan.setProblem(problem.simple());
                        return true;
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());

        // 选出已完成的小课
        List<ImprovementPlan> closeProblems = improvementPlans.stream()
                .filter(improvementPlan -> improvementPlan.getStatus() == ImprovementPlan.CLOSE)
                .collect(Collectors.toList());

        //如果本月主修小课没有开始,加到推荐列表
        currentMonthProblemIds.forEach(currentMonthProblemId -> {
            boolean inRunning = containsProblemId(problems, currentMonthProblemId);
            boolean inClose = containsProblemId(closeProblems, currentMonthProblemId);
            if (!inRunning && !inClose) {
                ImprovementPlan improvementPlan = new ImprovementPlan();
                improvementPlan.setMonth(month);
                Problem problem = cacheService.getProblem(currentMonthProblemId).simple();
                improvementPlan.setProblem(problem);
                improvementPlan.setProblemId(problem.getId());
                improvementPlan.setTotalSeries(problem.getLength());
                improvementPlan.setCompleteSeries(0);
                problems.add(improvementPlan);
            }
        });

        problems.addAll(getPreUnopenMajorProblems(improvementPlans, courseSchedules));

        return problems;
    }

    // 之前月份未开课的小课
    private List<ImprovementPlan> getPreUnopenMajorProblems(List<ImprovementPlan> improvementPlans,
                                                            List<CourseSchedule> courseSchedules) {
        List<ImprovementPlan> improvementPlanList = Lists.newArrayList();

        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        int month = DateUtils.getMonth(monthlyCampConfig.getOpenDate());
        int year = DateUtils.getYear(monthlyCampConfig.getOpenDate());

        // 过去几个月的主修课id
        List<CourseSchedule> courseScheduleList = courseSchedules.stream().filter(courseSchedule -> {
            if (courseSchedule.getType() == CourseScheduleDefault.Type.MINOR) {
                return false;
            }
            return courseSchedule.getYear() < year ||
                    (courseSchedule.getYear() == year && courseSchedule.getMonth() < month);
        }).sorted((o1, o2) -> o2.getMonth() - o1.getMonth()).collect(Collectors.toList());

        //如果之前月份的主修课没有开始,加到推荐列表
        courseScheduleList.forEach(courseSchedule -> {
            Integer problemId = courseSchedule.getProblemId();
            boolean in = containsProblemId(improvementPlans, problemId);
            if (!in) {
                ImprovementPlan improvementPlan = new ImprovementPlan();
                improvementPlan.setMonth(courseSchedule.getMonth());
                Problem problem = cacheService.getProblem(problemId).simple();
                improvementPlan.setProblem(problem.simple());
                improvementPlan.setProblemId(problem.getId());
                improvementPlan.setTotalSeries(problem.getLength());
                improvementPlan.setCompleteSeries(0);
                improvementPlanList.add(improvementPlan);
            }
        });

        return improvementPlanList;
    }
}
