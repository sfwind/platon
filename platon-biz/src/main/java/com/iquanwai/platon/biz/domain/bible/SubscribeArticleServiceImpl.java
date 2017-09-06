package com.iquanwai.platon.biz.domain.bible;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.bible.ArticleFavorDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeArticleDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeArticleTagDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeArticleViewDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeViewPointDao;
import com.iquanwai.platon.biz.dao.common.CustomerStatusDao;
import com.iquanwai.platon.biz.po.bible.ArticleFavor;
import com.iquanwai.platon.biz.po.bible.SubscribeArticle;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleView;
import com.iquanwai.platon.biz.po.bible.SubscribePointCompare;
import com.iquanwai.platon.biz.po.bible.SubscribeViewPoint;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/9/6.
 */
@Service
public class SubscribeArticleServiceImpl implements SubscribeArticleService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubscribeArticleDao subscribeArticleDao;
    @Autowired
    private SubscribeArticleViewDao subscribeArticleViewDao;
    @Autowired
    private ArticleFavorDao articleFavorDao;
    @Autowired
    private SubscribeArticleTagDao subscribeArticleTagDao;
    @Autowired
    private SubscribeViewPointDao subscribeViewPointDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;

    @Override
    public Boolean isFirstOpenBible(Integer profileId) {
        return customerStatusDao.load(profileId, CustomerStatus.OPEN_BIBLE) == null;
    }

    @Override
    public Boolean openBible(Integer profileId) {
        if (customerStatusDao.load(profileId, CustomerStatus.OPEN_BIBLE) == null) {
            return customerStatusDao.insert(profileId, CustomerStatus.OPEN_BIBLE) > 0;
        }
        return true;
    }

    @Override
    public List<SubscribeArticle> loadSubscribeArticleList(Integer profileId, Page page) {
        // 查询
        List<SubscribeArticle> subscribeArticleGroup = subscribeArticleDao.load(page);
        // 标签数据
        Map<Integer, SubscribeArticleTag> tagGroup = Maps.newHashMap();
        subscribeArticleTagDao.loadAll(SubscribeArticleTag.class).stream().filter(item -> !item.getDel()).forEach(tag -> {
            tagGroup.put(tag.getId(), tag);
        });
        // 填充数据
        subscribeArticleGroup.forEach(article -> {
            SubscribeArticleView acknowledged = subscribeArticleViewDao.load(profileId, article.getId());
            article.setAcknowledged(acknowledged != null);
            ArticleFavor articleFavor = articleFavorDao.load(profileId, article.getId());
            article.setFavor(articleFavor != null ? articleFavor.getFavor() : null);
            // 处理标签数据
            article.setTagNames(Lists.newArrayList());
            if (StringUtils.isNotEmpty(article.getTag())) {
                String[] tagsId = article.getTag().split(",");
                for (String tagsIdStr : tagsId) {
                    if (StringUtils.isNumeric(tagsIdStr)) {
                        Integer tagId = Integer.parseInt(tagsIdStr);
                        if (tagGroup.get(tagId) != null) {
                            article.getTagNames().add(tagGroup.get(tagId).getName());
                        }
                    }
                }
            }
        });
        // 查看是否最后一个
        Long totalCount = subscribeArticleDao.count(SubscribeArticle.class);
        page.setTotal(totalCount.intValue());
        return subscribeArticleGroup;
    }

    @Override
    public Boolean favorArticle(Integer profileId, Integer articleId) {
        Assert.notNull(subscribeArticleDao.load(SubscribeArticle.class, articleId), "文章不能为空");
        ArticleFavor articleFavor = articleFavorDao.load(profileId, articleId);
        if (articleFavor == null) {
            articleFavor = new ArticleFavor();
            articleFavor.setProfileId(profileId);
            articleFavor.setArticleId(articleId);
            articleFavor.setFavor(true);
            Integer count = articleFavorDao.insert(articleFavor);
            return count > 0;
        } else {
            if (!articleFavor.getFavor()) {
                Integer count = articleFavorDao.refavor(articleFavor.getId());
                return count > 0;
            } else {
                return true;
            }
        }
    }

    @Override
    public Boolean disfavorArticle(Integer profileId, Integer articleId) {
        Assert.notNull(subscribeArticleDao.load(SubscribeArticle.class, articleId), "文章不能为空");
        ArticleFavor articleFavor = articleFavorDao.load(profileId, articleId);
        if (articleFavor == null) {
            articleFavor = new ArticleFavor();
            articleFavor.setProfileId(profileId);
            articleFavor.setArticleId(articleId);
            articleFavor.setFavor(false);
            Integer count = articleFavorDao.insert(articleFavor);
            return count > 0;
        } else {
            if (articleFavor.getFavor()) {
                Integer count = articleFavorDao.disfavor(articleFavor.getId());
                return count > 0;
            } else {
                return true;
            }
        }
    }

    @Override
    public Boolean viewArticle(Integer profileId, Integer articleId) {
        SubscribeArticle article = subscribeArticleDao.load(SubscribeArticle.class, articleId);
        Assert.notNull("文章不能为空");
        SubscribeArticleView view = subscribeArticleViewDao.load(profileId, articleId);
        //TODO 文章积分计算
        Double viewPoint = 0.3;
        if (view == null) {
            // 还没有打开过，此时才会计算
            subscribeArticleViewDao.insert(profileId, articleId);
            String tagsId = article.getTag();
            // 已经有的标签分数
            List<SubscribeViewPoint> userViewPointGroup = subscribeViewPointDao.load(profileId, new Date(), tagsId);
            // 过滤出可用的标签
            List<Integer> tagIdGroup = Lists.newArrayList(tagsId.split(","))
                    .stream()
                    .filter(StringUtils::isNumeric)
                    .map(Integer::parseInt)
                    .filter(tagId -> {
                        SubscribeArticleTag subscribeArticleTag = subscribeArticleTagDao.load(SubscribeArticleTag.class, tagId);
                        return subscribeArticleTag != null && !subscribeArticleTag.getDel();
                    })
                    .collect(Collectors.toList());
            for (Integer tagId : tagIdGroup) {
                // 如果tag内容不是数字，则不会计算
                // tag存在，并且没有被删除，对当天的tag进行加分
                SubscribeViewPoint subscribeViewPoint = userViewPointGroup.stream().filter(point -> tagId.equals(point.getTagId())).findFirst().orElse(null);
                if (subscribeViewPoint == null) {
                    // 当天还没加分，先插入一条
                    SubscribeViewPoint insertTemp = new SubscribeViewPoint();
                    insertTemp.setProfileId(profileId);
                    insertTemp.setLearnDate(new Date());
                    insertTemp.setPoint(viewPoint);
                    insertTemp.setTagId(tagId);
                    subscribeViewPointDao.insert(insertTemp);
                } else {
                    Double finalPoint = subscribeViewPoint.getPoint() + viewPoint;
                    subscribeViewPointDao.update(finalPoint, tagId);
                }
            }
        }
        return true;
    }

    @Override
    public List<SubscribePointCompare> loadSubscribeViewPointList(Integer profileId) {
        Date yesterday = DateUtils.beforeDays(new Date(), 1);
        Date today = new Date();

        List<SubscribeViewPoint> yesterdayPointList = subscribeViewPointDao.load(profileId, yesterday);
        List<SubscribeViewPoint> todayPointList = subscribeViewPointDao.load(profileId, today)
                .stream()
                .sorted((o1, o2) -> o1.getPoint() > o2.getPoint() ? -1 : 1)
                .limit(7)
                .collect(Collectors.toList());

        List<SubscribePointCompare> compareList = Lists.newArrayList();
        todayPointList.forEach(todayPoint -> {
            Integer tagId = todayPoint.getTagId();
            SubscribeViewPoint yesterdayPoint = yesterdayPointList.stream().filter(item -> tagId.equals(item.getTagId())).findFirst().orElse(null);
            SubscribePointCompare compare = new SubscribePointCompare();
            // 标签
            SubscribeArticleTag tag = subscribeArticleTagDao.load(SubscribeArticleTag.class, tagId);
            // 今天的积分
            Double todayPointNumber = todayPoint.getPoint();
            // 昨天的积分
            Double yesterdayPointNumber = yesterdayPoint != null ? yesterdayPoint.getPoint() : 0;
            // 总积分
            Double totalPoint = subscribeViewPointDao.load(profileId, tagId).stream().mapToDouble(SubscribeViewPoint::getPoint).sum();

            compare.setTagId(tagId);
            compare.setToday(DateUtils.parseDateToString(today));
            compare.setTagName(tag.getName());
            compare.setTodayPoint(todayPointNumber);
            compare.setYesterdayPoint(yesterdayPointNumber);
            compare.setTotalPoint(totalPoint);
            compareList.add(compare);
        });
        // 增长积分多的纬度排前面
        compareList.sort(((o1, o2) -> o1.getTodayPoint() > o2.getTodayPoint() ? -1 : 1));
        return compareList;
    }
}
