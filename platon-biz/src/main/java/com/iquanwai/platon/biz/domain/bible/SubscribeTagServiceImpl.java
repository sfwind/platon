package com.iquanwai.platon.biz.domain.bible;

import com.iquanwai.platon.biz.dao.bible.SubscribeArticleTagDao;
import com.iquanwai.platon.biz.dao.bible.SubscribeUserTagDao;
import com.iquanwai.platon.biz.dao.common.CustomerStatusDao;
import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;
import com.iquanwai.platon.biz.po.bible.SubscribeUserTag;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
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
    @Autowired
    private CustomerStatusDao customerStatusDao;

    @Override
    public List<SubscribeArticleTag> loadTag(Integer profileId) {
        List<SubscribeArticleTag> subscribeArticleTags = subscribeArticleTagDao.loadAll(SubscribeArticleTag.class);
        //去掉del=1的数据
        subscribeArticleTags = subscribeArticleTags.stream().filter(subscribeArticleTag -> !subscribeArticleTag.getDel()).collect(Collectors.toList());
        //标记已选的标签
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
        //先将以前的标签去掉,然后插入或者更新标签
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

        //标记完成选择标签
        if(customerStatusDao.load(profileId, CustomerStatus.EDIT_TAG) == null){
            customerStatusDao.insert(profileId, CustomerStatus.EDIT_TAG);
        }
    }

    @Override
    public Boolean isEditTag(Integer profileId) {
        return customerStatusDao.load(profileId, CustomerStatus.EDIT_TAG) != null;
    }
}
