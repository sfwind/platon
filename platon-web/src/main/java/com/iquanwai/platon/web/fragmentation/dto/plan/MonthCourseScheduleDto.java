package com.iquanwai.platon.web.fragmentation.dto.plan;

import com.iquanwai.platon.biz.po.CourseSchedule;
import lombok.Data;

import java.util.List;

@Data
public class MonthCourseScheduleDto {

    private Integer year;
    private Integer month;
    private List<CourseSchedule> courseSchedules;

}
