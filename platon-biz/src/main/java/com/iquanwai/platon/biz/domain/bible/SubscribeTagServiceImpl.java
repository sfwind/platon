package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.dao.bible.SubscribeArticleTagDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeUserTagDao;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;
import com.iquanwai.platon.biz.po.bible.SubscribeUserTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 2017/9/13.
 */
@Service
public class SubscribeTagServiceImpl implements SubscribeTagService {
    @Autowired
    private SubscribeUserTagDao subscribeUserTagDao;
    @Autowired
    private SubscribeArticleTagDao subscribeArticleTagDao;

    @Override
    public List<SubscribeArticleTag> loadTag(Integer profileId) {
        List<SubscribeArticleTag> subscribeArticleTags = subscribeArticleTagDao.loadAll(SubscribeArticleTag.class);
        //去掉del=1的数据
        subscribeArticleTags = subscribeArticleTags.stream().filter(subscribeArticleTag -> !subscribeArticleTag.getDel()).collect(Collectors.toList());

        subscribeArticleTags.forEach(subscribeArticleTag -> {
            SubscribeUserTag subscribeUserTag = subscribeUserTagDao.
                    loadUserTag(profileId, subscribeArticleTag.getId());

            if(subscribeUserTag!=null && !subscribeUserTag.getDel()){
                subscribeArticleTag.setChosen(true);
            }else{
                subscribeArticleTag.setChosen(false);
            }
        });

        return subscribeArticleTags;
    }

    @Override
    public void submit(List<SubscribeArticleTag> tags, Integer profileId) {
        subscribeUserTagDao.unchooseAll(profileId);
        tags.forEach(subscribeArticleTag -> {
            if(subscribeArticleTag.getChosen()){
                SubscribeUserTag subscribeUserTag = subscribeUserTagDao.
                        loadUserTag(profileId, subscribeArticleTag.getId());
                if (subscribeUserTag == null) {
                    subscribeUserTagDao.insert(profileId, subscribeArticleTag.getId());
                } else {
                    subscribeUserTagDao.choose(subscribeUserTag.getId());
                }
            }
        });
    }
}
