package com.iquanwai.platon.web.personal.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/2/7.
 */
@Data
public class PlanDto {
    private String name;
    private Integer point;
    private Integer problemId;
    private Integer planId;
    private String pic; // 头图地址，切换成 static 前缀
}
