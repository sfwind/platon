package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

/**
 * Created by nethunder on 2017/7/18.
 */
@Data
public class RiseCourseDto {
    private Problem problem;
    private Double fee;
    private Integer buttonStatus;
    private Integer planId;
}
