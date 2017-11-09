package com.iquanwai.platon.biz.po.schedule;

import lombok.Data;

/**
 * @author nethunder
 */
@Data
public class ScheduleChoiceSubmit {
    private Integer id;
    private Integer profileId;
    private Integer choiceId;
    private Boolean del;
}
