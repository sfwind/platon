package com.iquanwai.platon.web.fragmentation.dto.schedule;

import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-04
 */
@Data
public class ScheduleInitDto {
    List<ScheduleQuestionDto> questionList;
}
