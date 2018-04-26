package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.CardManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.Section;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private ProblemScoreDao problemScoreDao;
    @Autowired
    private ProblemExtensionDao problemExtensionDao;
    @Autowired
    private ProblemActivityDao problemActivityDao;
    @Autowired
    private ProblemCollectionDao problemCollectionDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CardManager cardManager;
    @Autowired
    private ProblemScheduleManager problemScheduleManager;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private OperationLogService operationLogService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<Problem> loadProblems() {
        //去除已删除的课程
        return cacheService.getProblems().stream().
                filter(problem -> !problem.getDel())
                .filter(Problem::getPublish)
                .collect(Collectors.toList());
    }

    @Override
    public Problem getProblem(Integer problemId) {
        return cacheService.getProblem(problemId);
    }

    @Override
    public Problem getProblemForSchedule(Integer problemId, Integer profileId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        Problem problem = cacheService.getProblem(problemId);
        List<Chapter> chapters;
        if (improvementPlan != null) {
            chapters = problemScheduleManager.loadRoadMap(improvementPlan.getId());
        } else {
            chapters = problemScheduleManager.loadDefaultRoadMap(problemId);
        }
        problem.setChapterList(chapters);
        problem.setProblemType(problemScheduleManager.getProblemType(problemId, profileId));
        return problem;
    }

    @Override
    public Problem getProblemForSchedule(Integer practicePlanId) {
        PracticePlan practicePlan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, practicePlan.getPlanId());
        Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
        List<Chapter> chapters = problemScheduleManager.loadRoadMap(improvementPlan.getId());
        problem.setChapterList(chapters);

        return problem;
    }

    @Override
    public List<ProblemCatalog> getProblemCatalogs() {
        return cacheService.loadProblemCatalogs();
    }

    @Override
    public ProblemCatalog getProblemCatalog(Integer catalogId) {
        return cacheService.getProblemCatalog(catalogId);
    }

    @Override
    public ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId) {
        return cacheService.getProblemSubCatalog(subCatalogId);
    }

    @Override
    public void gradeProblem(Integer problem, Integer profileId, List<ProblemScore> problemScores) {
        problemScores.forEach(item -> {
            item.setProfileId(profileId);
            item.setProblemId(problem);
            Integer question = item.getQuestion();
            Integer choice = item.getChoice();
            if (choice != null) {
                operationLogService.trace(profileId, "gradeCourse", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    prop.add("problemId", problem);
                    prop.add("question", question);
                    prop.add("choice", choice);
                    return prop;
                });
            }
        });

        problemScoreDao.gradeProblem(problemScores);
    }

    @Override
    public ProblemExtension loadProblemExtensionByProblemId(Integer problemId) {
        return problemExtensionDao.loadByProblemId(problemId);
    }

    @Override
    public List<ProblemActivity> loadProblemActivitiesByProblemId(Integer problemId) {
        return problemActivityDao.loadProblemActivitiesByProblemId(problemId);
    }

    @Override
    public List<ProblemCard> loadProblemCardsList(Integer profileId) {
        List<ProblemCard> problemCards = Lists.newArrayList();
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadAllPlans(profileId);
        improvementPlans.forEach(plan -> {
            Problem problem = cacheService.getProblem(plan.getProblemId());
            ProblemCard problemCard = new ProblemCard();
            problemCard.setPlanId(plan.getId());
            problemCard.setProblemId(plan.getProblemId());
            problemCard.setName(problem.getProblem());
            problemCard.setAbbreviation(problem.getAbbreviation());
            List<EssenceCard> essenceCards = loadProblemCardsByPlanId(plan.getId()).getRight();
            int completeCount = (int) essenceCards.stream().filter(EssenceCard::getCompleted).count();
            problemCard.setCompleteCount(completeCount);
            problemCards.add(problemCard);
        });
        return problemCards;
    }

    @Override
    public Pair<Problem, List<EssenceCard>> loadProblemCardsByPlanId(Integer planId) {
        // 根据 planId 获取 improvement 中的 problemId
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Integer problemId = plan.getProblemId();
        // 获取 essenceCard 所有与当前课程相关的数据
        Problem problem = cacheService.getProblem(problemId);
        List<Chapter> chapters = problemScheduleManager.loadRoadMap(planId);
        Integer completeSeries = plan.getCompleteSeries();
        // 目标 essenceList
        List<EssenceCard> cards = Lists.newArrayList();
        Integer tempChapter = 0;
        for (Chapter chapter : chapters) {
            Integer chapterId = chapter.getChapter();
            EssenceCard essenceCard = new EssenceCard();
            essenceCard.setProblemId(problemId);
            essenceCard.setChapterId(chapterId);
            essenceCard.setThumbnail(cardManager.loadTargetThumbnailByChapterId(chapterId, chapters.size()));
            essenceCard.setThumbnailLock(cardManager.loadTargetThumbnailLockByChapterId(chapterId, chapters.size()));
            essenceCard.setChapterNo("第" + NumberToHanZi.formatInteger(chapterId) + "章");
            if (chapterId == chapters.size()) {
                essenceCard.setChapter("课程知识清单");
            } else {
                essenceCard.setChapter(chapter.getName());
            }
            cards.add(essenceCard);
            // 计算已完成的章节号
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                if (section.getSeries().equals(completeSeries)) {
                    tempChapter = section.getChapter();
                }
            }
        }
        Integer completedChapter = 0;
        for (Chapter chapter : chapters) {
            if (chapter.getChapter().equals(tempChapter)) {
                List<Section> sections = chapter.getSections();
                Long resultCnt = sections.stream().filter(section -> section.getSeries() > completeSeries).count();
                completedChapter = resultCnt > 0 ? chapter.getChapter() - 1 : chapter.getChapter();
            }
        }
        for (EssenceCard essenceCard : cards) {
            essenceCard.setCompleted(essenceCard.getChapterId() <= completedChapter);
        }
        return new MutablePair<>(problem, cards);
    }

    // 获取精华卡图
    @Override
    public String loadEssenceCardImg(Integer profileId, Integer problemId, Integer chapterId) {
        ImprovementPlan improvementPlan = improvementPlanDao.loadPlanByProblemId(profileId, problemId);
        return cardManager.loadEssenceCardImg(profileId, problemId, chapterId, improvementPlan.getId());
    }

    @Override
    public boolean hasCollectedProblem(Integer profileId, Integer problemId) {
        ProblemCollection collection = problemCollectionDao.loadUsefulCollection(profileId, problemId);
        return collection != null;
    }

    @Override
    public List<Problem> loadProblemCollections(Integer profileId) {
        List<ProblemCollection> collections = problemCollectionDao.loadCollectionsByProfileId(profileId);

        List<Problem> problemCollections = Lists.newArrayList();
        for (ProblemCollection collection : collections) {
            Problem problem = cacheService.getProblem(collection.getProblemId());
            if (problem != null) {
                problemCollections.add(problem);
            }
        }
        return problemCollections;
    }

    @Override
    public List<Problem> loadHotProblems(List<Integer> problemIds) {
        List<Problem> problems = Lists.newArrayList();
        for (Integer problemId : problemIds) {
            Problem problem = cacheService.getProblem(problemId);
            Assert.notNull(problem, "配置的课程不能为空");
            problems.add(problem);
        }
        return problems;
    }

    @Override
    public List<ExploreBanner> loadExploreBanner() {
        JSONArray bannerArray = JSONArray.parseArray(ConfigUtils.getExploreBannerString());
        List<ExploreBanner> banners = Lists.newArrayList();
        // 专项课 Banner 放第一个
        ExploreBanner campBanner = new ExploreBanner();
        campBanner.setImageUrl(ConfigUtils.getCampProblemBanner());
        campBanner.setLinkUrl(ConfigUtils.domainName() + "/pay/camp");
        banners.add(campBanner);

        for (int i = 0; i < bannerArray.size(); i++) {
            JSONObject json = bannerArray.getJSONObject(i);
            ExploreBanner banner = new ExploreBanner();
            banner.setImageUrl(json.getString("imageUrl"));
            banner.setLinkUrl(json.getString("linkUrl"));
            banners.add(banner);
        }
        return banners;
    }

}
