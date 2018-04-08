package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.ApplicationSubmitDao;
import com.iquanwai.platon.biz.dao.fragmentation.CommentDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.KnowledgeDiscussDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.WarmupSubmitDao;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;

public class SensorsDataTest extends TestBase {
    @Autowired
    OperationLogService operationLogService;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private WarmupSubmitDao warmupSubmitDao;
    @Autowired
    private KnowledgeDiscussDao knowledgeDiscussDao;

}
