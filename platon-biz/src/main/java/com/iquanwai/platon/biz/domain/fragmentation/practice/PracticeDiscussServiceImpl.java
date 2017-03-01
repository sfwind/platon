package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.po.WarmupPracticeDiscuss;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
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
    private ProfileDao profileDao;
    @Autowired
    private MessageService messageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void discuss(String openid, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setOpenid(openid);
        if(repliedId!=null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if(repliedDiscuss!=null){
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
            }
        }
//        List<UserRole> userRoles = userRoleDao.getRoles(openid);
//        //普通用户不会在角色表中存记录
//        if(CollectionUtils.isEmpty(userRoles)){
//            //普通用户的讨论优先级较低
//            warmupPracticeDiscuss.setPriority(0);
//        }else{
//            warmupPracticeDiscuss.setPriority(1);
//        }
        int id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if(repliedId!=null) {
            String url = "/rise/static/message/warmup/reply?commentId={1}&warmupPracticeId={2}";
            url = MessageFormat.format(url, id, warmupPracticeId);
            String message = "回复了我的热身训练问题";
            messageService.sendMessage(message, warmupPracticeDiscuss.getRepliedOpenid(),
                    openid, url);
        }
    }

    @Override
    public List<WarmupPracticeDiscuss> loadDiscuss(Integer warmupPracticeId, Page page) {
        List<WarmupPracticeDiscuss> discussList = warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page);
        fulfilDiscuss(discussList);
        return discussList;
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
                fulfilDiscuss(discuss);
                result.put(warmupPracticeId, discuss);
            } catch (Exception e){
                logger.error(e.getLocalizedMessage(), e);
            }
        });

        return result;
    }

    @Override
    public WarmupPracticeDiscuss loadDiscuss(Integer discussId) {
        WarmupPracticeDiscuss discuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);
        if(discuss!=null){
            fulfilDiscuss(discuss);
        }
        return discuss;
    }

    //填充评论的其他字段
    private void fulfilDiscuss(List<WarmupPracticeDiscuss> discuss) {
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
        List<Profile> accounts = profileDao.queryAccounts(openids);
        //设置名称、头像和时间
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            accounts.stream().forEach(account -> {
                if(account.getOpenid().equals(warmupPracticeDiscuss.getOpenid())){
                    warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
                    warmupPracticeDiscuss.setName(account.getNickname());
                }
                if(account.getOpenid().equals(warmupPracticeDiscuss.getRepliedOpenid())){
                    warmupPracticeDiscuss.setRepliedName(account.getNickname());
                }
            });
            warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
        });
    }

    private void fulfilDiscuss(WarmupPracticeDiscuss warmupPracticeDiscuss) {
        //批量获取用户信息
        Profile account = profileDao.queryByOpenId(warmupPracticeDiscuss.getOpenid());
        //设置名称、头像和时间
        if(account.getOpenid().equals(warmupPracticeDiscuss.getOpenid())){
            warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
            warmupPracticeDiscuss.setName(account.getNickname());
        }
        if(account.getOpenid().equals(warmupPracticeDiscuss.getRepliedOpenid())){
            warmupPracticeDiscuss.setRepliedName(account.getNickname());
        }

        warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
    }
}
