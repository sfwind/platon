package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.plan.BusinessPlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleModifyDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.MonthCourseScheduleDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 2017/11/4.
 */
@RestController
@RequestMapping("/rise/schedule")
public class ScheduleController {

    @Autowired
    private BusinessPlanService businessPlanService;
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取个人的学习计划
     */
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

    /**
     * 获取默认小课计划
     */
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

}
