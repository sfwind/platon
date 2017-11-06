package com.iquanwai.platon.web.fragmentation.dto.schedule;

import com.iquanwai.platon.biz.po.schedule.ScheduleQuestion;
import lombok.Data;

import java.util.List;

/**
 * @author nethunder
 * @version 2017-11-04
 */
@Data
public class ScheduleInitDto {
    List<ScheduleQuestion> questionList;
}
