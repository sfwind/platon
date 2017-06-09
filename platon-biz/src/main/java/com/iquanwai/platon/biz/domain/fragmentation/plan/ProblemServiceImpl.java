package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.ProblemScoreDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.ProblemCatalog;
import com.iquanwai.platon.biz.po.ProblemScore;
import com.iquanwai.platon.biz.po.ProblemSubCatalog;
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

}
