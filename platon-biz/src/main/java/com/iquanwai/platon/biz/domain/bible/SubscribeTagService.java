package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;

import java.util.List;

/**
 * Created by justin on 2017/9/13.
 */
public interface SubscribeTagService {
    /**
     * 加载学习标签
     *
     * @param profileId 用户id
     */
    List<SubscribeArticleTag> loadTag(Integer profileId);
    /**
     * 提交选择标签
     *
     * @param tags 提交标签列表
     * @param profileId 用户id
     */
    void submit(List<SubscribeArticleTag> tags, Integer profileId);


    /**
     * 是否选择过标签
     *
     * @param profileId 用户id
     */
    Boolean isEditTag(Integer profileId);
}
