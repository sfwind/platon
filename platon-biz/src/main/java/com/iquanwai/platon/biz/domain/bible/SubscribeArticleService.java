package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.po.bible.SubscribePointCompare;
import com.iquanwai.platon.biz.util.page.Page;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
public interface SubscribeArticleService {
    /**
     * 是否第一次打开bible
     *
     * @param profileId 用户id
     */
    Boolean isFirstOpenBible(Integer profileId);

    /**
     * 打开bible
     *
     * @param profileId 用户id
     */
    Boolean openBible(Integer profileId);

    Boolean isLastArticleDate(String date);

    List<SubscribeArticle> loadSubscribeArticleListToCertainDate(Integer profileId, Page page, String date);

    /**
     * 加载文章列表
     *
     * @param profileId 用户id
     * @param page      页数
     * @return 文章list
     */
    List<SubscribeArticle> loadSubscribeArticleList(Integer profileId, Page page, String date);

    /**
     * 喜欢某篇文章
     *
     * @param profileId 用户id
     * @param articleId 文章id
     * @return 处理结果
     */
    Boolean favorArticle(Integer profileId, Integer articleId);

    /**
     * 不喜欢某篇文章
     *
     * @param profileId 用户di
     * @param articleId 文章id
     * @return 处理结果
     */
    Boolean disfavorArticle(Integer profileId, Integer articleId);

    /**
     * 打开某篇文章
     *
     * @param profileId 用户id
     * @param articleId 文章id
     * @return 处理结果
     */
    Boolean viewArticle(Integer profileId, Integer articleId);

    /**
     * 获取不同纬度的得分对比情况
     *
     * @param profileId 用户id
     * @return 得分对比情况
     */
    List<SubscribePointCompare> loadSubscribeViewPointList(Integer profileId);

    Integer loadCertainDayReadWords(Integer profileId, Date date);

    String loadUserQrCode(Integer profileId);

    Double totalScores(Integer profileId);
}
