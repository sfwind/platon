package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.KnowledgeDiscussDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupPracticeDiscussDao;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.AbstractComment;
import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
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
import java.util.Objects;
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
    @Autowired
    private KnowledgeDiscussDao knowledgeDiscussDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void discuss(String openid, Integer profileId, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setOpenid(openid);
        warmupPracticeDiscuss.setProfileId(profileId);
        if (repliedId != null) {
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if (repliedDiscuss != null) {
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
                warmupPracticeDiscuss.setRepliedProfileId(repliedDiscuss.getProfileId());
                warmupPracticeDiscuss.setRepliedDel(0);
            }
        }
        warmupPracticeDiscuss.setPriority(0);
        Integer id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);

        //发送回复通知
        if (repliedId != null && !profileId.equals(warmupPracticeDiscuss.getRepliedProfileId())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, Objects.toString(id), Objects.toString(warmupPracticeId));
            String message = "回复了我的巩固练习问题";
            messageService.sendMessage(message, Objects.toString(warmupPracticeDiscuss.getRepliedProfileId()),
                    Objects.toString(profileId), url);
        }
    }


    @Override
    public void discussKnowledge(String openid, Integer profileId, Integer knowledgeId, String comment, Integer repliedId) {
        KnowledgeDiscuss knowledgeDiscuss = new KnowledgeDiscuss();
        knowledgeDiscuss.setKnowledgeId(knowledgeId);
        knowledgeDiscuss.setComment(comment);
        knowledgeDiscuss.setDel(0);
        knowledgeDiscuss.setOpenid(openid);
        knowledgeDiscuss.setProfileId(profileId);
        if (repliedId != null) {
            KnowledgeDiscuss repliedDiscuss = knowledgeDiscussDao.load(KnowledgeDiscuss.class, repliedId);
            if (repliedDiscuss != null) {
                knowledgeDiscuss.setRepliedId(repliedId);
                knowledgeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                knowledgeDiscuss.setRepliedOpenid(repliedDiscuss.getOpenid());
                knowledgeDiscuss.setRepliedProfileId(repliedDiscuss.getProfileId());
            }
        }
        knowledgeDiscuss.setPriority(0);
        Integer id = knowledgeDiscussDao.insert(knowledgeDiscuss);

        //发送回复通知
        if (repliedId != null && !profileId.equals(knowledgeDiscuss.getRepliedProfileId())) {
            String url = "/rise/static/message/knowledge/reply?commentId={0}&knowledgeId={1}";
            url = MessageFormat.format(url, Objects.toString(id), Objects.toString(knowledgeId));
            String message = "回复了我的知识理解问题";
            messageService.sendMessage(message, Objects.toString(knowledgeDiscuss.getRepliedProfileId()),
                    Objects.toString(profileId), url);
        }
    }

    @Override
    public List<WarmupPracticeDiscuss> loadDiscuss(Integer warmupPracticeId, Page page) {
        List<WarmupPracticeDiscuss> discussList = warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page);
        fulfilDiscuss(discussList);
        return discussList;
    }

    @Override
    public List<KnowledgeDiscuss> loadKnowledgeDiscusses(Integer knowledgeId, Page page) {
        List<KnowledgeDiscuss> discussesList = knowledgeDiscussDao.loadDiscuss(knowledgeId, page);
        fulfilDiscuss(discussesList);
        return discussesList;
    }

    @Override
    public Integer deleteKnowledgeDiscussById(Integer id) {
        //标记回复该评论的评论
        knowledgeDiscussDao.markRepliedCommentDelete(id);
        // 删除KnowledgeDiscuss记录，将del字段置为1
        return knowledgeDiscussDao.updateDelById(1, id);
    }

    @Override
    public Map<Integer, List<WarmupPracticeDiscuss>> loadDiscuss(List<Integer> warmupPracticeIds, Page page) {
        Map<Integer, List<WarmupPracticeDiscuss>> result = Maps.newHashMap();

        //并发获取评论提高效率
        warmupPracticeIds.forEach(warmupPracticeId -> {
            FutureTask futureTask = new FutureTask(() -> warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page));
            futureTask.run();
            try {
                List<WarmupPracticeDiscuss> discuss = (List<WarmupPracticeDiscuss>) futureTask.get();
                fulfilDiscuss(discuss);
                result.put(warmupPracticeId, discuss);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });

        return result;
    }

    @Override
    public WarmupPracticeDiscuss loadDiscuss(Integer discussId) {
        WarmupPracticeDiscuss discuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);
        if (discuss != null) {
            fulfilDiscuss(discuss);
            discuss.setReferenceId(discuss.getWarmupPracticeId());
        }
        return discuss;
    }

    @Override
    public void deleteComment(Integer discussId) {
        //删除评论
        warmupPracticeDiscussDao.deleteComment(discussId);
        //标记回复该评论的评论
        warmupPracticeDiscussDao.markRepliedCommentDelete(discussId);
    }

    public KnowledgeDiscuss loadKnowledgeDiscuss(Integer discussId) {
        KnowledgeDiscuss discuss = knowledgeDiscussDao.load(KnowledgeDiscuss.class, discussId);
        if (discuss != null) {
            fulfilDiscuss(discuss);
            discuss.setReferenceId(discuss.getKnowledgeId());
        }
        return discuss;
    }

    //填充评论的其他字段
    private void fulfilDiscuss(List<? extends AbstractComment> discuss) {
        List<Integer> profileIds = Lists.newArrayList();
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            if (!profileIds.contains(warmupPracticeDiscuss.getProfileId())) {
                profileIds.add(warmupPracticeDiscuss.getProfileId());
            }
            if (warmupPracticeDiscuss.getRepliedProfileId() != null) {
                if (!profileIds.contains(warmupPracticeDiscuss.getRepliedProfileId())) {
                    profileIds.add(warmupPracticeDiscuss.getRepliedProfileId());
                }
            }
        });
        //批量获取用户信息
        List<Profile> accounts = accountService.getProfiles(profileIds);
        //设置名称、头像和时间
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            accounts.stream().forEach(account -> {
                if (warmupPracticeDiscuss.getProfileId()!=null &&
                        account.getId() == warmupPracticeDiscuss.getProfileId()) {
                    warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
                    warmupPracticeDiscuss.setName(account.getNickname());
                    warmupPracticeDiscuss.setRole(account.getRole());
                    warmupPracticeDiscuss.setSignature(account.getSignature());
                }
                if (warmupPracticeDiscuss.getRepliedProfileId() != null &&
                        account.getId() == warmupPracticeDiscuss.getRepliedProfileId()) {
                    warmupPracticeDiscuss.setRepliedName(account.getNickname());
                }
            });
            warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
        });
    }

    private void fulfilDiscuss(AbstractComment warmupPracticeDiscuss) {
        Profile account = accountService.getProfile(warmupPracticeDiscuss.getProfileId());
        //设置名称、头像和时间
        if (warmupPracticeDiscuss.getProfileId()!=null &&
                account.getId() == warmupPracticeDiscuss.getProfileId()) {
            warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
            warmupPracticeDiscuss.setName(account.getNickname());
            warmupPracticeDiscuss.setRole(account.getRole());
            warmupPracticeDiscuss.setSignature(account.getSignature());
        }
        if (warmupPracticeDiscuss.getRepliedProfileId() != null &&
                account.getId() == warmupPracticeDiscuss.getRepliedProfileId()) {
            warmupPracticeDiscuss.setRepliedName(account.getNickname());
        }

        warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
    }


}
