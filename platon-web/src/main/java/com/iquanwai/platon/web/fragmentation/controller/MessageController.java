package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.AppMsgCommentReplyDto;
import com.iquanwai.platon.web.fragmentation.dto.NotifyMessageDto;
import com.iquanwai.platon.web.fragmentation.dto.RiseWorkCommentDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/2/27.
 * 消息中心相关的请求处理类
 */
@RestController
@RequestMapping("/rise/message")
public class MessageController {
    @Autowired
    private PracticeDiscussService practiceDiscussService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PracticeService practiceService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final static int MESSAGE_PER_PAGE = 20;

    @RequestMapping("/warmup/discuss/reply/{discussId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer discussId) {
        Assert.notNull(loginUser, "用户不能为空");
        WarmupPracticeDiscuss warmupPracticeDiscuss = practiceDiscussService.loadDiscuss(discussId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("讨论区回复")
                .action("打开讨论区回复页")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDiscuss);
    }

    @RequestMapping(value = "/comment/reply/{moduleId}/{commentId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadApplicationReplyMsg(LoginUser loginUser,
                                                                       @PathVariable Integer moduleId,
                                                                       @PathVariable Integer commentId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("讨论区回复")
                .action("打开讨论区回复")
                .memo(commentId.toString());
        operationLogService.log(operationLog);
        AppMsgCommentReplyDto dto = new AppMsgCommentReplyDto();
        if (moduleId == 2) {
            dto.setComment(getCommentDto(loginUser, commentId));
            // 通过commentId从ApplicationPractice数据库中读取该评论所针对的文章数据
            ApplicationPractice applicationPractice = messageService.loadAppPracticeByCommentId(commentId);
            if (applicationPractice != null) {
                dto.setId(applicationPractice.getId());
                dto.setTopic(applicationPractice.getTopic());
                dto.setDescription(applicationPractice.getDescription());
                dto.setPlanId(applicationPractice.getPlanId());
                dto.setIntegrated(Knowledge.isReview(applicationPractice.getKnowledgeId()));
            }
            return WebUtils.result(dto);
        }

        return WebUtils.result("获取文章内容失败");
    }

    /**
     * 获取消息回复页面
     */
    private RiseWorkCommentDto getCommentDto(LoginUser loginUser, Integer commentId) {
        Comment comment = practiceService.loadComment(commentId);
        RiseWorkCommentDto commentDto = new RiseWorkCommentDto();

        if (comment != null) {
            Profile account = accountService.getProfile(comment.getCommentProfileId());
            if (account != null) {
                commentDto.setId(comment.getId());
                commentDto.setName(account.getNickname());
                commentDto.setAvatar(account.getHeadimgurl());
                commentDto.setDiscussTime(DateUtils.parseDateTimeToString(comment.getAddTime()));
                commentDto.setComment(comment.getContent());
                commentDto.setRepliedComment(comment.getRepliedComment());
                commentDto.setRepliedName(account.getNickname());
                commentDto.setSignature(account.getSignature());
                commentDto.setIsMine(loginUser.getId().equals(comment.getCommentProfileId()));
                commentDto.setRepliedComment(comment.getRepliedComment());
                Profile repliedAccount = accountService.getProfile(comment.getRepliedProfileId());
                if (repliedAccount != null) {
                    commentDto.setRepliedName(repliedAccount.getNickname());
                }
                commentDto.setRepliedDel(comment.getRepliedDel());
            }
        }

        return commentDto;
    }

    @RequestMapping("/knowledge/discuss/reply/{discussId}")
    public ResponseEntity<Map<String, Object>> loadKnowledgeDiscuss(LoginUser loginUser,
                                                                    @PathVariable Integer discussId) {
        Assert.notNull(loginUser, "用户不能为空");
        KnowledgeDiscuss warmupPracticeDiscuss = practiceDiscussService.loadKnowledgeDiscuss(discussId);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("理解练习讨论区回复")
                .action("打开理解练习讨论区回复页")
                .memo(discussId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPracticeDiscuss);
    }

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> loadMessage(LoginUser loginUser, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        page.setPageSize(MESSAGE_PER_PAGE);
        //首次加载时把消息置为非最新
        if (page.getPage() == 1) {
            messageService.mark(loginUser.getId());
        }
        // 修改 comment 提交设备类型
        List<NotifyMessage> notifyMessage = messageService.getNotifyMessage(loginUser.getId(), loginUser.getDevice(), page);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("打开消息中心")
                .action("加载消息");
        operationLogService.log(operationLog);
        NotifyMessageDto notifyMessageDto = new NotifyMessageDto();
        notifyMessageDto.setNotifyMessageList(notifyMessage);
        notifyMessageDto.setEnd(page.isLastPage());

        return WebUtils.result(notifyMessageDto);
    }

    @RequestMapping("/old/count/load")
    public ResponseEntity<Map<String, Object>> loadUnreadCount(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
//        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
//                .module("消息")
//                .function("未读消息")
//                .action("查看条数");
//        operationLogService.log(operationLog);

        logger.info("登录用户 id 为：", loginUser.getId());
        Integer count = messageService.unreadCount(loginUser.getId());
        return WebUtils.result(count);
    }

    @RequestMapping(value = "/read/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> readMessage(LoginUser loginUser, @PathVariable Integer id) {
        Assert.notNull(loginUser, "用户不能为空");
        messageService.readMessage(id);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("打开消息中心")
                .action("读消息")
                .memo(id.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/old/get")
    public ResponseEntity<Map<String, Object>> readAll(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        messageService.mark(loginUser.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("老消息")
                .action("标记已读");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/open/learning/notify", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openLearningNotifyStatus(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        accountService.openLearningNotify(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("学习消息提醒")
                .action("打开");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/close/learning/notify", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> closeLearningNotifyStatus(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        accountService.closeLearningNotify(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("学习消息提醒")
                .action("关闭");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/status/learning/notify", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getLearningNotifyStatus(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        Profile profile = accountService.getProfile(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("学习消息提醒")
                .action("查看");
        operationLogService.log(operationLog);
        return WebUtils.result(profile != null && profile.getLearningNotify());
    }

}

