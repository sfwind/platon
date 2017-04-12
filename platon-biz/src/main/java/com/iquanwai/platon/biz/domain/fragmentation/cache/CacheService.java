package com.iquanwai.platon.biz.domain.fragmentation.cache;

import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.WarmupPractice;

import java.util.List;

/**
 * Created by justin on 17/1/1.
 */
public interface CacheService {
    /** 获取所有的专题*/
    List<Problem> getProblems();
    /** 获取某个专题*/
    Problem getProblem(Integer problemId);
    /** 获取某个知识点*/
    Knowledge getKnowledge(Integer knowledgeId);
    /** 获取某个巩固训练*/
    WarmupPractice getWarmupPractice(Integer practiceId);
    /** 更新缓存*/
    void reload();
}
