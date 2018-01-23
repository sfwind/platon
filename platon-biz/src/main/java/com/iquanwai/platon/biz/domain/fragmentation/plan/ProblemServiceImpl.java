package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    private ProblemDao problemDao;
    @Autowired
    private ProblemCollectionDao problemCollectionDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CourseScheduleDefaultDao courseScheduleDefaultDao;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ProblemScheduleRepository problemScheduleRepository;
    @Autowired
    private AccountService accountService;

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
            chapters = problemScheduleRepository.loadRoadMap(improvementPlan.getId());
        } else {
            chapters = problemScheduleRepository.loadDefaultRoadMap(problemId);
        }
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
        });
        problemScoreDao.gradeProblem(problemScores);
    }

    @Override
    public Integer insertProblemExtension(ProblemExtension problemExtension) {
        ProblemExtension extensionTarget = new ProblemExtension();
        BeanUtils.copyProperties(problemExtension, extensionTarget);

        Integer problemId = problemExtension.getProblemId();
        Problem cacheProblem = cacheService.getProblem(problemId);
        if (cacheProblem == null) {
            return -1;
        }
        extensionTarget.setProblem(cacheProblem.getProblem());
        if (cacheProblem.getCatalogId() != null) {
            String problemCatalogName = cacheService.getProblemCatalog(cacheProblem.getCatalogId()).getName();
            if (problemCatalogName != null) {
                extensionTarget.setCatalog(problemCatalogName);
            }
        }
        if (cacheProblem.getSubCatalogId() != null) {
            String problemSubCatalogName = cacheService.getProblemSubCatalog(cacheProblem.getSubCatalogId()).getName();
            if (problemSubCatalogName != null) {
                extensionTarget.setSubCatalog(problemSubCatalogName);
            }
        }
        Integer result1 = problemExtensionDao.insert(extensionTarget);
        Integer result2 = problemDao.insertRecommendationById(problemId, problemExtension.getRecommendation());
        return result1 < result2 ? result1 : result2;
    }

    @Override
    public Integer insertProblemActivity(ProblemActivity problemActivity) {
        return problemActivityDao.insertProblemActivity(problemActivity);
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
    public Pair<Problem, List<EssenceCard>> loadProblemCards(Integer planId) {
        // 根据 planId 获取 improvement 中的 problemId
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Integer problemId = plan.getProblemId();
        // 获取 essenceCard 所有与当前课程相关的数据
        Problem problem = cacheService.getProblem(problemId);
        List<Chapter> chapters = problemScheduleRepository.loadRoadMap(planId);
        Integer completeSeries = plan.getCompleteSeries();
        // 目标 essenceList
        List<EssenceCard> cards = Lists.newArrayList();
        Integer tempChapter = 0;
        for (Chapter chapter : chapters) {
            Integer chapterId = chapter.getChapter();
            EssenceCard essenceCard = new EssenceCard();
            essenceCard.setProblemId(problemId);
            essenceCard.setChapterId(chapterId);
            essenceCard.setThumbnail(cardRepository.loadTargetThumbnailByChapterId(chapterId, chapters.size()));
            essenceCard.setThumbnailLock(cardRepository.loadTargetThumbnailLockByChapterId(chapterId, chapters.size()));
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
        return cardRepository.loadEssenceCardImg(profileId, problemId, chapterId, improvementPlan.getId());
    }

    @Override
    public String loadProblemScheduleMonth(Integer profileId, Integer problemId) {
        Integer category = accountService.loadUserScheduleCategory(profileId);
        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);

        CourseScheduleDefault courseScheduleDefault = courseScheduleDefaults.stream()
                .filter(scheduleDefault -> problemId.equals(scheduleDefault.getProblemId())).findAny().orElse(null);
        return courseScheduleDefault != null ? courseScheduleDefault.getMonth() + "" : null;
    }

    @Override
    public int loadChosenPersonCount(Integer problemId) {
        // return improvementPlanDao.loadChosenPersonCount(problemId);
        return 0;
    }

    @Override
    public boolean hasCollectedProblem(Integer profileId, Integer problemId) {
        ProblemCollection collection = problemCollectionDao.loadUsefulCollection(profileId, problemId);
        return collection != null;
    }

    @Override
    public int collectProblem(Integer profileId, Integer problemId) {
        int result = -1;
        // 判断以前是否收藏过这门课程
        ProblemCollection collection = problemCollectionDao.loadSingleCollection(profileId, problemId);
        if (collection != null) {
            // 已经存在过这门课，如果 Del 字段为 1，将其置为 0
            if (collection.getDel() == 1) {
                result = problemCollectionDao.restoreCollection(collection.getId());
            }
        } else {
            // 收藏名单不存在这门课，直接新增记录
            result = problemCollectionDao.insert(profileId, problemId);
        }
        return result;
    }

    @Override
    public int disCollectProblem(Integer profileId, Integer problemId) {
        int result = -1;
        // 判断以前是否收藏过这门课程
        ProblemCollection collection = problemCollectionDao.loadSingleCollection(profileId, problemId);
        if (collection != null) {
            // 已经存在过这门课，如果 Del 字段为 0，将其置为 1
            if (collection.getDel() == 0) {
                result = problemCollectionDao.delete(collection.getId());
            }
        }
        return result;
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
            problem.setChosenPersonCount(loadChosenPersonCount(problemId));
            problems.add(problem);
        }
        return problems;
    }

    @Override
    public Integer loadCoursePlanSchedule(Integer profileId, Integer problemId) {
        Integer category = accountService.loadUserScheduleCategory(profileId);

        List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadMajorCourseScheduleDefaultByCategory(category);
        CourseScheduleDefault courseScheduleDefault = courseScheduleDefaults.stream()
                .filter(scheduleDefault -> problemId.equals(scheduleDefault.getProblemId())).findAny().orElse(null);

        return courseScheduleDefault != null ? courseScheduleDefault.getMonth() : null;
    }

    @Override
    public List<ExploreBanner> loadExploreBanner() {
        JSONArray bannerArray = JSONArray.parseArray(ConfigUtils.getExploreBannerString());
        List<ExploreBanner> banners = Lists.newArrayList();

        // 训练营 Banner 放第一个
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
