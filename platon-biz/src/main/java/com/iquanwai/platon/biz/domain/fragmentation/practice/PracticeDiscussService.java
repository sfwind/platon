package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/8.
 */
public interface PracticeDiscussService {
    /**
     * 讨论某个巩固训练
     * @param openid 发表讨论的用户openid
     * @param warmupPracticeId 巩固训练id
     * @param comment 讨论内容
     * @param repliedId 回复的讨论id
     * */
    void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId);

    /**
     * 获取讨论内容
     * @param warmupPracticeId 巩固训练id
     * @param page 分页信息
     * */
    List<WarmupPracticeDiscuss> loadDiscuss(Integer warmupPracticeId, Page page);

    /**
     * 获取多个问题的讨论内容
     * @param warmupPracticeIds 巩固训练id列表
     * @param page 分页信息
     * */
    Map<Integer, List<WarmupPracticeDiscuss>> loadDiscuss(List<Integer> warmupPracticeIds, Page page);

    /**
     * 获取讨论内容
     * @param discussId 讨论内容id
     * */
    WarmupPracticeDiscuss loadDiscuss(Integer discussId);
}
