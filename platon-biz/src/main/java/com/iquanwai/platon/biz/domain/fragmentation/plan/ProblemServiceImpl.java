package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemPlanDao;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemPlan;
import org.apache.commons.collections.CollectionUtils;
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
    private ProblemDao problemDao;
    @Autowired
    private ProblemPlanDao problemPlanDao;
    //缓存问题
    private List<Problem> problems = Lists.newArrayList();

    @Override
    public List<Problem> loadProblems() {
        if(CollectionUtils.isEmpty(problems)) {
            problems = problemDao.loadAll(Problem.class);
        }
        return problems;
    }

    @Override
    public void saveProblems(List<Integer> problemIds, String openid) {
        List<ProblemPlan> problemPlans = problemIds.stream().map(problemId -> {
            ProblemPlan problemPlan = new ProblemPlan();
            problemPlan.setOpenid(openid);
            problemPlan.setProblemId(problemId);
            return problemPlan;
        }).collect(Collectors.toList());
        problemPlanDao.saveProblems(problemPlans);
    }

    @Override
    public List<ProblemPlan> loadProblems(String openid) {
        return problemPlanDao.loadProblems(openid);
    }

    @Override
    public String getProblemContent(Integer problemId) {
        Problem problem = getProblem(problemId);
        if(problem!=null){
            return problem.getProblem();
        }
        return null;
    }

    @Override
    public Problem getProblem(Integer problemId) {
        if(CollectionUtils.isEmpty(problems)){
            problems = loadProblems();
        }

        for(Problem problem:problems){
            if(problem.getId()==problemId){
                return problem;
            }
        }

        return null;
    }
}
