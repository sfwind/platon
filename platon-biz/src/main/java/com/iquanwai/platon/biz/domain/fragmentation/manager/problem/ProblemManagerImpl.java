package com.iquanwai.platon.biz.domain.fragmentation.manager.problem;

import com.iquanwai.platon.biz.dao.fragmentation.ProblemDao;
import com.iquanwai.platon.biz.po.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProblemManagerImpl implements ProblemManager {
    @Autowired
    private ProblemDao problemDao;

    @Override
    public Boolean isThoughtProblem(Integer problemId) {
        Problem problem = problemDao.load(Problem.class, problemId);
        if (problem != null && problem.getProject().equals(Problem.ProjectId.THOUGHT)) {
            return true;
        }
        return false;
    }
}
