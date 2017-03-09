package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemPlanDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemPlan;
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
    private ProblemPlanDao problemPlanDao;
    @Autowired
    private CacheService cacheService;

    @Override
    public List<Problem> loadProblems() {
        //去除已删除的专题
        return cacheService.getProblems().stream().
                filter(problem -> !problem.getDel()).collect(Collectors.toList());
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
        List<ProblemPlan> problemPlans = problemPlanDao.loadProblems(openid);
        //去重后的数据
        List<ProblemPlan> afterFilterPlans = Lists.newArrayList();

        problemPlans.stream().forEach(problemPlan -> {
            boolean isDup = false;
            for(ProblemPlan afterFilter:afterFilterPlans){
                if(afterFilter.getProblemId().equals(problemPlan.getProblemId())){
                    isDup = true;
                    break;
                }
            }
            if(!isDup){
                afterFilterPlans.add(problemPlan);
            }
        });

        return afterFilterPlans;
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
        List<Problem> problems = cacheService.getProblems();

        for(Problem problem:problems){
            if(problem.getId()==problemId){
                return problem;
            }
        }

        return null;
    }
}
