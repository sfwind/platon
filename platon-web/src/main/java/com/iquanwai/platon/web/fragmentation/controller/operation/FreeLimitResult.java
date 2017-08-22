package com.iquanwai.platon.web.fragmentation.controller.operation;

import lombok.Data;

/**
 * Created by justin on 17/8/22.
 */
@Data
public class FreeLimitResult {
    private Integer percent; // 打败多少用户
    private Boolean learnFreeLimit; //是否学过限免课程
    private Integer score;
}
