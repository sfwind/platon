package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.po.ApplicationPractice;
import com.iquanwai.platon.biz.po.ChallengePractice;
import com.iquanwai.platon.biz.po.WarmupPractice;

import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
public interface PracticeService {
    /**
     * 获取热身训练列表
     * @param idList 热身训练id列表
     * */
    List<WarmupPractice> getWarmupPractice(List<Integer> idList);

    /**
     * 回答热身训练问题
     * @param warmupPracticeList 练习答案
     * @param planId 训练id
     * */
    WarmupResult answerWarmupPractice(List<WarmupPractice> warmupPracticeList, Integer planId, String openid);

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

    /**
     * 获取挑战训练
     * @param code 随机码
     * */
    ChallengePractice getChallengePractice(String code);

    /**
     * 提交挑战训练
     * @param code 随机码
     * @param content 挑战训练答案
     * */
    Boolean submit(String code, String content);
}
