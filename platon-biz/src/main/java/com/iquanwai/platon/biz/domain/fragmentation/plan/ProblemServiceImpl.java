package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private MonthlyCampScheduleDao monthlyCampScheduleDao;
    @Autowired
    private ProblemExtensionDao problemExtensionDao;
    @Autowired
    private ProblemActivityDao problemActivityDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private CardRepository cardRepository;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<Problem> loadProblems() {
        //去除已删除的小课
        return cacheService.getProblems().stream().
                filter(problem -> !problem.getDel()).collect(Collectors.toList());
    }

    @Override
    public Problem getProblem(Integer problemId) {
        return cacheService.getProblem(problemId);
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
    public void gradeProblem(Integer problem, String openId, Integer profileId, List<ProblemScore> problemScores) {
        problemScores.forEach(item -> {
            item.setOpenid(openId);
            item.setProfileId(profileId);
            item.setProblemId(problem);
        });
        problemScoreDao.gradeProblem(problemScores);
    }

    @Override
    public boolean hasProblemScore(Integer profileId, Integer problemId) {
        return problemScoreDao.userProblemScoreCount(profileId, problemId) > 0;
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
        // 获取 essenceCard 所有与当前小课相关的数据
        Problem problem = cacheService.getProblem(problemId);
        Integer completeSeries = plan.getCompleteSeries();
        List<Chapter> chapters = problem.getChapterList();
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
                essenceCard.setChapter("小课知识清单");
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
        return cardRepository.loadEssenceCardImg(profileId, problemId, chapterId);
    }

    @Override
    public String loadProblemSchedule(Integer problemId) {
        MonthlyCampSchedule schedule = monthlyCampScheduleDao.loadByProblemId(problemId);
        if (schedule != null && schedule.getMonth() != null) {
            return Integer.toString(schedule.getMonth());
        } else {
            return null;
        }
    }

    @Override
    public int loadChosenPersonCount(Integer problemId) {
        return improvementPlanDao.loadChosenPersonCount(problemId);
    }

}
