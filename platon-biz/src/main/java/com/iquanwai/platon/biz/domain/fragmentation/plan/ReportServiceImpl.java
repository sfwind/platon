package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationPracticeDao;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.HomeworkVoteDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.SubjectArticleDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupSubmitDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepoImpl;
import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.HomeworkVote;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.PracticePlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupSubmit;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/6/8.
 */
@Service
public class ReportServiceImpl implements ReportService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private ApplicationPracticeDao applicationPracticeDao;
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private SubjectArticleDao subjectArticleDao;

    @Override
    public ImprovementReport loadUserImprovementReport(ImprovementPlan plan) {

        Problem problem = cacheService.getProblem(plan.getProblemId());
        ImprovementReport report = new ImprovementReport();
        report.setPlanId(plan.getId());
        // problem
        report.setProblem(problem.getProblem());
        report.setPic(problem.getPic());
        // 用时
        Integer studyDays = plan.getCompleteTime() == null ? -1 : (DateUtils.interval(plan.getStartDate(), plan.getCompleteTime()) + 1);
        report.setStudyDays(studyDays);
        // 打败多少人
        Integer percent = improvementPlanDao.defeatOthers(plan.getProblemId(), plan.getPoint());
        report.setPercent(percent);
        // 总分
        report.setTotalScore(plan.getPoint());
        // 计算
        calculateReport(report, plan);
        // 点赞与被点赞
        List<HomeworkVote> voteList = homeworkVoteDao.voteList(plan.getProfileId());
        List<HomeworkVote> votedList = homeworkVoteDao.votedList(plan.getProfileId());
        Integer voteCount = calculateVoteCount(voteList, plan);
        Integer votedCount = calculateVoteCount(votedList, plan);

        report.setReceiveVoteCount(votedCount);
        report.setShareVoteCount(voteCount);
        report.setVotedScore(votedCount * ConfigUtils.getVoteScore());

        // 如果有正在进行的则不显示按钮
        ImprovementPlan lastPlan = improvementPlanDao.getLastPlan(plan.getProfileId());
        if (lastPlan.getStatus() == 1) {
            report.setShowNextBtn(false);
        } else {
            report.setShowNextBtn(true);
        }
        return report;
    }

    private Integer calculateVoteCount(List<HomeworkVote> list,ImprovementPlan plan){
        Integer result = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            List<Integer> appList = list.stream().filter(item -> item.getType().equals(2)).map(HomeworkVote::getReferencedId).collect(Collectors.toList());
            List<Integer> subjectList = list.stream().filter(item -> item.getType().equals(3)).map(HomeworkVote::getReferencedId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(appList)) {
                // 查询点了多少应用练习
                result += applicationSubmitDao.problemReferenceCount(plan.getProblemId(), appList);
            }
            if (CollectionUtils.isNotEmpty(subjectList)) {
                // 查询点了多少精华分享
                result += subjectArticleDao.problemReferenceCount(plan.getProblemId(), subjectList);
            }
        }
        return result;
    }


    private void calculateReport(ImprovementReport report,ImprovementPlan plan){
        // 获得所有练习
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(plan.getId());
        // 热身练习
        List<PracticePlan> warmPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.WARM_UP) || item.getType().equals(PracticePlan.WARM_UP_REVIEW))
                .collect(Collectors.toList());
        // 应用练习
        List<PracticePlan> applicationPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.APPLICATION))
                .collect(Collectors.toList());
        // 综合练习
        List<PracticePlan> integratedPlanList = practicePlans.stream()
                .filter(item -> item.getType().equals(PracticePlan.APPLICATION_REVIEW))
                .collect(Collectors.toList());

        // 计算章节练习分数
        calculateWarmupScores(report, plan, warmPlanList);
        // 计算完成数量
        calculateCompleteCount(report, applicationPlanList, integratedPlanList);
        // 计算应用练习与综合练习分数
        calculateAppScores(report, applicationPlanList, integratedPlanList);
    }

    private void calculateAppScores(ImprovementReport report,List<PracticePlan> applicationPlanList, List<PracticePlan> integratedPlanList) {
        List<Integer> applicationIds = applicationPlanList.stream().map(item -> Integer.valueOf(item.getPracticeId())).collect(Collectors.toList());
        List<Integer> integratedIds = integratedPlanList.stream().map(item -> Integer.valueOf(item.getPracticeId())).collect(Collectors.toList());

        List<ApplicationPractice> applicationPractices;
        if (CollectionUtils.isEmpty(applicationIds)) {
            applicationPractices = Lists.newArrayList();
        } else {
            applicationPractices = applicationPracticeDao.loadPracticeList(applicationIds);
        }
        List<ApplicationPractice> integratedPractices;
        if (CollectionUtils.isEmpty(integratedIds)) {
            integratedPractices = Lists.newArrayList();
        } else {
            integratedPractices = applicationPracticeDao.loadPracticeList(integratedIds);
        }

        report.setApplicationTotalScore(0);
        report.setApplicationScore(0);
        report.setIntegratedTotalScore(0);
        report.setIntegratedScore(0);
        // 计算分数
        applicationPlanList.forEach(item->{
            // 已完成，计算分数
            Optional<ApplicationPractice> first = applicationPractices.stream().filter(app -> app.getId() == Integer.parseInt(item.getPracticeId())).findFirst();
            first.ifPresent(practice->{
                Integer point = PointRepoImpl.score.get(practice.getDifficulty());
                if (item.getStatus() == 1) {
                    report.setApplicationScore(report.getApplicationScore() + point);
                }
                report.setApplicationTotalScore(report.getApplicationTotalScore() + point);
            });
        });

        integratedPlanList.forEach(item->{
            // 已完成，计算分数
            Optional<ApplicationPractice> first = integratedPractices.stream().filter(app -> app.getId() == Integer.parseInt(item.getPracticeId())).findFirst();
            first.ifPresent(practice->{
                Integer point = PointRepoImpl.score.get(practice.getDifficulty());
                if (item.getStatus() == 1) {
                    report.setIntegratedScore(report.getIntegratedScore() + point);
                }
                report.setIntegratedTotalScore(report.getIntegratedTotalScore() + point);
            });
        });
    }

    private void calculateCompleteCount(ImprovementReport report, List<PracticePlan> applicationPlanList, List<PracticePlan> integratedPlanList) {
        // 数量计算
        Integer totalApplication = applicationPlanList.size();
        Integer integratedPlan = integratedPlanList.size();
        Long totalCompeleteApp = applicationPlanList.stream().filter(item -> item.getStatus() == 1).count();
        Long totalCompeleteIntegrated = integratedPlanList.stream().filter(item -> item.getStatus() == 1).count();
        report.setApplicationShouldCount(totalApplication);
        report.setIntegratedShouldCount(integratedPlan);
        report.setApplicationCompleteCount(totalCompeleteApp.intValue());
        report.setIntegratedCompleteCount(totalCompeleteIntegrated.intValue());
    }

    private void calculateWarmupScores(ImprovementReport report,ImprovementPlan plan,List<PracticePlan> warmPlanList){
        // 获得warmPlanList
        List<WarmupPractice> warmupPractices = initWarmupScores(warmPlanList);
        // 获得用户的提交记录
        List<Integer> questionLists = warmupPractices.stream().map(WarmupPractice::getId).collect(Collectors.toList());
        List<WarmupSubmit> warmupSubmit = warmupSubmitDao.getWarmupSubmit(plan.getId(), questionLists);
        // 获得章
        List<Chapter> chapters = cacheService.loadRoadMap(plan.getProblemId());
        // 用户提交的小节
        Map<Integer, List<WarmupSubmit>> submitMap = Maps.newHashMap();
        // 总的小节题目
        Map<Integer,List<WarmupPractice>> totalMap = Maps.newHashMap();
        // 填充数据
        warmPlanList.forEach(item->{
            // 每一个就是一个小节
            Integer series = item.getSeries();
            List<WarmupSubmit> seriesSubmits = submitMap.computeIfAbsent(series, key -> Lists.newArrayList());
            List<WarmupPractice> warmupPracticeList = totalMap.computeIfAbsent(series, key -> Lists.newArrayList());
            String practiceId = item.getPracticeId();
            String[] split = practiceId.split(",");
            for (String p : split) {
                Integer id = Integer.parseInt(p);
                // 给用户提交的小节里填充
                Optional<WarmupSubmit> firstSubmit = warmupSubmit.stream().filter(submit -> submit.getQuestionId().equals(id)).findFirst();
                firstSubmit.ifPresent(seriesSubmits::add);
                // 给总分那里填充
                Optional<WarmupPractice> firstPractice = warmupPractices.stream().filter(practice -> practice.getId() == id).findFirst();
                firstPractice.ifPresent(warmupPracticeList::add);
            }
        });
        // 计算小节得分
        Map<Integer,Integer> seriesScores = Maps.newHashMap();
        Map<Integer,Integer> seriesTotalScores = Maps.newHashMap();
        submitMap.forEach((series,submits)->{
            // 综合小节内题目的得分
            seriesScores.putIfAbsent(series, 0);
            submits.forEach(item->{
                seriesScores.computeIfPresent(series, (key, oldValue) -> oldValue + item.getScore());
            });
        });
        totalMap.forEach((series,practice)->{
            // 综合小节内题目的得分
            seriesTotalScores.putIfAbsent(series, 0);
            practice.forEach(item -> seriesTotalScores.computeIfPresent(series, (key, oldValue) -> oldValue + item.getScore()));
        });
        // 按章来区分
        chapters.forEach(item->{
            List<Section> sections = item.getSections();
            item.setMyWarmScore(0);
            item.setTotalWarmScore(0);
            // 综合每章内小节得分
            sections.forEach(section -> {
                item.setMyWarmScore(item.getMyWarmScore() + seriesScores.getOrDefault(section.getSeries(), 0));
                item.setTotalWarmScore(item.getTotalWarmScore() + seriesTotalScores.getOrDefault(section.getSeries(), 0));
            });
        });
        report.setChapterList(chapters);
    }

    private List<WarmupPractice> initWarmupScores(List<PracticePlan> warmPlanList){
        List<WarmupPractice> warmupPractices = Lists.newArrayList();
        warmPlanList.forEach(item->{
            String practiceIds = item.getPracticeId();
            String[] practiceIdArr = practiceIds.split(",");
            for (String arrItem : practiceIdArr) {
                try {
                    Integer practiceId = Integer.parseInt(arrItem);
                    WarmupPractice warmupPractice = cacheService.getWarmupPractice(practiceId);
                    Integer score = null;
                    if (warmupPractice == null) {
                        logger.error("该题已被删除:{}", practiceId);
                        continue;
                    } else {
                        Integer difficulty = warmupPractice.getDifficulty();
                        if (difficulty == 1) {
                            score = PointRepo.EASY_SCORE;
                        } else if(difficulty == 2){
                            score = PointRepo.NORMAL_SCORE;
                        } else if(difficulty == 3){
                            score = PointRepo.HARD_SCORE;
                        } else {
                            logger.error("难度系数不正常,{},{}", difficulty, practiceId);
                            score = 0;
                        }
                    }
                    warmupPractice.setScore(score);
                    warmupPractices.add(warmupPractice);

                } catch (Exception e) {
                    // ignore
                }

            }
        });
        return warmupPractices;
    }
}
