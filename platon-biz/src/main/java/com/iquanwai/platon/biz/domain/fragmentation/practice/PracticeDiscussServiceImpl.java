package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.po.Account;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.common.UserRole;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by justin on 17/2/8.
 */
@Service
public class PracticeDiscussServiceImpl implements PracticeDiscussService {
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    //TODO:待切换成rise用户表
    private FollowUserDao followUserDao;

    @Override
    public void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setOpenid(openid);
        warmupPracticeDiscuss.setRepliedId(repliedId);
        if(repliedId!=null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if(repliedDiscuss!=null){
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
            }
        }
        List<UserRole> userRoles = userRoleDao.getRoles(openid);
        //普通用户不会在角色表中存记录
        if(CollectionUtils.isEmpty(userRoles)){
            //普通用户的讨论优先级较低
            warmupPracticeDiscuss.setPriority(0);
        }else{
            warmupPracticeDiscuss.setPriority(1);
        }
        warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);
    }

    @Override
    public List<WarmupPracticeDiscuss> loadDiscuss(Integer warmupPracticeId, Page page) {
        return warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page);
    }

    @Override
    public Map<Integer, List<WarmupPracticeDiscuss>> loadDiscuss(List<Integer> warmupPracticeIds, Page page) {
        Map<Integer, List<WarmupPracticeDiscuss>> result = Maps.newHashMap();

        //并发获取评论提高效率
        warmupPracticeIds.stream().forEach(warmupPracticeId ->{
            FutureTask futureTask = new FutureTask(() -> warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page));
            futureTask.run();
            try {
                List<WarmupPracticeDiscuss> discuss = (List<WarmupPracticeDiscuss>)futureTask.get();
                List<String> openids = Lists.newArrayList();
                discuss.stream().forEach(warmupPracticeDiscuss -> {
                    if(!openids.contains(warmupPracticeDiscuss.getOpenid())){
                        openids.add(warmupPracticeDiscuss.getOpenid());
                    }
                    if(warmupPracticeDiscuss.getRepliedOpenid()!=null) {
                        if (!openids.contains(warmupPracticeDiscuss.getRepliedOpenid())) {
                            openids.add(warmupPracticeDiscuss.getRepliedOpenid());
                        }
                    }
                });
                //批量获取用户信息
                result.put(warmupPracticeId, discuss);
                List<Account> accounts = followUserDao.queryAccounts(openids);
                //设置名称、头像和时间
                discuss.stream().forEach(warmupPracticeDiscuss -> {
                    accounts.stream().forEach(account -> {
                        if(account.getOpenid().equals(warmupPracticeDiscuss.getOpenid())){
                            warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
                            warmupPracticeDiscuss.setName(account.getNickname());
                        }else if(account.getOpenid().equals(warmupPracticeDiscuss.getRepliedOpenid())){
                            warmupPracticeDiscuss.setRepliedName(account.getNickname());
                        }
                    });
                    warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateTimeToString(warmupPracticeDiscuss.getAddTime()));
                });
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                // ignore
            }
        });

        return result;
    }
}
