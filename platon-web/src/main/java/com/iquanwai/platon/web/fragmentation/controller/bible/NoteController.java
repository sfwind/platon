package com.iquanwai.platon.web.fragmentation.controller.bible;

import com.iquanwai.platon.biz.domain.bible.StudyNoteService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.bible.StudyNote;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by nethunder on 2017/9/14.
 */
@RestController
@RequestMapping(value = "/rise/bible/note")
public class NoteController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private StudyNoteService studyNoteService;

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createOrUpdateNote(LoginUser loginUser, @RequestBody StudyNote studyNote) {
        Assert.notNull(loginUser,"用户不能为空");
        Assert.notNull(studyNote, "提交记录不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("笔记")
                .action("提交学习记录");
        operationLogService.log(operationLog);
        studyNoteService.createOrUpdateStudyNote(loginUser.getId(), studyNote);
        return WebUtils.success();
    }

    @RequestMapping(value = "/load/{noteId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadNote(LoginUser loginUser, @PathVariable Integer noteId) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习工具")
                .function("笔记")
                .action("打开笔记");
        operationLogService.log(operationLog);
        StudyNote studyNote = studyNoteService.loadStudyNote(loginUser.getId(), noteId);
        return WebUtils.result(studyNote);
    }
}
