package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.plan.BusinessPlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.SchedulePlan;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleInitDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleQuestionDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
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

    @RequestMapping("/load/personal")
    public ResponseEntity<Map<String, Object>> loadPersonalCourseSchedulePlan(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("获取学习计划");
        operationLogService.log(operationLog);
        List<List<CourseSchedule>> courseScheduleMap = businessPlanService.loadPersonalCourseSchedule(loginUser.getId());
        return WebUtils.result(courseScheduleMap);
    }

    @RequestMapping("/load/default")
    public ResponseEntity<Map<String, Object>> loadDefaultCourseSchedulePlan(LoginUser loginUser) {
        Assert.notNull(loginUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("学习计划")
                .action("获取默认学习计划");
        operationLogService.log(operationLog);
        List<List<CourseSchedule>> defaultCourseScheduleMap = businessPlanService.loadDefaultCourseSchedule();
        return WebUtils.result(defaultCourseScheduleMap);
    }
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestions(LoginUser loginUser, @RequestBody ScheduleInitDto initDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("学习")
                .function("课程表")
                .action("初始化课程表");
        operationLogService.log(operationLog);
        Assert.notNull(initDto);
        List<ScheduleQuestionDto> questions = initDto.getQuestionList();

        return WebUtils.success();
    }

    @RequestMapping(value = "/update/problem", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyProblemSchedule(LoginUser loginUser,@RequestBody CourseScheduleDto courseScheduleDto) {
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
}
