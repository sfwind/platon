package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ChoiceDao;
import com.iquanwai.platon.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemCatalogDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemSubCatalogDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Section;
import com.iquanwai.platon.biz.po.Choice;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;
import com.iquanwai.platon.biz.po.WarmupPractice;
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


    //缓存问题
    private List<Problem> problems = Lists.newArrayList();
    //缓存知识点
    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();
    //缓存巩固练习
    private Map<Integer, WarmupPractice> warmupPracticeMap = Maps.newHashMap();
    //缓存问题分类
    private Map<Integer,ProblemCatalog> problemCatalogMap = Maps.newHashMap();
    //缓存问题子分类
    private Map<Integer,ProblemSubCatalog> problemSubCatalogMap = Maps.newHashMap();


    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        List<Knowledge> knowledgeList = knowledgeDao.loadAll(Knowledge.class);
        knowledgeList.stream().forEach(knowledge -> {
            knowledgeMap.put(knowledge.getId(), knowledge);
            if(ConfigUtils.isHttps()){
                knowledge.setAudio(StringUtils.replace(knowledge.getAudio(), "http:", "https:"));
                knowledge.setPic(StringUtils.replace(knowledge.getPic(), "http:", "https:"));
            }
        });
        // 缓存知识点
        knowledgeList.stream().forEach(knowledge -> knowledgeMap.put(knowledge.getId(), knowledge));
        logger.info("knowledge init complete");

        // 缓存问题
        problems = problemDao.loadAll(Problem.class);
        problems.stream().forEach(problem -> {
            List<Chapter> chapterList = loadRoadMap(problem.getId());
            problem.setChapterList(chapterList);
            if(ConfigUtils.isHttps()){
                problem.setAudio(StringUtils.replace(problem.getAudio(), "http:", "https:"));
                problem.setPic(StringUtils.replace(problem.getPic(), "http:", "https:"));
                problem.setDescPic(StringUtils.replace(problem.getDescPic(), "http:", "https:"));
            }
        });
        logger.info("problem init complete");

        // 缓存热身训练
        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadAll(WarmupPractice.class);
        warmupPractices.stream().forEach(warmupPractice -> {
            warmupPractice.setChoiceList(Lists.newArrayList());
            warmupPractice.setKnowledge(knowledgeMap.get(warmupPractice.getKnowledgeId()));
            if(ConfigUtils.isHttps()){
                warmupPractice.setPic(StringUtils.replace(warmupPractice.getPic(), "http:", "https:"));
            }
            warmupPracticeMap.put(warmupPractice.getId(), warmupPractice);
        });
        List<Choice> choices = choiceDao.loadAll(Choice.class);
        choices.stream().forEach(choice -> {
            Integer questionId = choice.getQuestionId();
            WarmupPractice warmupPractice = warmupPracticeMap.get(questionId);
            if(warmupPractice!=null){
                warmupPractice.getChoiceList().add(choice);
            }
        });

        //选项按sequence排序
        warmupPractices.stream().forEach(warmupPractice ->
                warmupPractice.getChoiceList().sort((o1, o2) -> o1.getSequence()-o2.getSequence()));
        logger.info("warmup practice init complete");

        // 缓存问题主分类
        List<ProblemCatalog> problemCatalogs = problemCatalogDao.loadAll(ProblemCatalog.class);
        problemCatalogs.forEach(item -> problemCatalogMap.put(item.getId(), item));

        // 缓存问题子分类
        List<ProblemSubCatalog> problemSubCatalogs = problemSubCatalogDao.loadAll(ProblemSubCatalog.class);
        problemSubCatalogs.forEach(item -> problemSubCatalogMap.put(item.getId(), item));
    }

    @Override
    public List<Problem> getProblems() {
        List<Problem> dest = Lists.newArrayList();
        problems.forEach(problem -> {
            Problem newOne = new Problem();
            try {
                BeanUtils.copyProperties(problem, newOne);
                dest.add(newOne);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        });
        return dest;
    }

    @Override
    public Problem getProblem(Integer problemId) {
        for(Problem problem:problems){
            if(problem.getId()==problemId){
                return problem;
            }
        }
        return null;
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        Knowledge knowledge = new Knowledge();
        try {
            BeanUtils.copyProperties(knowledgeMap.get(knowledgeId),knowledge );
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


    private List<Chapter> loadRoadMap(Integer problemId) {
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        Map<Integer, List<ProblemSchedule>> problemScheduleMap = Maps.newLinkedHashMap();
        //按节组合成一组知识点
        problemSchedules.stream().forEach(problemSchedule -> {
            List<ProblemSchedule> problemScheduleList = problemScheduleMap.getOrDefault(problemSchedule.getChapter(), Lists.newArrayList());
            problemScheduleList.add(problemSchedule);
            problemScheduleMap.put(problemSchedule.getChapter(), problemScheduleList);
        });

        List<Chapter> chapterList = Lists.newArrayList();

        //构建章节
        problemScheduleMap.keySet().stream().forEach(chapterSequence ->{
            Chapter chapter = new Chapter();
            List<ProblemSchedule> scheduleList = problemScheduleMap.get(chapterSequence);
            List<Section> sectionList = scheduleList.stream().sorted((o1, o2) -> o1.getSection()-o2.getSection())
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
            if(CollectionUtils.isNotEmpty(sectionList)) {
                chapter.setIntegrated(Knowledge.isReview(sectionList.get(0).getKnowledgeId()));
            }
            chapterList.add(chapter);
        });

        return chapterList;
    }

    @Override
    public ProblemCatalog getProblemCatalog(Integer id){
        return problemCatalogMap.get(id);
    }

    @Override
    public ProblemSubCatalog getProblemSubCatalog(Integer id){
        return problemSubCatalogMap.get(id);
    }

    @Override
    public List<ProblemCatalog> loadProblemCatalogs(){
        List<ProblemCatalog> lists = Lists.newArrayList();
        lists.addAll(problemCatalogMap.values());
        return lists;
    }

    @Override
    public void reload() {
        init();
    }


    private String chapterName(List<Section> sectionList) {
        if(CollectionUtils.isEmpty(sectionList)){
            return "";
        }
        //步骤
        return sectionList.get(0).getChapterName();
    }
}
