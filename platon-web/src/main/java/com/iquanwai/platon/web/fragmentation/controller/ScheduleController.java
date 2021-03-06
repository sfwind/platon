package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.*;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import com.iquanwai.platon.web.fragmentation.dto.PersonalCoursePlanDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.CourseScheduleModifyDto;
import com.iquanwai.platon.web.fragmentation.dto.plan.MonthCourseScheduleDto;
import com.iquanwai.platon.web.fragmentation.dto.schedule.ScheduleInitDto;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.ApiOperation;
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

    @Autowired
    private BusinessPlanService businessPlanService;
    @Autowired
    private PlanService planService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private StudyService studyService;

    /**
     * 获取个人的学习计划
     */
    @RequestMapping("/load/personal")
    public ResponseEntity<Map<String, Object>> loadPersonalCourseSchedulePlan(UnionUser unionUser) {
        Assert.notNull(unionUser, "登录用户不能为空");

        List<List<CourseSchedule>> courseScheduleMap = businessPlanService.loadPersonalCourseSchedule(unionUser.getId());
        return WebUtils.result(courseScheduleMap);
    }

    /**
     * 更新单门课程的年月信息
     */
    @RequestMapping(value = "/update/problem", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyProblemSchedule(UnionUser unionUser, @RequestBody CourseScheduleDto courseScheduleDto) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(courseScheduleDto, "课程更改参数不能为空");

        Integer problemId = courseScheduleDto.getProblemId();
        Integer year = courseScheduleDto.getTargetYear();
        Integer month = courseScheduleDto.getTargetMonth();
        boolean updateResult = businessPlanService.modifyProblemSchedule(unionUser.getId(), problemId, year, month);
        if (updateResult) {
            return WebUtils.success();
        } else {
            return WebUtils.error("课程计划调整出错，请联系管理员！");
        }
    }

    /**
     * 单门课程更新是否选择
     */
    @RequestMapping(value = "/update/selected", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateProblemScheduleSelectedStatus(UnionUser unionUser, @RequestBody CourseScheduleDto courseScheduleDto) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(courseScheduleDto, "课程更改参数不能为空");

        Integer courseScheduleId = courseScheduleDto.getId();
        Boolean selected = courseScheduleDto.getSelected();
        businessPlanService.updateProblemScheduleSelected(courseScheduleId, selected);
        return WebUtils.success();
    }

    /**
     * 全量更新课程的选择情况
     */
    @RequestMapping(value = "/update/all", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyCourseSchedule(UnionUser unionUser, @RequestBody CourseScheduleModifyDto courseScheduleModifyDto) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(courseScheduleModifyDto, "课程更改参数不能为空");

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
    public ResponseEntity<Map<String, Object>> loadScheduleQuestions(UnionUser unionUser) {
        Assert.notNull(unionUser, "登录用户不能为空");

        List<ScheduleQuestion> questions = businessPlanService.loadScheduleQuestions(unionUser.getId());
        return WebUtils.result(questions);
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitQuestions(UnionUser unionUser, @RequestBody ScheduleInitDto initDto) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(initDto);
        List<ScheduleQuestion> questions = initDto.getQuestionList();
        businessPlanService.initCourseSchedule(unionUser.getId(), questions);
        return WebUtils.success();
    }

    @RequestMapping("/load/plan")
    public ResponseEntity<Map<String, Object>> loadCoursePlan(UnionUser unionUser) {
        SchedulePlan schedulePlan = businessPlanService.getSchedulePlan(unionUser.getId());
        return WebUtils.result(schedulePlan);
    }

    @ApiOperation("获取个人课表")
    @RequestMapping("/load/person/plan")
    public ResponseEntity<Map<String, Object>> loadPersonalCoursePlan(UnionUser unionUser) {
        PersonalSchedulePlan schedulePlan = businessPlanService.getPersonalSchedulePlans(unionUser.getId());
        PersonalCoursePlanDto dto = new PersonalCoursePlanDto();
        dto.setRunningPlans(schedulePlan.getRunningPlans());
        dto.setCompletePlans(schedulePlan.getCompletePlans());
        dto.setLoginCount(customerService.loadContinuousLoginCount(unionUser.getId()));
        dto.setJoinDays(customerService.loadJoinDays(unionUser.getId()));
        dto.setTotalPoint(customerService.loadPersonalTotalPoint(unionUser.getId()));
        dto.setAnnounce(customerService.loadAnnounceMessage(unionUser.getId()));
        dto.setHasCourseSchedule(planService.loadAllCourseSchedules(unionUser.getId()).size() > 0);
        dto.setCountDownElement(studyService.loadLatestCountDownElement(unionUser.getId()));
        return WebUtils.result(dto);
    }
}

