package com.iquanwai.platon.web.fragmentation.controller.operation;

import lombok.Data;

/**
 * Created by justin on 17/8/22.
 */
@Data
public class FreeLimitResult {
    private Boolean learnFreeLimit; //是否学过限免课程
    private String result; //测评结果
    private String suggestion; //测评建议
    private Integer percent; //打败百分比
}
