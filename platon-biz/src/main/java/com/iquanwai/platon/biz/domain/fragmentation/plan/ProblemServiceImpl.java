package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemListDao;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemList;
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
    private ProblemListDao problemListDao;
    //缓存问题
    private List<Problem> problems = Lists.newArrayList();

    @Override
    public List<Problem> loadProblems() {
        if(problems.isEmpty()) {
            problems = problemDao.loadAll(Problem.class);
        }
        return problems;
    }

    @Override
    public void saveProblems(List<Integer> problemIds, String openid) {
        List<ProblemList> problemLists = problemIds.stream().map(problemId -> {
            ProblemList problemList = new ProblemList();
            problemList.setOpenid(openid);
            problemList.setProblemId(problemId);
            return problemList;
        }).collect(Collectors.toList());
        problemListDao.saveProblems(problemLists);
    }

    @Override
    public List<ProblemList> loadProblems(String openid) {
        return problemListDao.loadProblems(openid);
    }

    @Override
    public String getProblemContent(Integer problemId) {
        if(problems==null){
            problems = loadProblems();
        }

        for(Problem problem:problems){
            if(problem.getId()==problemId){
                return problem.getProblem();
            }
        }
        return null;
    }
}
