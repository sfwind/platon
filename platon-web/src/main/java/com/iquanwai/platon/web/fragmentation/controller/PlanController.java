package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.fragmentation.plan.Chapter;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.ProblemSchedule;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.fragmentation.dto.ChapterDto;
import com.iquanwai.platon.web.fragmentation.dto.CompletePlanDto;
import com.iquanwai.platon.web.fragmentation.dto.OpenStatusDto;
import com.iquanwai.platon.web.fragmentation.dto.PlayIntroduceDto;
import com.iquanwai.platon.web.fragmentation.dto.SectionDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/8.
 * 训练计划相关的请求处理类
 */
@RestController
@RequestMapping("/rise/plan")
public class PlanController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/choose/problem/check/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> checkChoosePlan(LoginUser loginUser, @PathVariable Integer problemId) {
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("检查是否能够重新选择")
                .memo(problemId.toString());
        operationLogService.log(operationLog);

        if(improvementPlan!=null){
            LOGGER.error("planId {} is existed", improvementPlan.getId());
            return WebUtils.error("先完成进行中的小课，才能选择另一个哦<br/>一次专心学一门吧");
        } else {
            return WebUtils.success();
        }
    }

    @RequestMapping(value = "/choose/problem/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlan(LoginUser loginUser,
                                                          @PathVariable Integer problemId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan!=null){
            //如果是同一个小课的训练,直接返回训练id
            if(improvementPlan.getProblemId().equals(problemId)){
                return WebUtils.result(improvementPlan.getId());
            }
            LOGGER.error("planId {} is existed", improvementPlan.getId());
            return WebUtils.error("先完成进行中的小课，才能选择另一个哦<br/>一次专心学一门吧");
        }
        List<ImprovementPlan> plans = planService.getPlans(loginUser.getOpenId());
        for(ImprovementPlan plan:plans){
            if(plan.getProblemId().equals(problemId)){
                return WebUtils.error("你已经选过该门小课了，你可以在\"我的\"菜单里找到以前的学习记录哦");
            }
        }
        Integer planId = generatePlanService.generatePlan(loginUser.getOpenId(), problemId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("选择小课")
                .action("选择小课")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(planId);
    }


    @RequestMapping("/play/{planId}")
    public ResponseEntity<Map<String, Object>> planPlayIntroduce(LoginUser loginUser,
                                                                 @PathVariable Integer planId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if(improvementPlan==null){
            LOGGER.error("planId {} is invalid", planId);
            return WebUtils.error("打开训练玩法介绍失败");
        }

        PlayIntroduceDto playIntroduceDto = new PlayIntroduceDto();

        int interval = DateUtils.interval(improvementPlan.getStartDate(),improvementPlan.getCloseDate());
        playIntroduceDto.setLength(interval);
        interval = DateUtils.interval(improvementPlan.getStartDate(),improvementPlan.getEndDate());
        playIntroduceDto.setTotalSeries(interval);
        DateTime dateTime = new DateTime(improvementPlan.getCloseDate());
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        playIntroduceDto.setEndDate(month + "月" + day + "日");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("玩法")
                .function("训练玩法介绍")
                .action("打开玩法页")
                .memo(planId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(playIntroduceDto);
    }

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> startPlan(LoginUser loginUser, HttpServletRequest request,
                                                         @RequestParam(required = false) Integer planId){
        LOGGER.info(request.getHeader("User-Agent")+", openid:"+loginUser.getOpenId());

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getLatestPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            return WebUtils.result(null);
        }

        planService.buildPlanDetail(improvementPlan);
        // openid置为null
        improvementPlan.setOpenid(null);
        if (!loginUser.getOpenRise()) {
            // 没有点开rise
            Profile profile = accountService.getProfile(loginUser.getOpenId(), false);
            loginUser.setOpenRise(profile.getOpenRise());
            improvementPlan.setOpenRise(profile.getOpenRise());
        }
        improvementPlan.setOpenRise(loginUser.getOpenRise());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("开始训练")
                .action("加载训练")
                .memo(improvementPlan.getId()+"");
        operationLogService.log(operationLog);
        return WebUtils.result(improvementPlan);
    }

    @RequestMapping("/knowledge/load/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer knowledgeId){

        Assert.notNull(loginUser, "用户不能为空");
        Knowledge knowledge = planService.getKnowledge(knowledgeId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点回顾")
                .action("打开回顾页")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledge);
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> complete(LoginUser loginUser,
                                                        @RequestParam(required = false) Integer planId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Pair<Integer,Integer> closeable = planService.checkCloseable(improvementPlan);
        if (closeable.getLeft() != 1) {
            return WebUtils.error(closeable.getLeft(), closeable.getRight());
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("结束训练")
                .action("结束训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
        if(improvementPlan.getStatus()==ImprovementPlan.CLOSE){
            return WebUtils.error(-4,"您的小课已完成");
        }

        Pair<Boolean,Integer> result = planService.completeCheck(improvementPlan);
        CompletePlanDto completePlanDto = new CompletePlanDto();
        completePlanDto.setIscomplete(result.getLeft());
        completePlanDto.setPercent(result.getRight());
        int minStudyDays = Double.valueOf(Math.ceil(improvementPlan.getTotalSeries() / 2.0D)).intValue();
        Date minDays = DateUtils.afterDays(improvementPlan.getStartDate(), minStudyDays);
            // 如果4.1号10点开始  +1 = 4.2号0点是最早时间，4.2白天就可以了
        if(new Date().before(minDays)){
            completePlanDto.setMustStudyDays(minStudyDays);
        } else {
            completePlanDto.setMustStudyDays(0);
        }

        return WebUtils.result(completePlanDto);
    }


    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> close(LoginUser loginUser,
                                                     @RequestParam(required = false) Integer planId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.error(-3,"您还没有制定训练计划哦");
        }

        Pair<Integer,Integer> closeable = planService.checkCloseable(improvementPlan);
        if (closeable.getLeft() != 1) {
            return WebUtils.error(closeable.getLeft(), closeable.getRight());
        }
        planService.completePlan(improvementPlan.getId(), ImprovementPlan.CLOSE);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("完成训练")
                .action("完成训练")
                .memo(improvementPlan.getId()+"");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/openrise", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> openRise(LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        int count = accountService.updateOpenRise(loginUser.getOpenId());
        if (count > 0) {
            loginUser.setOpenRise(true);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/open/application", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> openComprehension(LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        int count = accountService.updateOpenApplication(loginUser.getOpenId());
        if (count > 0) {
            loginUser.setOpenApplication(true);
        }
        return WebUtils.success();
    }

    @RequestMapping(value = "/open/consolidation", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> openConsolidation(LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        int count = accountService.updateOpenConsolidation(loginUser.getOpenId());
        if (count > 0) {
            loginUser.setOpenConsolidation(true);
        }
        return WebUtils.success();
    }

    @RequestMapping("/welcome")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小课")
                .function("打开小课")
                .action("打开欢迎页");
        operationLogService.log(operationLog);
        return WebUtils.result(loginUser.getRiseMember());
    }

    @RequestMapping("/risemember")
    public ResponseEntity<Map<String,Object>> isRiseMember(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("打开小课")
                .action("查看是否为rise会员")
                .memo(loginUser.getRiseMember() + "");
        operationLogService.log(operationLog);
        return WebUtils.result(loginUser.getRiseMember());
    }

    @RequestMapping("/roadmap")
    public ResponseEntity<Map<String, Object>> getRoadMap(LoginUser loginUser,
                                                          @RequestParam(required = false) Integer planId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        List<Chapter> chapter = planService.loadRoadMap(improvementPlan.getProblemId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("学习知识点")
                .action("打开知识点路线页");
        operationLogService.log(operationLog);
        return WebUtils.result(chapter);
    }

    @RequestMapping("/open/status")
    public ResponseEntity<Map<String, Object>> getOpenStatus(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("数据")
                .action("查看打开状态");
        operationLogService.log(operationLog);
        OpenStatusDto dto = new OpenStatusDto();
        if (!loginUser.getOpenApplication() || !loginUser.getOpenConsolidation() || !loginUser.getOpenRise()) {
            // 没有点开其中一个
            Profile profile = accountService.getProfile(loginUser.getOpenId(), false);
            loginUser.setOpenRise(profile.getOpenRise());
            loginUser.setOpenConsolidation(profile.getOpenConsolidation());
            loginUser.setOpenApplication(profile.getOpenApplication());
        }

        dto.setOpenRise(loginUser.getOpenRise());
        dto.setOpenConsolidation(loginUser.getOpenConsolidation());
        dto.setOpenApplication(loginUser.getOpenApplication());
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/check/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> practiceCheck(LoginUser loginUser,
                                                             @PathVariable Integer series,
                                                             @RequestParam(required = false) Integer planId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        if (!loginUser.getRiseMember() && series > ConfigUtils.preStudySerials()) {
            if(!improvementPlan.getRiseMember()){
                return WebUtils.error("试用版仅能体验前三节内容 <br/> 点击右上角按钮，升级正式版吧");
            }
        }
        Integer result = planService.checkPractice(series, improvementPlan);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练校验")
                .action("训练开始校验")
                .memo(series.toString());
        operationLogService.log(operationLog);
        if(result==-1){
            // 前一组已完成 这一组未解锁
            // 会员都会解锁，未解锁应该都是非会员
            return WebUtils.error("该内容为付费内容，只有会员可以查看");
        }else if(result==-2){
            // 前一组未完成
            return WebUtils.error("完成之前的任务，这一组才能解锁<br> 学习和内化，都需要循序渐进哦");
        }else if(result==-3){
            // 小课已过期
            return WebUtils.error("抱歉哦，课程开放期间，你未能完成前面的练习，导致这个练习无法解锁");
        }
        return WebUtils.success();
    }

    @RequestMapping("/chapter/list")
    public ResponseEntity<Map<String, Object>> chapterList(LoginUser loginUser,@RequestParam(required = false) Integer planId) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("章节")
                .action("查询章节列表")
                .memo(planId != null ? planId.toString() : null);
        operationLogService.log(operationLog);
        ImprovementPlan plan = null;
        if (planId == null) {
            plan = planService.getLatestPlan(loginUser.getOpenId());
        } else {
            plan = planService.getPlan(planId);
        }
        if (plan == null) {
            return WebUtils.error(null);
        }
        List<ProblemSchedule> chapterList = planService.getChapterList(plan);
        Map<Integer,ChapterDto> filterChapter = Maps.newHashMap();
        chapterList.forEach(item->{
            ChapterDto chapterDto = filterChapter.computeIfAbsent(item.getChapter(), (chapterId) -> {
                ChapterDto dto = new ChapterDto();
                dto.setChapterId(chapterId);
                dto.setChapter(item.getChapterStr());
                dto.setSectionList(Lists.newArrayList());
                return dto;
            });
            SectionDto sectionDto = new SectionDto();
            sectionDto.setSeries(item.getSeries());
            sectionDto.setSection(item.getSectionStr());
            sectionDto.setSectionId(item.getSection());
            chapterDto.getSectionList().add(sectionDto);
        });
        return WebUtils.result(filterChapter.values());
    }

    @RequestMapping(value = "/mark/{series}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> markSeries(LoginUser loginUser,
                                                             @PathVariable Integer series,
                                                             @RequestParam(required = false) Integer planId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan;
        if(planId==null){
            improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        }else{
            improvementPlan = planService.getPlan(planId);
        }
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        planService.markPlan(series, improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练")
                .function("训练首页")
                .action("记录训练小结")
                .memo(series.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
}
