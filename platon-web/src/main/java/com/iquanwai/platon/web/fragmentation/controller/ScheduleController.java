package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.fragmentation.audition.AuditionService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.BusinessPlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.SchedulePlan;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.AuditionClassMember;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleModifyDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.MonthCourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.CountDownDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleInitDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author justin
 * @version 2017/11/4
 */
@RestController
@RequestMapping("/rise/schedule")
public class ScheduleController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BusinessPlanService businessPlanService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AuditionService auditionService;
    @Autowired
    private RiseMemberService riseMemberService;
    @Autowired
    private PlanService planService;

    /**
     * 获取个人的学习计划
     */
    @RequestMapping("/load/personal")
    public ResponseEntity<Map<String, Object>> loadPersonalCourseSchedulePlan(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("获取个人学习计划");
        operationLogService.log(operationLog);
        List<List<CourseSchedule>> courseScheduleMap = businessPlanService.loadPersonalCourseSchedule(loginUser.getId());
        return WebUtils.result(courseScheduleMap);
    }

    /**
     * 更新单门小课的年月信息
     */
    @RequestMapping(value = "/update/problem", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyProblemSchedule(LoginUser loginUser, @RequestBody CourseScheduleDto courseScheduleDto) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(courseScheduleDto, "小课更改参数不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("小课计划更改")
                .memo(courseScheduleDto.getProblemId().toString());
        operationLogService.log(operationLog);

        Integer problemId = courseScheduleDto.getProblemId();
        Integer year = courseScheduleDto.getTargetYear();
        Integer month = courseScheduleDto.getTargetMonth();
        boolean updateResult = businessPlanService.modifyProblemSchedule(loginUser.getId(), problemId, year, month);
        if (updateResult) {
            return WebUtils.success();
        } else {
            return WebUtils.error("小课计划调整出错，请联系管理员！");
        }
    }

    /**
     * 单门小课更新是否选择
     */
    @RequestMapping(value = "/update/selected", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateProblemScheduleSelectedStatus(LoginUser loginUser, @RequestBody CourseScheduleDto courseScheduleDto) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(courseScheduleDto, "小课更改参数不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("小课计划更改");
        operationLogService.log(operationLog);
        Integer courseScheduleId = courseScheduleDto.getId();
        Boolean selected = courseScheduleDto.getSelected();
        businessPlanService.updateProblemScheduleSelected(courseScheduleId, selected);
        return WebUtils.success();
    }

    /**
     * 全量更新小课的选择情况
     */
    @RequestMapping(value = "/update/all", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyCourseSchedule(LoginUser loginUser, @RequestBody CourseScheduleModifyDto courseScheduleModifyDto) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(courseScheduleModifyDto, "小课更改参数不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("小课计划整体更改");
        operationLogService.log(operationLog);
        List<MonthCourseScheduleDto> monthCourseScheduleDtos = courseScheduleModifyDto.getMonthCourseSchedules();

        monthCourseScheduleDtos.forEach(monthCourseScheduleDto -> {
            Integer year = monthCourseScheduleDto.getYear();
            Integer month = monthCourseScheduleDto.getMonth();
            List<CourseSchedule> courseSchedules = monthCourseScheduleDto.getCourseSchedules();
            businessPlanService.batchModifyCourseSchedule(year, month, courseSchedules);
        });
        return WebUtils.success();
    }

    @RequestMapping(value = "/load/questions", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadScheduleQuestions(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("获取学习计划题目");
        operationLogService.log(operationLog);
        List<ScheduleQuestion> questions = businessPlanService.loadScheduleQuestions(loginUser.getId());
        return WebUtils.result(questions);
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestions(LoginUser loginUser, @RequestBody ScheduleInitDto initDto) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习")
                .function("课程表")
                .action("初始化课程表");
        operationLogService.log(operationLog);
        Assert.notNull(initDto);
        List<ScheduleQuestion> questions = initDto.getQuestionList();
        businessPlanService.initCourseSchedule(loginUser.getId(), questions);
        return WebUtils.success();
    }

    @RequestMapping("/load/plan")
    public ResponseEntity<Map<String, Object>> loadCoursePlan(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("获取个人学习进度");
        operationLogService.log(operationLog);
        SchedulePlan schedulePlan = businessPlanService.getSchedulePlan(loginUser.getId());
        return WebUtils.result(schedulePlan);
    }

    @RequestMapping(value = "/count/down", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadBusinessCountDown(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("倒计时查询");
        operationLogService.log(operationLog);
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
            Integer days = Days.daysBetween(DateTime.now().withTimeAtStartOfDay(), new DateTime(riseMember.getOpenDate())).getDays();
            List<CourseSchedule> plan = businessPlanService.getPlan(loginUser.getId());
            AuditionClassMember auditionClassMember = auditionService.loadAuditionClassMember(loginUser.getId());
            boolean hasPlan = planService.loadUserPlans(loginUser.getId()).stream().anyMatch(item -> !item.getDel() && item.getProblemId().equals(ConfigUtils.getTrialProblemId()));
            CountDownDto dto = new CountDownDto();
            dto.setDays(days);
            dto.setHasSchedule(!CollectionUtils.isEmpty(plan));
            dto.setHasAudition(auditionClassMember != null && hasPlan);
            return WebUtils.result(dto);
        } else {
            // 非商学院
            return WebUtils.error("请先报名商学院哦");
        }
    }
}

