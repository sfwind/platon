package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.plan.RoadMap;
import com.iquanwai.platon.biz.po.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    //缓存问题
    private List<Problem> problems = Lists.newArrayList();
    //缓存知识点
    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();
    //缓存理解训练
    private Map<Integer, WarmupPractice> warmupPracticeMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        List<Knowledge> knowledgeList = knowledgeDao.loadAll(Knowledge.class);
        knowledgeList.stream().forEach(knowledge -> knowledgeMap.put(knowledge.getId(), knowledge));
        logger.info("knowledge init complete");

        problems = problemDao.loadAll(Problem.class);
        problems.stream().forEach(problem -> {
            List<RoadMap> roadMapList = loadRoadMap(problem.getId());
            problem.setRoadMapList(roadMapList);
        });
        logger.info("problem init complete");

        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadAll(WarmupPractice.class);
        warmupPractices.stream().forEach(warmupPractice -> {
            warmupPractice.setChoiceList(Lists.newArrayList());
            warmupPractice.setKnowledge(knowledgeMap.get(warmupPractice.getKnowledgeId()));
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
        logger.info("warmup practice init complete");
    }

    @Override
    public List<Problem> getProblems() {
        List<Problem> dest = Lists.newArrayList();
        problems.forEach(problem -> {
            Problem newOne = new Problem();
            try {
                BeanUtils.copyProperties(newOne, problem);
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
            BeanUtils.copyProperties(knowledge, knowledgeMap.get(knowledgeId));
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

    public List<RoadMap> loadRoadMap(Integer problemId) {
        List<ProblemSchedule> problemSchedules = problemScheduleDao.loadProblemSchedule(problemId);
        Map<Integer, List<ProblemSchedule>> problemScheduleMap = Maps.newLinkedHashMap();
        //按节组合成一组知识点
        problemSchedules.stream().forEach(problemSchedule -> {
            if(problemScheduleMap.get(problemSchedule.getSeries())==null){
                problemScheduleMap.put(problemSchedule.getSeries(), Lists.newArrayList());
            }
            problemScheduleMap.get(problemSchedule.getSeries()).add(problemSchedule);
        });

        List<RoadMap> roadMapList = Lists.newArrayList();

        problemScheduleMap.keySet().stream().forEach(series ->{
            RoadMap roadMap = new RoadMap();
            roadMap.setSeries(series);
            List<ProblemSchedule> dailySchedule = problemScheduleMap.get(series);
            List<Knowledge> knowledges = dailySchedule.stream()
                    .map(problemSchedule -> getKnowledge(problemSchedule.getKnowledgeId()))
                    .collect(Collectors.toList());
            roadMap.setIntro(introMsg(knowledges));
            roadMap.setKnowledgeList(knowledges);
            roadMap.setStep(getStep(knowledges));
            if(CollectionUtils.isNotEmpty(knowledges)) {
                roadMap.setIntegrated(Knowledge.isReview(knowledges.get(0).getId()));
            }
            roadMapList.add(roadMap);
        });

        return roadMapList;
    }

    private String getStep(List<Knowledge> knowledges) {
        if(CollectionUtils.isEmpty(knowledges)){
            return "";
        }
        //步骤
        String step = knowledges.get(0).getStep();
        if(StringUtils.isEmpty(step)){
            step = knowledges.get(0).getKnowledge();
        }

        return step;
    }

    @Override
    public void reload() {
        init();
    }

    //创建首页介绍句
    private String introMsg(List<Knowledge> knowledges) {
        if(CollectionUtils.isEmpty(knowledges)){
            return "";
        }
        //步骤
        String step = knowledges.get(0).getStep();
        if(StringUtils.isEmpty(step)){
            step = "";
        }else{
            step = step+":";
        }
        List<String> knowledgeName = knowledges.stream().map(Knowledge::getKnowledge).collect(Collectors.toList());

        String knowledge = StringUtils.join(knowledgeName, " & ");
        return step+knowledge;
    }
}
