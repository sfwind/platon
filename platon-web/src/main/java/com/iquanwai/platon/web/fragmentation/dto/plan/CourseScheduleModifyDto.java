package com.iquanwai.platon.web.fragmentation.dto.plan;

import lombok.Data;

import java.util.List;

@Data
public class CourseScheduleModifyDto {

    private List<MonthCourseScheduleDto> monthCourseSchedules;

}
