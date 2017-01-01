package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.ChoiceDao;
import com.iquanwai.platon.biz.dao.fragmentation.KnowledgeDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDao;
import com.iquanwai.platon.biz.po.Choice;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.WarmupPractice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

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
    //缓存问题
    private List<Problem> problems = Lists.newArrayList();
    //缓存知识点
    private Map<Integer, Knowledge> knowledgeMap = Maps.newHashMap();
    //缓存热身训练
    private Map<Integer, WarmupPractice> warmupPracticeMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        problems = problemDao.loadAll(Problem.class);
        logger.info("problem init complete");
        List<Knowledge> knowledgeList = knowledgeDao.loadAll(Knowledge.class);
        knowledgeList.stream().forEach(knowledge -> knowledgeMap.put(knowledge.getId(), knowledge));
        logger.info("knowledge init complete");

        List<WarmupPractice> warmupPractices = warmupPracticeDao.loadAll(WarmupPractice.class);
        warmupPractices.stream().forEach(warmupPractice -> {
            warmupPractice.setChoiceList(Lists.newArrayList());
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
        return problems;
    }

    @Override
    public Knowledge getKnowledge(Integer knowledgeId) {
        return knowledgeMap.get(knowledgeId);
    }

    @Override
    public WarmupPractice getWarmupPractice(Integer practiceId) {
        return warmupPracticeMap.get(practiceId);
    }

    @Override
    public void reload() {
        init();
    }
}
