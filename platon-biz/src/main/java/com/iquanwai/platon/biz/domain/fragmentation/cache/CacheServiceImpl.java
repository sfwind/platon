package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Section;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/1/1.
 */
@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private ChoiceDao choiceDao;
    @Autowired
    private ProblemScheduleDao problemScheduleDao;
    @Autowired
    private ProblemCatalogDao problemCatalogDao;
    @Autowired
    private ProblemSubCatalogDao problemSubCatalogDao;
    @Autowired
    private AudioDao audioDao;
    @Autowired
    private MonthlyCampConfigDao monthlyCampConfigDao;

    //缓存问题
    private List<Problem> problems = Lists.newArrayList();
    //缓存知识点
    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();
    //缓存巩固练习
    private Map<Integer, WarmupPractice> warmupPracticeMap = Maps.newHashMap();
    //缓存问题分类
    private Map<Integer, ProblemCatalog> problemCatalogMap = Maps.newHashMap();
    //缓存问题子分类
    private Map<Integer, ProblemSubCatalog> problemSubCatalogMap = Maps.newHashMap();
    //缓存小课训练营配置
    private MonthlyCampConfig monthlyCampConfig;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        List<Knowledge> knowledgeList = knowledgeDao.loadAll(Knowledge.class);
        knowledgeList.forEach(knowledge -> {
            knowledgeMap.put(knowledge.getId(), knowledge);
            if (ConfigUtils.isHttps()) {
                knowledge.setPic(StringUtils.replace(knowledge.getPic(), "http:", "https:"));
            }
            // 设置音频字段
            initAudio(knowledge);
        });
        logger.info("knowledge init complete");

        // 缓存问题
        problems = problemDao.loadAll(Problem.class);
        problems.forEach(problem -> {
            List<Chapter> chapterList = loadRoadMap(problem.getId());
            problem.setChapterList(chapterList);
            Integer subCatalogId = problem.getSubCatalogId();
            ProblemSubCatalog problemSubCatalog = problemSubCatalogDao.load(ProblemSubCatalog.class, subCatalogId);
            if (problemSubCatalog != null) {
                problem.setCategoryPic(problemSubCatalog.getPic());
            }
            if (ConfigUtils.isHttps()) {
                if (problem.getAudioId() != null) {
                    Audio audio = audioDao.load(Audio.class, problem.getAudioId());
                    problem.setAudio(audio.getUrl());
                    problem.setAudioWords(audio.getWords());
                }
                problem.setPic(StringUtils.replace(problem.getPic(), "http:", "https:"));
                problem.setDescPic(StringUtils.replace(problem.getDescPic(), "http:", "https:"));
                problem.setAuthorPic(StringUtils.replace(problem.getAuthorPic(), "http:", "https:"));
            }
        });
        logger.info("problem init complete");

        // 缓存热身训练
        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadAll(WarmupPractice.class);
        warmupPractices.forEach(warmupPractice -> {
            warmupPractice.setChoiceList(Lists.newArrayList());
            //添加非复习知识点
            if (!Knowledge.isReview(warmupPractice.getKnowledgeId())) {
                warmupPractice.setKnowledge(knowledgeMap.get(warmupPractice.getKnowledgeId()));
            }
            if (ConfigUtils.isHttps()) {
                warmupPractice.setPic(StringUtils.replace(warmupPractice.getPic(), "http:", "https:"));
            }
            warmupPracticeMap.put(warmupPractice.getId(), warmupPractice);
        });
        List<Choice> choices = choiceDao.loadAll(Choice.class);
        choices.forEach(choice -> {
            Integer questionId = choice.getQuestionId();
            WarmupPractice warmupPractice = warmupPracticeMap.get(questionId);
            if (warmupPractice != null) {
                warmupPractice.getChoiceList().add(choice);
            }
        });

        //选项按sequence排序
        warmupPractices.forEach(warmupPractice ->
                warmupPractice.getChoiceList().sort((o1, o2) -> o1.getSequence() - o2.getSequence()));
        logger.info("warmup practice init complete");

        // 缓存问题主分类
        List<ProblemCatalog> problemCatalogs = problemCatalogDao.loadAll(ProblemCatalog.class);
        problemCatalogs.forEach(item -> {
            if (!item.getDel()) {
                problemCatalogMap.put(item.getId(), item);
            }
            // 设置分类名字
            problems.forEach(problem -> {
                if (item.getId().equals(problem.getCatalogId())) {
                    problem.setCatalog(item.getName());
                }
            });
        });

        // 缓存问题子分类
        List<ProblemSubCatalog> problemSubCatalogs = problemSubCatalogDao.loadAll(ProblemSubCatalog.class);
        problemSubCatalogs.forEach(item -> {
            // 设置子分类名字
            problems.forEach(problem -> {
                if (item.getId().equals(problem.getSubCatalogId())) {
                    problem.setSubCatalog(item.getName());
                }
            });
        });
        problemSubCatalogs.forEach(item -> problemSubCatalogMap.put(item.getId(), item));

        // 缓存小课训练营配置缓存
        monthlyCampConfig = monthlyCampConfigDao.loadActiveMonthlyCampConfig();
    }

    private void initAudio(Knowledge knowledge) {
        if (knowledge.getAudioId() != null) {
            Audio audio = audioDao.load(Audio.class, knowledge.getAudioId());
            if (audio != null) {
                knowledge.setAudioWords(audio.getWords());
                knowledge.setAudio(audio.getUrl());
            }
        }
        if (knowledge.getKeynoteAudioId() != null) {
            Audio audio = audioDao.load(Audio.class, knowledge.getKeynoteAudioId());
            if (audio != null) {
                knowledge.setKeynoteAudioWords(audio.getWords());
                knowledge.setKeynoteAudio(audio.getUrl());
            }
        }
        if (knowledge.getMeansAudioId() != null) {
            Audio audio = audioDao.load(Audio.class, knowledge.getMeansAudioId());
            if (audio != null) {
                knowledge.setMeansAudioWords(audio.getWords());
                knowledge.setMeansAudio(audio.getUrl());
            }
        }
        if (knowledge.getAnalysisAudioId() != null) {
            Audio audio = audioDao.load(Audio.class, knowledge.getAnalysisAudioId());
            if (audio != null) {
                knowledge.setAnalysisAudioWords(audio.getWords());
                knowledge.setAnalysisAudio(audio.getUrl());
            }
        }
    }

    @Override
    public List<Problem> getProblems() {
        return problems.stream().map(Problem::copy).collect(Collectors.toList());
    }

    @Override
    public Problem getProblem(Integer problemId) {
        return problems.stream().filter(problem -> problem.getId() == problemId).map(Problem::copy).findFirst().orElse(null);
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        Knowledge knowledge = new Knowledge();
        try {
            BeanUtils.copyProperties(knowledgeMap.get(knowledgeId), knowledge);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
        return knowledge;
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer practiceId) {
        WarmupPractice warmupPractice = null;
        try {
            warmupPractice = warmupPracticeMap.get(practiceId).clone();
        } catch (CloneNotSupportedException e) {
            // ignore
        }
        return warmupPractice;
    }

    @Override
    public List<Chapter> loadRoadMap(Integer problemId) {
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        Map<Integer, List<ProblemSchedule>> problemScheduleMap = Maps.newLinkedHashMap();
        //按节组合成一组知识点
        problemSchedules.forEach(problemSchedule -> {
            List<ProblemSchedule> problemScheduleList = problemScheduleMap.getOrDefault(problemSchedule.getChapter(), Lists.newArrayList());
            problemScheduleList.add(problemSchedule);
            problemScheduleMap.put(problemSchedule.getChapter(), problemScheduleList);
        });

        List<Chapter> chapterList = Lists.newArrayList();

        //构建章节
        problemScheduleMap.keySet().forEach(chapterSequence -> {
            Chapter chapter = new Chapter();
            List<ProblemSchedule> scheduleList = problemScheduleMap.get(chapterSequence);
            List<Section> sectionList = scheduleList.stream().sorted((o1, o2) -> o1.getSection() - o2.getSection())
                    .map(problemSchedule -> {
                        //构建小节
                        Section section = new Section();
                        Knowledge knowledge = getKnowledge(problemSchedule.getKnowledgeId());
                        section.setKnowledgeId(knowledge.getId());
                        section.setName(knowledge.getKnowledge());
                        section.setSection(problemSchedule.getSection());
                        section.setSeries(problemSchedule.getSeries());
                        section.setIntegrated(Knowledge.isReview(problemSchedule.getKnowledgeId()));
                        section.setChapterName(knowledge.getStep());
                        section.setChapter(problemSchedule.getChapter());
                        return section;
                    })
                    .collect(Collectors.toList());
            chapter.setName(chapterName(sectionList));
            chapter.setSections(sectionList);
            chapter.setChapter(chapterSequence);
            if (CollectionUtils.isNotEmpty(sectionList)) {
                chapter.setIntegrated(Knowledge.isReview(sectionList.get(0).getKnowledgeId()));
            }
            chapterList.add(chapter);
        });

        return chapterList;
    }

    @Override
    public ProblemCatalog getProblemCatalog(Integer id) {
        return problemCatalogMap.get(id);
    }

    @Override
    public ProblemSubCatalog getProblemSubCatalog(Integer id) {
        return problemSubCatalogMap.get(id);
    }

    @Override
    public List<ProblemCatalog> loadProblemCatalogs() {
        List<ProblemCatalog> lists = Lists.newArrayList();
        lists.addAll(problemCatalogMap.values());
        return lists;
    }

    @Override
    public MonthlyCampConfig loadMonthlyCampConfig() {
        return JSONObject.parseObject(JSON.toJSONString(monthlyCampConfig), MonthlyCampConfig.class);
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void reloadMonthlyCampConfig() {
        monthlyCampConfig = monthlyCampConfigDao.loadActiveMonthlyCampConfig();
    }

    private String chapterName(List<Section> sectionList) {
        if (CollectionUtils.isEmpty(sectionList)) {
            return "";
        }
        //步骤
        return sectionList.get(0).getChapterName();
    }
}
