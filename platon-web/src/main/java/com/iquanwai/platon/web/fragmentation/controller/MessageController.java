package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.AppMsgCommentReplyDto;
import com.iquanwai.platon.web.fragmentation.dto.ApplicationCommentDto;
import com.iquanwai.platon.web.fragmentation.dto.NotifyMessageDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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


    private final static int MESSAGE_PER_PAGE = 20;

    @RequestMapping("/warmup/discuss/reply/{discussId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer discussId){
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

    @RequestMapping(value = "/comment/reply/{moduleId}/{submitId}/{commentId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadApplicationReplyMsg(LoginUser loginUser,
                                                                       @PathVariable Integer moduleId,
                                                                       @PathVariable Integer submitId,
                                                                       @PathVariable Integer commentId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("消息中心")
                .function("讨论区回复")
                .action("打开讨论区回复")
                .memo(commentId.toString());
        operationLogService.log(operationLog);
        AppMsgCommentReplyDto dto = new AppMsgCommentReplyDto();
        if (moduleId == 2) {
            dto.setComments(loadRelativeComments(loginUser, commentId));
            // 通过commentId从ApplicationPractice数据库中读取该评论所针对的文章数据
            ApplicationPractice applicationPractice = messageService.loadAppPracticeByCommentId(commentId);
            if(applicationPractice != null) {
                dto.setId(applicationPractice.getId());
                dto.setTopic(applicationPractice.getTopic());
                dto.setDescription(applicationPractice.getDescription());
                dto.setPlanId(applicationPractice.getPlanId());
                dto.setIntegrated(Knowledge.isReview(applicationPractice.getKnowledgeId()));
            }
            return WebUtils.result(dto);
        }
        if (moduleId == 3) {
            dto.setComments(loadRelativeComments(loginUser, commentId));
            SubjectArticle subjectArticle = messageService.loadSubjectArticleByCommentId(commentId);
            if(subjectArticle != null) {
                dto.setId(subjectArticle.getId());
                dto.setTopic(subjectArticle.getTitle());
                dto.setDescription(subjectArticle.getContent());
            }
            return WebUtils.result(dto);
        }
        return WebUtils.result("获取文章内容失败");
    }


    /**
     * 获取消息回复页面相关评论内容
     * @return
     */
    private List<ApplicationCommentDto> loadRelativeComments(LoginUser loginUser, Integer commentId) {
        List<ApplicationCommentDto> commentDtoList = messageService.loadRelativeComments(commentId).stream().map(item -> {
            ApplicationCommentDto commentDto = new ApplicationCommentDto();
            Profile account = accountService.getProfile(item.getCommentOpenId(), false);
            if(account != null) {
                commentDto.setId(item.getId());
                commentDto.setName(account.getNickname());
                commentDto.setAvatar(account.getHeadimgurl());
                commentDto.setDiscussTime(DateUtils.parseDateTimeToString(item.getAddTime()));
                commentDto.setComment(item.getContent());
                commentDto.setRepliedComment(item.getRepliedComment());
                commentDto.setRepliedName(accountService.getAccount(item.getRepliedOpenId(), false).getNickname());
                commentDto.setSignature(account.getSignature());
                commentDto.setIsMine(loginUser.getOpenId().equals(item.getCommentOpenId()));
                commentDto.setRepliedComment(item.getRepliedComment());
                commentDto.setRepliedName(accountService.getAccount(item.getCommentOpenId(), false).getNickname());
                commentDto.setRepliedDel(item.getRepliedDel());
            }
            return commentDto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return commentDtoList;
    }


    @RequestMapping("/knowledge/discuss/reply/{discussId}")
    public ResponseEntity<Map<String, Object>> loadKnowledgeDiscuss(LoginUser loginUser,
                                                             @PathVariable Integer discussId){
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
    public ResponseEntity<Map<String, Object>> loadMessage(LoginUser loginUser, @ModelAttribute Page page){
        Assert.notNull(loginUser, "用户不能为空");
        page.setPageSize(MESSAGE_PER_PAGE);
        //首次加载时把消息置为非最新
        if(page.getPage()==1){
            messageService.mark(loginUser.getOpenId());
        }
        List<NotifyMessage> notifyMessage = messageService.getNotifyMessage(loginUser.getOpenId(), page);

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

    @RequestMapping(value = "/read/{id}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> readMessage(LoginUser loginUser, @PathVariable Integer id){
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
}
