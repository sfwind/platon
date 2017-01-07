package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.exception.AnswerException;
import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.ChallengePractice;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.WarmupSubmit;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {
    /**
     * 获取热身训练列表
     * @param planId 训练id
     * @param practicePlanId 训练组编号
     * */
    List<WarmupPractice> getWarmupPractice(Integer planId, Integer practicePlanId);

    /**
     * 获取热身训练解析
     * @param planId 训练id
     * @param questionIds 练习编号
     * */
    List<WarmupSubmit> getWarmupSubmit(Integer planId, List<Integer> questionIds);
    /**
     * 回答热身训练问题
     * @param warmupPracticeList 练习答案
     * @param planId 训练id
     * */
    WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer practicePlanId,
                                      Integer planId, String openid) throws AnswerException;

    /**
     * 获取应用训练
     * @param id 应用训练id
     * */
    ApplicationPractice getApplicationPractice(Integer id, Integer planId);

    /**
     * 获取挑战训练
     * @param id 挑战训练id
     * @param openid 学员id
     * @param planId 训练计划id
     * */
    ChallengePractice getChallengePractice(Integer id, String openid, Integer planId);
}
