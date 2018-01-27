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

    private Boolean bindMobile; // 是否已经绑定过电话
    private Boolean isFull; // 是否填写全部用户信息
    private String togetherClassMonth; // 专项课学习月份

    private Boolean problemCollected; // 当前用户是否已经收藏该课程
}
