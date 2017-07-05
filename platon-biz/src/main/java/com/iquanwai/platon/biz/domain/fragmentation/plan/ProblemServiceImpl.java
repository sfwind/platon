package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.dao.fragmentation.ProblemExtensionDao;
import com.iquanwai.platon.biz.dao.fragmentation.ProblemScoreDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.po.*;
import com.sun.org.apache.xml.internal.resolver.Catalog;
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
    private ProblemExtensionDao problemExtensionDao;

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
    public Integer updateProblemExtension(ProblemExtension problemExtension) {
        ProblemExtension extensionTarget = new ProblemExtension();
        BeanUtils.copyProperties(problemExtension, extensionTarget);

        Integer problemId = problemExtension.getProblemId();
        Problem cacheProblem = cacheService.getProblem(problemId);
        if (cacheProblem == null || cacheProblem.getCatalogId() == null || cacheProblem.getSubCatalogId() == null) {
            return -1;
        }
        String problemCatalogName = cacheService.getProblemCatalog(cacheProblem.getCatalogId()).getName();
        String problemSubCatalogName = cacheService.getProblemSubCatalog(cacheProblem.getSubCatalogId()).getName();

        extensionTarget.setProblem(cacheProblem.getProblem());
        extensionTarget.setCatalog(problemCatalogName);
        extensionTarget.setSubCatalog(problemSubCatalogName);
        Integer result = problemExtensionDao.insert(extensionTarget);
        return result;
    }

}
