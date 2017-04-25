package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
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
    private MessageService messageService;
    @Autowired
    private AccountService accountService;

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
        warmupPracticeDiscuss.setPriority(0);
        Integer id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if(repliedId!=null && !openid.equals(warmupPracticeDiscuss.getRepliedOpenid())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, id.toString(), warmupPracticeId.toString());
            String message = "回复了我的巩固练习问题";
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
        List<Profile> accounts = accountService.getProfiles(openids);
        //设置名称、头像和时间
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            accounts.stream().forEach(account -> {
                if (account.getOpenid().equals(warmupPracticeDiscuss.getOpenid())) {
                    warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
                    warmupPracticeDiscuss.setName(account.getNickname());
                    warmupPracticeDiscuss.setRole(account.getRole());
                }
                if (account.getOpenid().equals(warmupPracticeDiscuss.getRepliedOpenid())) {
                    warmupPracticeDiscuss.setRepliedName(account.getNickname());
                }
            });
            warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
        });
    }

    private void fulfilDiscuss(WarmupPracticeDiscuss warmupPracticeDiscuss) {
        Profile account = accountService.getProfile(warmupPracticeDiscuss.getOpenid(), false);
        //设置名称、头像和时间
        if(account.getOpenid().equals(warmupPracticeDiscuss.getOpenid())){
            warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
            warmupPracticeDiscuss.setName(account.getNickname());
            warmupPracticeDiscuss.setRole(account.getRole());
        }
        if(account.getOpenid().equals(warmupPracticeDiscuss.getRepliedOpenid())){
            warmupPracticeDiscuss.setRepliedName(account.getNickname());
        }

        warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
    }
}
