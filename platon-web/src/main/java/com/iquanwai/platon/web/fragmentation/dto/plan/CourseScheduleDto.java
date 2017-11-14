package com.iquanwai.platon.web.fragmentation.dto.plan;

import lombok.Data;

@Data
public class CourseScheduleDto {

    private Integer problemId;
    private Integer targetYear;
    private Integer targetMonth;

    private Integer id;
    private Boolean selected;

}
