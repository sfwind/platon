package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.QuanwaiEmployeeDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/13.
 */
@Service
public class GeneratePlanServiceImpl implements GeneratePlanService {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserProblemScheduleDao userProblemScheduleDao;
    @Autowired
    private QuanwaiEmployeeDao quanwaiEmployeeDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INDEX_URL = "/rise/static/learn";

    /**
     * 1带1带学相关值
     */
    private static final Integer TEAM_LEARNING_PROBLEM_ID = 20;
    private static final Integer TEAM_LEARNING_MAX_SERIES = 2;

    @Override
    public void forceReopenPlan(Integer planId) {
        improvementPlanDao.reopenPlan(planId, DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH));
    }

    @Override
    public Integer createTeamLearningPlan(Integer profileId) {
        MonthlyCampConfig monthlyCampConfig = cacheService.loadMonthlyCampConfig();
        Date startDate = monthlyCampConfig.getOpenDate() != null && new Date().before(monthlyCampConfig.getOpenDate()) ? monthlyCampConfig.getOpenDate() : new Date();
        ImprovementPlan plan = improvementPlanDao.loadPlanByProblemId(profileId, TEAM_LEARNING_PROBLEM_ID);
        if (plan != null) {
            return this.magicUnlockProblem(profileId, TEAM_LEARNING_PROBLEM_ID, DateUtils.afterDays(startDate, PROBLEM_MAX_LENGTH));
        } else {
            // TODO 修改maxSeries和closeDate
            return this.generatePlan(profileId, TEAM_LEARNING_PROBLEM_ID, TEAM_LEARNING_MAX_SERIES, startDate, DateUtils.afterDays(startDate, 7));
        }
    }

    @Override
    public Integer generatePlan(Integer profileId, Integer problemId, Integer maxSeries, Date startDate, Date closeDate) {
        Assert.notNull(profileId, "profileId不能为空");
        Problem problem = cacheService.getProblem(problemId);
        if (problem == null) {
            logger.error("problemId {} is invalid", problemId);
        }
        //生成训练计划
        int planId = createPlan(problem, profileId, startDate, closeDate);

        List<PracticePlan> practicePlans = Lists.newArrayList();
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);

        problemSchedules.sort((o1, o2) -> {
            if (!o1.getChapter().equals(o2.getChapter())) {
                return o1.getChapter() - o2.getChapter();
            }
            return o1.getSection() - o2.getSection();
        });
        //生成章节和计划的映射关系
        List<UserProblemSchedule> userProblemSchedules = problemSchedules.stream().map(problemSchedule -> {
            ModelMapper modelMapper = new ModelMapper();
            UserProblemSchedule userProblemSchedule = modelMapper.map(problemSchedule, UserProblemSchedule.class);
            userProblemSchedule.setPlanId(planId);

            return userProblemSchedule;
        }).collect(Collectors.toList());
        userProblemScheduleDao.batchInsert(userProblemSchedules);

        // 生成小课介绍
        practicePlans.addAll(createIntroduction(problem, planId));
        // 生成小目标
        practicePlans.addAll(createChallengePractice(problem, planId));
        // 生成知识点
        practicePlans.addAll(createKnowledge(planId, problemSchedules));
        // 生成巩固练习
        practicePlans.addAll(createWarmupPractice(planId, problemSchedules));
        // 生成应用练习
        practicePlans.addAll(createApplicationPractice(problem, planId, problemSchedules));
        if (maxSeries != null) {
            practicePlans.stream().filter(item -> item.getSeries() > maxSeries).forEach(item -> item.setStatus(PracticePlan.STATUS.NEVER_UNLOCK));
        }
        //插入数据库
        practicePlanDao.batchInsert(practicePlans);

        return planId;
    }

    private List<PracticePlan> createIntroduction(Problem problem, Integer planId) {
        List<PracticePlan> selected = Lists.newArrayList();

        PracticePlan practicePlan = new PracticePlan();
        practicePlan.setPlanId(planId);
        practicePlan.setType(PracticePlan.INTRODUCTION);
        practicePlan.setPracticeId(problem.getId() + "");
        practicePlan.setSeries(0);
        practicePlan.setSequence(1);
        practicePlan.setStatus(PracticePlan.STATUS.UNCOMPLETED);
        practicePlan.setUnlocked(true);

        selected.add(practicePlan);
        return selected;
    }

    @Override
    public Integer generatePlan(Integer profileId, Integer problemId) {
        //员工没有选课限制
        if (quanwaiEmployeeDao.loadEmployee(profileId) != null) {
            return forceOpenProblem(profileId, problemId, null, null, true);
        } else {
            return generatePlan(profileId, problemId, null, null, null);
        }
    }

    private List<PracticePlan> createKnowledge(int planId, List<ProblemSchedule> problemScheduleList) {
        List<PracticePlan> selected = Lists.newArrayList();

        for (int sequence = 1; sequence <= problemScheduleList.size(); sequence++) {
            PracticePlan practicePlan = new PracticePlan();
            Integer knowledgeId = problemScheduleList.get(sequence - 1).getKnowledgeId();
            practicePlan.setUnlocked(false);
            practicePlan.setType(PracticePlan.KNOWLEDGE);
            practicePlan.setPlanId(planId);
            practicePlan.setPracticeId(knowledgeId.toString());
            practicePlan.setStatus(PracticePlan.STATUS.UNCOMPLETED);
            practicePlan.setSequence(KNOWLEDGE_SEQUENCE);
            practicePlan.setSeries(sequence);
            selected.add(practicePlan);
        }

        return selected;
    }

    @Override
    public void sendOpenPlanMsg(String openid, Integer problemId) {
        Problem problem = cacheService.getProblem(problemId);
        if (problem == null) {
            logger.error("problemId {} is invalid", problemId);
        }
        Assert.notNull(openid, "openid不能为空");
        Assert.notNull(problem, "problem不能为空");
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(openid);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.courseStartMsg());
        templateMessage.setUrl(ConfigUtils.domainName() + INDEX_URL);
        Profile profile = accountService.getProfile(openid);
        String first = "Hi，" + profile.getNickname() + "，你刚才选择了圈外课程：\n";
        int length = problem.getLength();

        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profile.getId(), problem.getId());

        String startDate = DateUtils.parseDateToStringByCommon(improvementPlan.getStartDate());
        String closeDate = DateUtils.parseDateToStringByCommon(DateUtils.beforeDays(improvementPlan.getCloseDate(), 1));

        data.put("first", new TemplateMessage.Keyword(first));
        data.put("keyword1", new TemplateMessage.Keyword(problem.getProblem()));
        data.put("keyword2", new TemplateMessage.Keyword(startDate + "——" + closeDate));
        data.put("remark", new TemplateMessage.Keyword("\n小tip：该课程共" + length + "节，建议每节至少做1道应用练习题，帮助你内化知识\n" +
                "\n如有疑问请在下方对话框留言，后台小哥哥会在24小时内回复你~"));
        templateMessageService.sendMessage(templateMessage);
    }

    /**
     * 创建小目标记录
     *
     * @param problem 小课信息
     * @param planId  计划id
     * @return 课程计划对象
     */
    private List<PracticePlan> createChallengePractice(Problem problem, int planId) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selected = Lists.newArrayList();

        PracticePlan practicePlan = new PracticePlan();
        practicePlan.setUnlocked(true);
        practicePlan.setPlanId(planId);
        practicePlan.setType(PracticePlan.CHALLENGE);
        practicePlan.setPracticeId(problem.getId() + "");
        practicePlan.setStatus(PracticePlan.STATUS.UNCOMPLETED);
        practicePlan.setSequence(WARMUP_SEQUENCE + APPLICATION_TASK_NUMBER + 1);
        practicePlan.setSeries(0);
        // practicePlan.setSummary(false);
        selected.add(practicePlan);

        return selected;
    }

    /**
     * 创建应用题记录
     *
     * @param planId              计划id
     * @param problemScheduleList 应用题课程计划
     * @return 课程计划对象
     */
    private List<PracticePlan> createApplicationPractice(Problem problem, int planId,
                                                         List<ProblemSchedule> problemScheduleList) {
        Assert.notNull(problem, "problem不能为空");
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        for (int sequence = 1; sequence <= problemScheduleList.size(); sequence++) {
            ProblemSchedule problemSchedule = problemScheduleList.get(sequence - 1);
            Integer knowledgeId = problemSchedule.getKnowledgeId();
            //该节是否是综合练习
            int problemId = problemSchedule.getProblemId();
            List<ApplicationPractice> practices = applicationPracticeDao.loadPractice(knowledgeId, problemId);
            practices = practices.stream().filter(applicationPractice -> !applicationPractice.getDel()).collect(Collectors.toList());
            //设置应用练习
            for (int i = 0; i < practices.size(); i++) {
                PracticePlan practicePlan = new PracticePlan();
                practicePlan.setUnlocked(false);
                practicePlan.setPlanId(planId);
                if (practices.get(i) != null && practices.get(i).getSequence() == 1) {
                    practicePlan.setType(PracticePlan.APPLICATION_BASE);
                } else {
                    practicePlan.setType(PracticePlan.APPLICATION_UPGRADED);
                }
                practicePlan.setSequence(WARMUP_SEQUENCE + 1 + i);
                practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
                //设置节序号
                practicePlan.setSeries(sequence);
                practicePlan.setStatus(PracticePlan.STATUS.UNCOMPLETED);
                practicePlan.setPracticeId(practices.get(i).getId() + "");
                selectedPractice.add(practicePlan);
            }
        }

        return selectedPractice;
    }

    /**
     * 创建选择题记录
     *
     * @param planId              计划id
     * @param problemScheduleList 选择题课程计划
     * @return 课程计划对象
     */
    private List<PracticePlan> createWarmupPractice(Integer planId,
                                                    List<ProblemSchedule> problemScheduleList) {
        List<PracticePlan> selectedPractice = Lists.newArrayList();

        //构建选择题
        for (int sequence = 1; sequence <= problemScheduleList.size(); sequence++) {
            PracticePlan practicePlan = new PracticePlan();
            ProblemSchedule problemSchedule = problemScheduleList.get(sequence - 1);
            Integer knowledgeId = problemSchedule.getKnowledgeId();
            //该节是否是综合练习
            boolean review = Knowledge.isReview(knowledgeId);
            practicePlan.setUnlocked(false);
            practicePlan.setPlanId(planId);
            if (!review) {
                practicePlan.setType(PracticePlan.WARM_UP);
            } else {
                practicePlan.setType(PracticePlan.WARM_UP_REVIEW);
            }
            practicePlan.setSequence(WARMUP_SEQUENCE);
            practicePlan.setSeries(sequence);
            practicePlan.setStatus(PracticePlan.STATUS.UNCOMPLETED);
            practicePlan.setKnowledgeId(problemSchedule.getKnowledgeId());
//            practicePlan.setSummary(false);
            int problemId = problemSchedule.getProblemId();
            List<WarmupPractice> practices = warmupPracticeDao.loadPractice(knowledgeId, problemId);
            //设置巩固练习的id
            List<Integer> practiceIds = Lists.newArrayList();
            practiceIds.addAll(practices.stream()
                    .filter(warmupPractice -> !warmupPractice.getExample() && !warmupPractice.getDel())
                    .sorted(Comparator.comparingInt(WarmupPractice::getSequence))
                    .map(WarmupPractice::getId)
                    .collect(Collectors.toList()));

            practicePlan.setPracticeId(StringUtils.join(practiceIds, ","));
            selectedPractice.add(practicePlan);
        }

        return selectedPractice;
    }

    /**
     * 创建plan记录
     *
     * @param problem   小课信息
     * @param profileId 用户id
     * @param startDate 开始时间
     * @param closeDate 结束时间
     * @return 小课id
     */
    private int createPlan(Problem problem, Integer profileId, Date startDate, Date closeDate) {
        Assert.notNull(problem, "problem不能为空");
        Assert.notNull(profileId, "profileId不能为空");
        if (startDate == null) {
            startDate = new Date();
        }
        if (closeDate == null) {
            closeDate = DateUtils.afterDays(startDate, PROBLEM_MAX_LENGTH);
        }
        Profile profile = accountService.getProfile(profileId);
        int length = problem.getLength();
        ImprovementPlan improvementPlan = new ImprovementPlan();
//        improvementPlan.setOpenid(profile.getOpenid());
        improvementPlan.setProfileId(profileId);
        improvementPlan.setWarmupComplete(0);
        improvementPlan.setApplicationComplete(0);
        improvementPlan.setProblemId(problem.getId());
        improvementPlan.setPoint(0);
        //初始化状态进行中
        improvementPlan.setStatus(ImprovementPlan.RUNNING);
        //初始化时有一把钥匙
        improvementPlan.setKeycnt(0);
        //总节数
        improvementPlan.setTotalSeries(length);
        improvementPlan.setCurrentSeries(1);
        improvementPlan.setStartDate(startDate);
        improvementPlan.setEndDate(null);

        improvementPlan.setRequestCommentCount(profile.getRequestCommentCount());
        improvementPlan.setCloseDate(closeDate);
        improvementPlan.setRiseMember(profile.getRiseMember() != Constants.RISE_MEMBER.FREE);
        return improvementPlanDao.insert(improvementPlan);
    }

    @Override
    public Integer magicUnlockProblem(Integer profileId, Integer problemId, Date closeDate) {
        return this.magicUnlockProblem(profileId, problemId, null, closeDate);
    }

    @Override
    public Integer magicUnlockProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate) {
        Integer resultPlanId = null;
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        if (improvementPlan != null) {
            List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(improvementPlan.getId());
            if (practicePlans.stream().anyMatch(item -> PracticePlan.STATUS.NEVER_UNLOCK.equals(item.getStatus()))) {
                // 有永不解锁的小节
                practicePlanDao.revertNeverUnlockPracticePlan(improvementPlan.getId());
            }

            Map<Integer, List<PracticePlan>> seriesGroup = practicePlans.stream().filter(item -> item.getSeries() != 0)
                    .collect(Collectors.groupingBy(PracticePlan::getSeries));
            List<Integer> seriesList = Lists.newArrayList(seriesGroup.keySet());
            seriesList.sort(Integer::compare);
            Integer maxSeries = seriesList.stream().mapToInt(item -> item).max().orElse(0);
            if (maxSeries == 0) {
                logger.error("获取最大小节数失败");
            }
            for (Integer series : seriesList) {
                List<PracticePlan> practicePlanList = seriesGroup.get(series);
                if (isDone(practicePlanList) && !series.equals(maxSeries)) {
                    // 这个小节做完了，并且不是最后一节，查看下一个小节是否解锁
                    List<PracticePlan> next = seriesGroup.get(series + 1);
                    if (!next.get(0).getUnlocked()) {
                        // 没有解锁，需要解锁
                        next.stream().mapToInt(PracticePlan::getId).forEach(practicePlanDao::unlock);
                    }
                } else {
                    // 小节没做完，或者已经是最后一节了，break
                    break;
                }
            }
            if (startDate != null) {
                // 刷新开始时间
                improvementPlanDao.updateStartDate(improvementPlan.getId(), startDate);
            }
            if (closeDate == null) {
                // 默认关闭时间，30天
                closeDate = DateUtils.afterDays(new Date(), PROBLEM_MAX_LENGTH);
            }
            // 设置为进行中，RiseMember为1，并且更新关闭时间
            improvementPlanDao.reopenPlan(improvementPlan.getId(), closeDate);

            resultPlanId = improvementPlan.getId();
        }
        return resultPlanId;
    }

    @Override
    public Integer magicOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg) {
        Integer resultPlanId;
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        Profile profile = accountService.getProfile(profileId);

        if (improvementPlan == null) {
            // 没课开课
            resultPlanId = generatePlan(profileId, problemId, null, null, null);
        } else {
            resultPlanId = improvementPlan.getId();
            // 已经开过课，则永远不发模板消息
            sendWelcomeMsg = false;
            // 解锁
            this.magicUnlockProblem(profileId, problemId, startDate, closeDate);
        }

        if (startDate != null) {
            improvementPlanDao.updateStartDate(resultPlanId, startDate);
        }
        if (closeDate != null) {
            improvementPlanDao.updateCloseDate(resultPlanId, closeDate);
        }
        if (sendWelcomeMsg) {
            sendOpenPlanMsg(profile.getOpenid(), problemId);
        }
        return resultPlanId;
    }


    @Override
    public Integer forceOpenProblem(Integer profileId, Integer problemId, Date startDate, Date closeDate, Boolean sendWelcomeMsg) {
        Integer resultPlanId;
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        Profile profile = accountService.getProfile(profileId);

        if (improvementPlan == null) {
            // 没课开课
            resultPlanId = generatePlan(profileId, problemId, null, null, null);
        } else {
            resultPlanId = improvementPlan.getId();
            // 已经开过课，则永远不发模板消息
            sendWelcomeMsg = false;
            // 强制解锁
            forceReopenPlan(resultPlanId);
        }

        practicePlanDao.batchUnlockByPlanId(resultPlanId);
        if (startDate != null) {
            improvementPlanDao.updateStartDate(resultPlanId, startDate);
        }
        if (closeDate != null) {
            improvementPlanDao.updateCloseDate(resultPlanId, closeDate);
        }
        if (sendWelcomeMsg) {
            sendOpenPlanMsg(profile.getOpenid(), problemId);
        }
        return resultPlanId;
    }

    private boolean isDone(List<PracticePlan> runningPractices) {
        if (CollectionUtils.isNotEmpty(runningPractices)) {
            // 练习题
            for (PracticePlan practicePlan : runningPractices) {
                //巩固练习或理解练习未完成时,返回false
                // 是否必做
                Boolean isRequest = practicePlan.getType() == PracticePlan.WARM_UP ||
                        practicePlan.getType() == PracticePlan.WARM_UP_REVIEW ||
                        practicePlan.getType() == PracticePlan.KNOWLEDGE ||
                        practicePlan.getType() == PracticePlan.KNOWLEDGE_REVIEW;
                // 是否未完成
                Boolean isNotComplete = !PracticePlan.STATUS.COMPLETED.equals(practicePlan.getStatus());
                if (isRequest && isNotComplete) {
                    // 必做并且未完成
                    return false;
                }
            }
        }

        return true;
    }
}
