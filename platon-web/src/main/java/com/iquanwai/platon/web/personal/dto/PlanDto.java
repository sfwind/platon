package com.iquanwai.platon.web.personal.dto;

import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

import java.util.Date;

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

    private Integer completeSeries; //已完成的节数
    private Integer totalSeries; //总节数
    private Integer deadline; //非db字段 离截止日期天数
    private Date startDate; //开始日期
    private Date closeTime; // 关闭时间
    private Boolean learnable;// 是否可以学习

    private Problem problem; // 该训练计划对应的小课

    private String errMsg;

}
